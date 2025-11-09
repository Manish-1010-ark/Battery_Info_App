package com.example.battery.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.battery.MainActivity
import com.example.battery.R
import com.example.battery.data.repository.BatteryRepository
import com.example.battery.widget.BatteryWidgetProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * BatteryMonitorService - The 24/7 Engine
 *
 * Responsibilities:
 * 1. Run a foreground service with persistent notification
 * 2. Every 1 second: call repository.updateCurrentBatteryData()
 * 3. Broadcast to widget immediately after each update
 * 4. Update the notification with fresh data
 *
 * This service NEVER directly touches UI or ViewModel - it only drives the Repository.
 */
@AndroidEntryPoint
class BatteryMonitorService : Service() {

    @Inject
    lateinit var batteryRepository: BatteryRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var monitoringJob: Job? = null

    private val notificationManager by lazy {
        getSystemService(NotificationManager::class.java)
    }

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "BATTERY_NOTIFICATION_CHANNEL"
        private const val UPDATE_INTERVAL_MS = 1000L // 1 second
        private const val TAG = "Heartbeat"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        createNotificationChannel()

        // Load stored power stats on service creation
        serviceScope.launch {
            try {
                batteryRepository.loadStoredPowerStats()
                Log.d(TAG, "Power stats loaded successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading stored power stats", e)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")

        // Start foreground service with initial notification
        try {
            startForeground(NOTIFICATION_ID, createInitialNotification())
            Log.d(TAG, "Foreground service started successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start foreground service", e)
            stopSelf()
            return START_NOT_STICKY
        }

        // Start the single monitoring loop
        startBatteryMonitoring()

        return START_STICKY
    }

    /**
     * Single coroutine that runs the main monitoring loop:
     * 1. Update battery data in repository
     * 2. Broadcast to widget (widget pulls data from repository)
     * 3. Update notification with fresh data
     * 4. Wait exactly 1 second
     * 5. Repeat
     *
     * This ensures perfect synchronization: Service → Repository → Widget → Notification
     */
    private fun startBatteryMonitoring() {
        // Cancel any existing job to prevent duplicates
        monitoringJob?.cancel()

        monitoringJob = serviceScope.launch {
            Log.d(TAG, "Battery monitoring loop started")

            while (isActive) {
                try {
                    // Step 1: Update repository (this updates the StateFlow)
                    batteryRepository.updateCurrentBatteryData()

                    // Step 2: Broadcast to widget so it can pull fresh data
                    sendWidgetBroadcast()

                    // Step 3: Update notification with the fresh data
                    val currentData = batteryRepository.batteryDataFlow.value
                    updateNotification(currentData)

                } catch (e: Exception) {
                    Log.e(TAG, "Error in battery monitoring loop", e)
                    // Continue running even if one iteration fails
                }

                // Step 4: Wait exactly 1 second before next update
                delay(UPDATE_INTERVAL_MS)
            }
        }
    }

    /**
     * Sends a broadcast to the widget provider.
     * The widget will receive this and pull fresh data from the repository.
     */
    private fun sendWidgetBroadcast() {
        try {
            val widgetIntent = Intent(BatteryWidgetProvider.ACTION_UPDATE_WIDGET).apply {
                // Explicitly set the package to ensure the broadcast is received
                setPackage(packageName)
            }
            sendBroadcast(widgetIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending widget broadcast", e)
            // Don't crash if broadcast fails
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Battery Monitoring",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                description = "Displays real-time battery information"
                setShowBadge(false)
            }
            notificationManager?.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created")
        }
    }

    private fun createInitialNotification(): Notification {
        val notificationLayout = RemoteViews(packageName, R.layout.notification_layout)

        // Default initial values
        notificationLayout.setTextViewText(R.id.notification_battery_percentage, "🔋 --%")
        notificationLayout.setTextViewText(R.id.notification_charging_power, "⚡ -- W")
        notificationLayout.setTextViewText(R.id.notification_temperature, "🌡️ --°C")
        notificationLayout.setTextViewText(R.id.notification_time_remaining, "⌛ Calculating...")

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_battery_notification) // CRITICAL: Required for all notifications
            .setContentText("Monitoring battery status")
            .setCustomContentView(notificationLayout)
            .setCustomBigContentView(notificationLayout)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun updateNotification(batteryData: com.example.battery.data.model.BatteryData) {
        try {
            val notificationLayout = RemoteViews(packageName, R.layout.notification_layout).apply {
                setTextViewText(
                    R.id.notification_battery_percentage,
                    "🔋 %.0f%%".format(batteryData.batteryPercentage)
                )
                setTextViewText(
                    R.id.notification_charging_power,
                    if (batteryData.isCharging)
                        "⚡ %.1f W".format(batteryData.chargingPower)
                    else
                        "🔋 Discharging"
                )
                setTextViewText(
                    R.id.notification_temperature,
                    "🌡️ %.1f°C".format(batteryData.temperature)
                )
                setTextViewText(
                    R.id.notification_time_remaining,
                    "⌛ ${batteryData.timeRemaining}"
                )
            }

            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_battery_notification) // CRITICAL: Required for all notifications
                .setContentText("Monitoring battery status")
                .setCustomContentView(notificationLayout)
                .setCustomBigContentView(notificationLayout)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .build()

            notificationManager?.notify(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating notification", e)
            // Don't crash the service if notification update fails
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "Service destroyed")
        monitoringJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}