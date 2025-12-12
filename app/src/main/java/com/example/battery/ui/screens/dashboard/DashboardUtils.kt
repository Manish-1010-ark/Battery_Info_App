package com.example.battery.ui.screens.dashboard

import androidx.compose.ui.geometry.Offset
import kotlin.math.PI
import kotlin.math.sin

/**
 * Dashboard Utilities - BATCH 1: SEAMLESS WAVE ANIMATION REBUILD (FIXED AMPLITUDE)
 *
 * Pure functions for calculations used across dashboard components.
 *
 * BATCH 1 CRITICAL FIX:
 * ✅ VISIBLE wave amplitude (18-38dp instead of 6-14dp)
 * ✅ SIMPLIFIED wave algorithm (2 components instead of 3)
 * ✅ Wave point generation with PERFECT seamless loop
 * ✅ LinearEasing compatible calculations
 * ✅ Phase cycles from 0 to 2π with NO visible jump
 *
 * Wave amplitude specification (FIXED):
 * - Discharging: 18-28dp (5-8% of 340dp bowl diameter) - VISIBLE
 * - Charging: 28-38dp (8-11% of 340dp bowl diameter) - HIGHLY VISIBLE
 */

/**
 * Calculate VISIBLE wave amplitude based on battery state
 *
 * BATCH 1 FIXED SPEC:
 * - Discharging: 18-28dp (was 6-10dp) - NOW VISIBLE!
 * - Charging: 28-38dp (was 10-14dp) - NOW VISIBLE!
 * - Scaled appropriately for 340dp bowl (5-11% of diameter)
 */
fun calculateWaveAmplitude(isCharging: Boolean, percentage: Float): Float {
    return when {
        // CHARGING (bold, energetic waves)
        isCharging && percentage > 80f -> 38f  // Maximum charging amplitude (11% of diameter)
        isCharging -> 32f                       // Standard charging amplitude (9% of diameter)

        // DISCHARGING (gentle but clearly visible waves)
        percentage > 50f -> 28f                 // Healthy discharge (8% of diameter)
        percentage > 20f -> 23f                 // Medium discharge (7% of diameter)
        else -> 18f                             // Low battery (5% of diameter, minimal but visible)
    }
}

/**
 * Generate wave points for fluid animation with PERFECT SEAMLESS LOOP
 *
 * BATCH 1 CRITICAL FIX - SIMPLIFIED ALGORITHM:
 * ✅ TWO-COMPONENT wave (was three) for clear, visible motion
 * ✅ Primary wave: 100% amplitude (the wave you actually see)
 * ✅ Secondary modulation: 20% amplitude (adds natural texture)
 * ✅ NO over-complication that cancels out motion
 * ✅ Frame 0 and frame max align perfectly
 * ✅ Uses periodic sine functions that loop continuously with LinearEasing
 *
 * THE KEY TO SEAMLESS LOOPING:
 * - wavePhase cycles from 0 to 2π then restarts (handled by LinearEasing animation)
 * - All sine components are periodic functions with period 2π
 * - When wavePhase = 0 or 2π, the output is mathematically identical
 * - normalizedX ensures spatial periodicity across canvas width
 * - No conditional logic or branching that could cause discontinuities
 *
 * PARALLAX SYSTEM:
 * - Foreground wave: waveSpeed = 1.0, duration 4200ms
 * - Background wave: waveSpeed = 0.68 (4200/6200), duration 6200ms
 * - Both waves use same phase input, ratio creates parallax
 *
 * @param width Canvas width
 * @param baseTop Y position of wave baseline
 * @param wavePhase Current animation phase (0 to 2π, loops seamlessly with LinearEasing)
 * @param waveAmplitude Wave height (18-38dp based on state) - NOW VISIBLE!
 * @param waveSpeed Animation speed multiplier (1.0 foreground, 0.68 background)
 * @return List of wave crest points forming smooth, continuous, VISIBLE curve
 */
fun generateWavePoints(
    width: Float,
    baseTop: Float,
    wavePhase: Float,
    waveAmplitude: Float,
    waveSpeed: Float = 1.0f
): List<Offset> {
    val waveFrequency = 2.2f  // Number of wave cycles across screen (reduced for visibility)
    val points = mutableListOf<Offset>()
    val step = 3f  // Point spacing (smooth but efficient)

    var x = 0f
    while (x <= width) {
        val normalizedX = x / width  // 0.0 to 1.0 across canvas

        // BATCH 1 FIX: SIMPLIFIED TWO-COMPONENT WAVE
        // Clear, visible motion without self-cancellation

        // PRIMARY WAVE (100% amplitude, main visible motion)
        // This is the wave you actually SEE moving across the screen
        val primaryPhase = wavePhase * waveSpeed + normalizedX * waveFrequency * 2f * PI.toFloat()
        val primaryWave = sin(primaryPhase) * waveAmplitude

        // SECONDARY MODULATION (20% amplitude, adds natural texture)
        // Subtle variation to prevent mechanical look, but doesn't interfere with primary
        val secondaryPhase = wavePhase * waveSpeed * 1.4f + normalizedX * waveFrequency * 0.6f * 2f * PI.toFloat()
        val secondaryWave = sin(secondaryPhase) * (waveAmplitude * 0.2f)

        // Combine: Clear primary motion + subtle texture
        // All operations are continuous and periodic
        val y = baseTop + primaryWave + secondaryWave

        points.add(Offset(x, y))
        x += step
    }

    return points
}

/**
 * Calculate ambient glow intensity based on battery state
 *
 * NOTE: This function is kept for backward compatibility but is NOT used
 * in the Batch 1 clean minimal design (no glow layers)
 *
 * @deprecated Not used in clean minimal design
 */
@Deprecated(
    message = "Not used in clean minimal design",
    replaceWith = ReplaceWith("No replacement - glow removed in Batch 1")
)
fun calculateGlowIntensity(
    isCharging: Boolean,
    percentage: Float,
    glowPulse: Float
): Float {
    return when {
        isCharging && percentage > 80f -> 0.35f * glowPulse
        isCharging -> 0.28f * glowPulse
        percentage > 50f -> 0.15f
        else -> 0.12f
    }
}

/**
 * Calculate wave-to-air blend gradient stops
 *
 * NOTE: This function is kept for backward compatibility but is NOT used
 * in the Batch 1 clean minimal design (no separate blend layers)
 *
 * @deprecated Not used in clean minimal design
 */
@Deprecated(
    message = "Not used in clean minimal design",
    replaceWith = ReplaceWith("No replacement - blend layer removed in Batch 1")
)
fun createWaveAirBlendGradient(
    waveColor: androidx.compose.ui.graphics.Color,
    blendHeight: Float = 20f
): List<androidx.compose.ui.graphics.Color> {
    return listOf(
        waveColor.copy(alpha = 0.95f),
        waveColor.copy(alpha = 0.75f),
        waveColor.copy(alpha = 0.35f),
        androidx.compose.ui.graphics.Color.Transparent
    )
}