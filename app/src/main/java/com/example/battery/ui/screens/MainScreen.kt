package com.example.battery.ui.screens

import android.content.Context
import android.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.airbnb.lottie.LottieAnimationView
import com.example.battery.R
import com.example.battery.data.db.GraphData
import com.example.battery.data.model.BatteryData
import com.example.battery.ui.theme.*
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter

@Composable
fun MainScreen(
    batteryData: BatteryData,
    graphData: List<GraphData>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hero: Charging Power Card
            ChargingPowerCard(
                batteryData = batteryData,
                context = context
            )

            // Power Usage Chart Card
            PowerUsageChartCard(
                batteryData = batteryData,
                graphData = graphData
            )

            // Min/Max Power Stats Card
            PowerStatsCard(batteryData = batteryData)
        }
    }
}

@Composable
fun ChargingPowerCard(
    batteryData: BatteryData,
    context: Context
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, shape = MaterialTheme.shapes.extraLarge),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // HERO: Charging Power Display
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Lottie Animation
                AndroidView(
                    factory = { ctx ->
                        LottieAnimationView(ctx).apply {
                            val animationFile = if (batteryData.isCharging) {
                                "charging_bolt_green.json"
                            } else {
                                "charging_bolt_red.json"
                            }
                            setAnimation(animationFile)
                            playAnimation()
                            repeatCount = Int.MAX_VALUE
                        }
                    },
                    update = { view ->
                        val animationFile = if (batteryData.isCharging) {
                            "charging_bolt_green.json"
                        } else {
                            "charging_bolt_red.json"
                        }
                        view.setAnimation(animationFile)
                        if (!view.isAnimating) {
                            view.playAnimation()
                        }
                    },
                    modifier = Modifier.size(80.dp)
                )

                // Large Charging Power - HERO ELEMENT
                Text(
                    text = if (batteryData.isCharging) {
                        "%.1f W".format(batteryData.chargingPower)
                    } else {
                        "Discharging"
                    },
                    style = TeckyTextStyles.NumericLarge,
                    color = if (batteryData.isCharging) {
                        ChargingPowerColor
                    } else {
                        DischargingColor
                    }
                )

                // Charging Status
                Text(
                    text = batteryData.chargingStatus,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextSecondary
                )
            }

            Divider(color = TextTertiary.copy(alpha = 0.3f))

            // Battery Stats Grid (2x2)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Column 1
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatItem(
                        label = "Voltage",
                        value = "%.2fV".format(batteryData.voltage),
                        color = VoltageColor
                    )
                    StatItem(
                        label = "Battery",
                        value = "%.1f%%".format(batteryData.batteryPercentage),
                        color = if (batteryData.isCharging) BatteryChargingColor else TextPrimary
                    )
                }

                // Column 2
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatItem(
                        label = "Current",
                        value = "%.3fA".format(batteryData.currentAmps),
                        color = CurrentColor
                    )
                    StatItem(
                        label = "Temp",
                        value = "%.1f°C".format(batteryData.temperature),
                        color = TemperatureColor
                    )
                }
            }

            Divider(color = TextTertiary.copy(alpha = 0.3f))

            // Time Remaining
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⌛ ",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = batteryData.timeRemaining,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
            }
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
        )
        Text(
            text = value,
            style = TeckyTextStyles.NumericMedium.copy(fontSize = 20.sp),
            color = color
        )
    }
}

@Composable
fun PowerUsageChartCard(
    batteryData: BatteryData,
    graphData: List<GraphData>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
            .shadow(12.dp, shape = MaterialTheme.shapes.large),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Power Usage Over Time (W)",
                style = MaterialTheme.typography.titleLarge,
                color = ElectricBlue,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            if (graphData.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Start charging to see graph data",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                }
            } else {
                key(graphData.size) {
                    AndroidView(
                        factory = { context ->
                            LineChart(context).apply {
                                description.isEnabled = false
                                setTouchEnabled(true)
                                isDragEnabled = true
                                setScaleEnabled(true)
                                setPinchZoom(true)
                                setDrawGridBackground(false)
                                legend.isEnabled = false
                                axisRight.isEnabled = false
                                setBackgroundColor(Color.TRANSPARENT)

                                xAxis.apply {
                                    position = XAxis.XAxisPosition.BOTTOM
                                    setDrawGridLines(false)
                                    textColor = Color.parseColor("#A0A0A0")
                                    textSize = 10f
                                    valueFormatter = object : ValueFormatter() {
                                        override fun getFormattedValue(value: Float): String {
                                            return "${value.toInt()}s"
                                        }
                                    }
                                }

                                axisLeft.apply {
                                    axisMinimum = 0f
                                    setDrawGridLines(true)
                                    textColor = Color.parseColor("#A0A0A0")
                                    gridColor = Color.parseColor("#404040")
                                    textSize = 10f
                                }
                            }
                        },
                        update = { chart ->
                            if (graphData.isEmpty()) return@AndroidView

                            try {
                                val startTime = graphData.firstOrNull()?.timestamp ?: 0L
                                val entries = graphData.mapIndexed { index, data ->
                                    val timeOffset = ((data.timestamp - startTime) / 1000f)
                                    Entry(timeOffset, data.power)
                                }

                                if (entries.isEmpty()) return@AndroidView

                                val dataSet = LineDataSet(entries, "Power Usage").apply {
                                    setDrawCircles(false)
                                    setDrawValues(false)
                                    mode = LineDataSet.Mode.CUBIC_BEZIER
                                    lineWidth = 3f
                                    color = Color.parseColor("#00E5FF") // Electric Blue

                                    // CRITICAL: Enable fill with gradient
                                    setDrawFilled(true)
                                    fillColor = Color.parseColor("#00E5FF")
                                    fillAlpha = 100 // 40% opacity for gradient effect
                                }

                                chart.data = LineData(dataSet)
                                chart.notifyDataSetChanged()
                                chart.invalidate()
                            } catch (e: Exception) {
                                android.util.Log.e("MainScreen", "Error updating chart", e)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun PowerStatsCard(batteryData: BatteryData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, shape = MaterialTheme.shapes.medium),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Min Power
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Min Power",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (batteryData.minPower > 0) "%.1f W".format(batteryData.minPower) else "--",
                    style = TeckyTextStyles.NumericMedium,
                    color = BrightGreen
                )
            }

            // Divider
            Divider(
                modifier = Modifier
                    .width(1.dp)
                    .height(60.dp),
                color = TextTertiary.copy(alpha = 0.3f)
            )

            // Max Power
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Max Power",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (batteryData.maxPower > 0) "%.1f W".format(batteryData.maxPower) else "--",
                    style = TeckyTextStyles.NumericMedium,
                    color = NeonRed
                )
            }
        }
    }
}