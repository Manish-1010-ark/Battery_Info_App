package com.example.battery.data.model

data class BatteryData(
    // Core Battery Info
    val batteryPercentage: Float = 0f,
    val voltage: Float = 0f,
    val currentAmps: Float = 0f,
    val temperature: Float = 0f,

    // Charging Info
    val isCharging: Boolean = false,
    val chargingPower: Float = 0f,
    val chargingStatus: String = "Unknown",
    val chargingType: String = "Unknown",
    val sourceType: String = "",

    // Power Statistics
    val minPower: Float = -1f,
    val maxPower: Float = -1f,
    val avgPower: Float = -1f,

    // Time Estimation
    val timeRemaining: String = "Calculating...",
    val timeRemainingMinutes: Int = 0,

    // Additional Info
    val pluggedType: Int = -1,
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        val EMPTY = BatteryData()
    }
}