package com.example.battery.data.model

data class BatteryData(
    val batteryPercentage: Float = 0f,
    val voltage: Float = 0f,
    val currentAmps: Float = 0f,
    val temperature: Float = 0f,

    val isCharging: Boolean = false,
    val chargingPower: Float = 0f,
    val chargingStatus: String = "Unknown",
    val chargingType: String = "Unknown",
    val sourceType: String = "",

    val thermalStatus: String = "Unknown",
    val thermalLevel: Int = 0,

    val batteryHealth: String = "Unknown",
    val chemistry: String = "Unknown",
    val designCapacityMah: Int = 0,
    val cycleCount: Int = 0, // ✅ Added cycle count field

    val timeRemainingCharging: String? = null,
    val timeRemainingOnScreen: String? = null,
    val timeRemainingOffScreen: String? = null,
    val timeRemainingState: String = "CALCULATING",

    val pluggedType: Int = -1,
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        val EMPTY = BatteryData()
    }
}