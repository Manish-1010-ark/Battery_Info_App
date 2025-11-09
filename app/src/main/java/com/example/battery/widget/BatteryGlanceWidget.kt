package com.example.battery.widget

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalState
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.battery.MainActivity
import com.example.battery.data.model.BatteryData

/**
 * Modern Battery Widget using Jetpack Glance (Compose for Widgets)
 *
 * TECKY THEME UPDATE:
 * - Uses consistent color palette across app, notification, and widget
 * - Electric Blue (#00E5FF) for primary data
 * - Neon Red (#FF4136) for alerts/discharging
 * - Dark surface (#1A1A24) for background
 * - Bright Green (#22FF00) for charging
 * - Neon Orange (#FF8800) for temperature
 *
 * Architecture:
 * - Reads data directly from GlanceStateDefinition DataStore
 * - Service writes to DataStore, widget reads from DataStore
 * - ZERO race conditions - data is always consistent
 */
class BatteryGlanceWidget : GlanceAppWidget() {

    companion object {
        private const val TAG = "BatteryGlanceWidget"

        // TECKY THEME COLORS - Matching app theme
        private val BackgroundDark = Color(0xFF1A1A24)      // SurfaceDark
        private val ElectricBlue = Color(0xFF00E5FF)        // Primary accent
        private val NeonRed = Color(0xFFFF4136)             // Alert/Discharging
        private val BrightGreen = Color(0xFF22FF00)         // Charging/Success
        private val NeonOrange = Color(0xFFFF8800)          // Temperature
        private val TextPrimary = Color(0xFFE0E0E0)         // Main text
        private val TextSecondary = Color(0xFFA0A0A0)       // Labels/Secondary
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        Log.d(TAG, "provideGlance called for widget ID: $id")

        provideContent {
            GlanceTheme {
                BatteryWidgetContent()
            }
        }
    }

    @Composable
    private fun BatteryWidgetContent() {
        // Read state directly from DataStore - NO race condition!
        val batteryData = (LocalState.current as? BatteryData) ?: BatteryData.EMPTY

        Log.d(TAG, "Widget reading data - timestamp: ${batteryData.timestamp}, battery: ${batteryData.batteryPercentage}%")

        if (batteryData.timestamp == 0L) {
            Log.d(TAG, "Showing loading state")
            LoadingWidget()
        } else {
            Log.d(TAG, "Showing battery data: ${batteryData.batteryPercentage}%, ${batteryData.chargingPower}W")
            BatteryWidget(batteryData)
        }
    }

    @Composable
    private fun BatteryWidget(batteryData: BatteryData) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(day = BackgroundDark, night = BackgroundDark))
                .padding(12.dp)
                .clickable(actionStartActivity<MainActivity>()),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            // Battery Percentage (Large and prominent) - Electric Blue
            Text(
                text = "%.0f%%".format(batteryData.batteryPercentage),
                style = TextStyle(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(day = ElectricBlue, night = ElectricBlue)
                )
            )

            Spacer(modifier = GlanceModifier.height(8.dp))

            // Charging Status / Power
            if (batteryData.isCharging) {
                // Show charging power in Bright Green
                val powerDisplay = if (batteryData.chargingPower > 0.1f) {
                    "%.1fW".format(batteryData.chargingPower)
                } else {
                    "Charging"
                }

                Row(
                    horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                    verticalAlignment = Alignment.Vertical.CenterVertically
                ) {
                    Text(
                        text = "⚡ ",
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = ColorProvider(day = TextPrimary, night = TextPrimary)
                        )
                    )
                    Text(
                        text = powerDisplay,
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = ColorProvider(day = BrightGreen, night = BrightGreen)
                        )
                    )
                }
            } else {
                // Show "Discharging" in Neon Red
                Text(
                    text = "Discharging",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = ColorProvider(day = NeonRed, night = NeonRed)
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(6.dp))

            // Charging Type (Secondary text)
            if (batteryData.chargingType.isNotEmpty() && batteryData.chargingType != "Unknown") {
                Text(
                    text = batteryData.chargingType,
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = ColorProvider(day = TextSecondary, night = TextSecondary)
                    ),
                    maxLines = 1
                )

                Spacer(modifier = GlanceModifier.height(6.dp))
            }

            // Temperature (Neon Orange)
            Row(
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Text(
                    text = "🌡️ ",
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = ColorProvider(day = TextPrimary, night = TextPrimary)
                    )
                )
                Text(
                    text = "%.1f°C".format(batteryData.temperature),
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = ColorProvider(day = NeonOrange, night = NeonOrange)
                    )
                )
            }
        }
    }

    @Composable
    private fun LoadingWidget() {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(day = BackgroundDark, night = BackgroundDark))
                .padding(12.dp),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            Text(
                text = "Loading...",
                style = TextStyle(
                    fontSize = 16.sp,
                    color = ColorProvider(day = ElectricBlue, night = ElectricBlue)
                )
            )
        }
    }
}

/**
 * Widget Receiver for Glance
 */
class BatteryGlanceWidgetReceiver : GlanceAppWidgetReceiver() {

    companion object {
        private const val TAG = "BatteryWidgetReceiver"
    }

    override val glanceAppWidget: GlanceAppWidget = BatteryGlanceWidget()

    override fun onUpdate(context: Context, appWidgetManager: android.appwidget.AppWidgetManager, appWidgetIds: IntArray) {
        Log.d(TAG, "onUpdate called for ${appWidgetIds.size} widgets")
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onEnabled(context: Context) {
        Log.d(TAG, "Widget enabled")
        super.onEnabled(context)
    }

    override fun onDisabled(context: Context) {
        Log.d(TAG, "Widget disabled")
        super.onDisabled(context)
    }
}