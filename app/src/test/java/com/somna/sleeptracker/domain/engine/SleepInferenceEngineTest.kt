package com.somna.sleeptracker.domain.engine

import com.somna.sleeptracker.data.local.entity.CircadianProfileEntity
import com.somna.sleeptracker.domain.model.SystemEventType
import com.somna.sleeptracker.domain.model.TelemetryEvent
import com.somna.sleeptracker.domain.personalization.BayesianPersonalizer
import com.somna.sleeptracker.domain.resilience.TimeAnchorValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class SleepInferenceEngineTest {

    private val personalizer = BayesianPersonalizer()
    private val engine = SleepInferenceEngine(personalizer)

    @Test
    fun testSleepInference_successfulSession() {
        val calendar = Calendar.getInstance()
        // Bedtime: 11:00 PM (23:00)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val bedtime = calendar.timeInMillis

        // Morning wake: 7:00 AM next morning (8 hours later)
        val wakeTime = bedtime + (8L * 60L * 60L * 1000L)

        val events = listOf(
            TelemetryEvent(timestamp = bedtime - 60_000L, eventType = SystemEventType.POWER_CONNECTED, isCharging = true),
            TelemetryEvent(timestamp = bedtime, eventType = SystemEventType.SCREEN_OFF, isCharging = true),
            TelemetryEvent(timestamp = wakeTime, eventType = SystemEventType.SCREEN_ON, isCharging = true),
            TelemetryEvent(timestamp = wakeTime + 5_000L, eventType = SystemEventType.USER_PRESENT, isCharging = true),
            TelemetryEvent(timestamp = wakeTime + 60_000L, eventType = SystemEventType.POWER_DISCONNECTED, isCharging = false)
        )

        val profile = CircadianProfileEntity(
            meanBedtimeHour = 23.0f,
            meanWakeHour = 7.0f
        )

        val result = engine.inferSleepSession(events, profile)

        assertNotNull("Inferred sleep session should not be null", result)
        assertEquals(bedtime, result!!.startTime)
        assertEquals(wakeTime, result.endTime)
        assertEquals(480L, result.durationMinutes)
        assertTrue("Efficiency score should be high", result.efficiencyScore >= 90)
        assertEquals("Optimal", result.quality)
    }

    @Test
    fun testSleepInference_withBriefMidnightAwakening() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 0)
        val bedtime = calendar.timeInMillis
        val midNightAwake = bedtime + (3L * 60L * 60L * 1000L) // 2:00 AM
        val midNightBackToSleep = midNightAwake + (5L * 60L * 1000L) // 5 min check
        val wakeTime = bedtime + (7L * 60L * 60L * 1000L) // 6:00 AM

        val events = listOf(
            TelemetryEvent(timestamp = bedtime, eventType = SystemEventType.SCREEN_OFF, isCharging = true),
            TelemetryEvent(timestamp = midNightAwake, eventType = SystemEventType.SCREEN_ON, isCharging = true),
            TelemetryEvent(timestamp = midNightBackToSleep, eventType = SystemEventType.SCREEN_OFF, isCharging = true),
            TelemetryEvent(timestamp = wakeTime, eventType = SystemEventType.SCREEN_ON, isCharging = true)
        )

        val profile = CircadianProfileEntity(
            meanBedtimeHour = 23.0f,
            meanWakeHour = 6.5f
        )

        val result = engine.inferSleepSession(events, profile)

        assertNotNull(result)
        assertEquals(1, result!!.awakeningsCount)
        assertTrue(result.durationMinutes > 400L)
    }

    @Test
    fun testSleepInference_rejectsShortDaytimeGap() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 14) // 2:00 PM
        calendar.set(Calendar.MINUTE, 0)
        val time1 = calendar.timeInMillis
        val time2 = time1 + (40L * 60L * 1000L) // 40 minutes only

        val events = listOf(
            TelemetryEvent(timestamp = time1, eventType = SystemEventType.SCREEN_OFF, isCharging = false),
            TelemetryEvent(timestamp = time2, eventType = SystemEventType.SCREEN_ON, isCharging = false)
        )

        val profile = CircadianProfileEntity(
            meanBedtimeHour = 23.0f,
            meanWakeHour = 7.0f
        )

        val result = engine.inferSleepSession(events, profile)
        assertNull("Short daytime gap should not be inferred as nighttime sleep session", result)
    }

    @Test
    fun testBayesianPersonalizer_updateProfile() {
        val initialProfile = CircadianProfileEntity(
            meanBedtimeHour = 23.0f,
            meanWakeHour = 7.0f,
            confidenceSamples = 5
        )

        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        val bed = cal.timeInMillis // 12:00 AM midnight (24.0 / 0.0)

        cal.set(Calendar.HOUR_OF_DAY, 8)
        cal.set(Calendar.MINUTE, 0)
        val wake = cal.timeInMillis // 8:00 AM

        val updated = personalizer.updateProfile(initialProfile, bed, wake)

        assertEquals(6, updated.confidenceSamples)
        assertTrue(updated.meanWakeHour > 7.0f) // Pulled slightly towards 8.0 AM
    }

    @Test
    fun testTimeAnchorValidator_detectsBackwardsClockJump() {
        val validator = TimeAnchorValidator()
        val baseWall = 1_000_000_000L
        val baseElapsed = 50_000L

        val initial = validator.validateTimestamp(baseWall, baseElapsed)
        assertEquals(baseWall, initial)

        // Wall clock jumped backwards by 10 minutes (clock change)
        val backwardsWall = baseWall - 600_000L
        val forwardElapsed = baseElapsed + 10_000L

        val corrected = validator.validateTimestamp(backwardsWall, forwardElapsed)
        // Should correct to baseWall + 10_000L rather than jumping back
        assertEquals(baseWall + 10_000L, corrected)
    }
}
