package com.example.battery.ui.screens.dashboard.components.fluid

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

/**
 * FluidBubbles - Natural Motion Particle System
 *
 * BATCH 3.9 POLISH:
 * - Organic motion with curved easing and horizontal drift
 * - Soft, depth-correct rendering with proper alpha
 * - 30fps update for power efficiency
 * - Natural color tinting instead of harsh cyan
 */

private data class Bubble(
    val id: Int,
    var x: Float,
    var y: Float,
    val baseX: Float,  // Original X for drift calculation
    val radius: Float,
    val speed: Float,
    var alpha: Float,
    val maxAlpha: Float,
    val wobblePhase: Float  // For horizontal drift
)

@Composable
fun FluidBubblesOverlay(
    percentage: Float,
    color: Color,
    isCharging: Boolean,
    modifier: Modifier = Modifier
) {
    val bubbles = remember {
        mutableStateListOf<Bubble>().apply {
            repeat(25) { index ->
                add(createRandomBubble(index, 0f, 400f))
            }
        }
    }

    var animationTime by remember { mutableFloatStateOf(0f) }

    // 30fps animation loop for power efficiency
    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()
        while (true) {
            delay(33L)  // ~30 FPS - imperceptible difference, better battery

            val elapsedMillis = System.currentTimeMillis() - startTime
            animationTime = elapsedMillis / 1000f

            // Update all bubbles with natural motion
            bubbles.forEachIndexed { index, bubble ->
                // Curved easing for organic rise (gentle acceleration/deceleration)
                val motionCurve = 1f + 0.3f * sin(animationTime * 0.5f + bubble.id)
                bubble.y -= bubble.speed * motionCurve

                // Horizontal drift (wobble)
                bubble.x = bubble.baseX + sin((animationTime + bubble.wobblePhase) * 0.5f) * 8f

                // Fade out as approaching surface (top 20% of travel)
                val fadeZone = 80f
                if (bubble.y < fadeZone) {
                    bubble.alpha = (bubble.y / fadeZone) * bubble.maxAlpha
                } else {
                    bubble.alpha = bubble.maxAlpha
                }

                // Reset when fully faded or out of bounds
                if (bubble.alpha <= 0.01f || bubble.y < -10f) {
                    bubbles[index] = createRandomBubble(
                        id = bubble.id,
                        minY = 350f,
                        maxY = 420f
                    )
                }
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val level = (percentage / 100f).coerceIn(0f, 1f)
        if (level <= 0.1f) return@Canvas

        val width = size.width
        val height = size.height
        val liquidTop = height * (1f - level)
        val liquidHeight = height - liquidTop

        // Lighten the base color for natural tinting
        val bubbleColor = color.copy(
            red = (color.red + 0.4f).coerceIn(0f, 1f),
            green = (color.green + 0.4f).coerceIn(0f, 1f),
            blue = (color.blue + 0.4f).coerceIn(0f, 1f)
        )

        // Draw all active bubbles with optimized rendering
        bubbles.forEach { bubble ->
            // Map bubble Y to liquid height
            val normalizedY = bubble.y / 420f
            val bubbleY = liquidTop + (normalizedY * liquidHeight)

            // Clamp X within bounds
            val bubbleX = bubble.x.coerceIn(0f, width)

            // Only draw if within liquid bounds
            if (bubbleY > liquidTop && bubbleY < height) {
                // Depth-based alpha (bubbles deeper = slightly dimmer)
                val depthFactor = (bubbleY - liquidTop) / liquidHeight
                val adjustedAlpha = (bubble.alpha * (0.7f + depthFactor * 0.3f)).coerceAtMost(0.35f)

                // Main bubble with bright white glow
                drawCircle(
                    color = Color.White.copy(alpha = adjustedAlpha * 2.2f),
                    radius = bubble.radius * 1.4f,
                    center = Offset(bubbleX, bubbleY),
                    blendMode = BlendMode.Screen
                )

                // Color tint layer for richness
                drawCircle(
                    color = bubbleColor.copy(alpha = adjustedAlpha * 1.3f),
                    radius = bubble.radius * 0.9f,
                    center = Offset(
                        bubbleX - bubble.radius * 0.2f,
                        bubbleY - bubble.radius * 0.2f
                    ),
                    blendMode = BlendMode.Screen
                )

                // Subtle highlight sparkle
                drawCircle(
                    color = Color.White.copy(alpha = adjustedAlpha * 1.8f),
                    radius = bubble.radius * 0.4f,
                    center = Offset(
                        bubbleX - bubble.radius * 0.3f,
                        bubbleY - bubble.radius * 0.35f
                    ),
                    blendMode = BlendMode.Screen
                )
            }
        }
    }
}

/**
 * Creates a bubble with natural randomized properties
 */
private fun createRandomBubble(
    id: Int,
    minY: Float,
    maxY: Float
): Bubble {
    val baseX = Random.nextFloat() * 935f
    return Bubble(
        id = id,
        x = baseX,
        baseX = baseX,
        y = Random.nextFloat() * (maxY - minY) + minY,
        radius = Random.nextFloat() * 3f + 2f,  // 2-5px - larger for visibility
        speed = Random.nextFloat() * 0.7f + 0.4f,  // 0.4-1.1 px/frame
        alpha = 0f,
        maxAlpha = Random.nextFloat() * 0.15f + 0.1f,  // 0.1-0.25 - much brighter
        wobblePhase = Random.nextFloat() * 6.28f  // Random phase offset
    )
}