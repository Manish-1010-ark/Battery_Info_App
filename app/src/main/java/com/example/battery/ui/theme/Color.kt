package com.example.battery.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Tecky/Futuristic Color Palette
 *
 * A dark, high-contrast theme inspired by cyberpunk aesthetics,
 * gaming interfaces, and futuristic tech displays.
 */

// === BACKGROUNDS ===
/** Near-black background - like a turned-off OLED screen */
val BackgroundBlack = Color(0xFF0A0A0A)

/** Dark card/surface color with subtle blue tint - for elevated content */
val SurfaceDark = Color(0xFF1A1A24)

/** Slightly lighter surface for nested cards or emphasis */
val SurfaceLight = Color(0xFF252530)

// === PRIMARY ACCENTS ===
/** Electric Blue - main accent color for highlights, important text, buttons */
val ElectricBlue = Color(0xFF00E5FF)

/** Darker blue for pressed/hover states */
val ElectricBlueDark = Color(0xFF00B8D4)

/** Lighter blue for subtle highlights */
val ElectricBlueLight = Color(0xFF6EFFFF)

// === TEXT COLORS ===
/** Primary text - off-white for main content */
val TextPrimary = Color(0xFFE0E0E0)

/** Secondary text - light gray for labels and subtitles */
val TextSecondary = Color(0xFFA0A0A0)

/** Tertiary text - darker gray for hints and disabled text */
val TextTertiary = Color(0xFF707070)

// === STATUS COLORS ===
/** Neon Red - for alerts, warnings, discharging state, max power */
val NeonRed = Color(0xFFFF4136)

/** Bright Green - for charging status, success states */
val BrightGreen = Color(0xFF22FF00)

/** Orange - for temperature warnings */
val NeonOrange = Color(0xFFFF8800)

/** Yellow - for moderate warnings or attention */
val NeonYellow = Color(0xFFFFDD00)

// === SEMANTIC COLOR MAPPINGS ===
/** Use for charging power display */
val ChargingPowerColor = ElectricBlue

/** Use for discharging state */
val DischargingColor = NeonRed

/** Use for battery percentage when charging */
val BatteryChargingColor = BrightGreen

/** Use for battery percentage when discharging */
val BatteryDischargingColor = NeonRed

/** Use for temperature displays */
val TemperatureColor = NeonOrange

/** Use for voltage displays */
val VoltageColor = Color(0xFFAA00FF) // Purple

/** Use for current displays */
val CurrentColor = Color(0xFF00DDFF) // Cyan

// === GRAPH COLORS ===
/** Main line color for power graph */
val GraphLineColor = ElectricBlue

/** Fill gradient start (at the line) */
val GraphFillStart = ElectricBlue.copy(alpha = 0.6f)

/** Fill gradient end (at the bottom, transparent) */
val GraphFillEnd = Color.Transparent

/** Grid lines color */
val GraphGridColor = TextTertiary.copy(alpha = 0.3f)