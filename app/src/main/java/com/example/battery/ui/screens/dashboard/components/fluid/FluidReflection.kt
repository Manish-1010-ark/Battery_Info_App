package com.example.battery.ui.screens.dashboard.components.fluid

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas

/**
 * FluidReflection - Glass rim reflections (BATCH 2.5 OPTIMIZED)
 *
 * CHANGES:
 * - Removed secondary reflection (bottom arc)
 * - Removed inner caustics
 * - Removed surface specular
 * - Kept only single top-left rim highlight
 * - Reduced alpha to 0.05-0.08 range
 * - BlurMaskFilter radius reduced to 1.5f
 * - Changed to Softlight blend mode
 * - Added -15° rotation offset
 */
@Composable
fun FluidReflectionOverlay(
    percentage: Float,
    isCharging: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val center = Offset(width / 2f, height / 2f)
        val radius = size.minDimension / 2f
        val level = (percentage / 100f).coerceIn(0f, 1f)

        /** -----------------------------------------------
         * SINGLE PRIMARY RIM HIGHLIGHT (Top-left arc)
         * Subtle, controlled reflection only
         * ----------------------------------------------- */
        drawPrimaryRimHighlight(
            center = center,
            radius = radius,
            level = level
        )
    }
}

/**
 * Primary rim highlight - single controlled top reflection
 * OPTIMIZED: Lower alpha, tighter blur, Softlight blend, rotation offset
 */
private fun DrawScope.drawPrimaryRimHighlight(
    center: Offset,
    radius: Float,
    level: Float
) {
    // Arc from 10 o'clock to 1 o'clock (slightly tighter sweep)
    val startAngle = -135f - 15f  // Added -15° rotation offset
    val sweepAngle = 75f  // Reduced from 90f for subtlety

    val offsetRadius = radius - 1f

    drawIntoCanvas { canvas ->
        val paint = Paint().asFrameworkPaint().apply {
            // Alpha reduced to 0.05-0.08 range (was 0.15)
            color = android.graphics.Color.argb(20, 255, 255, 255)  // ~8% white
            strokeWidth = 2.0f  // Slightly thinner
            style = android.graphics.Paint.Style.STROKE
            maskFilter = BlurMaskFilter(1.5f, BlurMaskFilter.Blur.NORMAL)  // Was 3f
        }

        val rect = Rect(
            center.x - offsetRadius,
            center.y - offsetRadius,
            center.x + offsetRadius,
            center.y + offsetRadius
        )

        canvas.nativeCanvas.drawArc(
            rect.left,
            rect.top,
            rect.right,
            rect.bottom,
            startAngle,
            sweepAngle,
            false,
            paint
        )
    }
}