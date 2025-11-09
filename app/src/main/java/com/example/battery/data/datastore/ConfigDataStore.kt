package com.example.battery.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "battery_config")

@Singleton
class ConfigDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        private val KEY_BATTERY_CAPACITY = intPreferencesKey("battery_capacity")
        private val KEY_CURRENT_PATTERN = stringPreferencesKey("current_pattern")
        private val KEY_MIN_POWER = floatPreferencesKey("min_power")
        private val KEY_MAX_POWER = floatPreferencesKey("max_power")
        private val KEY_AVG_POWER = floatPreferencesKey("avg_power")
        private val KEY_LAST_CURRENT_NOW = intPreferencesKey("last_current_now")
        private val KEY_VOLTAGE = floatPreferencesKey("voltage")
        private val KEY_TEMPERATURE = floatPreferencesKey("temperature")
    }

    // Battery Capacity
    suspend fun saveBatteryCapacity(capacity: Int) {
        dataStore.edit { preferences ->
            preferences[KEY_BATTERY_CAPACITY] = capacity
        }
    }

    suspend fun getBatteryCapacity(): Int {
        return dataStore.data.map { preferences ->
            preferences[KEY_BATTERY_CAPACITY] ?: -1
        }.first()
    }

    val batteryCapacityFlow: Flow<Int> = dataStore.data.map { preferences ->
        preferences[KEY_BATTERY_CAPACITY] ?: -1
    }

    // Current Pattern
    suspend fun saveCurrentPattern(currentValues: List<Float>) {
        val formatted = currentValues.joinToString(",")
        dataStore.edit { preferences ->
            preferences[KEY_CURRENT_PATTERN] = formatted
        }
    }

    suspend fun getCurrentPattern(): List<Float> {
        val data = dataStore.data.map { preferences ->
            preferences[KEY_CURRENT_PATTERN]
        }.first()

        return data?.split(",")?.mapNotNull { it.toFloatOrNull() } ?: emptyList()
    }

    val currentPatternFlow: Flow<List<Float>> = dataStore.data.map { preferences ->
        preferences[KEY_CURRENT_PATTERN]?.split(",")?.mapNotNull { it.toFloatOrNull() } ?: emptyList()
    }

    // Min Power
    suspend fun saveMinPower(value: Float) {
        dataStore.edit { preferences ->
            preferences[KEY_MIN_POWER] = value
        }
    }

    suspend fun getMinPower(): Float {
        return dataStore.data.map { preferences ->
            preferences[KEY_MIN_POWER] ?: -1f
        }.first()
    }

    val minPowerFlow: Flow<Float> = dataStore.data.map { preferences ->
        preferences[KEY_MIN_POWER] ?: -1f
    }

    // Max Power
    suspend fun saveMaxPower(value: Float) {
        dataStore.edit { preferences ->
            preferences[KEY_MAX_POWER] = value
        }
    }

    suspend fun getMaxPower(): Float {
        return dataStore.data.map { preferences ->
            preferences[KEY_MAX_POWER] ?: -1f
        }.first()
    }

    val maxPowerFlow: Flow<Float> = dataStore.data.map { preferences ->
        preferences[KEY_MAX_POWER] ?: -1f
    }

    // Avg Power
    suspend fun saveAvgPower(value: Float) {
        dataStore.edit { preferences ->
            preferences[KEY_AVG_POWER] = value
        }
    }

    suspend fun getAvgPower(): Float {
        return dataStore.data.map { preferences ->
            preferences[KEY_AVG_POWER] ?: -1f
        }.first()
    }

    val avgPowerFlow: Flow<Float> = dataStore.data.map { preferences ->
        preferences[KEY_AVG_POWER] ?: -1f
    }

    // Last Current Now
    suspend fun saveLastCurrentNow(currentNow: Int) {
        dataStore.edit { preferences ->
            preferences[KEY_LAST_CURRENT_NOW] = currentNow
        }
    }

    suspend fun getLastCurrentNow(): Int {
        return dataStore.data.map { preferences ->
            preferences[KEY_LAST_CURRENT_NOW] ?: 0
        }.first()
    }

    val lastCurrentNowFlow: Flow<Int> = dataStore.data.map { preferences ->
        preferences[KEY_LAST_CURRENT_NOW] ?: 0
    }

    // Voltage
    suspend fun saveVoltage(voltage: Float) {
        dataStore.edit { preferences ->
            preferences[KEY_VOLTAGE] = voltage
        }
    }

    suspend fun getVoltage(): Float {
        return dataStore.data.map { preferences ->
            preferences[KEY_VOLTAGE] ?: -1f
        }.first()
    }

    val voltageFlow: Flow<Float> = dataStore.data.map { preferences ->
        preferences[KEY_VOLTAGE] ?: -1f
    }

    // Temperature
    suspend fun saveTemperature(temp: Float) {
        dataStore.edit { preferences ->
            preferences[KEY_TEMPERATURE] = temp
        }
    }

    suspend fun getTemperature(): Float {
        return dataStore.data.map { preferences ->
            preferences[KEY_TEMPERATURE] ?: -1f
        }.first()
    }

    val temperatureFlow: Flow<Float> = dataStore.data.map { preferences ->
        preferences[KEY_TEMPERATURE] ?: -1f
    }

    // Reset Min/Max Power
    suspend fun resetMinMaxPower() {
        dataStore.edit { preferences ->
            preferences[KEY_MIN_POWER] = -1f
            preferences[KEY_MAX_POWER] = -1f
        }
    }

    // Configuration Check
    suspend fun isConfigured(): Boolean {
        return dataStore.data.map { preferences ->
            val capacity = preferences[KEY_BATTERY_CAPACITY] ?: -1
            val pattern = preferences[KEY_CURRENT_PATTERN]
            capacity != -1 && !pattern.isNullOrEmpty()
        }.first()
    }

    val isConfiguredFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        val capacity = preferences[KEY_BATTERY_CAPACITY] ?: -1
        val pattern = preferences[KEY_CURRENT_PATTERN]
        capacity != -1 && !pattern.isNullOrEmpty()
    }
}