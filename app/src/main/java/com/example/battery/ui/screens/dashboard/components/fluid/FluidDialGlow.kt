package com.example.battery.ui.screens.dashboard.components.fluid

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.battery.ui.screens.dashboard.calculateGlowIntensity

/**
 * Ambient Glow Layer
 *
 * Large radial glow that appears to light the scene from behind the dial.
 * Intensity scales with battery state and pulsates when charging.
 *
 * This creates the atmospheric "lighting" effect that makes the dial
 * appear to illuminate the surrounding space.
 */
@Composable
internal fun FluidDialGlowLayer(
    dialSize: androidx.compose.ui.unit.Dp,
    color: Color,
    isCharging: Boolean,
    percentage: Float,
    glowPulse: Float
) {
    Box(
        modifier = Modifier
            .size(dialSize * 1.8f)
            .drawWithContent {
                val center = Offset(size.width / 2f, size.height / 2f)
                val baseRadius = size.minDimension / 2f

                // Calculate ambient glow intensity based on state
                val glowIntensity = calculateGlowIntensity(isCharging, percentage, glowPulse)

                // Draw large ambient radial gradient
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            color.copy(alpha = glowIntensity),
                            color.copy(alpha = glowIntensity * 0.6f),
                            color.copy(alpha = glowIntensity * 0.3f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = baseRadius
                    ),
                    center = center,
                    radius = baseRadius,
                    blendMode = BlendMode.Screen
                )
            }
    )
}