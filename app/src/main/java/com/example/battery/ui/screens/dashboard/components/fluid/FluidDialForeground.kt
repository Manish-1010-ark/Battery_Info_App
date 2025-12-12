package com.example.battery.ui.screens.dashboard.components.fluid

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Foreground Content - CLEAN PERCENTAGE DISPLAY (NO GLOW)
 *
 * ✅ Large percentage text (88sp)
 * ✅ Pure white color
 * ✅ Clean shadow for depth
 * ✅ Smooth animations
 * ✅ NO canvas glow, NO multi-layer effects
 */
@Composable
internal fun FluidDialForegroundContent(
    percentage: Float,
    isCharging: Boolean,
    color: Color,
    iconOpacity: Float
) {
    // Smooth animation for percentage changes
    val animatedPercentage by animateIntAsState(
        targetValue = percentage.toInt(),
        animationSpec = tween(
            durationMillis = 800,
            easing = FastOutSlowInEasing
        ),
        label = "percentage_animation"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        // CLEAN PERCENTAGE TEXT - Simple and elegant
        Text(
            text = "$animatedPercentage%",
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 88.sp,  // Clean readable size
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-3.5).sp,
                fontFeatureSettings = "tnum",
                // Simple shadow for depth
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = Color.Black.copy(alpha = 0.4f),
                    offset = Offset(0f, 4f),
                    blurRadius = 10f
                )
            ),
            color = Color.White  // Pure white for maximum contrast
        )
    }
}