package com.somna.sleeptracker.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.SystemClock
import com.somna.sleeptracker.di.AppModule
import com.somna.sleeptracker.domain.model.SystemEventType
import com.somna.sleeptracker.domain.model.TelemetryEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PowerConnectionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        val (eventType, isCharging) = when (action) {
            Intent.ACTION_POWER_CONNECTED -> SystemEventType.POWER_CONNECTED to true
            Intent.ACTION_POWER_DISCONNECTED -> SystemEventType.POWER_DISCONNECTED to false
            else -> return
        }

        val batteryStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale) else null

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = AppModule.provideSleepRepository(context)
                repository.logEvent(
                    TelemetryEvent(
                        timestamp = System.currentTimeMillis(),
                        eventType = eventType,
                        batteryLevel = batteryPct,
                        isCharging = isCharging,
                        elapsedRealtime = SystemClock.elapsedRealtime()
                    )
                )
            } finally {
                pendingResult.finish()
            }
        }
    }
}
