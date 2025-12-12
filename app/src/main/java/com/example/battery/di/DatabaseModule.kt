package com.example.battery.di

import android.content.Context
import androidx.room.Room
import com.example.battery.data.db.AppDatabase
import com.example.battery.data.db.ChargingPowerDao
import com.example.battery.data.db.DischargingPowerDao
import com.example.battery.data.db.TemperatureDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * DatabaseModule - Provides Room Database dependencies
 *
 * This module provides:
 * - AppDatabase (the Room database instance)
 * - ChargingPowerDao (DAO for charging power data)
 * - DischargingPowerDao (DAO for discharging power data)
 * - TemperatureDao (DAO for temperature data)
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "battery_data.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideChargingPowerDao(database: AppDatabase): ChargingPowerDao {
        return database.chargingPowerDao()
    }

    @Provides
    @Singleton
    fun provideDischargingPowerDao(database: AppDatabase): DischargingPowerDao {
        return database.dischargingPowerDao()
    }

    @Provides
    @Singleton
    fun provideTemperatureDao(database: AppDatabase): TemperatureDao {
        return database.temperatureDao()
    }
}