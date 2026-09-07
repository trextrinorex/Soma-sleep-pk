package com.somna.sleeptracker

import android.app.Application
import com.somna.sleeptracker.data.service.TelemetryGuardianService
import com.somna.sleeptracker.di.AppModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SomnaApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Seed demo data if database is fresh
        CoroutineScope(Dispatchers.IO).launch {
            val repository = AppModule.provideSleepRepository(this@SomnaApplication)
            repository.seedDemoDataIfEmpty()
        }

        // Start background scheduling
        AppModule.provideWorkScheduler(this).schedulePeriodicTasks()

        // Start Telemetry Guardian service for passive sleep monitoring
        TelemetryGuardianService.startGuardian(this)
    }
}
