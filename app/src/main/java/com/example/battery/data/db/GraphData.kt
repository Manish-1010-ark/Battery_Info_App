package com.example.battery.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "charging_power")
data class GraphData(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "power")
    val power: Float,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long
)