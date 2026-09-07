package com.somna.sleeptracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "circadian_profile")
data class CircadianProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    val meanBedtimeHour: Float = 23.5f, // 11:30 PM
    val meanWakeHour: Float = 7.25f,     // 07:15 AM
    val bedtimeVariance: Float = 1.5f,
    val wakeVariance: Float = 1.2f,
    val confidenceSamples: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)
