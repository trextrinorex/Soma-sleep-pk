package com.somna.sleeptracker.data.worker

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar

class WorkScheduler(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Default)

    fun schedulePeriodicTasks() {
        // Routine background loop: daily retention cleanup & periodic morning inference check
        scope.launch {
            while (isActive) {
                runCatching {
                    val calendar = Calendar.getInstance()
                    val hour = calendar.get(Calendar.HOUR_OF_DAY)

                    // If between 6:00 AM and 10:00 AM, attempt morning batch inference
                    if (hour in 6..10) {
                        MorningBatchInferenceWorker(context).execute()
                    }

                    // Daily data retention purge
                    DataRetentionWorker(context).execute(retentionDays = 14)
                }

                // Run every 2 hours
                delay(2 * 60 * 60 * 1000L)
            }
        }
    }
}
