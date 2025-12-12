package com.example.battery.ui.screens.dashboard.components.fluid

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * FluidWaves - Triple-wave harmonic animation system
 *
 * Renders 3 overlapping sine waves with:
 * - Different amplitudes, wavelengths, and speeds
 * - Soft alpha blending for depth
 * - Seamless looping via continuous time parameter
 * - Physics-accurate liquid surface motion
 */
@Composable
fun FluidWaves(
    percentage: Float,
    color: Color,
    time: Float,
    isCharging: Boolean,
    modifier: Modifier = Modifier
) {
    val waveRenderer = WaveRendererV2()

    Canvas(modifier = modifier.fillMaxSize()) {
        val level = (percentage / 100f).coerceIn(0f, 1f)

        waveRenderer.draw(
            drawScope = this,
            level = level,
            color = color,
            time = time,
            isCharging = isCharging
        )
    }
}

const val WAVE_1_AMPLITUDE = 80f
const val WAVE_2_AMPLITUDE = 40f
const val WAVE_3_AMPLITUDE = 60f

const val WAVE_1_SPEED = 0.6f
const val WAVE_2_SPEED = 1.0f
const val WAVE_3_SPEED = 1.5f

const val WAVE_1_WAVELENGTH_RATIO = 0.9f
const val WAVE_2_WAVELENGTH_RATIO = 1.2f
const val WAVE_3_WAVELENGTH_RATIO = 0.7f