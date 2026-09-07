package com.somna.sleeptracker.domain.personalization

import com.somna.sleeptracker.data.local.entity.CircadianProfileEntity
import java.util.Calendar
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

class BayesianPersonalizer {

    /**
     * Converts a timestamp in milliseconds into a normalized hour [0.0..24.0).
     */
    fun getNormalizedHour(timestamp: Long): Float {
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        return hour + (minute / 60f)
    }

    /**
     * Calculates probability that an event/gap at [hourOfDay] is inside the user's sleep window.
     * Uses circular normal distribution distance to handle midnight crossover.
     */
    fun calculateCircadianSleepProbability(
        hourOfDay: Float,
        profile: CircadianProfileEntity
    ): Float {
        val bedtime = profile.meanBedtimeHour
        val waketime = profile.meanWakeHour

        // Check if hour falls within the sleep interval [bedtime .. waketime] wrapping midnight
        val inInterval = if (bedtime > waketime) {
            hourOfDay >= (bedtime - 1.5f) || hourOfDay <= (waketime + 1.5f)
        } else {
            hourOfDay >= (bedtime - 1.5f) && hourOfDay <= (waketime + 1.5f)
        }

        if (!inInterval) {
            // Midday background sleep probability (naps or inactive period)
            return 0.12f
        }

        // Compute Gaussian likelihood peak around midpoint of sleep
        val sleepMidpoint = if (bedtime > waketime) {
            ((bedtime + (waketime + 24f)) / 2f) % 24f
        } else {
            (bedtime + waketime) / 2f
        }

        val diff = circularHourDistance(hourOfDay, sleepMidpoint)
        val variance = max(2.0f, (profile.bedtimeVariance + profile.wakeVariance) / 2f)
        val gaussian = exp(-0.5 * (diff / variance).toDouble().pow(2.0)).toFloat()

        return min(0.98f, max(0.20f, 0.45f + 0.53f * gaussian))
    }

    /**
     * Updates the circadian profile using Bayesian conjugate normal updating based on observed sleep session.
     */
    fun updateProfile(
        currentProfile: CircadianProfileEntity,
        observedStartTime: Long,
        observedEndTime: Long
    ): CircadianProfileEntity {
        val observedBedHour = getNormalizedHour(observedStartTime)
        val observedWakeHour = getNormalizedHour(observedEndTime)

        val priorWeight = max(2, min(currentProfile.confidenceSamples, 20)).toFloat()
        val newWeight = priorWeight + 1f

        // Normalize bed hour for averaging around midnight (e.g. 23.0 vs 1.0)
        val adjustedObservedBed = if (observedBedHour < 12f && currentProfile.meanBedtimeHour > 18f) {
            observedBedHour + 24f
        } else if (observedBedHour > 18f && currentProfile.meanBedtimeHour < 12f) {
            observedBedHour - 24f
        } else {
            observedBedHour
        }

        val updatedBedHourRaw = ((priorWeight * currentProfile.meanBedtimeHour) + adjustedObservedBed) / newWeight
        val updatedBedHour = (updatedBedHourRaw + 24f) % 24f

        val updatedWakeHour = ((priorWeight * currentProfile.meanWakeHour) + observedWakeHour) / newWeight

        val bedDiff = circularHourDistance(observedBedHour, currentProfile.meanBedtimeHour)
        val wakeDiff = circularHourDistance(observedWakeHour, currentProfile.meanWakeHour)

        val updatedBedVar = max(0.5f, currentProfile.bedtimeVariance * 0.9f + (bedDiff * bedDiff) * 0.1f)
        val updatedWakeVar = max(0.5f, currentProfile.wakeVariance * 0.9f + (wakeDiff * wakeDiff) * 0.1f)

        return currentProfile.copy(
            meanBedtimeHour = updatedBedHour,
            meanWakeHour = updatedWakeHour,
            bedtimeVariance = updatedBedVar,
            wakeVariance = updatedWakeVar,
            confidenceSamples = min(30, currentProfile.confidenceSamples + 1),
            lastUpdated = System.currentTimeMillis()
        )
    }

    private fun circularHourDistance(h1: Float, h2: Float): Float {
        val diff = Math.abs(h1 - h2)
        return if (diff > 12f) 24f - diff else diff
    }
}
