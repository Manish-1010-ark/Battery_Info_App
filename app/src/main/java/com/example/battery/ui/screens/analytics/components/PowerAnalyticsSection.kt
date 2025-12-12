package com.example.battery.ui.screens.analytics.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.battery.data.repository.TimeRange
import com.example.battery.ui.screens.analytics.viewmodel.AnalyticsUiState
import com.example.battery.ui.screens.analytics.viewmodel.PowerSubTab

/**
 * Power Analytics Section with Charging/Discharging tabs
 * BATCH 6: Reordered - Graph first, then Subtabs, then TimeRange, then Stats
 */
@Composable
fun PowerAnalyticsSection(
    uiState: AnalyticsUiState,
    onSubTabSelected: (PowerSubTab) -> Unit,
    onRangeSelected: (TimeRange) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Graph and stats with Crossfade animation between charging/discharging
        Crossfade(
            targetState = uiState.selectedPowerSubTab,
            animationSpec = tween(350),
            label = "power_content_crossfade"
        ) { selectedSubTab ->
            Column {
                when (selectedSubTab) {
                    PowerSubTab.CHARGING -> {
                        GraphTemplate(
                            title = "Charging Power",
                            data = uiState.chargingPower,
                            unit = "Watts (W)",
                            color = GraphColors.AccentMint,
                            timeRange = uiState.selectedRange
                        )
                    }

                    PowerSubTab.DISCHARGING -> {
                        GraphTemplate(
                            title = "Discharging Power",
                            data = uiState.dischargingPower,
                            unit = "Watts (W)",
                            color = GraphColors.AccentOrange,
                            timeRange = uiState.selectedRange
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Sub-tabs: Charging | Discharging (MOVED BELOW GRAPH)
        PowerSubTabs(
            selectedSubTab = uiState.selectedPowerSubTab,
            onSubTabSelected = onSubTabSelected
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Time Range Selector (MOVED HERE)
        TimeRangeSelector(
            selectedRange = uiState.selectedRange,
            onRangeSelected = onRangeSelected
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Stats section
        Crossfade(
            targetState = uiState.selectedPowerSubTab,
            animationSpec = tween(350),
            label = "power_stats_crossfade"
        ) { selectedSubTab ->
            when (selectedSubTab) {
                PowerSubTab.CHARGING -> {
                    PowerStatsSection(
                        title = "Charging Statistics",
                        stats = uiState.statsPowerCharging,
                        color = GraphColors.AccentMint
                    )
                }

                PowerSubTab.DISCHARGING -> {
                    PowerStatsSection(
                        title = "Discharging Statistics",
                        stats = uiState.statsPowerDischarging,
                        color = GraphColors.AccentOrange
                    )
                }
            }
        }
    }
}

/**
 * Custom pill-style sub-tabs for Power section with animations
 */
@Composable
private fun PowerSubTabs(
    selectedSubTab: PowerSubTab,
    onSubTabSelected: (PowerSubTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        PowerSubTab.values().forEach { tab ->
            val isSelected = selectedSubTab == tab

            val backgroundColor by animateColorAsState(
                targetValue = if (isSelected) {
                    when (tab) {
                        PowerSubTab.CHARGING -> GraphColors.AccentMint.copy(alpha = 0.25f)
                        PowerSubTab.DISCHARGING -> GraphColors.AccentOrange.copy(alpha = 0.25f)
                    }
                } else {
                    Color.Transparent
                },
                animationSpec = spring(stiffness = Spring.StiffnessLow),
                label = "tab_background"
            )

            val textColor by animateColorAsState(
                targetValue = if (isSelected) {
                    when (tab) {
                        PowerSubTab.CHARGING -> GraphColors.AccentMint
                        PowerSubTab.DISCHARGING -> GraphColors.AccentOrange
                    }
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                },
                animationSpec = spring(stiffness = Spring.StiffnessLow),
                label = "tab_text"
            )

            val borderWidth by androidx.compose.animation.core.animateDpAsState(
                targetValue = if (isSelected) 2.dp else 0.dp,
                animationSpec = spring(stiffness = Spring.StiffnessLow),
                label = "border_width"
            )

            // Spring "pop" animation when selecting
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1f else 0.92f,
                animationSpec = spring(stiffness = Spring.StiffnessLow),
                label = "scale"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .scale(scale)
                    .clip(RoundedCornerShape(12.dp))
                    .background(backgroundColor)
                    .then(
                        if (isSelected) {
                            Modifier.border(
                                width = borderWidth,
                                color = if (tab == PowerSubTab.CHARGING) GraphColors.AccentMint else GraphColors.AccentOrange,
                                shape = RoundedCornerShape(12.dp)
                            )
                        } else {
                            Modifier
                        }
                    )
                    .clickable(onClick = { onSubTabSelected(tab) })
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (tab) {
                        PowerSubTab.CHARGING -> "Charging"
                        PowerSubTab.DISCHARGING -> "Discharging"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
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