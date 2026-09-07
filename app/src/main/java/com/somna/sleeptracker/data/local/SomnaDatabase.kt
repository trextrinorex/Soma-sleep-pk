package com.somna.sleeptracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.somna.sleeptracker.data.local.dao.TelemetryDao
import com.somna.sleeptracker.data.local.entity.CircadianProfileEntity
import com.somna.sleeptracker.data.local.entity.EventLogEntity
import com.somna.sleeptracker.data.local.entity.SleepSessionEntity

@Database(
    entities = [
        EventLogEntity::class,
        SleepSessionEntity::class,
        CircadianProfileEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SomnaDatabase : RoomDatabase() {

    abstract fun telemetryDao(): TelemetryDao

    companion object {
        @Volatile
        private var INSTANCE: SomnaDatabase? = null

        fun getInstance(context: Context): SomnaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SomnaDatabase::class.java,
                    "somna_sleep.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
