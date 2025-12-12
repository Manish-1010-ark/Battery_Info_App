package com.example.battery.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChargingPowerDao {

    /**
     * Insert a new charging power sample.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chargingPower: ChargingPower)

    /**
     * Get all charging power samples after the specified cutoff time.
     * Returns as a Flow for reactive updates.
     *
     * @param cutoffTime Unix timestamp - only return samples after this time
     */
    @Query("SELECT * FROM charging_power WHERE timestamp >= :cutoffTime ORDER BY timestamp ASC")
    fun getFlow(cutoffTime: Long): Flow<List<ChargingPower>>

    /**
     * Delete all charging power samples older than the specified time.
     * Used for cleanup to prevent database growth.
     *
     * @param cutoffTime Unix timestamp - delete samples before this time
     */
    @Query("DELETE FROM charging_power WHERE timestamp < :cutoffTime")
    suspend fun deleteOlderThan(cutoffTime: Long)

    /**
     * Delete all charging power samples.
     */
    @Query("DELETE FROM charging_power")
    suspend fun clearAll()

    /**
     * Get the total count of charging power samples.
     */
    @Query("SELECT COUNT(*) FROM charging_power")
    suspend fun getCount(): Int
}