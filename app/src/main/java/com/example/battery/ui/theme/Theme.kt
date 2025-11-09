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

private val DarkColorScheme = darkColorScheme(
    primary = NeonBlueGlow,
    onPrimary = White,
    primaryContainer = PrimaryDark,
    onPrimaryContainer = NeonBlue,

    secondary = NeonPurple,
    onSecondary = White,
    secondaryContainer = NeonPurpleGlow,
    onSecondaryContainer = NeonCyan,

    tertiary = NeonGreen,
    onTertiary = Black,
    tertiaryContainer = NeonGreenGlow,
    onTertiaryContainer = NeonGreen,

    background = DarkBackground,
    onBackground = White,

    surface = DarkCard,
    onSurface = White,
    surfaceVariant = DarkInput,
    onSurfaceVariant = LightGray,

    error = NeonRed,
    onError = White,
    errorContainer = RedAccent,
    onErrorContainer = White,

    outline = Gray,
    outlineVariant = DarkDisabled
)

@Composable
fun BatteryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}