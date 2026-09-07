package com.somna.sleeptracker.domain.engine

import com.somna.sleeptracker.data.local.entity.CircadianProfileEntity
import com.somna.sleeptracker.domain.model.InferredSleepResult
import com.somna.sleeptracker.domain.model.RawGap
import com.somna.sleeptracker.domain.model.SystemEventType
import com.somna.sleeptracker.domain.model.TelemetryEvent
import com.somna.sleeptracker.domain.personalization.BayesianPersonalizer
import kotlin.math.max
import kotlin.math.min

class SleepInferenceEngine(
    private val personalizer: BayesianPersonalizer = BayesianPersonalizer()
) {

    companion object {
        const val MIN_SLEEP_DURATION_MINUTES = 90L // 1.5 hours minimum
        const val MAX_AWAKENING_GAP_MINUTES = 25L  // Max duration of mid-night interruption
    }

    /**
     * Infers the primary sleep session from raw telemetry events.
     */
    fun inferSleepSession(
        events: List<TelemetryEvent>,
        profile: CircadianProfileEntity
    ): InferredSleepResult? {
        if (events.size < 2) return null

        val sortedEvents = events.sortedBy { it.timestamp }
        val rawGaps = extractInactivityGaps(sortedEvents, profile)

        if (rawGaps.isEmpty()) return null

        // Filter potential sleep candidate gaps
        val nocturnalGaps = rawGaps.filter { gap ->
            gap.durationMinutes >= 35L && gap.probabilityScore >= 0.35f
        }

        if (nocturnalGaps.isEmpty()) return null

        // Cluster consecutive gaps separated by short disruptions (screen checks < 25 mins)
        val clusters = clusterGaps(nocturnalGaps)
        if (clusters.isEmpty()) return null

        // Find the most significant cluster (longest sleep with highest combined confidence)
        val bestCluster = clusters.maxByOrNull { cluster ->
            val totalMin = cluster.sumOf { it.durationMinutes }
            val avgProb = cluster.map { it.probabilityScore }.average().toFloat()
            totalMin * avgProb
        } ?: return null

        val sessionStart = bestCluster.first().startTime
        val sessionEnd = bestCluster.last().endTime
        val totalSpanMinutes = max(1L, (sessionEnd - sessionStart) / 60_000L)

        if (totalSpanMinutes < MIN_SLEEP_DURATION_MINUTES) {
            return null
        }

        val actualSleepMinutes = bestCluster.sumOf { it.durationMinutes }
        val awakeningsCount = max(0, bestCluster.size - 1)

        val rawEfficiency = (actualSleepMinutes.toFloat() / totalSpanMinutes.toFloat()) * 100f
        val efficiency = rawEfficiency.toInt().coerceIn(40, 100)

        val anyCharging = bestCluster.any { it.wasCharging }
        val avgGapProb = bestCluster.map { it.probabilityScore }.average().toFloat()
        val confidenceScore = min(0.99f, avgGapProb * (if (anyCharging) 1.15f else 1.0f) - (awakeningsCount * 0.02f)).coerceIn(0.3f, 0.99f)

        val quality = when {
            efficiency >= 88 && actualSleepMinutes >= 420 -> "Optimal"
            efficiency >= 80 && actualSleepMinutes >= 360 -> "Restful"
            actualSleepMinutes >= 300 -> "Fair"
            else -> "Fragmented"
        }

        return InferredSleepResult(
            startTime = sessionStart,
            endTime = sessionEnd,
            durationMinutes = actualSleepMinutes,
            efficiencyScore = efficiency,
            quality = quality,
            confidenceScore = confidenceScore,
            awakeningsCount = awakeningsCount,
            gaps = bestCluster
        )
    }

    private fun extractInactivityGaps(
        events: List<TelemetryEvent>,
        profile: CircadianProfileEntity
    ): List<RawGap> {
        val gaps = mutableListOf<RawGap>()
        var lastOffTime: Long? = null
        var isCharging = false

        for (event in events) {
            when (event.eventType) {
                SystemEventType.POWER_CONNECTED -> isCharging = true
                SystemEventType.POWER_DISCONNECTED -> isCharging = false
                SystemEventType.SCREEN_OFF -> {
                    if (lastOffTime == null) {
                        lastOffTime = event.timestamp
                    }
                }
                SystemEventType.SCREEN_ON, SystemEventType.USER_PRESENT -> {
                    if (lastOffTime != null) {
                        val durationMin = (event.timestamp - lastOffTime) / 60_000L
                        if (durationMin >= 10L) {
                            val midPoint = (lastOffTime + event.timestamp) / 2L
                            val midHour = personalizer.getNormalizedHour(midPoint)
                            var prob = personalizer.calculateCircadianSleepProbability(midHour, profile)
                            if (isCharging || event.isCharging == true) {
                                prob = min(0.98f, prob + 0.15f)
                            }
                            gaps.add(
                                RawGap(
                                    startTime = lastOffTime,
                                    endTime = event.timestamp,
                                    durationMinutes = durationMin,
                                    wasCharging = isCharging || (event.isCharging == true),
                                    probabilityScore = prob
                                )
                            )
                        }
                        lastOffTime = null
                    }
                }
                else -> Unit
            }
        }

        return gaps
    }

    private fun clusterGaps(gaps: List<RawGap>): List<List<RawGap>> {
        val sorted = gaps.sortedBy { it.startTime }
        val clusters = mutableListOf<MutableList<RawGap>>()
        var currentCluster = mutableListOf<RawGap>()

        for (gap in sorted) {
            if (currentCluster.isEmpty()) {
                currentCluster.add(gap)
            } else {
                val lastGap = currentCluster.last()
                val gapIntervalMinutes = (gap.startTime - lastGap.endTime) / 60_000L
                if (gapIntervalMinutes <= MAX_AWAKENING_GAP_MINUTES) {
                    currentCluster.add(gap)
                } else {
                    clusters.add(currentCluster)
                    currentCluster = mutableListOf(gap)
                }
            }
        }
        if (currentCluster.isNotEmpty()) {
            clusters.add(currentCluster)
        }

        return clusters
    }
}
