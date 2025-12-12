package com.example.battery.ui.screens.analytics.components

import androidx.compose.ui.graphics.Color

/**
 * Unified color palette for analytics graphs
 * Matches your app's theme aesthetic
 */
object GraphColors {

    // Primary graph colors
    val AccentMint = Color(0xFF00D9A3)        // Charging - vibrant mint
    val AccentOrange = Color(0xFFFF6B35)      // Discharging - warm orange
    val AccentBlue = Color(0xFF4A90E2)        // Temperature - cool blue

    // Alternative colors for variety
    val AccentPurple = Color(0xFF9B59B6)      // Usage metrics
    val AccentYellow = Color(0xFFFFD93D)      // Warnings/alerts
    val AccentRed = Color(0xFFE74C3C)         // Critical states

    // Gradient variations (for fills)
    val MintGradient = Color(0xFF00D9A3).copy(alpha = 0.3f)
    val OrangeGradient = Color(0xFFFF6B35).copy(alpha = 0.3f)
    val BlueGradient = Color(0xFF4A90E2).copy(alpha = 0.3f)

    // Grid and axis colors (auto-adapt to theme in GraphTemplate)
    val GridLight = Color(0xFFE0E0E0)
    val GridDark = Color(0xFF2C2C2C)
}

/**
 * Helper to get appropriate color based on graph type
 */
fun getGraphColor(type: GraphType): Color {
    return when (type) {
        GraphType.CHARGING -> GraphColors.AccentMint
        GraphType.DISCHARGING -> GraphColors.AccentOrange
        GraphType.TEMPERATURE -> GraphColors.AccentBlue
        GraphType.USAGE -> GraphColors.AccentPurple
    }
}

enum class GraphType {
    CHARGING,
    DISCHARGING,
    TEMPERATURE,
    USAGE
}