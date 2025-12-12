package com.example.battery.ui.screens.dashboard.components.fluid

import android.graphics.BlurMaskFilter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.toArgb

/**
 * Paint Cache for Performance - BATCH 2: CLEAN MINIMAL REDESIGN
 *
 * ✅ Shadow paint: 12-16dp blur, black @ 10-12% alpha (soft, circular)
 * ✅ Specular glow paint: For single thin highlight on wave crest
 * ✅ Outer rim feather paint: 8dp blur for soft rim feathering
 * ✅ Removed: Outer glow, mid glow, and other unnecessary paint objects
 *
 * All Paint objects are cached to prevent per-frame allocations.
 * Colors are updated dynamically, but Paint instances are reused.
 */
data class FluidDialPaintCache(
    val shadowPaint: android.graphics.Paint,
    val specularGlowPaint: android.graphics.Paint,
    val outerRimFeatherPaint: android.graphics.Paint
)

/**
 * Remember and cache all Paint objects used in fluid dial rendering
 *
 * BATCH 2 CLEAN SPEC:
 * - Shadow paint: 14dp blur (middle of 12-16dp range), black @ 11% alpha
 * - Specular glow: Soft blur for thin wave highlight
 * - Outer rim feather: 8dp blur for soft rim edge
 */
@Composable
internal fun rememberFluidDialPaintCache(): FluidDialPaintCache {
    return remember {
        FluidDialPaintCache(
            shadowPaint = Paint().asFrameworkPaint().apply {
                color = Color.Black.copy(alpha = 0.11f).toArgb()  // 11% alpha
                maskFilter = BlurMaskFilter(14f, BlurMaskFilter.Blur.NORMAL)  // 14dp blur
            },
            specularGlowPaint = Paint().asFrameworkPaint().apply {
                maskFilter = BlurMaskFilter(8f, BlurMaskFilter.Blur.NORMAL)  // Soft blur for highlight
            },
            outerRimFeatherPaint = Paint().asFrameworkPaint().apply {
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = 2f
                maskFilter = BlurMaskFilter(8f, BlurMaskFilter.Blur.NORMAL)  // 8dp feathering
            }
        )
    }
}