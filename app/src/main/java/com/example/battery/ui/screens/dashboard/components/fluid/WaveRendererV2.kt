package com.example.battery.ui.screens.dashboard.components.fluid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.PI
import kotlin.math.sin

/**
 * WaveRendererV2 - Triple-wave rendering (BATCH 2.5 OPTIMIZED)
 *
 * CHANGES:
 * - Reduced amplitudes by ~40% (80→48, 40→24, 60→36)
 * - Added baselineOffset per wave layer (index * 6f)
 * - Adjusted alphas (0.8, 0.6, 0.4) for better depth separation
 * - Maintained motion math for seamless looping
 * - Vertical gradients use color lightening/darkening, not alpha fading
 */
class WaveRendererV2 {

    // Wave configuration constants (OPTIMIZED)
    private companion object {
        const val WAVE_1_AMPLITUDE = 48f  // Was 80f (40% reduction)
        const val WAVE_2_AMPLITUDE = 24f  // Was 40f (40% reduction)
        const val WAVE_3_AMPLITUDE = 36f  // Was 60f (40% reduction)

        const val WAVE_1_SPEED = 0.6f
        const val WAVE_2_SPEED = 1.0f
        const val WAVE_3_SPEED = 1.5f

        const val WAVE_1_WAVELENGTH_RATIO = 0.9f
        const val WAVE_2_WAVELENGTH_RATIO = 1.2f
        const val WAVE_3_WAVELENGTH_RATIO = 0.7f

        const val WAVE_1_ALPHA = 0.8f  // Was 0.65f
        const val WAVE_2_ALPHA = 0.6f  // Was 0.80f
        const val WAVE_3_ALPHA = 0.4f  // Was 0.92f

        const val PHASE_OFFSET_WAVE_2 = 0.7f * PI.toFloat()
        const val PHASE_OFFSET_WAVE_3 = 1.4f * PI.toFloat()

        const val CHARGING_SPEED_MULTIPLIER = 1.4f
        const val SAMPLE_STEP = 4

        const val BASELINE_OFFSET_PER_LAYER = 6f  // NEW: Vertical separation
    }

