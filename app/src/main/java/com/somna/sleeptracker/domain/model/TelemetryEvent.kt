package com.somna.sleeptracker.domain.model

data class TelemetryEvent(
    val id: Long = 0L,
    val timestamp: Long,
    val eventType: SystemEventType,
    val batteryLevel: Int? = null,
    val isCharging: Boolean? = null,
    val elapsedRealtime: Long = 0L
)
