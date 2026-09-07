package com.somna.sleeptracker.data.worker

import android.content.Context
import com.somna.sleeptracker.di.AppModule
import com.somna.sleeptracker.domain.model.InferredSleepResult
import java.util.Calendar

class MorningBatchInferenceWorker(private val context: Context) {

    suspend fun execute(): InferredSleepResult? {
        val repository = AppModule.provideSleepRepository(context)

        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis

        // Window: from yesterday 18:00 (6 PM) to now
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        calendar.set(Calendar.HOUR_OF_DAY, 18)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val fromTime = calendar.timeInMillis

        return repository.runInference(fromTime = fromTime, toTime = now)
    }
}
