package com.example.battery.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [GraphData::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun graphDataDao(): GraphDataDao
}