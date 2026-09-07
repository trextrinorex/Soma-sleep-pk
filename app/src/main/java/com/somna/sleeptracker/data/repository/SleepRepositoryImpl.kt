package com.somna.sleeptracker.data.repository

import com.somna.sleeptracker.data.local.dao.TelemetryDao
import com.somna.sleeptracker.data.local.entity.CircadianProfileEntity
import com.somna.sleeptracker.data.local.entity.EventLogEntity
import com.somna.sleeptracker.data.local.entity.SleepSessionEntity
import com.somna.sleeptracker.domain.engine.SleepInferenceEngine
import com.somna.sleeptracker.domain.model.InferredSleepResult
import com.somna.sleeptracker.domain.model.SystemEventType
import com.somna.sleeptracker.domain.model.TelemetryEvent
import com.somna.sleeptracker.domain.personalization.BayesianPersonalizer
import com.somna.sleeptracker.domain.repository.SleepRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SleepRepositoryImpl(
    private val dao: TelemetryDao,
    private val inferenceEngine: SleepInferenceEngine = SleepInferenceEngine(),
    private val personalizer: BayesianPersonalizer = BayesianPersonalizer()
) : SleepRepository {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    override suspend fun logEvent(event: TelemetryEvent): Long {
        return dao.insertEvent(EventLogEntity.fromDomain(event))
    }

    override fun getRecentEvents(since: Long): Flow<List<TelemetryEvent>> {
        return dao.getRecentEvents(since).map { list -> list.map { it.toDomain() } }
    }

    override fun getEventCount(): Flow<Int> {
        return dao.getEventCount()
    }

    override suspend fun pruneOldEvents(olderThanDays: Int): Int {
        val threshold = System.currentTimeMillis() - (olderThanDays * 24L * 60L * 60L * 1000L)
        return dao.deleteEventsOlderThan(threshold)
    }

    override fun getAllSleepSessions(): Flow<List<SleepSessionEntity>> {
        return dao.getAllSleepSessions()
    }

    override fun getLatestSleepSession(): Flow<SleepSessionEntity?> {
        return dao.getLatestSleepSession()
    }

    override suspend fun saveSleepSession(session: SleepSessionEntity): Long {
        val id = dao.insertSleepSession(session)
        // Update Bayesian circadian profile with this session
        val currentProfile = dao.getCircadianProfileSync() ?: CircadianProfileEntity()
        val updated = personalizer.updateProfile(currentProfile, session.startTime, session.endTime)
        dao.insertOrUpdateCircadianProfile(updated)
        return id
    }

    override suspend fun deleteSleepSession(sessionId: Long) {
        dao.deleteSleepSessionById(sessionId)
    }

    override fun getCircadianProfile(): Flow<CircadianProfileEntity?> {
        return dao.getCircadianProfile()
    }

    override suspend fun updateCircadianProfile(profile: CircadianProfileEntity) {
        dao.insertOrUpdateCircadianProfile(profile)
    }

    override suspend fun runInference(fromTime: Long, toTime: Long): InferredSleepResult? {
        val rawEntities = dao.getEventsBetween(fromTime, toTime)
        val events = rawEntities.map { it.toDomain() }
        val profile = dao.getCircadianProfileSync() ?: CircadianProfileEntity()

        val inferred = inferenceEngine.inferSleepSession(events, profile) ?: return null

        val dateStr = dateFormat.format(Date(inferred.startTime))
        val existingSession = dao.getSleepSessionForDate(dateStr)

        val sessionEntity = SleepSessionEntity(
            id = existingSession?.id ?: 0L,
            startTime = inferred.startTime,
            endTime = inferred.endTime,
            durationMinutes = inferred.durationMinutes,
            efficiencyScore = inferred.efficiencyScore,
            quality = inferred.quality,
            confidence = inferred.confidenceScore,
            awakeningsCount = inferred.awakeningsCount,
            isManual = false,
            dateString = dateStr,
            notes = "Autonomously inferred from on-device telemetry (${inferred.gaps.size} inactive segments)"
        )

        saveSleepSession(sessionEntity)
        return inferred
    }

    override suspend fun seedDemoDataIfEmpty() {
        val existingLatest = dao.getLatestSleepSession().firstOrNull()
        if (existingLatest != null) return

        // Initialize circadian profile
        val defaultProfile = CircadianProfileEntity(
            meanBedtimeHour = 23.25f, // 11:15 PM
            meanWakeHour = 7.15f,     // 07:09 AM
            bedtimeVariance = 1.2f,
            wakeVariance = 0.9f,
            confidenceSamples = 7
        )
        dao.insertOrUpdateCircadianProfile(defaultProfile)

        val calendar = Calendar.getInstance()
        val sessions = mutableListOf<SleepSessionEntity>()
        val events = mutableListOf<EventLogEntity>()

        // Generate past 7 days of realistic sleep sessions and telemetry
        val durations = listOf(465L, 440L, 480L, 420L, 495L, 460L, 475L) // minutes ~ 7h to 8h15m
        val efficiencies = listOf(92, 88, 95, 84, 94, 90, 93)
        val awakenings = listOf(1, 2, 0, 3, 1, 2, 1)
        val qualities = listOf("Optimal", "Restful", "Optimal", "Fair", "Optimal", "Restful", "Optimal")

        for (i in 6 downTo 0) {
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(Calendar.DAY_OF_YEAR, -i)

            // Bedtime ~ 11:15 PM yesterday evening relative to wake
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 10 + (i * 7) % 25)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startTime = calendar.timeInMillis

            val dur = durations[i]
            val endTime = startTime + (dur * 60_000L)
            val dateStr = dateFormat.format(Date(startTime))

            sessions.add(
                SleepSessionEntity(
                    startTime = startTime,
                    endTime = endTime,
                    durationMinutes = dur,
                    efficiencyScore = efficiencies[i],
                    quality = qualities[i],
                    confidence = 0.94f - (i * 0.01f),
                    awakeningsCount = awakenings[i],
                    isManual = false,
                    dateString = dateStr,
                    notes = "Passive inference • Verified via charging and ambient screen stillness"
                )
            )

            // Seed telemetry events for the most recent night
            if (i == 0) {
                events.add(EventLogEntity(timestamp = startTime - 120_000L, eventType = SystemEventType.POWER_CONNECTED.name, isCharging = true, batteryLevel = 38))
                events.add(EventLogEntity(timestamp = startTime, eventType = SystemEventType.SCREEN_OFF.name, isCharging = true, batteryLevel = 39))
                // Brief awakening in middle of night
                val midNightAwakening = startTime + (200L * 60_000L)
                events.add(EventLogEntity(timestamp = midNightAwakening, eventType = SystemEventType.SCREEN_ON.name, isCharging = true, batteryLevel = 84))
                events.add(EventLogEntity(timestamp = midNightAwakening + 180_000L, eventType = SystemEventType.SCREEN_OFF.name, isCharging = true, batteryLevel = 85))
                // Morning wake
                events.add(EventLogEntity(timestamp = endTime, eventType = SystemEventType.SCREEN_ON.name, isCharging = true, batteryLevel = 100))
                events.add(EventLogEntity(timestamp = endTime + 10_000L, eventType = SystemEventType.USER_PRESENT.name, isCharging = true, batteryLevel = 100))
                events.add(EventLogEntity(timestamp = endTime + 180_000L, eventType = SystemEventType.POWER_DISCONNECTED.name, isCharging = false, batteryLevel = 100))
            }
        }

        dao.insertSleepSessions(sessions)
        if (events.isNotEmpty()) {
            dao.insertEvents(events)
        }
    }
}
