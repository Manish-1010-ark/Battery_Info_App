package com.example.battery.ui.screens.analytics.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.battery.data.repository.TimeRange
import com.example.battery.ui.screens.analytics.viewmodel.GraphPoint
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Unified graph template for all analytics charts
 *
 * Features:
 * - Straight line (more accurate)
 * - Theme-aligned colors with glow effect
 * - Optional gradient fill
 * - Smooth animations
 * - Proper time-based x-axis
 * - Responsive to data changes
 */
@Composable
fun GraphTemplate(
    title: String,
    data: List<GraphPoint>,
    unit: String,
    color: androidx.compose.ui.graphics.Color,
    timeRange: TimeRange,
    modifier: Modifier = Modifier,
    showGradient: Boolean = true,
    lineWidth: Float = 2f
) {
    val surfaceColor = MaterialTheme.colorScheme.surface.toArgb()
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val gridColorInt = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f).toArgb()

    // Fade-in animation for entire graph
    val alphaAnim by animateFloatAsState(
        targetValue = if (data.isEmpty()) 0.6f else 1f,
        animationSpec = tween(500),
        label = "graph_alpha"
    )

    // Slide-in animation on first load
    var hasLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        hasLoaded = true
    }

    val translationY by animateFloatAsState(
        targetValue = if (hasLoaded) 0f else 50f,
        animationSpec = tween(600),
        label = "graph_slide"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
            .alpha(alphaAnim)
            .graphicsLayer {
                this.translationY = translationY
            }
    ) {
        // Title
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Chart
        if (data.isEmpty()) {
            EmptyGraphState(timeRange)
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .padding(top = 12.dp)
            ) {
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    factory = { context ->
                        LineChart(context).apply {
                            description.isEnabled = false
                            legend.isEnabled = false
                            setTouchEnabled(true)
                            isDragEnabled = true
                            setScaleEnabled(false)
                            setPinchZoom(false)
                            setDrawGridBackground(false)
                            setBackgroundColor(surfaceColor)

                            // Disable right Y-axis
                            axisRight.isEnabled = false

                            // Configure left Y-axis
                            axisLeft.apply {
                                setDrawGridLines(true)
                                gridColor = gridColorInt
                                gridLineWidth = 0.5f
                                textColor = onSurfaceColor
                                textSize = 10f
                                setDrawAxisLine(false)
                                valueFormatter = object : ValueFormatter() {
                                    override fun getFormattedValue(value: Float): String {
                                        return "%.1f".format(value)
                                    }
                                }
                            }

                            // Configure X-axis
                            xAxis.apply {
                                position = XAxis.XAxisPosition.BOTTOM
                                setDrawGridLines(true)
                                gridColor = gridColorInt
                                gridLineWidth = 0.5f
                                textColor = onSurfaceColor
                                textSize = 10f
                                setDrawAxisLine(false)
                                granularity = when (timeRange) {
                                    TimeRange.MIN_5 -> 30_000f     // 30 seconds
                                    TimeRange.HOUR_1 -> 300_000f   // 5 minutes
                                    TimeRange.DAY_1 -> 3_600_000f  // 1 hour
                                }
                                labelRotationAngle = -30f
                                labelCount = 4
                            }

                            // Enhanced animation - animateXY instead of animateX
                            animateXY(600, 600)
                        }
                    },
                    update = { chart ->
                        // Normalize timestamps
                        val base = data.first().timestamp
                        val entries = data.map { point ->
                            Entry((point.timestamp - base).toFloat(), point.value)
                        }

                        val dataSet = LineDataSet(entries, "").apply {
                            // Line style
                            this.color = color.toArgb()
                            this.lineWidth = lineWidth
                            mode = LineDataSet.Mode.LINEAR // Straight lines (more accurate)

                            // Remove circles on data points (clean look)
                            setDrawCircles(false)
                            setDrawCircleHole(false)

                            // Disable value labels on points
                            setDrawValues(false)

                            // Gradient fill
                            if (showGradient) {
                                setDrawFilled(true)
                                fillColor = color.toArgb()
                                fillAlpha = 30
                            }

                            // Highlight settings (when user taps)
                            highLightColor = color.copy(alpha = 0.8f).toArgb()
                            setDrawHighlightIndicators(true)
                            highlightLineWidth = 1.5f
                        }

                        // Update formatter with base timestamp
                        val formatter = TimeAxisFormatter(timeRange)
                        formatter.setBase(base)
                        chart.xAxis.valueFormatter = formatter

                        chart.data = LineData(dataSet)
                        chart.invalidate()
                    }
                )
            }
        }

        // Unit label
        if (data.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = unit,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

/**
 * Empty state when no data is available
 */
@Composable
private fun EmptyGraphState(timeRange: TimeRange) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "No data available",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = getEmptyStateMessage(timeRange),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}

/**
 * Get appropriate message for empty state based on time range
 */
private fun getEmptyStateMessage(timeRange: TimeRange): String {
    return when (timeRange) {
        TimeRange.MIN_5 -> "Data will appear after 5 minutes of monitoring"
        TimeRange.HOUR_1 -> "Data will appear after 1 hour of monitoring"
        TimeRange.DAY_1 -> "Data will appear after 24 hours of monitoring"
    }
}

/**
 * Custom X-axis formatter for time-based data
 * Formats timestamps intelligently based on time range
 */
private class TimeAxisFormatter(
    private val timeRange: TimeRange
) : ValueFormatter() {

    private var baseTimestamp: Long = 0L
    private val dateFormat5Min = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateFormat1Hour = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateFormat24Hour = SimpleDateFormat("HH:mm", Locale.getDefault())

    fun setBase(base: Long) {
        baseTimestamp = base
    }

    override fun getFormattedValue(value: Float): String {
        val timestamp = baseTimestamp + value.toLong()

        return when (timeRange) {
            TimeRange.MIN_5 -> {
                // Show every minute for 5-min range
                dateFormat5Min.format(Date(timestamp))
            }
            TimeRange.HOUR_1 -> {
                // Show every 10-15 minutes for 1-hour range
                dateFormat1Hour.format(Date(timestamp))
            }
            TimeRange.DAY_1 -> {
                // Show every few hours for 24-hour range
                dateFormat24Hour.format(Date(timestamp))
            }
        }
    }
}

// ====== PREVIEW HELPERS (Optional - for testing) ======

/**
 * Generate sample data for preview/testing
 */
fun generateSampleData(
    count: Int = 20,
    minValue: Float = 0f,
    maxValue: Float = 30f,
    timeRangeMillis: Long = 3600000L // 1 hour
): List<GraphPoint> {
    val now = System.currentTimeMillis()
    val step = timeRangeMillis / count

    return (0 until count).map { i ->
        val timestamp = now - timeRangeMillis + (i * step)
        val value = minValue + (Math.random() * (maxValue - minValue)).toFloat()
        GraphPoint(timestamp, value)
    }
}