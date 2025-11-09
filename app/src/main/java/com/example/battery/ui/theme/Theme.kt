package com.example.battery.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Tecky/Futuristic Dark Theme
 *
 * A Material 3 theme optimized for dark environments with high-contrast,
 * glowing accents that create a cyberpunk/tech aesthetic.
 */

private val TeckyColorScheme = darkColorScheme(
    // === PRIMARY COLORS ===
    primary = ElectricBlue,              // Main accent - buttons, FABs, important elements
    onPrimary = BackgroundBlack,         // Text on primary color
    primaryContainer = ElectricBlueDark, // Container for primary elements
    onPrimaryContainer = TextPrimary,    // Text on primary containers

    // === SECONDARY COLORS ===
    secondary = ElectricBlueLight,       // Secondary accent
    onSecondary = BackgroundBlack,       // Text on secondary
    secondaryContainer = SurfaceLight,   // Secondary containers
    onSecondaryContainer = TextPrimary,  // Text on secondary containers

    // === TERTIARY COLORS ===
    tertiary = NeonOrange,               // Tertiary accent (for temperature, etc.)
    onTertiary = BackgroundBlack,        // Text on tertiary
    tertiaryContainer = SurfaceDark,     // Tertiary containers
    onTertiaryContainer = TextPrimary,   // Text on tertiary containers

    // === BACKGROUND COLORS ===
    background = BackgroundBlack,        // Main background
    onBackground = TextPrimary,          // Text on background

    // === SURFACE COLORS ===
    surface = SurfaceDark,               // Cards, dialogs, bottom sheets
    onSurface = TextPrimary,             // Text on surfaces
    surfaceVariant = SurfaceLight,       // Variant surfaces (slightly lighter)
    onSurfaceVariant = TextSecondary,    // Text on variant surfaces

    // === OUTLINE COLORS ===
    outline = TextTertiary,              // Borders, dividers
    outlineVariant = TextTertiary.copy(alpha = 0.5f), // Subtle dividers

    // === ERROR COLORS ===
    error = NeonRed,                     // Error states, alerts
    onError = BackgroundBlack,           // Text on error color
    errorContainer = NeonRed.copy(alpha = 0.2f), // Error backgrounds
    onErrorContainer = NeonRed,          // Text on error containers

    // === INVERSE COLORS ===
    inverseSurface = TextPrimary,        // Inverse of surface (light on dark theme)
    inverseOnSurface = BackgroundBlack,  // Text on inverse surface
    inversePrimary = ElectricBlueDark,   // Inverse primary

    // === SCRIM ===
    scrim = BackgroundBlack.copy(alpha = 0.8f) // Overlay for modals
)

@Composable
fun BatteryTheme(
    darkTheme: Boolean = true, // Always dark - this is a tecky theme!
    content: @Composable () -> Unit
) {
    val colorScheme = TeckyColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false // Always dark status bar
                isAppearanceLightNavigationBars = false // Always dark nav bar
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = TeckyTypography,
        content = content
    )
}