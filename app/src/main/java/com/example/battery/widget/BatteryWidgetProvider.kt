package com.example.battery.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import com.example.battery.MainActivity
import com.example.battery.R
import com.example.battery.data.repository.BatteryRepository
import com.example.battery.service.BatteryMonitorService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * BatteryWidgetProvider - The Home Screen Display
 *
 * Architecture:
 * - Uses Hilt to inject BatteryRepository
 * - ALL update logic is in onReceive() (not onUpdate())
 * - Listens for ACTION_UPDATE_WIDGET broadcast from BatteryMonitorService
 * - Synchronously reads batteryDataFlow.value (no coroutines needed)
 *
 * Data Flow:
 * Service broadcasts → onReceive() → Read repository.value → Update RemoteViews
 *
 * This eliminates race conditions because the widget ONLY updates when
 * the service explicitly tells it to, and data is guaranteed to exist.
 */
@AndroidEntryPoint
class BatteryWidgetProvider : AppWidgetProvider() {

    @Inject
    lateinit var batteryRepository: BatteryRepository

    companion object {
        const val ACTION_UPDATE_WIDGET = "com.example.battery.widget.UPDATE_WIDGET"
        private const val TAG = "BatteryWidget"
    }

    /**
     * Standard Android widget update - only used for initial setup.
     * We don't put any data logic here because it's unreliable.
     */
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d(TAG, "onUpdate called for ${appWidgetIds.size} widgets")

        // Ensure BatteryMonitorService is running
        startBatteryMonitorService(context)

        // Show loading state on all widgets
        for (appWidgetId in appWidgetIds) {
            val views = createLoadingViews(context)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    /**
     * ALL widget update logic happens here.
     * This is called when we receive the ACTION_UPDATE_WIDGET broadcast from the service.
     */
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_UPDATE_WIDGET -> {
                Log.d(TAG, "Received update broadcast")
                updateAllWidgets(context)
            }
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Log.d(TAG, "Widget enabled - starting service")
        startBatteryMonitorService(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        Log.d(TAG, "Widget disabled")
        // Don't stop the service - the app might still be using it
    }

    /**
     * Updates all widget instances with fresh data from the repository.
     * This is a synchronous operation - no coroutines needed because
     * we're reading .value which is an immediate property access.
     */
    private fun updateAllWidgets(context: Context) {
        try {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, BatteryWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

            // Synchronously read current battery data
            val batteryData = batteryRepository.batteryDataFlow.value

            // Update each widget instance
            for (appWidgetId in appWidgetIds) {
                val views = if (batteryData.timestamp > 0) {
                    createDataViews(context, batteryData)
                } else {
                    createLoadingViews(context)
                }
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }

            Log.d(TAG, "Updated ${appWidgetIds.size} widgets with fresh data")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating widgets", e)
        }
    }

    /**
     * Creates RemoteViews with actual battery data
     */
    private fun createDataViews(
        context: Context,
        batteryData: com.example.battery.data.model.BatteryData
    ): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_battery)

        // Format power display
        val powerDisplay = if (batteryData.chargingPower > 0.1f) {
            "%.1fW".format(batteryData.chargingPower)
        } else {
            "N/A"
        }

        // Update all text views
        views.setTextViewText(
            R.id.widget_battery_percentage,
            "%.0f%%".format(batteryData.batteryPercentage)
        )
        views.setTextViewText(
            R.id.widget_charging_power,
            "Power: $powerDisplay"
        )
        views.setTextViewText(
            R.id.widget_charging_type,
            "${batteryData.chargingType} ${batteryData.sourceType}".trim()
        )
        views.setTextViewText(
            R.id.widget_battery_temperature,
            "Temp: %.1f°C".format(batteryData.temperature)
        )

        // Set click intent to open MainActivity
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_battery_percentage, pendingIntent)

        return views
    }

    /**
     * Creates RemoteViews with loading/placeholder text
     */
    private fun createLoadingViews(context: Context): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_battery)

        views.setTextViewText(R.id.widget_battery_percentage, "--%")
        views.setTextViewText(R.id.widget_charging_power, "Power: --")
        views.setTextViewText(R.id.widget_charging_type, "Loading...")
        views.setTextViewText(R.id.widget_battery_temperature, "Temp: --°C")

        // Set click intent even in loading state
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_battery_percentage, pendingIntent)

        return views
    }

    /**
     * Ensures the BatteryMonitorService is running
     */
    private fun startBatteryMonitorService(context: Context) {
        try {
            val intent = Intent(context, BatteryMonitorService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            Log.d(TAG, "Started BatteryMonitorService")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting service", e)
        }
    }
}