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
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import com.example.battery.MainActivity
import com.example.battery.R
import com.example.battery.data.repository.BatteryRepository
import com.example.battery.widget.BatteryDataStateDefinition
import com.example.battery.widget.BatteryGlanceWidget
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
import android.graphics.Color as AndroidColor

/**
 * BatteryMonitorService - The 24/7 Engine (FIXED)
 *
 * KEY FIX:
 * - Now starts immediately when app launches (not after config)
 * - Provides live battery data to ConfigScreen for charging detection
 * - Enhanced logging to diagnose any issues
 *
 * Responsibilities:
 * 1. Run a foreground service with persistent notification
 * 2. Every 1 second: call repository.updateCurrentBatteryData()
 * 3. Push live updates to Glance widgets via DataStore
 * 4. Update the notification with fresh data
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
        private const val TAG = "BatteryService"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "🚀 Service onCreate() called")
        createNotificationChannel()

        // Load stored power stats on service creation
        serviceScope.launch {
            try {
                Log.d(TAG, "📊 Loading stored power stats...")
                batteryRepository.loadStoredPowerStats()
                Log.d(TAG, "✅ Power stats loaded successfully")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error loading stored power stats", e)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "🎬 Service onStartCommand() called")

        // Start foreground service with initial notification
        try {
            val notification = createInitialNotification()
            startForeground(NOTIFICATION_ID, notification)
            Log.d(TAG, "✅ Foreground service started successfully")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to start foreground service", e)
            stopSelf()
            return START_NOT_STICKY
        }

        // Start the single monitoring loop
        startBatteryMonitoring()

        return START_STICKY
    }

    /**
     * ✅ FIXED: Enhanced logging for diagnostics
     * Single coroutine that runs the main monitoring loop
     */
    private fun startBatteryMonitoring() {
        // Cancel any existing job to prevent duplicates
        monitoringJob?.cancel()

        monitoringJob = serviceScope.launch {
            Log.d(TAG, "🔄 Battery monitoring loop started")

            var loopCount = 0

            while (isActive) {
                try {
                    loopCount++

                    // Step 1: Update repository (this updates the StateFlow)
                    batteryRepository.updateCurrentBatteryData()

                    // Step 2: Get the fresh data
                    val currentData = batteryRepository.batteryDataFlow.value

                    // ✅ CRITICAL LOG: Verify charging state is being detected
                    if (loopCount % 5 == 0) { // Log every 5 seconds to avoid spam
                        Log.d(TAG, "🔋 [Loop $loopCount] Battery: ${currentData.batteryPercentage}%, " +
                                "Charging: ${currentData.isCharging}, " +
                                "Power: ${currentData.chargingPower}W, " +
                                "Current: ${currentData.currentAmps}A")
                    }

                    // Step 3: Push to all Glance widgets
                    updateGlanceWidgets(currentData)

                    // Step 4: Update notification with the fresh data
                    updateNotification(currentData)

                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error in battery monitoring loop", e)
                    // Continue running even if one iteration fails
                }

                // Step 5: Wait exactly 1 second before next update
                delay(UPDATE_INTERVAL_MS)
            }

            Log.d(TAG, "🛑 Battery monitoring loop stopped")
        }
    }

    /**
     * Pushes fresh battery data to all Glance widget instances
     */
    private suspend fun updateGlanceWidgets(batteryData: com.example.battery.data.model.BatteryData) {
        try {
            val glanceManager = GlanceAppWidgetManager(this)
            val glanceIds = glanceManager.getGlanceIds(BatteryGlanceWidget::class.java)

            if (glanceIds.isEmpty()) {
                // Only log this occasionally to avoid spam
                return
            }

            // Update each widget's state and force recomposition
            glanceIds.forEachIndexed { index, glanceId ->
                try {
                    // Write data to widget's DataStore
                    updateAppWidgetState(
                        context = this,
                        definition = BatteryDataStateDefinition,
                        glanceId = glanceId
                    ) {
                        batteryData
                    }

                    // Force the widget to recompose with new data
                    BatteryGlanceWidget().update(this, glanceId)

                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error updating widget #${index + 1}", e)
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Fatal error in updateGlanceWidgets", e)
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
            Log.d(TAG, "🔔 Notification channel created")
        }
    }

    private fun createInitialNotification(): Notification {
        val notificationLayout = RemoteViews(packageName, R.layout.notification_layout)

        // Default initial values
        notificationLayout.setTextViewText(R.id.notification_battery_percentage, "🔋 --%")
        notificationLayout.setTextViewText(R.id.notification_charging_power, "⚡ Initializing...")
        notificationLayout.setTextViewText(R.id.notification_temperature, "🌡️ --°C")
        notificationLayout.setTextViewText(R.id.notification_time_remaining, "⌛ Starting...")

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
            .setSmallIcon(R.drawable.ic_battery_notification)
            .setContentTitle("Battery Monitor")
            .setContentText("Initializing monitoring...")
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
                // Battery Percentage - always white/primary text
                setTextViewText(
                    R.id.notification_battery_percentage,
                    "🔋 %.0f%%".format(batteryData.batteryPercentage)
                )

                // Charging Power or Discharging - changes color based on state
                if (batteryData.isCharging) {
                    setTextViewText(
                        R.id.notification_charging_power,
                        "⚡ %.1f W".format(batteryData.chargingPower)
                    )
                    // Electric Blue for charging
                    setTextColor(R.id.notification_charging_power, AndroidColor.parseColor("#00E5FF"))
                } else {
                    setTextViewText(
                        R.id.notification_charging_power,
                        "🔋 Discharging"
                    )
                    // Neon Red for discharging
                    setTextColor(R.id.notification_charging_power, AndroidColor.parseColor("#FF4136"))
                }

                // Temperature - Neon Orange
                setTextViewText(
                    R.id.notification_temperature,
                    "🌡️ %.1f°C".format(batteryData.temperature)
                )

                // Time Remaining - secondary text
                setTextViewText(
                    R.id.notification_time_remaining,
                    "⌛ ${batteryData.timeRemainingCharging}"
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
                .setSmallIcon(R.drawable.ic_battery_notification)
                .setContentTitle("Battery Monitor")
                .setContentText("Active")
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
            Log.e(TAG, "❌ Error updating notification", e)
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "🛑 Service onDestroy() called")
        monitoringJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}