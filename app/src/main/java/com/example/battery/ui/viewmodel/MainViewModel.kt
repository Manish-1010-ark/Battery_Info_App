package com.example.battery.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.battery.data.datastore.ConfigDataStore
import com.example.battery.data.model.BatteryData
import com.example.battery.data.repository.BatteryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the main battery monitoring screen
 */
data class BatteryUiState(
    val batteryData: BatteryData = BatteryData.EMPTY,
    val isConfigured: Boolean = false,
    val isLoading: Boolean = true
)

/**
 * UI state for the configuration screen
 */
data class ConfigUiState(
    val deviceInfo: String = "",
    val currentUnit: String = "",
    val currentPattern: String = "Collecting...",
    val batteryCapacity: String = "",
    val collectedValues: List<Float> = emptyList(),
    val isCollecting: Boolean = false,
    val canConfigure: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val batteryRepository: BatteryRepository,
    private val configDataStore: ConfigDataStore
) : ViewModel() {

    /**
     * CRITICAL: Config loading state
     * Prevents UI from flashing between screens during startup
     * Only set to true after configuration status has been fully determined
     */
    var isConfigLoaded by mutableStateOf(false)
        private set

    /**
     * Main UI state that combines:
     * 1. Live battery data from repository
     * 2. Historical graph data from database
     * 3. Configuration status from DataStore
     *
     * This is the SINGLE SOURCE OF TRUTH for the UI.
     * No race conditions because all flows are combined reactively.
     */
    val uiState: StateFlow<BatteryUiState> = combine(
        batteryRepository.batteryDataFlow,
        configDataStore.isConfiguredFlow
    ) { batteryData, isConfigured ->
        // Mark config as loaded once we receive the first emission
        if (!isConfigLoaded) {
            isConfigLoaded = true
        }

        BatteryUiState(
            batteryData = batteryData,
            isConfigured = isConfigured,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BatteryUiState(isLoading = true)
    )

    /**
     * Configuration screen state
     */
    private val _configUiState = MutableStateFlow(ConfigUiState())
    val configUiState: StateFlow<ConfigUiState> = _configUiState

    /**
     * Track current sampling job to allow cancellation
     */
    private var samplingJob: Job? = null

    /**
     * Initialize configuration data when ConfigScreen is opened
     * This fetches device info and auto-detects battery capacity
     */
    fun initializeConfigData() {
        viewModelScope.launch {
            try {
                val configData = batteryRepository.getInitialConfigData()

                _configUiState.value = ConfigUiState(
                    deviceInfo = "Device: ${configData.manufacturer} ${configData.model}\nAndroid ${configData.androidVersion}",
                    currentUnit = "Current Unit: ${configData.currentUnit}",
                    batteryCapacity = if (configData.detectedCapacity > 0)
                        configData.detectedCapacity.toString()
                    else
                        "",
                    isCollecting = false,
                    canConfigure = false,
                    errorMessage = null
                )

                Log.d("MainViewModel", "Config data initialized successfully")
            } catch (e: Exception) {
                Log.e("MainViewModel", "Failed to initialize config data", e)
                _configUiState.value = _configUiState.value.copy(
                    deviceInfo = "Error loading device info",
                    currentUnit = "Error detecting current unit",
                    errorMessage = "Failed to initialize: ${e.message}"
                )
            }
        }
    }

    /**
     * CRITICAL: Collect 5 current samples over 5 seconds
     * This helps establish a baseline for battery calculations
     * MUST ONLY be called when device is NOT charging
     *
     * Safety checks:
     * 1. Not already collecting
     * 2. No samples collected yet
     * 3. Device NOT charging (triple-checked)
     */
    fun collectCurrentSamples() {
        Log.d("MainViewModel", "collectCurrentSamples() called")

        // Safety check 1: Not already collecting
        if (_configUiState.value.isCollecting) {
            Log.w("MainViewModel", "Already collecting - ignoring call")
            return
        }

        // Safety check 2: No samples collected yet
        if (_configUiState.value.collectedValues.isNotEmpty()) {
            Log.w("MainViewModel", "Samples already collected - ignoring call")
            return
        }

        // CRITICAL Safety check 3: Get LATEST charging status from repository
        val currentBatteryData = batteryRepository.batteryDataFlow.value
        Log.d("MainViewModel", "📊 ENTRY CHECK: isCharging=${currentBatteryData.isCharging}, isCollecting=${_configUiState.value.isCollecting}, samplesCount=${_configUiState.value.collectedValues.size}")
        if (currentBatteryData.isCharging) {
            Log.e("MainViewModel", "BLOCKED: Device is charging - cannot collect samples")
            _configUiState.value = _configUiState.value.copy(
                errorMessage = "Cannot collect samples - device is charging",
                currentPattern = "Blocked: Device charging"
            )
            return
        }

        Log.d("MainViewModel", "All checks passed - starting sample collection")

        // Cancel any existing job
        samplingJob?.cancel()

        samplingJob = viewModelScope.launch {
            try {
                _configUiState.value = _configUiState.value.copy(
                    isCollecting = true,
                    canConfigure = false,
                    errorMessage = null,
                    currentPattern = "Starting collection..."
                )

                Log.d("MainViewModel", "Sample collection started")

                val samples = mutableListOf<Float>()

                repeat(5) { index ->
                    Log.d("MainViewModel", "🔄 LOOP ITERATION $index - About to check charging state")
                    // CRITICAL: Check charging state BEFORE each sample
                    val batteryData = batteryRepository.batteryDataFlow.value

                    Log.d("MainViewModel", "Sample ${index + 1}/5 - Charging: ${batteryData.isCharging}")

                    if (batteryData.isCharging) {
                        // Charger was connected mid-sampling - ABORT IMMEDIATELY
                        Log.e("MainViewModel", "ABORT: Charger connected during sampling at sample ${index + 1}")

                        _configUiState.value = _configUiState.value.copy(
                            isCollecting = false,
                            canConfigure = false,
                            collectedValues = emptyList(),
                            currentPattern = "Aborted: Charger connected during sampling",
                            errorMessage = "Sampling stopped - charger was connected"
                        )
                        return@launch
                    }

                    // Wait 1 second before taking sample
                    kotlinx.coroutines.delay(1000)

                    // Collect the sample
                    val sample = batteryRepository.collectCurrentSample()
                    samples.add(sample)

                    Log.d("MainViewModel", "Sample ${index + 1}/5 collected: ${sample}A")

                    // Update UI with current progress
                    _configUiState.value = _configUiState.value.copy(
                        collectedValues = samples.toList(),
                        currentPattern = "Current Pattern: ${samples.joinToString(", ") { "%.3fA".format(it) }}",
                        isCollecting = true,
                        canConfigure = false
                    )
                }

                // CRITICAL: Final charging check after all samples collected
                val finalBatteryData = batteryRepository.batteryDataFlow.value

                Log.d("MainViewModel", "All samples collected - Final charging check: ${finalBatteryData.isCharging}")

                if (finalBatteryData.isCharging) {
                    // Charger was connected right at the end - discard all samples
                    Log.e("MainViewModel", "DISCARD: Charger connected after collection completed")

                    _configUiState.value = _configUiState.value.copy(
                        isCollecting = false,
                        canConfigure = false,
                        collectedValues = emptyList(),
                        currentPattern = "Discarded: Charger connected",
                        errorMessage = "Samples discarded - charger was connected"
                    )
                } else {
                    // SUCCESS - All samples collected without charging interruption
                    Log.d("MainViewModel", "SUCCESS: All 5 samples collected successfully while unplugged")

                    _configUiState.value = _configUiState.value.copy(
                        isCollecting = false,
                        canConfigure = true,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error during sample collection", e)

                _configUiState.value = _configUiState.value.copy(
                    isCollecting = false,
                    canConfigure = false,
                    collectedValues = emptyList(),
                    currentPattern = "Error during collection",
                    errorMessage = "Sampling failed: ${e.message}"
                )
            }
        }
    }

    /**
     * CRITICAL: Stop current sampling immediately
     * Called when charger is detected during active sampling
     */
    fun stopCurrentSampling() {
        Log.d("MainViewModel", "stopCurrentSampling() called - Cancelling job")

        samplingJob?.cancel()
        samplingJob = null

        _configUiState.value = _configUiState.value.copy(
            isCollecting = false,
            canConfigure = false,
            collectedValues = emptyList(),
            currentPattern = "Stopped: Charger detected",
            errorMessage = "Sampling aborted - charger connected"
        )

        Log.d("MainViewModel", "Sampling stopped and state cleared")
    }

    /**
     * Reset sampling state to allow retry
     * Called when user wants to retry after unplugging charger
     */
    fun resetSamplingState() {
        Log.d("MainViewModel", "resetSamplingState() called")

        samplingJob?.cancel()
        samplingJob = null

        _configUiState.value = _configUiState.value.copy(
            collectedValues = emptyList(),
            isCollecting = false,
            canConfigure = false,
            currentPattern = "Ready to collect...",
            errorMessage = null
        )

        Log.d("MainViewModel", "Sampling state reset - ready for new attempt")
    }

    /**
     * Update battery capacity input
     */
    fun updateBatteryCapacity(capacity: String) {
        _configUiState.value = _configUiState.value.copy(
            batteryCapacity = capacity,
            errorMessage = null
        )
    }

    /**
     * CRITICAL: Save configuration and mark app as configured
     * This triggers MainActivity to switch from ConfigScreen to MainScreen
     *
     * Final validation before saving:
     * 1. NOT charging
     * 2. Valid samples collected (exactly 5)
     * 3. Valid capacity (positive integer)
     */
    fun saveConfiguration(capacity: Int) {
        viewModelScope.launch {
            try {
                Log.d("MainViewModel", "saveConfiguration() called with capacity: $capacity")

                // CRITICAL: Final validation - Get LATEST charging status
                val currentBatteryData = batteryRepository.batteryDataFlow.value

                Log.d("MainViewModel", "Final validation - Charging: ${currentBatteryData.isCharging}")

                if (currentBatteryData.isCharging) {
                    Log.e("MainViewModel", "BLOCKED: Cannot save - device is charging")

                    _configUiState.value = _configUiState.value.copy(
                        errorMessage = "Cannot save configuration while charging"
                    )
                    return@launch
                }

                if (_configUiState.value.collectedValues.isEmpty()) {
                    Log.e("MainViewModel", "BLOCKED: No samples collected")

                    _configUiState.value = _configUiState.value.copy(
                        errorMessage = "No samples collected"
                    )
                    return@launch
                }

                if (_configUiState.value.collectedValues.size < 5) {
                    Log.e("MainViewModel", "BLOCKED: Insufficient samples (${_configUiState.value.collectedValues.size}/5)")

                    _configUiState.value = _configUiState.value.copy(
                        errorMessage = "Insufficient samples (need 5, got ${_configUiState.value.collectedValues.size})"
                    )
                    return@launch
                }

                if (capacity <= 0) {
                    Log.e("MainViewModel", "BLOCKED: Invalid capacity: $capacity")

                    _configUiState.value = _configUiState.value.copy(
                        errorMessage = "Invalid battery capacity"
                    )
                    return@launch
                }

                // All validations passed - Save configuration
                Log.d("MainViewModel", "All validations passed - Saving configuration")
                Log.d("MainViewModel", "Capacity: $capacity mAh")
                Log.d("MainViewModel", "Samples: ${_configUiState.value.collectedValues}")

                batteryRepository.saveConfiguration(capacity, _configUiState.value.collectedValues)

                Log.d("MainViewModel", "Configuration saved successfully!")

                // Clean up
                samplingJob?.cancel()

            } catch (e: Exception) {
                Log.e("MainViewModel", "Failed to save configuration", e)

                _configUiState.value = _configUiState.value.copy(
                    errorMessage = "Failed to save configuration: ${e.message}"
                )
            }
        }
    }

    /**
     * Reset configuration state (for testing or reconfiguration)
     */
    fun resetConfigState() {
        Log.d("MainViewModel", "resetConfigState() called")

        samplingJob?.cancel()
        _configUiState.value = ConfigUiState()
    }

    /**
     * Clean up when ViewModel is cleared
     */
    override fun onCleared() {
        super.onCleared()
        Log.d("MainViewModel", "ViewModel cleared - cancelling jobs")
        samplingJob?.cancel()
    }
}