package com.example.battery.util

import com.example.battery.data.repository.TimeRange

/**
 * Format duration in milliseconds to human-readable string
 */
fun formatDuration(millis: Long): String {
    if (millis <= 0) return "N/A"

    val seconds = millis / 1000
    val minutes = seconds / 60
    val hours = minutes / 60

    return when {
        hours > 0 -> {
            val remainingMinutes = minutes % 60
            if (remainingMinutes > 0) {
                "${hours}h ${remainingMinutes}m"
            } else {
                "${hours}h"
            }
        }
        minutes > 0 -> "${minutes}m"
        else -> "${seconds}s"
    }
}

/**
 * Format power value with proper unit
 */
fun formatPower(watts: Float): String {
    return "%.2f W".format(watts)
}

/**
 * Format temperature value
 */
fun formatTemperature(celsius: Float): String {
    return "%.1f°C".format(celsius)
}

/**
 * Get display label for time range
 */
fun getTimeRangeLabel(range: TimeRange): String {
    return when (range) {
        TimeRange.MIN_5 -> "5 Minutes"
        TimeRange.HOUR_1 -> "1 Hour"
        TimeRange.DAY_1 -> "24 Hours"
    }
}

/**
 * Calculate duration from timestamp list
 */
fun calculateDuration(timestamps: List<Long>): Long {
    return if (timestamps.size >= 2) {
        timestamps.maxOrNull()!! - timestamps.minOrNull()!!
    } else {
        0L
    }
}