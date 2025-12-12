package com.example.battery.ui.screens.dashboard.components.fluid

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.sin

/**
 * FluidCrestShimmer - Enhanced Premium Circumference Sweep
 *
 * FINAL POLISH:
 * - Multi-layer shimmer with depth and glow
 * - Enhanced visibility with optimized gradients
 * - Smooth normalized angle wrapping
 * - Premium pulsing and trailing effects
 */

@Composable
fun FluidCrestShimmerOverlay(
    percentage: Float,
    modifier: Modifier = Modifier
) {
    var shimmerAngle by remember { mutableFloatStateOf(0f) }
    var animationTime by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()

        while (true) {
            kotlinx.coroutines.delay(16L)  // 60 FPS for smooth rotation

            val elapsedMillis = System.currentTimeMillis() - startTime
            animationTime = elapsedMillis / 1000f

            // Variable speed with organic curve (1.5° ± 0.7° per frame)
            val speedVariation = 1.5f + sin(animationTime * 0.5f) * 0.7f
            shimmerAngle = (shimmerAngle + speedVariation) % 360f
        }
    }

    // Normalize angle to 0-360° range
    val normalizeAngle = { angle: Float -> ((angle % 360f) + 360f) % 360f }

    // Dynamic alpha modulation for organic pulsing
    val basePulse = 0.5f + 0.2f * sin(animationTime * 2f)  // 0.5-0.7 range
    val sweepAngle = 50f  // Slightly wider sweep for better visibility

    // Main shimmer brush with enhanced gradient
    val mainBrush = remember(shimmerAngle, animationTime) {
        Brush.sweepGradient(
            normalizeAngle(shimmerAngle - 10f) to Color.Transparent,
            normalizeAngle(shimmerAngle - 5f) to Color.White.copy(alpha = basePulse * 0.3f),
            normalizeAngle(shimmerAngle) to Color.White.copy(alpha = basePulse * 0.7f),
            normalizeAngle(shimmerAngle + sweepAngle * 0.3f) to Color.White.copy(alpha = basePulse),
            normalizeAngle(shimmerAngle + sweepAngle * 0.6f) to Color.White.copy(alpha = basePulse * 0.8f),
            normalizeAngle(shimmerAngle + sweepAngle) to Color.White.copy(alpha = basePulse * 0.4f),
            normalizeAngle(shimmerAngle + sweepAngle + 10f) to Color.Transparent
        )
    }

    // Trailing glow brush with cyan-blue tint
    val glowBrush = remember(shimmerAngle, animationTime) {
        val glowPulse = 0.3f + 0.15f * sin(animationTime * 1.5f)
        Brush.sweepGradient(
            normalizeAngle(shimmerAngle - 5f) to Color.Transparent,
            normalizeAngle(shimmerAngle + sweepAngle * 0.2f) to Color(0xFFE0F7FF).copy(alpha = glowPulse),
            normalizeAngle(shimmerAngle + sweepAngle * 0.5f) to Color(0xFFB3E5FF).copy(alpha = glowPulse * 1.2f),
            normalizeAngle(shimmerAngle + sweepAngle * 0.8f) to Color(0xFF80D4FF).copy(alpha = glowPulse * 0.8f),
            normalizeAngle(shimmerAngle + sweepAngle + 5f) to Color.Transparent
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val radius = size.width / 2f

        // LAYER 1: Outer glow halo (soft ambient)
        drawCircle(
            brush = glowBrush,
            radius = radius + 4f,
            center = Offset(centerX, centerY),
            style = Stroke(width = 8f),
            blendMode = BlendMode.Screen,
            alpha = 0.3f
        )

        // LAYER 2: Main shimmer ring (primary highlight)
        drawCircle(
            brush = mainBrush,
            radius = radius,
            center = Offset(centerX, centerY),
            style = Stroke(width = 16f),
            blendMode = BlendMode.Screen
        )

        // LAYER 3: Inner core glow (depth)
        drawCircle(
            brush = mainBrush,
            radius = radius - 12f,
            center = Offset(centerX, centerY),
            style = Stroke(width = 10f),
            blendMode = BlendMode.Screen,
            alpha = 0.5f
        )

        // LAYER 4: Leading bright spot (animated)
        val spotAngle = shimmerAngle + sweepAngle * 0.4f
        val angleRad = Math.toRadians(spotAngle.toDouble())
        val spotX = centerX + (radius * cos(angleRad)).toFloat()
        val spotY = centerY + (radius * sin(angleRad)).toFloat()

        // Outer spot halo
        drawCircle(
            color = Color.White.copy(alpha = basePulse * 0.4f),
            radius = 14f,
            center = Offset(spotX, spotY),
            blendMode = BlendMode.Screen
        )

        // Inner bright core
        drawCircle(
            color = Color.White.copy(alpha = basePulse * 0.9f),
            radius = 7f,
            center = Offset(spotX, spotY),
            blendMode = BlendMode.Screen
        )

        // LAYER 5: Trailing accent spots
        val trailSpots = listOf(
            Pair(sweepAngle * 0.15f, 0.5f),
            Pair(sweepAngle * 0.7f, 0.6f)
        )

        trailSpots.forEach { (offset, alphaMult) ->
            val trailAngle = shimmerAngle + offset
            val trailRad = Math.toRadians(trailAngle.toDouble())
            val trailX = centerX + (radius * cos(trailRad)).toFloat()
            val trailY = centerY + (radius * sin(trailRad)).toFloat()

            drawCircle(
                color = Color.White.copy(alpha = basePulse * alphaMult * 0.7f),
                radius = 6f,
                center = Offset(trailX, trailY),
                blendMode = BlendMode.Screen
            )
        }

        // LAYER 6: Sparkle effect at leading edge
        val sparkleAngle = shimmerAngle + sweepAngle * 0.25f
        val sparkleRad = Math.toRadians(sparkleAngle.toDouble())
        val sparkleX = centerX + ((radius - 6f) * cos(sparkleRad)).toFloat()
        val sparkleY = centerY + ((radius - 6f) * sin(sparkleRad)).toFloat()

        drawCircle(
            color = Color(0xFFFFFFFF).copy(alpha = basePulse * 0.8f),
            radius = 3f,
            center = Offset(sparkleX, sparkleY),
            blendMode = BlendMode.Screen
        )
    }
}