    fun draw(
        drawScope: DrawScope,
        level: Float,
        color: Color,
        time: Float,
        isCharging: Boolean
    ) = with(drawScope) {

        if (level <= 0f) return@with

        val width = size.width
        val height = size.height
        val liquidBaseline = height * (1f - level)

        // Speed multiplier for charging state
        val speedMultiplier = if (isCharging) CHARGING_SPEED_MULTIPLIER else 1f

        /** -----------------------------------------------
         * WAVE 1: Background layer (slow, deep)
         * Darker shade for depth perception
         * ----------------------------------------------- */
        val wave1Path = buildWavePath(
            width = width,
            height = height,
            baseline = liquidBaseline + (0 * BASELINE_OFFSET_PER_LAYER),  // Layer 0
            amplitude = WAVE_1_AMPLITUDE,
            wavelength = width * WAVE_1_WAVELENGTH_RATIO,
            phase = time * WAVE_1_SPEED * speedMultiplier
        )

        // Darken by 20%
        val wave1Color = Color(
            red = color.red * 0.80f,
            green = color.green * 0.80f,
            blue = color.blue * 0.80f,
            alpha = 1f
        )

        drawPath(
            path = wave1Path,
            brush = Brush.verticalGradient(
                colors = listOf(
                    wave1Color,  // Full color at top
                    wave1Color.copy(
                        red = wave1Color.red * 0.85f,
                        green = wave1Color.green * 0.85f,
                        blue = wave1Color.blue * 0.85f
                    )  // Slightly darker at bottom
                ),
                startY = liquidBaseline,
                endY = height
            ),
            alpha = WAVE_1_ALPHA
        )

        /** -----------------------------------------------
         * WAVE 2: Middle layer (medium speed, main motion)
         * Base color - true to original
         * ----------------------------------------------- */
        val wave2Path = buildWavePath(
            width = width,
            height = height,
            baseline = liquidBaseline + (1 * BASELINE_OFFSET_PER_LAYER),  // Layer 1
            amplitude = WAVE_2_AMPLITUDE,
            wavelength = width * WAVE_2_WAVELENGTH_RATIO,
            phase = time * WAVE_2_SPEED * speedMultiplier + PHASE_OFFSET_WAVE_2
        )

        val wave2Color = color

        drawPath(
            path = wave2Path,
            brush = Brush.verticalGradient(
                colors = listOf(
                    wave2Color,  // Full color at top
                    wave2Color.copy(
                        red = wave2Color.red * 0.90f,
                        green = wave2Color.green * 0.90f,
                        blue = wave2Color.blue * 0.90f
                    )  // Slightly darker at bottom
                ),
                startY = liquidBaseline,
                endY = height
            ),
            alpha = WAVE_2_ALPHA
        )

        /** -----------------------------------------------
         * WAVE 3: Foreground layer (fast, surface detail)
         * Lighter shade for surface brightness
         * ----------------------------------------------- */
        val wave3Path = buildWavePath(
            width = width,
            height = height,
            baseline = liquidBaseline + (2 * BASELINE_OFFSET_PER_LAYER),  // Layer 2
            amplitude = WAVE_3_AMPLITUDE,
            wavelength = width * WAVE_3_WAVELENGTH_RATIO,
            phase = time * WAVE_3_SPEED * speedMultiplier + PHASE_OFFSET_WAVE_3
        )

        // Lighten by 15%
        val wave3Color = Color(
            red = color.red + (1f - color.red) * 0.15f,
            green = color.green + (1f - color.green) * 0.15f,
            blue = color.blue + (1f - color.blue) * 0.15f,
            alpha = 1f
        )

        drawPath(
            path = wave3Path,
            brush = Brush.verticalGradient(
                colors = listOf(
                    wave3Color,  // Full color at top
                    wave3Color.copy(
                        red = wave3Color.red * 0.92f,
                        green = wave3Color.green * 0.92f,
                        blue = wave3Color.blue * 0.92f
                    )  // Slightly darker at bottom
                ),
                startY = liquidBaseline,
                endY = height
            ),
            alpha = WAVE_3_ALPHA
        )

        /** -----------------------------------------------
         * GLOSSY CREST HIGHLIGHT (Surface reflection)
         * ----------------------------------------------- */
        drawCrestHighlight(
            path = wave3Path,
            liquidTop = liquidBaseline,
            width = width
        )
    }

    /**
     * Builds a sine wave path that fills from baseline to bottom
     *
     * Math: y = baseline + amplitude * sin((x / λ) * 2π + φ)
     *
     * @param phase Continuous time parameter (radians)
     * @return Path object ready for drawing
     */
    private fun buildWavePath(
        width: Float,
        height: Float,
        baseline: Float,
        amplitude: Float,
        wavelength: Float,
        phase: Float
    ): Path {
        return Path().apply {
            reset()

            // Start at bottom-left corner
            moveTo(0f, height)

            // Generate wave curve points
            for (x in 0..width.toInt() step SAMPLE_STEP) {
                val fx = x.toFloat()
                val y = baseline + amplitude * sin(
                    ((fx / wavelength) * 2f * PI.toFloat()) + phase
                )
                lineTo(fx, y)
            }

            // Close path to bottom-right corner
            lineTo(width, height)
            close()
        }
    }

    /**
     * Draws subtle white gradient highlight on wave crest
     * Creates glossy, wet surface appearance
     */
    private fun DrawScope.drawCrestHighlight(
        path: Path,
        liquidTop: Float,
        width: Float
    ) {
        val highlightSize = 20f  // Reduced from 24f

        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.10f),  // Reduced from 0.12f
                    Color.Transparent
                ),
                startY = liquidTop - highlightSize,
                endY = liquidTop + highlightSize
            ),
            blendMode = BlendMode.Softlight
        )
    }
}