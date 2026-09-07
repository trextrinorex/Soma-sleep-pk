package com.somna.sleeptracker.domain.repository

import com.somna.sleeptracker.data.local.entity.CircadianProfileEntity
import com.somna.sleeptracker.data.local.entity.SleepSessionEntity
import com.somna.sleeptracker.domain.model.InferredSleepResult
import com.somna.sleeptracker.domain.model.TelemetryEvent
import kotlinx.coroutines.flow.Flow

interface SleepRepository {
    suspend fun logEvent(event: TelemetryEvent): Long
    fun getRecentEvents(since: Long): Flow<List<TelemetryEvent>>
    fun getEventCount(): Flow<Int>
    suspend fun pruneOldEvents(olderThanDays: Int = 14): Int

    fun getAllSleepSessions(): Flow<List<SleepSessionEntity>>
    fun getLatestSleepSession(): Flow<SleepSessionEntity?>
    suspend fun saveSleepSession(session: SleepSessionEntity): Long
    suspend fun deleteSleepSession(sessionId: Long)

    fun getCircadianProfile(): Flow<CircadianProfileEntity?>
    suspend fun updateCircadianProfile(profile: CircadianProfileEntity)

    suspend fun runInference(fromTime: Long, toTime: Long): InferredSleepResult?
    suspend fun seedDemoDataIfEmpty()
}
