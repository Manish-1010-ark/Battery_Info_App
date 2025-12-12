package com.example.battery.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DischargingPowerDao {

    /**
     * Insert a new discharging power sample.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(dischargingPower: DischargingPower)

    /**
     * Get all discharging power samples after the specified cutoff time.
     * Returns as a Flow for reactive updates.
     *
     * @param cutoffTime Unix timestamp - only return samples after this time
     */
    @Query("SELECT * FROM discharging_power WHERE timestamp >= :cutoffTime ORDER BY timestamp ASC")
    fun getFlow(cutoffTime: Long): Flow<List<DischargingPower>>

    /**
     * Delete all discharging power samples older than the specified time.
     * Used for cleanup to prevent database growth.
     *
     * @param cutoffTime Unix timestamp - delete samples before this time
     */
    @Query("DELETE FROM discharging_power WHERE timestamp < :cutoffTime")
    suspend fun deleteOlderThan(cutoffTime: Long)

    /**
     * Delete all discharging power samples.
     */
    @Query("DELETE FROM discharging_power")
    suspend fun clearAll()

    /**
     * Get the total count of discharging power samples.
     */
    @Query("SELECT COUNT(*) FROM discharging_power")
    suspend fun getCount(): Int
}