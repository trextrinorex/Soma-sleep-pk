package com.somna.sleeptracker.data.worker

import android.content.Context
import com.somna.sleeptracker.di.AppModule

class DataRetentionWorker(private val context: Context) {

    suspend fun execute(retentionDays: Int = 14): Int {
        val repository = AppModule.provideSleepRepository(context)
        return repository.pruneOldEvents(olderThanDays = retentionDays)
    }
}
