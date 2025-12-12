package com.example.battery.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * App Theme
 *
 * A sophisticated dark theme designed for battery monitoring with:
 * - Deep navy backgrounds for OLED optimization
 * - Dynamic accent colors that change based on battery state
 * - Monospace typography for technical data displays
 * - Mint green accents for the "tecky" aesthetic
 */

private val DarkColorScheme = darkColorScheme(
    // === PRIMARY COLORS ===
    // Mint accent used for primary actions, highlights, and interactive elements
    primary = AppColors.AccentMint,
    onPrimary = AppColors.Background,
    primaryContainer = AppColors.AccentMintDim,
    onPrimaryContainer = AppColors.TextPrimary,

    // === SECONDARY COLORS ===
    // Subtle secondary actions and less prominent UI elements
    secondary = AppColors.TextSecondary,
    onSecondary = AppColors.Background,
    secondaryContainer = AppColors.CardBackground,
    onSecondaryContainer = AppColors.TextSecondary,

    // === TERTIARY COLORS ===
    // Used for state indicators and dynamic elements
    tertiary = AppColors.StateCharging,
    onTertiary = AppColors.Background,
    tertiaryContainer = AppColors.CardBackground,
    onTertiaryContainer = AppColors.StateCharging,

    // === BACKGROUND COLORS ===
    background = AppColors.Background,
    onBackground = AppColors.TextPrimary,

    // === SURFACE COLORS ===
    // Cards, dialogs, bottom sheets
    surface = AppColors.CardBackground,
    onSurface = AppColors.TextPrimary,
    surfaceVariant = AppColors.Background,
    onSurfaceVariant = AppColors.TextSecondary,

    // === CONTAINER COLORS ===
    surfaceContainer = AppColors.CardBackground,
    surfaceContainerHigh = AppColors.CardBackground,
    surfaceContainerHighest = AppColors.Outline,
    surfaceContainerLow = AppColors.Background,
    surfaceContainerLowest = AppColors.Background,

    // === OUTLINE COLORS ===
    outline = AppColors.Outline,
    outlineVariant = AppColors.Divider,

    // === ERROR COLORS ===
    error = AppColors.StateCritical,
    onError = AppColors.TextPrimary,
    errorContainer = AppColors.StateCritical.copy(alpha = 0.2f),
    onErrorContainer = AppColors.StateCritical,

    // === INVERSE COLORS ===
    inverseSurface = AppColors.TextPrimary,
    inverseOnSurface = AppColors.Background,
    inversePrimary = AppColors.AccentMintDim,

    // === SCRIM ===
    scrim = AppColors.Background.copy(alpha = 0.85f)
)

/**
 * Main theme composable for the Battery Monitoring App
 *
 * Features:
 * - Always uses dark theme (optimized for OLED displays)
 * - Sets status bar and navigation bar to match background
 * - Applies custom typography with monospace for numbers
 * - Provides Material 3 color scheme with dynamic state colors
 */
@Composable
fun BatteryTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current

    // Configure system bars to match theme
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // Set status bar and navigation bar to deep navy background
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()

            // Always use light icons/text on dark background
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}

/**
 * Preview theme for Compose previews
 * Same as main theme but without window configuration
 */
@Composable
fun BatteryThemePreview(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = AppTypography,
        content = content
    )
}