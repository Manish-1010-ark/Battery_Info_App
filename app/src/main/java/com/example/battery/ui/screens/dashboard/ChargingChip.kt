package com.example.battery.ui.screens.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.battery.ui.theme.MaterialSymbols

/**
 * Charging Chip Component - CLEAN & SIMPLE (NO GLOW)
 *
 * ✅ Clean glass-like panel
 * ✅ Readable text (16sp)
 * ✅ Clear icon (24dp)
 * ✅ Simple progress bar (3dp)
 * ✅ NO glow effects, NO pulsing animations
 * ✅ Slide-in animation only
 */
@Composable
fun ChargingChip(
    chargingType: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    // Slide-up animation offset
    val slideOffset by animateDpAsState(
        targetValue = 0.dp,
        animationSpec = tween(
            durationMillis = 600,
            easing = FastOutSlowInEasing
        ),
        label = "slide_up_animation"
    )

    // Simple progress animation
    val infiniteTransition = rememberInfiniteTransition(label = "progress_animation")
    val progressWidth by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress_width"
    )

    Column(
        modifier = modifier.offset(y = slideOffset),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                // Simple elevation
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(28.dp)
                )
                // Glass-like panel background
                .background(
                    color = Color(0xFF10172A).copy(alpha = 0.30f),
                    shape = RoundedCornerShape(28.dp)
                )
                // Subtle border
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(horizontal = 24.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Icon(
                imageVector = MaterialSymbols.BoltRounded,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )

            // Text
            Text(
                text = chargingType,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.6.sp
                ),
                color = Color.White.copy(alpha = 0.85f)
            )
        }

//        // Simple progress indicator
//        Spacer(modifier = Modifier.height(6.dp))
//
//        Box(
//            modifier = Modifier
//                .width(140.dp)
//                .height(3.dp)
//                .background(
//                    color = Color.White.copy(alpha = 0.12f),
//                    shape = RoundedCornerShape(1.5.dp)
//                )
//        ) {
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth(progressWidth)
//                    .height(3.dp)
//                    .background(
//                        color = color.copy(alpha = 0.7f),
//                        shape = RoundedCornerShape(1.5.dp)
//                    )
//            )
//        }
    }
}