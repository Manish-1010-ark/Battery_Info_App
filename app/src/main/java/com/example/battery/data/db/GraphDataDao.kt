package com.example.battery.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GraphDataDao {

    @Insert
    suspend fun insertChargingPower(graphData: GraphData)

    @Query("SELECT * FROM charging_power WHERE timestamp >= :cutoffTime ORDER BY timestamp ASC")
    fun getChargingPowerDataFlow(cutoffTime: Long): Flow<List<GraphData>>

    @Query("SELECT * FROM charging_power WHERE timestamp >= :cutoffTime ORDER BY timestamp ASC")
    suspend fun getChargingPowerData(cutoffTime: Long): List<GraphData>

    @Query("DELETE FROM charging_power")
    suspend fun clearChargingPowerData()

    @Query("DELETE FROM charging_power WHERE timestamp < :cutoffTime")
    suspend fun deleteOldData(cutoffTime: Long)
}