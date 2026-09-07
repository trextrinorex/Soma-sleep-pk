package com.somna.sleeptracker.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.somna.sleeptracker.domain.model.SystemEventType
import com.somna.sleeptracker.domain.model.TelemetryEvent

@Entity(
    tableName = "event_logs",
    indices = [Index(value = ["timestamp"])]
)
data class EventLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val timestamp: Long,
    val eventType: String,
    val batteryLevel: Int? = null,
    val isCharging: Boolean? = null,
    val elapsedRealtime: Long = 0L
) {
    fun toDomain(): TelemetryEvent {
        val type = runCatching { SystemEventType.valueOf(eventType) }
            .getOrDefault(SystemEventType.HEARTBEAT)
        return TelemetryEvent(
            id = id,
            timestamp = timestamp,
            eventType = type,
            batteryLevel = batteryLevel,
            isCharging = isCharging,
            elapsedRealtime = elapsedRealtime
        )
    }

    companion object {
        fun fromDomain(domain: TelemetryEvent): EventLogEntity {
            return EventLogEntity(
                id = domain.id,
                timestamp = domain.timestamp,
                eventType = domain.eventType.name,
                batteryLevel = domain.batteryLevel,
                isCharging = domain.isCharging,
                elapsedRealtime = domain.elapsedRealtime
            )
        }
    }
}
