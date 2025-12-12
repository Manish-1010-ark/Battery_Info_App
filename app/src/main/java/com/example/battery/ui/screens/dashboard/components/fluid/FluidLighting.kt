package com.example.battery.ui.screens.dashboard.components.fluid

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope

/**
 * FluidLighting - Subsurface light gradient (BATCH 2.5 OPTIMIZED)
 *
 * CHANGES:
 * - Halved all alpha values for clarity
 * - Removed volumetric glow (causing haze)
 * - Changed to Overlay blend mode for better color retention
 * - Added subtle bottom vignette for depth
 * - Gradient direction: top darker → bottom lighter
 */
@Composable
fun FluidLightingOverlay(
    baseColor: Color,
    intensity: Float,
    isCharging: Boolean,
    percentage: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val level = (percentage / 100f).coerceIn(0f, 1f)

        if (level <= 0f) return@Canvas

        val width = size.width
        val height = size.height
        val liquidTop = height * (1f - level)

        // Adjust intensity for charging state
        val adjustedIntensity = if (isCharging) intensity * 1.2f else intensity

        /** -----------------------------------------------
         * 1. SUBSURFACE VERTICAL GRADIENT
         * Simulates light passing through liquid depth
         * ----------------------------------------------- */
        drawSubsurfaceGradient(
            liquidTop = liquidTop,
            height = height,
            width = width,
            baseColor = baseColor,
            intensity = adjustedIntensity
        )

        /** -----------------------------------------------
         * 2. BOTTOM LIGHT POOLING
         * Brighter illumination at the base
         * ----------------------------------------------- */
        drawBottomPooling(
            width = width,
            height = height,
            baseColor = baseColor,
            intensity = adjustedIntensity * 0.4f
        )

        /** -----------------------------------------------
         * 3. BOTTOM VIGNETTE (NEW)
         * Subtle darkening for enhanced depth
         * ----------------------------------------------- */
        drawBottomVignette(
            width = width,
            height = height,
            intensity = adjustedIntensity * 0.3f
        )
    }
}

/**
 * Draws vertical gradient simulating light transmission
 * OPTIMIZED: Halved alpha values, changed to Overlay blend
 */
private fun DrawScope.drawSubsurfaceGradient(
    liquidTop: Float,
    height: Float,
    width: Float,
    baseColor: Color,
    intensity: Float
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(
            baseColor.copy(alpha = 0.025f * intensity),  // Was 0.05f
            baseColor.copy(alpha = 0.075f * intensity),  // Was 0.15f
            baseColor.copy(alpha = 0.12f * intensity)    // Was 0.25f
        ),
        startY = liquidTop,
        endY = height
    )

    drawRect(
        brush = gradient,
        topLeft = Offset(0f, liquidTop),
        size = androidx.compose.ui.geometry.Size(width, height - liquidTop),
        blendMode = BlendMode.Overlay  // Changed from Softlight
    )
}

/**
 * Draws bottom light pooling effect
 * OPTIMIZED: Reduced alpha
 */
private fun DrawScope.drawBottomPooling(
    width: Float,
    height: Float,
    baseColor: Color,
    intensity: Float
) {
    val poolHeight = height * 0.15f

    val poolGradient = Brush.verticalGradient(
        colors = listOf(
            Color.Transparent,
            Color.White.copy(alpha = 0.04f * intensity)  // Was 0.08f
        ),
        startY = height - poolHeight,
        endY = height
    )

    drawRect(
        brush = poolGradient,
        topLeft = Offset(0f, height - poolHeight),
        size = androidx.compose.ui.geometry.Size(width, poolHeight),
        blendMode = BlendMode.Screen
    )
}

/**
 * NEW: Bottom vignette for enhanced depth without haze
 */
private fun DrawScope.drawBottomVignette(
    width: Float,
    height: Float,
    intensity: Float
) {
    val vignetteHeight = height * 0.25f

    val vignetteGradient = Brush.verticalGradient(
        colors = listOf(
            Color.Transparent,
            Color.Black.copy(alpha = 0.05f * intensity)
        ),
        startY = height - vignetteHeight,
        endY = height
    )

    drawRect(
        brush = vignetteGradient,
        topLeft = Offset(0f, height - vignetteHeight),
        size = androidx.compose.ui.geometry.Size(width, vignetteHeight),
        blendMode = BlendMode.Multiply
    )
}