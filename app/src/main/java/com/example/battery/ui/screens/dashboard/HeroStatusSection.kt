package com.example.battery.ui.screens.dashboard

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.battery.ui.theme.MaterialSymbols

/**
 * Hero Status Section - CLEAN & SIMPLE (NO GLOW)
 *
 * ✅ Clean wattage display (44sp)
 * ✅ Clear status text (20sp)
 * ✅ Simple bolt icon (32dp)
 * ✅ Minimal shadows only
 * ✅ NO glow effects, NO pulsing animations
 * ✅ Smooth value transitions only
 */
@Composable
fun HeroStatusSection(
    isCharging: Boolean,
    wattage: Float,
    color: Color,
    percentage: Float,
    modifier: Modifier = Modifier
) {
    // Smooth animation for wattage changes
    val animatedWattage by animateFloatAsState(
        targetValue = wattage,
        animationSpec = tween(
            durationMillis = 500,
            easing = FastOutSlowInEasing
        ),
        label = "wattage_animation"
    )

    // Smooth animation for percentage changes
    val animatedPercentage by animateIntAsState(
        targetValue = percentage.toInt(),
        animationSpec = tween(
            durationMillis = 400,
            easing = LinearOutSlowInEasing
        ),
        label = "percentage_animation"
    )

    // Smooth fade animation for charging state text
    val chargingTextAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = 400,
            easing = LinearOutSlowInEasing
        ),
        label = "charging_text_fade"
    )

    // Dynamic watt color based on charging state
    val wattColor = when {
        isCharging && wattage > 15f -> Color(0xFF3CFF55)  // Pure green for active charging
        isCharging && wattage > 5f -> Color(0xFFE9E870)   // Warm yellow for trickle charging
        isCharging -> Color(0xFF8DD0FF)                    // Cool cyan for idle/slow charging
        else -> color.copy(alpha = 0.92f)
    }

    val animatedWattColor by animateColorAsState(
        targetValue = wattColor,
        animationSpec = tween(
            durationMillis = 600,
            easing = FastOutSlowInEasing
        ),
        label = "watt_color_animation"
    )

    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // 1. WATTAGE (CLEAN) - only when charging
        if (isCharging && wattage > 0f) {
            Text(
                text = "%.1f W".format(animatedWattage),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = 44.sp,  // Clean size: readable but not overwhelming
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                    fontFeatureSettings = "tnum",
                    // Minimal shadow only
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = animatedWattColor.copy(alpha = 0.25f),
                        offset = Offset(0f, 3f),
                        blurRadius = 8f
                    )
                ),
                color = animatedWattColor,
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // 2. STATUS LABEL (ENHANCED)
        val statusColor by animateColorAsState(
            targetValue = if (isCharging) Color(0xFF90E8FF) else Color(0xFFB0BEC5),
            animationSpec = tween(400)
        )

        val statusScale by animateFloatAsState(
            targetValue = if (isCharging) 1.06f else 1.0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        )

        Text(
            text = if (isCharging) "Charging" else "Discharging",
            modifier = Modifier.graphicsLayer {
                scaleX = statusScale
                scaleY = statusScale
            },
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = statusColor.copy(alpha = 0.35f),
                    offset = Offset(0f, 2f),
                    blurRadius = 6f
                )
            ),
            color = statusColor.copy(alpha = chargingTextAlpha),
            textAlign = TextAlign.Center,
            maxLines = 1
        )


        // 3. BOLT ICON (CLEAN) - only when charging
        if (isCharging) {
            Spacer(modifier = Modifier.height(20.dp))

            Icon(
                imageVector = MaterialSymbols.BoltRounded,
                contentDescription = "Charging",
                tint = Color.White,
                modifier = Modifier.size(32.dp)  // Clean size
            )
        }
    }
}