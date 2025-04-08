package com.example.battery.widget

import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.RemoteViews
import com.example.battery.R
import com.example.battery.storage.BatteryDataStorage
import kotlin.math.abs
//import android.util.Log


class BatteryWidgetService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private val batteryStorage by lazy { BatteryDataStorage(this) }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        updateWidget()
        return START_STICKY
    }

    private fun updateWidget() {
        val widgetManager = AppWidgetManager.getInstance(this)
        val componentName = ComponentName(this, BatteryWidgetProvider::class.java)
        val appWidgetIds = widgetManager.getAppWidgetIds(componentName)

        val batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        if (batteryIntent == null) return

        // Battery percentage
        val level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale.toFloat()) else -1f

        // Get the voltage from the battery intent
        var voltage = batteryIntent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0).toFloat()

        // Auto-detect voltage unit (millivolts or volts)
        voltage = if (voltage > 100) voltage / 1000f else voltage  // If > 100, assume it's in mV and convert to V



        // Charging status
        val plugged = batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val isCharging = plugged in listOf(
            BatteryManager.BATTERY_PLUGGED_AC,
            BatteryManager.BATTERY_PLUGGED_USB,
            BatteryManager.BATTERY_PLUGGED_WIRELESS
        )

        // Get battery current
        val batteryManager = getSystemService(BATTERY_SERVICE) as BatteryManager
        var currentNow = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)

        if (currentNow == Integer.MIN_VALUE || currentNow == 0) {
            currentNow = batteryStorage.getLastCurrentNow() // Use stored value if unavailable
        } else {
            batteryStorage.saveLastCurrentNow(currentNow) // Save for future reference
        }

        // Correctly convert current value
        val isMicroAmp = abs(currentNow) > 10_000  // If >10,000, assume it's in μA

        val currentAmps = when {
            currentNow == Integer.MIN_VALUE -> 0f
            isMicroAmp -> abs(currentNow) / 1_000_000f  // Convert μA to A
            else -> abs(currentNow) / 1000f  // Convert mA to A
        }


        // Calculate charging power safely
        val chargingPower = if (isCharging && voltage > 0 && currentAmps > 0) voltage * currentAmps else 0f

        // Determine charging type
        val chargingType = when {
            !isCharging -> "Discharging"
            chargingPower > 18 -> "Super Fast Charging ⚡⚡⚡"
            chargingPower in 10.0..18.0 -> "Fast Charging ⚡⚡"
            chargingPower in 4.0..10.0 -> "Normal Charging ⚡"
            chargingPower < 4.0 -> "Slow Charging"
            else -> "Unknown"
        }

        val sourceType = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> "(AC Charging)"
            BatteryManager.BATTERY_PLUGGED_USB -> "(USB Charging)"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "(Wireless Charging)"
            else -> ""
        }
        // Inside updateWidget() after chargingPower calculation:
//        Log.d("BatteryWidgetService", "Device Info - Voltage: $voltage V, CurrentNow: $currentNow μA, CurrentAmps: $currentAmps A, ChargingPower: $chargingPower W")


        // Get battery temperature
        val temperature = batteryIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f
        val temperatureText = "%.1f°C".format(temperature)

        // Update each widget instance
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(packageName, R.layout.widget_battery)
            val powerDisplay = if (chargingPower > 0.1f) "%.1fW".format(chargingPower) else "N/A"

            views.setTextViewText(R.id.widget_battery_percentage, "%.0f%%".format(batteryPct))
            views.setTextViewText(R.id.widget_charging_power, "Power: $powerDisplay")
            views.setTextViewText(R.id.widget_charging_type, "$chargingType $sourceType")
            views.setTextViewText(R.id.widget_battery_temperature, "Temp: $temperatureText")

            widgetManager.updateAppWidget(appWidgetId, views)
        }

        // Prevent excessive updates (every 10s)
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed({ updateWidget() }, 2000) // 2 sec
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
