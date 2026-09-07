package com.somna.sleeptracker.domain.model

data class InferredSleepResult(
    val startTime: Long,
    val endTime: Long,
    val durationMinutes: Long,
    val efficiencyScore: Int,
    val quality: String,
    val confidenceScore: Float,
    val awakeningsCount: Int,
    val gaps: List<RawGap> = emptyList()
)
