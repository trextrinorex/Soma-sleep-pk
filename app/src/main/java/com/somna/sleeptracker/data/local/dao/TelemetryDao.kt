package com.somna.sleeptracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.somna.sleeptracker.data.local.entity.CircadianProfileEntity
import com.somna.sleeptracker.data.local.entity.EventLogEntity
import com.somna.sleeptracker.data.local.entity.SleepSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TelemetryDao {

    // --- Telemetry Event Logs ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<EventLogEntity>)

    @Query("SELECT * FROM event_logs WHERE timestamp >= :since ORDER BY timestamp ASC")
    fun getRecentEvents(since: Long): Flow<List<EventLogEntity>>

    @Query("SELECT * FROM event_logs WHERE timestamp BETWEEN :fromTime AND :toTime ORDER BY timestamp ASC")
    suspend fun getEventsBetween(fromTime: Long, toTime: Long): List<EventLogEntity>

    @Query("SELECT COUNT(*) FROM event_logs")
    fun getEventCount(): Flow<Int>

    @Query("DELETE FROM event_logs WHERE timestamp < :threshold")
    suspend fun deleteEventsOlderThan(threshold: Long): Int

    // --- Sleep Sessions ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSleepSession(session: SleepSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSleepSessions(sessions: List<SleepSessionEntity>)

    @Update
    suspend fun updateSleepSession(session: SleepSessionEntity)

    @Delete
    suspend fun deleteSleepSession(session: SleepSessionEntity)

    @Query("SELECT * FROM sleep_sessions ORDER BY startTime DESC")
    fun getAllSleepSessions(): Flow<List<SleepSessionEntity>>

    @Query("SELECT * FROM sleep_sessions ORDER BY startTime DESC LIMIT 1")
    fun getLatestSleepSession(): Flow<SleepSessionEntity?>

    @Query("SELECT * FROM sleep_sessions WHERE startTime >= :since ORDER BY startTime ASC")
    fun getSleepSessionsSince(since: Long): Flow<List<SleepSessionEntity>>

    @Query("SELECT * FROM sleep_sessions WHERE dateString = :date LIMIT 1")
    suspend fun getSleepSessionForDate(date: String): SleepSessionEntity?

    @Query("DELETE FROM sleep_sessions WHERE id = :sessionId")
    suspend fun deleteSleepSessionById(sessionId: Long)

    // --- Circadian Profile ---

    @Query("SELECT * FROM circadian_profile WHERE id = 1 LIMIT 1")
    fun getCircadianProfile(): Flow<CircadianProfileEntity?>

    @Query("SELECT * FROM circadian_profile WHERE id = 1 LIMIT 1")
    suspend fun getCircadianProfileSync(): CircadianProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCircadianProfile(profile: CircadianProfileEntity)
}
