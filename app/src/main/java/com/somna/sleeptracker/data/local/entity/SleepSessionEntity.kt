package com.somna.sleeptracker.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sleep_sessions",
    indices = [
        Index(value = ["startTime"]),
        Index(value = ["dateString"])
    ]
)
data class SleepSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val startTime: Long,
    val endTime: Long,
    val durationMinutes: Long,
    val efficiencyScore: Int,
    val quality: String, // "Optimal", "Restful", "Fair", "Fragmented"
    val confidence: Float,
    val awakeningsCount: Int,
    val isManual: Boolean = false,
    val dateString: String, // e.g. "2026-09-06"
    val notes: String? = null
)
