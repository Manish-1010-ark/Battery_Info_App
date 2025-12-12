package com.example.battery.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing

/**
 * Material 3 Motion Tokens
 *
 * Implements Material Design 3 motion system with semantic easing curves
 * and duration tokens for consistent, expressive animations.
 *
 * References:
 * - https://m3.material.io/styles/motion/easing-and-duration
 * - Material Components Android motion guidance
 *
 * USAGE PATTERNS:
 * - Standard easing: Most common, for elements moving on-screen
 * - Emphasized easing: Dramatic, expressive transitions
 * - Enter/Exit variants: For elements entering/leaving screen
 * - Duration scales with distance and prominence
 */
object MotionTokens {

    // === EASING CURVES ===

    /**
     * Standard Easing (Emphasized Decelerate in M3)
     * Used for most common transitions where elements stay on screen
     * Creates smooth, natural motion with gentle acceleration/deceleration
     *
     * Use for: Dial percentage changes, color transitions, scale animations
     */
    val EasingStandard: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)

    /**
     * Emphasized Easing
     * More dramatic, expressive motion for prominent state changes
     * Stronger acceleration at start, gentler landing
     *
     * Use for: Charging state changes, major UI transitions
     */
    val EasingEmphasized: Easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)

    /**
     * Emphasized Decelerate (Enter animations)
     * Elements enter at speed and gently decelerate to rest
     *
     * Use for: Chips appearing, wattage text entering
     */
    val EasingEmphasizedDecelerate: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)

    /**
     * Emphasized Accelerate (Exit animations)
     * Elements accelerate quickly when leaving
     *
     * Use for: Chips disappearing, elements exiting
     */
    val EasingEmphasizedAccelerate: Easing = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)

    /**
     * Legacy Curve - Smooth sine wave for continuous animations
     * Use sparingly, primarily for ambient/decorative motion
     *
     * Use for: Wave animations, glow pulses, ambient effects
     */
    val EasingLegacySine: Easing = CubicBezierEasing(0.45f, 0.05f, 0.55f, 0.95f)

    // === DURATION TOKENS (in milliseconds) ===

    /**
     * Short 1 - Quick feedback animations (150ms)
     * Use for: Icon state changes, micro-interactions
     */
    const val DurationShort1 = 150

    /**
     * Short 2 - Small element transitions (200ms)
     * Use for: Chip color changes, small scale animations
     */
    const val DurationShort2 = 200

    /**
     * Short 3 - Button presses, small movements (250ms)
     * Use for: Icon pulses, small position shifts
     */
    const val DurationShort3 = 250

    /**
     * Short 4 - Standard small transitions (300ms)
     * Use for: Text appearing, minor layout changes
     */
    const val DurationShort4 = 300

    /**
     * Medium 1 - Standard medium transitions (400ms)
     * Use for: Chip appearing/disappearing, wattage updates
     */
    const val DurationMedium1 = 400

    /**
     * Medium 2 - Prominent transitions (500ms)
     * Use for: Color state changes, emphasized animations
     */
    const val DurationMedium2 = 500

    /**
     * Medium 3 - Larger scale transitions (600ms)
     * Use for: Dial color changes, ambient glow transitions
     */
    const val DurationMedium3 = 600

    /**
     * Medium 4 - Extended medium (700ms)
     * Use for: Complex multi-property animations
     */
    const val DurationMedium4 = 700

    /**
     * Long 1 - Long transitions (800ms)
     * Use for: Major state changes, screen transitions
     */
    const val DurationLong1 = 800

    /**
     * Long 2 - Extended transitions (1000ms)
     * Use for: Percentage value animations, complex dial changes
     */
    const val DurationLong2 = 1000

    /**
     * Long 3 - Very long transitions (1200ms)
     * Use for: Hero animations, dramatic state changes
     */
    const val DurationLong3 = 1200

    /**
     * Long 4 - Maximum standard duration (1600ms)
     * Use for: Full screen transitions, major reflows
     */
    const val DurationLong4 = 1600

    /**
     * Extra Long 1 - Ambient continuous animations (2000ms)
     * Use for: Slow glow pulses, ambient background animations
     */
    const val DurationExtraLong1 = 2000

    /**
     * Extra Long 2 - Wave cycle duration (3000ms)
     * Use for: Full wave animation cycles, slow ambient motion
     */
    const val DurationExtraLong2 = 3000

    // === SEMANTIC ANIMATION SPECS ===
    // Pre-configured duration + easing pairs for common use cases

    object Semantic {
        /** Percentage value changing (smooth, takes time to read) */
        data class PercentageChange(
            val duration: Int = DurationLong2,
            val easing: Easing = EasingStandard
        )

        /** State color transition (noticeable but not disruptive) */
        data class StateColorChange(
            val duration: Int = DurationMedium3,
            val easing: Easing = EasingEmphasized
        )

        /** Wattage number updating (quick, clear) */
        data class WattageUpdate(
            val duration: Int = DurationMedium2,
            val easing: Easing = EasingStandard
        )

        /** Chip appearing (emphasized entrance) */
        data class ChipAppear(
            val duration: Int = DurationMedium1,
            val easing: Easing = EasingEmphasizedDecelerate
        )

        /** Chip disappearing (quick exit) */
        data class ChipDisappear(
            val duration: Int = DurationShort4,
            val easing: Easing = EasingEmphasizedAccelerate
        )

        /** Icon pulse (subtle attention) */
        data class IconPulse(
            val duration: Int = DurationMedium3,
            val easing: Easing = EasingLegacySine
        )

        /** Glow pulse (ambient, continuous) */
        data class GlowPulse(
            val duration: Int = DurationExtraLong1,
            val easing: Easing = EasingLegacySine
        )

        /** Wave animation (continuous, smooth cycle) */
        data class WaveMotion(
            val duration: Int = DurationExtraLong2,
            val easing: Easing = EasingLegacySine
        )

        /** Ambient glow radius change (follows state, calm) */
        data class AmbientGlowChange(
            val duration: Int = DurationLong1,
            val easing: Easing = EasingStandard
        )
    }
}

/**
 * Extension functions for easier motion token usage
 */

/**
 * Returns appropriate duration for an animation based on travel distance
 * @param distance Normalized distance (0.0 to 1.0) element will travel
 * @param base Base duration to scale from
 */
fun scaleDurationByDistance(distance: Float, base: Int): Int {
    return (base * (0.7f + distance * 0.6f)).toInt().coerceIn(base / 2, base * 2)
}

/**
 * Returns appropriate duration for prominence level
 * @param prominence How prominent the animation should be (0.0 = subtle, 1.0 = prominent)
 */
fun scaleDurationByProminence(prominence: Float): Int {
    return when {
        prominence < 0.3f -> MotionTokens.DurationShort3
        prominence < 0.6f -> MotionTokens.DurationMedium2
        else -> MotionTokens.DurationLong2
    }
}