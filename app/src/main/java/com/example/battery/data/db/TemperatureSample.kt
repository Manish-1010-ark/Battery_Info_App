package com.example.battery.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a temperature sample from the battery sensor.
 * Logged every ~15 seconds regardless of charging state.
 */
@Entity(tableName = "temperature_samples")
data class TemperatureSample(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val temp: Float,       // Temperature in Celsius (°C)
    val timestamp: Long    // Unix timestamp in milliseconds
)