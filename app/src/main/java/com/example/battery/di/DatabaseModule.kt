package com.example.battery.di

import android.content.Context
import androidx.room.Room
import com.example.battery.data.db.AppDatabase
import com.example.battery.data.db.GraphDataDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * DatabaseModule - Provides Room Database dependencies
 *
 * This module ONLY provides:
 * - AppDatabase (the Room database instance)
 * - GraphDataDao (the DAO for accessing graph data)
 *
 * NOTE: ConfigDataStore is provided in DataStoreModule, NOT here!
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
    fun provideGraphDataDao(database: AppDatabase): GraphDataDao {
        return database.graphDataDao()
    }
}