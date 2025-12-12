package com.example.battery.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.example.battery.ui.screens.ConfigScreen
import com.example.battery.ui.screens.dashboard.components.fluid.FluidDial
import com.example.battery.ui.theme.AppColors
import com.example.battery.ui.viewmodel.MainViewModel

/**
 * Dashboard Root Component - BATCH 1 + 4.2: ENHANCED CLEAN MINIMAL REDESIGN
 *
 * ✅ Solid dark navy base (#0C1424)
 * ✅ Soft vertical gradient (±4% brightness variation)
 * ✅ Single radial spotlight (8% alpha, centered behind dial)
 * ✅ Single vignette layer (edges only, 8-12% alpha)
 * ✅ FIXED DIAL POSITION - No vertical shifting between states
 * ✅ ENHANCED: Smooth animations, improved text hierarchy, polished micro-interactions
 *
 * IMPROVEMENTS (Batch 4.2):
 * - Enhanced text hierarchy and readability
 * - Smooth state change animations
 * - Dynamic watt color transitions
 * - Polished charging chip with elevation and glow
 * - Improved spacing rhythm (28dp, 24dp, 12dp)
 * - Animated percentage changes
 */
@Composable
fun DashboardRoot(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    // If not configured, show config screen
    if (!uiState.isConfigured) {
        ConfigScreen(
            viewModel = viewModel,
            onConfigComplete = { /* uiState will automatically update */ }
        )
        return
    }

    // Get dynamic color from AppColors system
    val dynamicColor = AppColors.getStateColor(
        percentage = uiState.batteryData.batteryPercentage,
        isCharging = uiState.batteryData.isCharging,
        chargingPower = uiState.batteryData.chargingPower
    )

    // Responsive top padding based on screen height
    val configuration = LocalConfiguration.current
    val screenHeightDp = configuration.screenHeightDp
    val topPadding = if (screenHeightDp < 700) 64.dp else 98.dp

    // ENHANCED: Animate spotlight intensity when charging state changes
    val spotlightAlpha by animateFloatAsState(
        targetValue = if (uiState.batteryData.isCharging) 0.12f else 0.08f,  // Brighter when charging
        animationSpec = tween(
            durationMillis = 600,
            easing = FastOutSlowInEasing
        ),
        label = "spotlight_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            // BATCH 1: BASE LAYER - Solid dark navy
            .background(Color(0xFF0C1424))
            // BATCH 1: SOFT VERTICAL GRADIENT (±4% brightness)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0C1424).copy(alpha = 1.04f),  // Top: +4% brightness
                        Color(0xFF0C1424),                       // Mid: Base
                        Color(0xFF0C1424).copy(alpha = 0.96f)   // Bottom: -4% brightness
                    ),
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            )
            // BATCH 1 + 4.2: ENHANCED RADIAL SPOTLIGHT + VIGNETTE
            .drawWithContent {
                drawContent()

                // ENHANCED: Single radial spotlight behind dial with animated intensity
                val spotlightCenter = Offset(size.width / 2f, size.height * 0.35f)
                val spotlightRadius = size.width * 0.75f / 2f

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            dynamicColor.copy(alpha = spotlightAlpha),  // Enhanced: animated alpha
                            dynamicColor.copy(alpha = spotlightAlpha * 0.5f),
                            Color.Transparent
                        ),
                        center = spotlightCenter,
                        radius = spotlightRadius
                    ),
                    center = spotlightCenter,
                    radius = spotlightRadius
                )

                // BATCH 1: Single vignette layer (edge-only)
                // Top edge
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.10f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = size.height * 0.12f
                    ),
                    topLeft = Offset.Zero,
                    size = androidx.compose.ui.geometry.Size(size.width, size.height * 0.12f)
                )

                // Bottom edge
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.12f)
                        ),
                        startY = size.height * 0.88f,
                        endY = size.height
                    ),
                    topLeft = Offset(0f, size.height * 0.88f),
                    size = androidx.compose.ui.geometry.Size(size.width, size.height * 0.12f)
                )

                // Left edge
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.10f),
                            Color.Transparent
                        ),
                        startX = 0f,
                        endX = size.width * 0.12f
                    ),
                    topLeft = Offset.Zero,
                    size = androidx.compose.ui.geometry.Size(size.width * 0.12f, size.height)
                )

                // Right edge
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.10f)
                        ),
                        startX = size.width * 0.88f,
                        endX = size.width
                    ),
                    topLeft = Offset(size.width * 0.88f, 0f),
                    size = androidx.compose.ui.geometry.Size(size.width * 0.12f, size.height)
                )
            }
    ) {
        // FIXED LAYOUT SYSTEM - No dynamic centering or spacing
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Fixed top breathing space
            Spacer(modifier = Modifier.height(topPadding))

            // HERO: Fluid Wave Battery Dial with Animation
            // Position is now absolutely fixed relative to screen top
            FluidDial(
                percentage = uiState.batteryData.batteryPercentage,
                isCharging = uiState.batteryData.isCharging,
                color = dynamicColor
            )

            // ENHANCED: Fixed spacing below dial (28dp for improved rhythm)
            Spacer(modifier = Modifier.height(28.dp))

            // SECONDARY: Charging Type Chip + Wattage Hero Stat
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(0.dp)  // Manual spacing
            ) {
                // ENHANCED: Charging type chip with slide-up animation
                AnimatedVisibility(
                    visible = uiState.batteryData.isCharging &&
                            uiState.batteryData.chargingType.isNotEmpty() &&
                            uiState.batteryData.chargingType != "Unknown",
                    enter = fadeIn(
                        animationSpec = tween(400, easing = LinearOutSlowInEasing)
                    ) + slideInVertically(
                        animationSpec = tween(600, easing = FastOutSlowInEasing),
                        initialOffsetY = { it / 2 }
                    ),
                    exit = fadeOut(
                        animationSpec = tween(300)
                    ) + slideOutVertically(
                        animationSpec = tween(400),
                        targetOffsetY = { it / 2 }
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ChargingChip(
                            chargingType = uiState.batteryData.chargingType,
                            color = dynamicColor
                        )

                        // ENHANCED: Spacing chip → watt (24dp)
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                // ENHANCED: Hero wattage/status with smooth animations
                HeroStatusSection(
                    isCharging = uiState.batteryData.isCharging,
                    wattage = uiState.batteryData.chargingPower,
                    color = dynamicColor,
                    percentage = uiState.batteryData.batteryPercentage
                )
            }

            // Flexible spacer pushes content down uniformly
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}