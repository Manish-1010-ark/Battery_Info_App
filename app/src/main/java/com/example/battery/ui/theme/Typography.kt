package com.example.battery.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.battery.R

/**
 * App Typography System
 *
 * A dual-font system that creates the "tecky" aesthetic:
 * - Roboto for labels, body text, and UI elements (readable, clean)
 * - Roboto Mono for numbers and data displays (technical, digital feel)
 *
 * SETUP REQUIRED: Place these font files in res/font/ folder:
 * - roboto_regular.ttf
 * - robotomono_regular.ttf
 */

// === FONT FAMILIES ===

/** Standard UI font - Used for all text labels, body copy, buttons */
private val AppFontFamily = FontFamily(
    Font(R.font.roboto_regular, FontWeight.Normal)
)

/** Monospace font - Used for all numeric displays (battery %, voltage, power, etc.) */
private val MonoFontFamily = FontFamily(
    Font(R.font.robotomono_regular, FontWeight.Normal)
)

// === MATERIAL 3 TYPOGRAPHY ===

val AppTypography = Typography(

    // === DISPLAY STYLES ===
    // Large hero numbers - Battery percentage, charging power
    displayLarge = TextStyle(
        fontFamily = MonoFontFamily,  // Monospace for that digital readout look
        fontWeight = FontWeight.Bold,
        fontSize = 72.sp,
        lineHeight = 80.sp,
        letterSpacing = (-0.5).sp
    ),

    displayMedium = TextStyle(
        fontFamily = MonoFontFamily,  // Main data displays (76%, 13.2W)
        fontWeight = FontWeight.Bold,
        fontSize = 56.sp,
        lineHeight = 64.sp,
        letterSpacing = 0.sp
    ),

    displaySmall = TextStyle(
        fontFamily = MonoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 44.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),

    // === HEADLINE STYLES ===
    headlineLarge = TextStyle(
        fontFamily = MonoFontFamily,  // Large numeric data
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),

    headlineMedium = TextStyle(
        fontFamily = MonoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),

    headlineSmall = TextStyle(
        fontFamily = MonoFontFamily,  // Card stats (4.12V, 3.2A, 42°C)
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),

    // === TITLE STYLES ===
    titleLarge = TextStyle(
        fontFamily = AppFontFamily,  // Section headers
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),

    titleMedium = TextStyle(
        fontFamily = AppFontFamily,  // Card titles, tab labels
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),

    titleSmall = TextStyle(
        fontFamily = AppFontFamily,  // Small section headers
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    // === BODY STYLES ===
    bodyLarge = TextStyle(
        fontFamily = AppFontFamily,  // Main body text
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),

    bodyMedium = TextStyle(
        fontFamily = AppFontFamily,  // Standard text
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),

    bodySmall = TextStyle(
        fontFamily = AppFontFamily,  // Small descriptive text
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),

    // === LABEL STYLES ===
    labelLarge = TextStyle(
        fontFamily = AppFontFamily,  // Large buttons
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    labelMedium = TextStyle(
        fontFamily = AppFontFamily,  // Standard labels (Voltage, Current, etc.)
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),

    labelSmall = TextStyle(
        fontFamily = AppFontFamily,  // Small labels, hints
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

/**
 * Custom Text Styles for specific use cases beyond Material 3
 */
object AppTextStyles {

    /** Extra large hero number (e.g., main battery percentage on home screen) */
    val HeroNumber = TextStyle(
        fontFamily = MonoFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 96.sp,
        lineHeight = 104.sp,
        letterSpacing = (-1).sp
    )

    /** Medium data display with mono spacing (graph values, stats) */
    val DataMedium = TextStyle(
        fontFamily = MonoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    )

    /** Small mono numbers (timeline labels, small stats) */
    val DataSmall = TextStyle(
        fontFamily = MonoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    )

    /** Extra small mono (graph axis labels) */
    val DataTiny = TextStyle(
        fontFamily = MonoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.sp
    )

    /** Unit label (W, V, A, °C - appears next to numbers) */
    val Unit = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    )

    /** Small unit label */
    val UnitSmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    )
}