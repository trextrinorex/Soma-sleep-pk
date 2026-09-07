package com.somna.sleeptracker.domain.model

data class RawGap(
    val startTime: Long,
    val endTime: Long,
    val durationMinutes: Long,
    val wasCharging: Boolean = false,
    val probabilityScore: Float = 0f
)
