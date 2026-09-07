package com.somna.sleeptracker.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.somna.sleeptracker.data.local.entity.SleepSessionEntity
import com.somna.sleeptracker.data.service.TelemetryGuardianService
import com.somna.sleeptracker.di.AppModule
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DashboardViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = AppModule.provideSleepRepository(application)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    private val _effects = Channel<DashboardEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            repository.getAllSleepSessions().collect { sessions ->
                _state.update { it.copy(recentSessions = sessions) }
            }
        }

        viewModelScope.launch {
            repository.getLatestSleepSession().collect { latest ->
                _state.update { it.copy(latestSession = latest) }
            }
        }

        viewModelScope.launch {
            repository.getCircadianProfile().collect { profile ->
                _state.update { it.copy(circadianProfile = profile) }
            }
        }

        viewModelScope.launch {
            repository.getEventCount().collect { count ->
                _state.update { it.copy(recentTelemetryCount = count) }
            }
        }

        viewModelScope.launch {
            TelemetryGuardianService.isRunning.collect { running ->
                _state.update { it.copy(isGuardianActive = running) }
            }
        }
    }

    fun handleIntent(intent: DashboardIntent) {
        when (intent) {
            is DashboardIntent.ToggleGuardian -> toggleGuardian(intent.enabled)
            is DashboardIntent.TriggerAnalysis -> triggerAnalysis()
            is DashboardIntent.OpenManualLogDialog -> _state.update { it.copy(showManualLogDialog = true) }
            is DashboardIntent.DismissManualLogDialog -> _state.update { it.copy(showManualLogDialog = false) }
            is DashboardIntent.SaveManualSession -> saveManualSession(intent.startTime, intent.endTime, intent.quality)
            is DashboardIntent.SelectSessionDetail -> _state.update { it.copy(selectedSessionDetail = intent.session) }
            is DashboardIntent.DeleteSession -> deleteSession(intent.sessionId)
            is DashboardIntent.TogglePrivacyDialog -> _state.update { it.copy(showPrivacyDialog = intent.show) }
            is DashboardIntent.DismissMessage -> _state.update { it.copy(userMessage = null) }
            is DashboardIntent.SeedDemoData -> seedDemoData()
        }
    }

    private fun toggleGuardian(enabled: Boolean) {
        if (enabled) {
            TelemetryGuardianService.startGuardian(getApplication())
            _state.update { it.copy(isGuardianActive = true, userMessage = "Telemetry Guardian active • Passive tracking enabled") }
        } else {
            TelemetryGuardianService.stopGuardian(getApplication())
            _state.update { it.copy(isGuardianActive = false, userMessage = "Telemetry Guardian paused") }
        }
    }

    private fun triggerAnalysis() {
        viewModelScope.launch {
            _state.update { it.copy(isAnalyzing = true) }
            val calendar = Calendar.getInstance()
            val now = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            calendar.set(Calendar.HOUR_OF_DAY, 18)
            val from = calendar.timeInMillis

            val result = repository.runInference(from, now)
            _state.update {
                it.copy(
                    isAnalyzing = false,
                    userMessage = if (result != null) {
                        "Analyzed night sleep: ${result.durationMinutes / 60}h ${result.durationMinutes % 60}m (${result.quality})"
                    } else {
                        "No new sleep window detected yet in recent telemetry"
                    }
                )
            }
        }
    }

    private fun saveManualSession(startTime: Long, endTime: Long, quality: String) {
        viewModelScope.launch {
            val durationMin = Math.max(1L, (endTime - startTime) / 60_000L)
            val dateStr = dateFormat.format(Date(startTime))
            val efficiency = when (quality) {
                "Optimal" -> 95
                "Restful" -> 88
                "Fair" -> 78
                else -> 65
            }

            val session = SleepSessionEntity(
                startTime = startTime,
                endTime = endTime,
                durationMinutes = durationMin,
                efficiencyScore = efficiency,
                quality = quality,
                confidence = 1.0f,
                awakeningsCount = if (quality == "Fragmented") 3 else 1,
                isManual = true,
                dateString = dateStr,
                notes = "Manually logged session"
            )

            repository.saveSleepSession(session)
            _state.update {
                it.copy(
                    showManualLogDialog = false,
                    userMessage = "Sleep session logged successfully"
                )
            }
        }
    }

    private fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            repository.deleteSleepSession(sessionId)
            _state.update {
                it.copy(
                    selectedSessionDetail = null,
                    userMessage = "Sleep session removed"
                )
            }
        }
    }

    private fun seedDemoData() {
        viewModelScope.launch {
            repository.seedDemoDataIfEmpty()
            _state.update { it.copy(userMessage = "Loaded recent sample sleep records") }
        }
    }
}
