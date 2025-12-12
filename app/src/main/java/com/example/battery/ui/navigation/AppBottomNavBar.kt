package com.example.battery.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.battery.R
import com.example.battery.ui.theme.AppColors
import com.example.battery.ui.theme.MotionTokens
import kotlin.math.roundToInt

/**
 * Bottom Navigation Bar - Floating Pill Design (REFINED)
 *
 * IMPROVEMENTS:
 * - More subtle active pill color
 * - Better spacing and sizing
 * - Refined elevation and shadows
 * - Smoother animations
 * - Perfect visual balance
 */

sealed class NavDestination(
    val route: String,
    val label: String,
    val iconRes: Int
) {
    data object Dashboard : NavDestination("dashboard", "Dashboard", R.drawable.ic_lightning_bolt)
    data object Details : NavDestination("details", "Details", R.drawable.ic_details)
    data object Analytics : NavDestination("analytics", "Analytics", R.drawable.ic_analytics)
    data object Settings : NavDestination("settings", "Settings", R.drawable.ic_settings_nav)

    companion object {
        val items = listOf(Dashboard, Details, Analytics, Settings)
    }
}

@Composable
fun AppBottomNavBar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Find active index
    val activeIndex = remember(currentRoute) {
        NavDestination.items.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)
    }

    // Store item positions for pill animation
    val itemPositions = remember { mutableStateMapOf<Int, Float>() }
    val itemWidths = remember { mutableStateMapOf<Int, Float>() }

    Box(
        modifier = modifier
            .wrapContentWidth()
            .padding(bottom = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        // Floating pill container
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp),
            shape = RoundedCornerShape(34.dp),
            color = AppColors.NavBarBackground,
            shadowElevation = 16.dp,
            tonalElevation = 3.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp, vertical = 6.dp)
            ) {
                // Animated active pill background
                AnimatedActivePill(
                    activeIndex = activeIndex,
                    itemPositions = itemPositions,
                    itemWidths = itemWidths
                )

                // Navigation items
                Row(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NavDestination.items.forEachIndexed { index, destination ->
                        NavItem(
                            destination = destination,
                            isSelected = currentRoute == destination.route,
                            onClick = {
                                if (currentRoute != destination.route) {
                                    navController.navigate(destination.route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            onPositioned = { position, width ->
                                itemPositions[index] = position
                                itemWidths[index] = width
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Animated active pill background - MORE SUBTLE
 * Uses a softer mint color with better contrast
 */
@Composable
private fun BoxScope.AnimatedActivePill(
    activeIndex: Int,
    itemPositions: Map<Int, Float>,
    itemWidths: Map<Int, Float>
) {
    val density = LocalDensity.current

    // Animate pill position and width
    val targetOffset = itemPositions[activeIndex] ?: 0f
    val targetWidth = itemWidths[activeIndex] ?: 0f

    val animatedOffset by animateFloatAsState(
        targetValue = targetOffset,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "pill_offset"
    )

    val animatedWidth by animateFloatAsState(
        targetValue = targetWidth,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "pill_width"
    )

    // Active pill background - REFINED COLOR
    if (targetOffset > 0f || targetWidth > 0f || activeIndex == 0) {
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = animatedOffset.roundToInt(),
                        y = 0
                    )
                }
                .width(with(density) { animatedWidth.toDp() })
                .fillMaxHeight()
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(28.dp),
                    clip = false
                )
                .background(
                    // More subtle, elegant mint - 85% of full brightness
                    color = AppColors.AccentMint.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(28.dp)
                )
        )
    }
}

/**
 * Individual navigation item - REFINED
 * Better sizing and spacing
 */
@Composable
private fun NavItem(
    destination: NavDestination,
    isSelected: Boolean,
    onClick: () -> Unit,
    onPositioned: (position: Float, width: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val interactionSource = remember { MutableInteractionSource() }

    // Icon color animation - BETTER CONTRAST
    val iconTint by animateColorAsState(
        targetValue = if (isSelected) {
            // Darker icon for better contrast on mint background
            Color(0xFF0A1220)
        } else {
            // Slightly more visible inactive state
            Color.White.copy(alpha = 0.55f)
        },
        animationSpec = tween(
            durationMillis = MotionTokens.DurationMedium2,
            easing = MotionTokens.EasingStandard
        ),
        label = "icon_tint"
    )

    // Scale animation for subtle feedback
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.0f else 0.92f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "icon_scale"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(28.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .onGloballyPositioned { coordinates ->
                with(density) {
                    onPositioned(
                        coordinates.positionInParent().x,
                        coordinates.size.width.toFloat()
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = destination.iconRes),
            contentDescription = "${destination.label} screen",
            tint = iconTint,
            modifier = Modifier
                .size(26.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
        )
    }
}