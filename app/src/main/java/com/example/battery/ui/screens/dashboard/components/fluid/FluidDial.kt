package com.example.battery.ui.screens.dashboard.components.fluid

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * FluidDial - Optimized Layer Composition
 *
 * BATCH 3.9 POLISH:
 * - Refined depth-correct layer order
 * - Minimized overdraw with strategic placement
 * - Shimmer blend optimization with alpha modulation
 * - Performance-tuned motion effects
 */
@Composable
fun FluidDial(
    percentage: Float,
    isCharging: Boolean,
    color: Color,
    modifier: Modifier = Modifier
) {
    /** -----------------------------------------------
     * 1. SMOOTH PERCENTAGE ANIMATION
     * ----------------------------------------------- */
    val animatedPercentage by animateFloatAsState(
        targetValue = percentage.coerceIn(0f, 100f),
        animationSpec = tween(
            durationMillis = 800,
            easing = FastOutSlowInEasing
        ),
        label = "percentage_transition"
    )

    /** -----------------------------------------------
     * 2. INFINITE SEAMLESS WAVE ANIMATION
     * ----------------------------------------------- */
    var waveTime by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()
        while (true) {
            val elapsedMillis = System.currentTimeMillis() - startTime
            waveTime = elapsedMillis / 1000f  // Convert to seconds
            delay(16L)  // ~60 FPS update rate
        }
    }

    /** -----------------------------------------------
     * 3. DEPTH-OPTIMIZED LAYER COMPOSITION
     * ----------------------------------------------- */
    Box(
        modifier = modifier.size(340.dp),
        contentAlignment = Alignment.Center
    ) {
        val dialSize = 340.dp

        // Main container with base layers
        FluidDialContainer(
            dialSize = dialSize,
            percentage = animatedPercentage,
            color = color,
            isCharging = isCharging
        ) {
            // LAYER 1: Base wave animation
            FluidWaves(
                percentage = animatedPercentage,
                color = color,
                time = waveTime,
                isCharging = isCharging
            )

            // LAYER 2: Subsurface lighting
            FluidLightingOverlay(
                baseColor = color,
                intensity = 1.0f,
                isCharging = isCharging,
                percentage = animatedPercentage,
                modifier = Modifier.fillMaxSize()
            )

            // LAYER 3: Glass reflection
            FluidReflectionOverlay(
                percentage = animatedPercentage,
                isCharging = isCharging,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Motion overlays OUTSIDE container for visibility
        Box(
            modifier = Modifier
                .size(dialSize)
                .align(Alignment.Center)
                .graphicsLayer {
                    clip = true
                    shape = CircleShape
                }
        ) {
            // LAYER 4: Rising bubbles (visible on top)
            if (isCharging){
                FluidBubblesOverlay(
                    percentage = animatedPercentage,
                    color = color,
                    isCharging = isCharging,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // LAYER 5: Circumference shimmer (only when charging)
            if (isCharging) {
                FluidCrestShimmerOverlay(
                    percentage = animatedPercentage,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // LAYER 6: Foreground content (text)
        FluidDialForegroundContent(
            percentage = animatedPercentage,
            isCharging = isCharging,
            color = color,
            iconOpacity = 1.0f
        )
    }
}