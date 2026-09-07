package com.somna.sleeptracker.data.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.somna.sleeptracker.data.receiver.PowerConnectionReceiver
import com.somna.sleeptracker.data.receiver.ScreenStateReceiver
import com.somna.sleeptracker.ui.MainActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TelemetryGuardianService : Service() {

    companion object {
        const val CHANNEL_ID = "somna_guardian_channel"
        const val NOTIFICATION_ID = 4091

        const val ACTION_START = "com.somna.sleeptracker.ACTION_START"
        const val ACTION_STOP = "com.somna.sleeptracker.ACTION_STOP"

        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

        fun startGuardian(context: Context) {
            val intent = Intent(context, TelemetryGuardianService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                runCatching { context.startForegroundService(intent) }
                    .onFailure { context.startService(intent) }
            } else {
                context.startService(intent)
            }
        }

        fun stopGuardian(context: Context) {
            val intent = Intent(context, TelemetryGuardianService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private var screenReceiver: ScreenStateReceiver? = null
    private var powerReceiver: PowerConnectionReceiver? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopMonitoring()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                _isRunning.value = false
                return START_NOT_STICKY
            }
            else -> {
                startMonitoring()
                val notification = buildNotification()
                try {
                    startForeground(NOTIFICATION_ID, notification)
                } catch (e: Exception) {
                    // In case foreground service permissions encounter restriction
                }
                _isRunning.value = true
                return START_STICKY
            }
        }
    }

    private fun startMonitoring() {
        if (screenReceiver == null) {
            screenReceiver = ScreenStateReceiver()
            val screenFilter = IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_USER_PRESENT)
            }
            registerReceiver(screenReceiver, screenFilter)
        }

        if (powerReceiver == null) {
            powerReceiver = PowerConnectionReceiver()
            val powerFilter = IntentFilter().apply {
                addAction(Intent.ACTION_POWER_CONNECTED)
                addAction(Intent.ACTION_POWER_DISCONNECTED)
            }
            registerReceiver(powerReceiver, powerFilter)
        }
    }

    private fun stopMonitoring() {
        screenReceiver?.let {
            runCatching { unregisterReceiver(it) }
            screenReceiver = null
        }
        powerReceiver?.let {
            runCatching { unregisterReceiver(it) }
            powerReceiver = null
        }
    }

    override fun onDestroy() {
        stopMonitoring()
        _isRunning.value = false
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Somna Guardian",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors on-device stillness telemetry to infer sleep privacy-first."
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Somna Guardian Active")
            .setContentText("Autonomous sleep inference • 100% on-device")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }
}
