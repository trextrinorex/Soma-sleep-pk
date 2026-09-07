package com.somna.sleeptracker.di

import android.content.Context
import com.somna.sleeptracker.data.local.SomnaDatabase
import com.somna.sleeptracker.data.local.dao.TelemetryDao
import com.somna.sleeptracker.data.repository.SleepRepositoryImpl
import com.somna.sleeptracker.data.worker.WorkScheduler
import com.somna.sleeptracker.domain.engine.SleepInferenceEngine
import com.somna.sleeptracker.domain.personalization.BayesianPersonalizer
import com.somna.sleeptracker.domain.repository.SleepRepository

object AppModule {

    @Volatile
    private var repository: SleepRepository? = null

    @Volatile
    private var workScheduler: WorkScheduler? = null

    fun provideDatabase(context: Context): SomnaDatabase {
        return SomnaDatabase.getInstance(context)
    }

    fun provideTelemetryDao(context: Context): TelemetryDao {
        return provideDatabase(context).telemetryDao()
    }

    fun provideBayesianPersonalizer(): BayesianPersonalizer {
        return BayesianPersonalizer()
    }

    fun provideSleepInferenceEngine(): SleepInferenceEngine {
        return SleepInferenceEngine(provideBayesianPersonalizer())
    }

    fun provideSleepRepository(context: Context): SleepRepository {
        return repository ?: synchronized(this) {
            repository ?: SleepRepositoryImpl(
                dao = provideTelemetryDao(context),
                inferenceEngine = provideSleepInferenceEngine(),
                personalizer = provideBayesianPersonalizer()
            ).also { repository = it }
        }
    }

    fun provideWorkScheduler(context: Context): WorkScheduler {
        return workScheduler ?: synchronized(this) {
            workScheduler ?: WorkScheduler(context.applicationContext).also { workScheduler = it }
        }
    }
}
