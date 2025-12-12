package com.example.battery.ui.screens.analytics

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.battery.ui.screens.analytics.components.*
import com.example.battery.ui.screens.analytics.viewmodel.AnalyticsTab
import com.example.battery.ui.screens.analytics.viewmodel.AnalyticsViewModel

/**
 * Analytics Screen v2 - Complete Analytics System
 *
 * Features:
 * - Floating tab bar (Power | Temperature | Usage)
 * - Power analytics with charging/discharging sub-tabs
 * - Temperature analytics with thermal zones
 * - Time range selector (5 min | 1 hour | 24 hours)
 * - Real-time reactive updates
 * - BATCH 6: Scrollable layout with proper ordering
 */
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = hiltViewModel(),
    scrollController: com.example.battery.util.ScrollVisibilityController,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(WindowInsets.statusBars.asPaddingValues())
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 80.dp // Prevent navbar overlap
                )
        ) {
            // Title
            Text(
                text = "Analytics",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Floating Tab Bar
            FloatingTabBar(
                selectedTab = uiState.selectedTab,
                onTabSelected = { viewModel.selectTab(it) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Content based on selected tab with Crossfade animation
            Crossfade(
                targetState = uiState.selectedTab,
                animationSpec = tween(350),
                label = "tab_content_crossfade"
            ) { selectedTab ->
                when (selectedTab) {
                    AnalyticsTab.POWER -> {
                        PowerAnalyticsSection(
                            uiState = uiState,
                            onSubTabSelected = { viewModel.selectPowerSubTab(it) },
                            onRangeSelected = { viewModel.selectTimeRange(it) }
                        )
                    }

                    AnalyticsTab.TEMPERATURE -> {
                        TemperatureAnalyticsSection(
                            uiState = uiState,
                            onRangeSelected = { viewModel.selectTimeRange(it) }
                        )
                    }

                    AnalyticsTab.USAGE -> {
                        UsagePlaceholderSection()
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

/**
 * Floating tab bar with pill-style design
 */
@Composable
private fun FloatingTabBar(
    selectedTab: AnalyticsTab,
    onTabSelected: (AnalyticsTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(14.dp),
                clip = false
            ),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AnalyticsTab.values().forEach { tab ->
                TabItem(
                    tab = tab,
                    isSelected = selectedTab == tab,
                    onClick = { onTabSelected(tab) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Individual tab item with smooth animations
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun TabItem(
    tab: AnalyticsTab,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            getTabColor(tab).copy(alpha = 0.20f)
        } else {
            Color.Transparent
        },
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "tab_background"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) {
            getTabColor(tab)
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        },
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "tab_text"
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.92f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "tab_scale"
    )

    Surface(
        modifier = modifier
            .scale(scale)
            .then(
                if (isSelected) {
                    Modifier.shadow(8.dp, RoundedCornerShape(14.dp))
                } else {
                    Modifier
                }
            )
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = backgroundColor,
        tonalElevation = 0.dp
    ) {
        // Use AnimatedContent for icon and text
        AnimatedContent(
            targetState = isSelected,
            transitionSpec = {
                fadeIn(tween(300)) with fadeOut(tween(300))
            },
            label = "tab_content"
        ) { selected ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon/Emoji
                Text(
                    text = getTabIcon(tab),
                    style = MaterialTheme.typography.titleLarge
                )

                if (selected) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = getTabLabel(tab),
                        style = MaterialTheme.typography.titleMedium,
                        color = textColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * Get color for each tab
 */
private fun getTabColor(tab: AnalyticsTab): Color {
    return when (tab) {
        AnalyticsTab.POWER -> GraphColors.AccentMint
        AnalyticsTab.TEMPERATURE -> GraphColors.AccentBlue
        AnalyticsTab.USAGE -> GraphColors.AccentPurple
    }
}

/**
 * Get icon for each tab
 */
private fun getTabIcon(tab: AnalyticsTab): String {
    return when (tab) {
        AnalyticsTab.POWER -> "⚡"
        AnalyticsTab.TEMPERATURE -> "🌡️"
        AnalyticsTab.USAGE -> "📊"
    }
}

/**
 * Get label for each tab
 */
private fun getTabLabel(tab: AnalyticsTab): String {
    return when (tab) {
        AnalyticsTab.POWER -> "Power"
        AnalyticsTab.TEMPERATURE -> "Temperature"
        AnalyticsTab.USAGE -> "Usage"
    }
}