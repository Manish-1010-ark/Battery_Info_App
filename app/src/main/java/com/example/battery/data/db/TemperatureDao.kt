package com.example.battery.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TemperatureDao {

    /**
     * Insert a new temperature sample.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(temperatureSample: TemperatureSample)

    /**
     * Get all temperature samples after the specified cutoff time.
     * Returns as a Flow for reactive updates.
     *
     * @param cutoffTime Unix timestamp - only return samples after this time
     */
    @Query("SELECT * FROM temperature_samples WHERE timestamp >= :cutoffTime ORDER BY timestamp ASC")
    fun getFlow(cutoffTime: Long): Flow<List<TemperatureSample>>

    /**
     * Delete all temperature samples older than the specified time.
     * Used for cleanup to prevent database growth.
     *
     * @param cutoffTime Unix timestamp - delete samples before this time
     */
    @Query("DELETE FROM temperature_samples WHERE timestamp < :cutoffTime")
    suspend fun deleteOlderThan(cutoffTime: Long)

    /**
     * Delete all temperature samples.
     */
    @Query("DELETE FROM temperature_samples")
    suspend fun clearAll()

    /**
     * Get the total count of temperature samples.
     */
    @Query("SELECT COUNT(*) FROM temperature_samples")
    suspend fun getCount(): Int
}