package com.example.battery.ui.screens.analytics.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.battery.data.repository.TimeRange
import com.example.battery.ui.screens.analytics.viewmodel.AnalyticsUiState

/**
 * Temperature Analytics Section
 * BATCH 6: Reordered - Graph first, then Info Card, then TimeRange, then Stats
 */
@Composable
fun TemperatureAnalyticsSection(
    uiState: AnalyticsUiState,
    onRangeSelected: (TimeRange) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Temperature graph (FIRST)
        GraphTemplate(
            title = "Battery Temperature",
            data = uiState.temperature,
            unit = "Celsius (°C)",
            color = GraphColors.AccentBlue,
            timeRange = uiState.selectedRange
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Temperature info card (SECOND)
        TemperatureInfoCard()

        Spacer(modifier = Modifier.height(20.dp))

        // Time Range Selector (THIRD)
        TimeRangeSelector(
            selectedRange = uiState.selectedRange,
            onRangeSelected = onRangeSelected
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Temperature stats card (FOURTH)
        TemperatureStatsSection(
            title = "Temperature Statistics",
            stats = uiState.statsTemperature
        )
    }
}

/**
 * Temperature info card with thermal zones and staggered animations
 */
@Composable
private fun TemperatureInfoCard(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(GraphColors.AccentBlue.copy(alpha = 0.04f))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🌡️",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Temperature Zones",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Thermal zones with staggered animations
        val thermalZones = listOf(
            Triple("Cool", "< 22°C", GraphColors.AccentBlue),
            Triple("Normal", "22-35°C", GraphColors.AccentMint),
            Triple("Warm", "35-42°C", GraphColors.AccentYellow),
            Triple("Hot", "42-48°C", GraphColors.AccentOrange),
            Triple("Critical", "> 48°C", GraphColors.AccentRed)
        )

        thermalZones.forEachIndexed { index, (label, range, color) ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(300, delayMillis = index * 120)) +
                        slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = tween(300, delayMillis = index * 120)
                        )
            ) {
                ThermalZoneItem(label, range, color)
            }
        }
    }
}

/**
 * Individual thermal zone item
 */
@Composable
private fun ThermalZoneItem(
    label: String,
    range: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = range,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

/**
 * Time range selector (5 min | 1 hour | 24 hours)
 */
@Composable
private fun TimeRangeSelector(
    selectedRange: TimeRange,
    onRangeSelected: (TimeRange) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TimeRange.values().forEach { range ->
            val isSelected = selectedRange == range

            val backgroundColor by animateColorAsState(
                targetValue = if (isSelected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                } else {
                    Color.Transparent
                },
                animationSpec = spring(stiffness = Spring.StiffnessVeryLow),
                label = "range_background"
            )

            val textColor by animateColorAsState(
                targetValue = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                },
                animationSpec = spring(stiffness = Spring.StiffnessVeryLow),
                label = "range_text"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(backgroundColor)
                    .clickable { onRangeSelected(range) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (range) {
                        TimeRange.MIN_5 -> "5 min"
                        TimeRange.HOUR_1 -> "1 hour"
                        TimeRange.DAY_1 -> "24 hrs"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}