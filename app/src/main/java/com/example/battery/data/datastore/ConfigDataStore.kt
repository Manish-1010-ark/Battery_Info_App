package com.example.battery.data.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

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

    /**
     * CRITICAL: Error handling for DataStore operations
     * Catches IOExceptions and returns empty preferences instead of crashing
     */
    private fun <T> Flow<T>.handleDataStoreErrors(defaultValue: T): Flow<T> {
        return this.catch { exception ->
            if (exception is IOException) {
                Log.e("ConfigDataStore", "Error reading preferences", exception)
                emit(defaultValue)
            } else {
                throw exception
            }
        }
    }

    // ==================== Battery Capacity ====================

    suspend fun saveBatteryCapacity(capacity: Int) {
        try {
            dataStore.edit { preferences ->
                preferences[KEY_BATTERY_CAPACITY] = capacity
            }
            Log.d("ConfigDataStore", "Saved battery capacity: $capacity")
        } catch (e: Exception) {
            Log.e("ConfigDataStore", "Failed to save battery capacity", e)
            throw e
        }
    }

    suspend fun getBatteryCapacity(): Int {
        return try {
            dataStore.data
                .handleDataStoreErrors(emptyPreferences())
                .map { preferences ->
                    preferences[KEY_BATTERY_CAPACITY] ?: -1
                }.first()
        } catch (e: Exception) {
            Log.e("ConfigDataStore", "Failed to get battery capacity", e)
            -1
        }
    }

    val batteryCapacityFlow: Flow<Int> = dataStore.data
        .handleDataStoreErrors(emptyPreferences())
        .map { preferences ->
            preferences[KEY_BATTERY_CAPACITY] ?: -1
        }

    // ==================== Current Pattern ====================

    suspend fun saveCurrentPattern(currentValues: List<Float>) {
        try {
            val formatted = currentValues.joinToString(",") { "%.6f".format(it) }
            dataStore.edit { preferences ->
                preferences[KEY_CURRENT_PATTERN] = formatted
            }
            Log.d("ConfigDataStore", "Saved current pattern: ${currentValues.size} values")
        } catch (e: Exception) {
            Log.e("ConfigDataStore", "Failed to save current pattern", e)
            throw e
        }
    }

    suspend fun getCurrentPattern(): List<Float> {
        return try {
            val data = dataStore.data
                .handleDataStoreErrors(emptyPreferences())
                .map { preferences ->
                    preferences[KEY_CURRENT_PATTERN]
                }.first()

            data?.split(",")?.mapNotNull {
                it.trim().toFloatOrNull()
            } ?: emptyList()
        } catch (e: Exception) {
            Log.e("ConfigDataStore", "Failed to get current pattern", e)
            emptyList()
        }
    }

    val currentPatternFlow: Flow<List<Float>> = dataStore.data
        .handleDataStoreErrors(emptyPreferences())
        .map { preferences ->
            preferences[KEY_CURRENT_PATTERN]?.split(",")?.mapNotNull {
                it.trim().toFloatOrNull()
            } ?: emptyList()
        }

    // ==================== Min Power ====================

    suspend fun saveMinPower(value: Float) {
        try {
            dataStore.edit { preferences ->
                preferences[KEY_MIN_POWER] = value
            }
        } catch (e: Exception) {
            Log.e("ConfigDataStore", "Failed to save min power", e)
        }
    }

    suspend fun getMinPower(): Float {
        return try {
            dataStore.data
                .handleDataStoreErrors(emptyPreferences())
                .map { preferences ->
                    preferences[KEY_MIN_POWER] ?: -1f
                }.first()
        } catch (e: Exception) {
            Log.e("ConfigDataStore", "Failed to get min power", e)
            -1f
        }
    }

    val minPowerFlow: Flow<Float> = dataStore.data
        .handleDataStoreErrors(emptyPreferences())
        .map { preferences ->
            preferences[KEY_MIN_POWER] ?: -1f
        }

    // ==================== Max Power ====================

    suspend fun saveMaxPower(value: Float) {
        try {
            dataStore.edit { preferences ->
                preferences[KEY_MAX_POWER] = value
            }
        } catch (e: Exception) {
            Log.e("ConfigDataStore", "Failed to save max power", e)
        }
    }

    suspend fun getMaxPower(): Float {
        return try {
            dataStore.data
                .handleDataStoreErrors(emptyPreferences())
                .map { preferences ->
                    preferences[KEY_MAX_POWER] ?: -1f
                }.first()
        } catch (e: Exception) {
            Log.e("ConfigDataStore", "Failed to get max power", e)
            -1f
        }
    }

    val maxPowerFlow: Flow<Float> = dataStore.data
        .handleDataStoreErrors(emptyPreferences())
        .map { preferences ->
            preferences[KEY_MAX_POWER] ?: -1f
        }

    // ==================== Avg Power ====================

    suspend fun saveAvgPower(value: Float) {
        try {
            dataStore.edit { preferences ->
                preferences[KEY_AVG_POWER] = value
            }
        } catch (e: Exception) {
            Log.e("ConfigDataStore", "Failed to save avg power", e)
        }
    }

    suspend fun getAvgPower(): Float {
        return try {
            dataStore.data
                .handleDataStoreErrors(emptyPreferences())
                .map { preferences ->
                    preferences[KEY_AVG_POWER] ?: -1f
                }.first()
        } catch (e: Exception) {
            Log.e("ConfigDataStore", "Failed to get avg power", e)
            -1f
        }
    }

    val avgPowerFlow: Flow<Float> = dataStore.data
        .handleDataStoreErrors(emptyPreferences())
        .map { preferences ->
            preferences[KEY_AVG_POWER] ?: -1f
        }

    // ==================== Last Current Now ====================

    suspend fun saveLastCurrentNow(currentNow: Int) {
        try {
            dataStore.edit { preferences ->
                preferences[KEY_LAST_CURRENT_NOW] = currentNow
            }
        } catch (e: Exception) {
            Log.e("ConfigDataStore", "Failed to save last current", e)
        }
    }

    suspend fun getLastCurrentNow(): Int {
        return try {
            dataStore.data
                .handleDataStoreErrors(emptyPreferences())
                .map { preferences ->
                    preferences[KEY_LAST_CURRENT_NOW] ?: 0
                }.first()
        } catch (e: Exception) {
            Log.e("ConfigDataStore", "Failed to get last current", e)
            0
        }
    }

    val lastCurrentNowFlow: Flow<Int> = dataStore.data
        .handleDataStoreErrors(emptyPreferences())
        .map { preferences ->
            preferences[KEY_LAST_CURRENT_NOW] ?: 0
        }

    // ==================== Voltage ====================

    suspend fun saveVoltage(voltage: Float) {
        try {
            dataStore.edit { preferences ->
                preferences[KEY_VOLTAGE] = voltage
            }
        } catch (e: Exception) {
            Log.e("ConfigDataStore", "Failed to save voltage", e)
        }
    }

    suspend fun getVoltage(): Float {
        return try {
            dataStore.data
                .handleDataStoreErrors(emptyPreferences())
                .map { preferences ->
                    preferences[KEY_VOLTAGE] ?: -1f
                }.first()
        } catch (e: Exception) {
            Log.e("ConfigDataStore", "Failed to get voltage", e)
            -1f
        }
    }

    val voltageFlow: Flow<Float> = dataStore.data
        .handleDataStoreErrors(emptyPreferences())
        .map { preferences ->
            preferences[KEY_VOLTAGE] ?: -1f
        }

    // ==================== Temperature ====================

    suspend fun saveTemperature(temp: Float) {
        try {
            dataStore.edit { preferences ->
                preferences[KEY_TEMPERATURE] = temp
            }
        } catch (e: Exception) {
            Log.e("ConfigDataStore", "Failed to save temperature", e)
        }
    }

    suspend fun getTemperature(): Float {
        return try {
            dataStore.data
                .handleDataStoreErrors(emptyPreferences())
                .map { preferences ->
                    preferences[KEY_TEMPERATURE] ?: -1f
                }.first()
        } catch (e: Exception) {
            Log.e("ConfigDataStore", "Failed to get temperature", e)
            -1f
        }
    }

    val temperatureFlow: Flow<Float> = dataStore.data
        .handleDataStoreErrors(emptyPreferences())
        .map { preferences ->
            preferences[KEY_TEMPERATURE] ?: -1f
        }

    // ==================== Reset Min/Max Power ====================

    suspend fun resetMinMaxPower() {
        try {
            dataStore.edit { preferences ->
                preferences[KEY_MIN_POWER] = -1f
                preferences[KEY_MAX_POWER] = -1f
            }
            Log.d("ConfigDataStore", "Reset min/max power")
        } catch (e: Exception) {
            Log.e("ConfigDataStore", "Failed to reset min/max power", e)
        }
    }

    // ==================== Configuration Status ====================

    /**
     * CRITICAL: Configuration validation
     * Returns true ONLY if both capacity AND pattern are valid
     * Prevents partial/corrupted configurations from being considered complete
     */
    suspend fun isConfigured(): Boolean {
        return try {
            dataStore.data
                .handleDataStoreErrors(emptyPreferences())
                .map { preferences ->
                    val capacity = preferences[KEY_BATTERY_CAPACITY] ?: -1
                    val pattern = preferences[KEY_CURRENT_PATTERN]

                    // CRITICAL: Both values must exist and be valid
                    val isCapacityValid = capacity > 0
                    val isPatternValid = !pattern.isNullOrEmpty() &&
                            pattern.split(",").mapNotNull { it.trim().toFloatOrNull() }.size >= 5

                    val configured = isCapacityValid && isPatternValid

                    Log.d("ConfigDataStore", "Config check - Capacity: $capacity, Pattern valid: $isPatternValid, Configured: $configured")

                    configured
                }.first()
        } catch (e: Exception) {
            Log.e("ConfigDataStore", "Failed to check configuration status", e)
            false
        }
    }

    /**
     * CRITICAL: Reactive configuration status flow
     * UI should observe this to automatically update when config changes
     */
    val isConfiguredFlow: Flow<Boolean> = dataStore.data
        .handleDataStoreErrors(emptyPreferences())
        .map { preferences ->
            val capacity = preferences[KEY_BATTERY_CAPACITY] ?: -1
            val pattern = preferences[KEY_CURRENT_PATTERN]

            val isCapacityValid = capacity > 0
            val isPatternValid = !pattern.isNullOrEmpty() &&
                    pattern.split(",").mapNotNull { it.trim().toFloatOrNull() }.size >= 5

            isCapacityValid && isPatternValid
        }

    /**
     * CRITICAL: Clear all configuration data
     * Used for testing or when user wants to reconfigure
     */
    suspend fun clearConfiguration() {
        try {
            dataStore.edit { preferences ->
                preferences.remove(KEY_BATTERY_CAPACITY)
                preferences.remove(KEY_CURRENT_PATTERN)
            }
            Log.d("ConfigDataStore", "Configuration cleared")
        } catch (e: Exception) {
            Log.e("ConfigDataStore", "Failed to clear configuration", e)
            throw e
        }
    }
}