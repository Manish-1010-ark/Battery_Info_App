package com.example.battery.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Main Room database for the analytics system.
 *
 * Version 2: Added discharging_power and temperature_samples tables
 */
@Database(
    entities = [
        ChargingPower::class,
        DischargingPower::class,
        TemperatureSample::class
    ],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    // DAOs
    abstract fun chargingPowerDao(): ChargingPowerDao
    abstract fun dischargingPowerDao(): DischargingPowerDao
    abstract fun temperatureDao(): TemperatureDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private const val DATABASE_NAME = "analytics_database"

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration() // For development
                    // TODO: Add proper migration strategy for production
                    // .addMigrations(MIGRATION_1_2)
                    .build()

                INSTANCE = instance
                instance
            }
        }

        // Example migration for production (uncomment when ready)
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create new tables
                database.execSQL(
                    """CREATE TABLE IF NOT EXISTS discharging_power (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        power REAL NOT NULL,
                        timestamp INTEGER NOT NULL
                    )"""
                )

                database.execSQL(
                    """CREATE TABLE IF NOT EXISTS temperature_samples (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        temp REAL NOT NULL,
                        timestamp INTEGER NOT NULL
                    )"""
                )

                // Create indices for better query performance
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_charging_power_timestamp ON charging_power(timestamp)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_discharging_power_timestamp ON discharging_power(timestamp)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_temperature_samples_timestamp ON temperature_samples(timestamp)"
                )
            }
        }
    }
}