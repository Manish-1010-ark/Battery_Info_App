package com.example.battery.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a charging power sample.
 * Logged every ~5 seconds during charging sessions.
 */
@Entity(tableName = "charging_power")
data class ChargingPower(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val power: Float,      // Power in Watts (W)
    val timestamp: Long    // Unix timestamp in milliseconds
)