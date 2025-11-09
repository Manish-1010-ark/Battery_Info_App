package com.example.battery.ui.screens

import android.content.Context
import android.graphics.Color
import androidx.compose.animation.core.*
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
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Charging Power Card
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
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Charging Animation + Power Display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
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
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Charging Power Text
                Text(
                    text = "%.1f W".format(batteryData.chargingPower),
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonBlueGlow
                )
            }

            // Battery Info
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoText(
                    text = "Voltage: %.1fV".format(batteryData.voltage),
                    color = NeonPurpleGlow
                )
                InfoText(
                    text = "Current: %.3fA".format(batteryData.currentAmps),
                    color = NeonPurpleGlow
                )
                InfoText(
                    text = "Battery Level: %.1f%%".format(batteryData.batteryPercentage),
                    color = NeonGreenGlow
                )
                InfoText(
                    text = "Temp: %.1f°C".format(batteryData.temperature),
                    color = NeonOrangeGlow
                )
                InfoText(
                    text = batteryData.chargingStatus,
                    color = NeonRed
                )
            }

            // Time Remaining
            Text(
                text = batteryData.timeRemaining,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryDark
            )
        }
    }
}

@Composable
fun InfoText(text: String, color: androidx.compose.ui.graphics.Color) {
    Text(
        text = text,
        fontSize = 16.sp,
        color = color,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun PowerUsageChartCard(
    batteryData: BatteryData,
    graphData: List<GraphData>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .shadow(12.dp, shape = MaterialTheme.shapes.large),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Power Usage Over Time (W)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = NeonPurple,
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
                        fontSize = 16.sp,
                        color = LightGray
                    )
                }
            } else {
                // Use key() to prevent unnecessary recompositions
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

                                xAxis.apply {
                                    position = XAxis.XAxisPosition.BOTTOM
                                    setDrawGridLines(false)
                                    textColor = Color.WHITE
                                    valueFormatter = object : ValueFormatter() {
                                        override fun getFormattedValue(value: Float): String {
                                            return "${value.toInt()}s"
                                        }
                                    }
                                }

                                axisLeft.apply {
                                    axisMinimum = 0f
                                    setDrawGridLines(true)
                                    textColor = Color.WHITE
                                    gridColor = Color.GRAY
                                }
                            }
                        },
                        update = { chart ->
                            if (graphData.isEmpty()) return@AndroidView

                            try {
                                // Convert graph data to chart entries
                                val startTime = graphData.firstOrNull()?.timestamp ?: 0L
                                val entries = graphData.mapIndexed { index, data ->
                                    val timeOffset = ((data.timestamp - startTime) / 1000f)
                                    Entry(timeOffset, data.power)
                                }

                                // Only update if we have valid entries
                                if (entries.isEmpty()) return@AndroidView

                                // Determine line color based on current power
                                val lineColor = when {
                                    batteryData.chargingPower > 20f -> Color.RED
                                    batteryData.chargingPower in 12.0f..20.0f -> Color.MAGENTA
                                    batteryData.chargingPower in 4.0f..12.0f -> Color.GREEN
                                    else -> Color.BLUE
                                }

                                val dataSet = LineDataSet(entries, "Power Usage").apply {
                                    setDrawCircles(false)
                                    setDrawValues(false)
                                    mode = LineDataSet.Mode.CUBIC_BEZIER
                                    lineWidth = 2.5f
                                    color = lineColor
                                    setDrawFilled(false)
                                }

                                chart.data = LineData(dataSet)
                                chart.notifyDataSetChanged()
                                chart.invalidate()
                            } catch (e: Exception) {
                                // Handle chart update errors gracefully
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
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Min Power
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Min Power",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonGreen
                )
                Text(
                    text = if (batteryData.minPower > 0) "%.1f W".format(batteryData.minPower) else "--",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Gray
                )
            }

            // Max Power
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Max Power",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = RedAccent
                )
                Text(
                    text = if (batteryData.maxPower > 0) "%.1f W".format(batteryData.maxPower) else "--",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Gray
                )
            }
        }
    }
}