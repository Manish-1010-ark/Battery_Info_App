package com.example.battery.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a discharging power sample.
 * Logged every ~15 seconds during discharging (not charging).
 */
@Entity(tableName = "discharging_power")
data class DischargingPower(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val power: Float,      // Power in Watts (W)
    val timestamp: Long    // Unix timestamp in milliseconds
)