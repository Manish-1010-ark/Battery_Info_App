package com.example.battery.notification

import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.os.*
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.battery.MainActivity
import com.example.battery.R
import com.example.battery.storage.BatteryDataStorage
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class BatteryNotificationService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval = 5000L // Update every 5 seconds

    private lateinit var batteryStorage: BatteryDataStorage
    private var minPower = Float.MAX_VALUE
    private var maxPower = 0f

    private val currentSamples = mutableListOf<Float>()
    private var avgCurrentFor10Sec = 0f
    private var currentSampleCounter = 0

    override fun onCreate() {
        super.onCreate()
        batteryStorage = BatteryDataStorage(this)

        minPower = batteryStorage.getMinPower()
        maxPower = batteryStorage.getMaxPower()

        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val permission = "android.permission.FOREGROUND_SERVICE_SPECIAL_USE"
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                stopSelf()
                return START_NOT_STICKY
            }
        }

        startForeground(1, createNotification()) // Initial notification
        handler.post(updateRunnable)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateRunnable)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private val updateRunnable = object : Runnable {
        override fun run() {
            updateNotification()
            handler.postDelayed(this, updateInterval)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "BATTERY_NOTIFICATION_CHANNEL",
                "Battery Monitoring",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val notificationLayout = RemoteViews(packageName, R.layout.notification_layout)

        // ✅ Intent to launch MainActivity
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // ✅ PendingIntent with correct flags
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, "BATTERY_NOTIFICATION_CHANNEL")
            .setSmallIcon(R.drawable.ic_battery)
            .setContentTitle("Battery Info")
            .setContentText("Battery monitoring active")
            .setCustomContentView(notificationLayout)
            .setCustomBigContentView(notificationLayout)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setContentIntent(pendingIntent) // 👈 This line makes tap work
            .build()
    }


    private fun updateNotification() {
        val batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val batteryManager = getSystemService(BATTERY_SERVICE) as BatteryManager

        val voltage = (batteryIntent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)?.toFloat() ?: 0f).let {
            if (it > 100) it / 1000f else it
        }

        val batteryPct = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val temperature = (batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0) / 10f
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        val chargingPower = getChargingPower(batteryManager, voltage)
        val currentNow = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        val currentAmps = abs(if (abs(currentNow) > 10000) currentNow / 1_000_000f else currentNow / 1000f)

        currentSamples.add(currentAmps)
        currentSampleCounter++

        if (currentSampleCounter >= 10) {
            avgCurrentFor10Sec = currentSamples.average().toFloat()
            currentSamples.clear()
            currentSampleCounter = 0
        }

        val layout = RemoteViews(packageName, R.layout.notification_layout).apply {
            setTextViewText(R.id.notification_battery_percentage, "🔋 $batteryPct%")
            setTextViewText(R.id.notification_charging_power, if (isCharging) "⚡ %.1f W".format(chargingPower) else "🔋 Discharging")
            setTextViewText(R.id.notification_temperature, "🌡️ %.0f°C".format(temperature))
            setTextViewText(R.id.notification_time_remaining, calculateTimeRemaining(batteryPct, voltage, avgCurrentFor10Sec, isCharging))
        }

        val updatedNotification = NotificationCompat.Builder(this, "BATTERY_NOTIFICATION_CHANNEL")
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSmallIcon(R.drawable.ic_battery)
            .setContentTitle("Battery Info")
            .setContentText("Battery monitoring active")
            .setCustomContentView(layout)
            .setCustomBigContentView(layout)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setOngoing(true)
            .build()

        startForeground(1, updatedNotification)
    }

    private fun getChargingPower(batteryManager: BatteryManager, voltage: Float): Float {
        var currentNow = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)

        if (currentNow == Integer.MIN_VALUE || currentNow == 0) {
            currentNow = batteryStorage.getLastCurrentNow()
        } else {
            batteryStorage.saveLastCurrentNow(currentNow)
        }

        val currentAmps = abs(if (abs(currentNow) > 10000) currentNow / 1_000_000f else currentNow / 1000f)
        val power = voltage * currentAmps

        // Store spike analysis if needed
        if (power > maxPower) maxPower = power
        if (power < minPower) minPower = power

        batteryStorage.saveMinPower(minPower)
        batteryStorage.saveMaxPower(maxPower)

        return power
    }

    private fun calculateTimeRemaining(
        batteryPct: Int,
        voltage: Float,
        avgCurrent: Float,
        isCharging: Boolean
    ): String {
        val batteryManager = getSystemService(BATTERY_SERVICE) as BatteryManager
        var chargeCounter = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)?.toFloat()

        if (chargeCounter == null || chargeCounter <= 0f || batteryPct <= 0) return "Calculating..."

        if (chargeCounter > 10000) chargeCounter /= 1000f

        val batteryCapacityMah = chargeCounter / (batteryPct / 100f)
        val batteryCapacityWh = (batteryCapacityMah * voltage) / 1000f
        val efficiency = 0.85f

        val avgPower = voltage * avgCurrent * efficiency
        if (avgPower <= 0f) return "⌛ Calculating..."

        val timeMinutes = if (isCharging) {
            val remainingFraction = (100f - batteryPct) / 100f
            ((remainingFraction * batteryCapacityWh) / avgPower * 60).toInt()
        } else {
            val dischargePower = 4.0f // Placeholder
            ((batteryPct * batteryCapacityWh) / dischargePower).toInt()
        }

        return when {
            timeMinutes < 60 -> if (isCharging) "⌛ $timeMinutes min left" else "⌛ $timeMinutes min left"
            else -> if (isCharging)
                "⌛ ${timeMinutes / 60}h ${timeMinutes % 60}m left"
            else
                "⌛ ${timeMinutes / 60}h ${timeMinutes % 60}m left"
        }
    }
}
