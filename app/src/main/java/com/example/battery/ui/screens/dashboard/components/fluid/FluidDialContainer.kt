package com.example.battery.ui.screens.dashboard.components.fluid

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.Dp

/**
 * FluidDialContainer - Glass-like circular enclosure
 *
 * Provides a circular clipped container with:
 * - Soft inner shadow for depth
 * - Dual-layered rim (outer dark, inner light)
 * - Radial gradient for bowl effect
 * - Composable content slot for fluid waves
 */
@Composable
fun FluidDialContainer(
    dialSize: Dp,
    percentage: Float,
    color: Color,
    isCharging: Boolean,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(dialSize)
            .drawWithContent {
                val width = size.width
                val height = size.height
                val center = Offset(width / 2f, height / 2f)
                val radius = size.minDimension / 2f

                // Create circular clip path for bowl enclosure
                val circlePath = Path().apply {
                    addOval(Rect(center.x - radius, center.y - radius,
                        center.x + radius, center.y + radius))
                }

                /** -----------------------------------------------
                 * 1. SUBTLE INNER SHADOW (Depth effect)
                 * ----------------------------------------------- */
                drawIntoCanvas { canvas ->
                    val shadowPaint = Paint().asFrameworkPaint().apply {
                        this.color = android.graphics.Color.argb(25, 0, 0, 0)
                        maskFilter = android.graphics.BlurMaskFilter(
                            20f,
                            android.graphics.BlurMaskFilter.Blur.NORMAL
                        )
                    }
                    canvas.nativeCanvas.drawCircle(
                        center.x,
                        center.y,
                        radius - 4f,
                        shadowPaint
                    )
                }

                /** -----------------------------------------------
                 * 2. RADIAL BACKGROUND GRADIENT (Bowl effect)
                 * ----------------------------------------------- */
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.02f),
                            Color.Black.copy(alpha = 0.08f)
                        ),
                        center = center,
                        radius = radius * 0.85f
                    ),
                    center = center,
                    radius = radius
                )

                /** -----------------------------------------------
                 * 3. CLIP TO CIRCLE & DRAW CONTENT (Waves)
                 * ----------------------------------------------- */
                clipPath(circlePath) {
                    this@drawWithContent.drawContent()
                }

                /** -----------------------------------------------
                 * 4. DUAL-LAYERED RIM STROKES
                 * ----------------------------------------------- */
                // Outer rim (dark, subtle depth)
                drawCircle(
                    color = Color.Black.copy(alpha = 0.12f),
                    radius = radius,
                    center = center,
                    style = Stroke(width = 2.5f)
                )

                // Inner rim (light, polish effect)
                drawCircle(
                    color = Color.White.copy(alpha = 0.08f),
                    radius = radius - 1.5f,
                    center = center,
                    style = Stroke(width = 1f)
                )
            }
    ) {
        // Content slot for fluid waves
        content()
    }
}