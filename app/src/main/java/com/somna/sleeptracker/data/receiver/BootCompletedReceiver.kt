package com.somna.sleeptracker.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import com.somna.sleeptracker.data.service.TelemetryGuardianService
import com.somna.sleeptracker.di.AppModule
import com.somna.sleeptracker.domain.model.SystemEventType
import com.somna.sleeptracker.domain.model.TelemetryEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED && action != Intent.ACTION_MY_PACKAGE_REPLACED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = AppModule.provideSleepRepository(context)
                repository.logEvent(
                    TelemetryEvent(
                        timestamp = System.currentTimeMillis(),
                        eventType = SystemEventType.BOOT_COMPLETED,
                        elapsedRealtime = SystemClock.elapsedRealtime()
                    )
                )
                // Restart guardian service
                TelemetryGuardianService.startGuardian(context)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
