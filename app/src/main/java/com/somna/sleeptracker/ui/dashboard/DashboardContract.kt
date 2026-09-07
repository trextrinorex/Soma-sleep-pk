package com.somna.sleeptracker.ui.dashboard

import com.somna.sleeptracker.data.local.entity.CircadianProfileEntity
import com.somna.sleeptracker.data.local.entity.SleepSessionEntity

data class DashboardState(
    val isLoading: Boolean = false,
    val isGuardianActive: Boolean = true,
    val latestSession: SleepSessionEntity? = null,
    val recentSessions: List<SleepSessionEntity> = emptyList(),
    val circadianProfile: CircadianProfileEntity? = null,
    val recentTelemetryCount: Int = 0,
    val isAnalyzing: Boolean = false,
    val selectedSessionDetail: SleepSessionEntity? = null,
    val showManualLogDialog: Boolean = false,
    val showPrivacyDialog: Boolean = false,
    val userMessage: String? = null
)

sealed interface DashboardIntent {
    data class ToggleGuardian(val enabled: Boolean) : DashboardIntent
    data object TriggerAnalysis : DashboardIntent
    data object OpenManualLogDialog : DashboardIntent
    data object DismissManualLogDialog : DashboardIntent
    data class SaveManualSession(
        val startTime: Long,
        val endTime: Long,
        val quality: String
    ) : DashboardIntent
    data class SelectSessionDetail(val session: SleepSessionEntity?) : DashboardIntent
    data class DeleteSession(val sessionId: Long) : DashboardIntent
    data class TogglePrivacyDialog(val show: Boolean) : DashboardIntent
    data object DismissMessage : DashboardIntent
    data object SeedDemoData : DashboardIntent
}

sealed interface DashboardEffect {
    data class ShowToast(val message: String) : DashboardEffect
}
