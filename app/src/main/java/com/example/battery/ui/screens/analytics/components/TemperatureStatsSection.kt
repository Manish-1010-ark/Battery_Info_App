package com.example.battery.ui.screens.analytics.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.battery.ui.screens.analytics.viewmodel.TempStats
import com.example.battery.util.formatDuration

/**
 * Reusable Temperature Stats Section Component
 * Can be used in TemperatureAnalyticsSection and other screens
 */
@Composable
fun TemperatureStatsSection(
    title: String,
    stats: TempStats,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (stats.dataPoints == 0) {
            EmptyStatsMessage()
        } else {
            // Main stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TempStatItem(
                    label = "Min",
                    value = stats.minTemp,
                    thermalLevel = getThermalLevel(stats.minTemp),
                    index = 0
                )
                TempStatItem(
                    label = "Max",
                    value = stats.maxTemp,
                    thermalLevel = getThermalLevel(stats.maxTemp),
                    index = 1
                )
                TempStatItem(
                    label = "Avg",
                    value = stats.avgTemp,
                    thermalLevel = getThermalLevel(stats.avgTemp),
                    index = 2
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Duration and data points with fade animation
            AnimatedVisibility(
                visible = stats.dataPoints > 0,
                enter = fadeIn(tween(300)),
                exit = fadeOut(tween(200))
            ) {
                MetadataRow(
                    duration = stats.duration,
                    dataPoints = stats.dataPoints
                )
            }
        }
    }
}

/**
 * Individual temperature stat item with thermal level and animated value
 */
@Composable
private fun TempStatItem(
    label: String,
    value: Float,
    thermalLevel: String,
    index: Int,
    modifier: Modifier = Modifier
) {
    // Animate the number changes
    val animatedValue by animateFloatAsState(
        targetValue = value,
        animationSpec = tween(600),
        label = "temp_value_$index"
    )

    // Staggered fade-in + slide animation
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 80L)
        isVisible = true
    }

    val offset by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (isVisible) 0.dp else 20.dp,
        animationSpec = tween(400, delayMillis = index * 80),
        label = "stat_offset_$index"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(400, delayMillis = index * 80),
        label = "stat_alpha_$index"
    )

    Column(
        modifier = modifier
            .offset(y = offset)
            .graphicsLayer { this.alpha = alpha },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "%.1f°C".format(animatedValue),
            style = MaterialTheme.typography.headlineSmall,
            color = GraphColors.AccentBlue,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = thermalLevel,
            style = MaterialTheme.typography.bodySmall,
            color = getThermalColor(thermalLevel).copy(alpha = 0.6f),
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Metadata row showing duration and data points
 */
@Composable
private fun MetadataRow(
    duration: Long,
    dataPoints: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        MetadataItem(
            label = "Duration",
            value = formatDuration(duration)
        )
        MetadataItem(
            label = "Samples",
            value = dataPoints.toString()
        )
    }
}

/**
 * Individual metadata item
 */
@Composable
private fun MetadataItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Empty state message
 */
@Composable
private fun EmptyStatsMessage() {
    Text(
        text = "No data available for selected time range",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    )
}

/**
 * Get thermal level based on temperature
 */
private fun getThermalLevel(temperature: Float): String {
    return when {
        temperature < 22f -> "Cool"
        temperature < 35f -> "Normal"
        temperature < 42f -> "Warm"
        temperature < 48f -> "Hot"
        else -> "Critical"
    }
}

/**
 * Get color based on thermal level
 */
private fun getThermalColor(level: String): Color {
    return when (level) {
        "Cool" -> GraphColors.AccentBlue
        "Normal" -> GraphColors.AccentMint
        "Warm" -> GraphColors.AccentYellow
        "Hot" -> GraphColors.AccentOrange
        "Critical" -> GraphColors.AccentRed
        else -> Color.Gray
    }
}