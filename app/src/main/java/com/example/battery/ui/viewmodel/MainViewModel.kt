package com.example.battery.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.battery.data.datastore.ConfigDataStore
import com.example.battery.data.db.GraphData
import com.example.battery.data.model.BatteryData
import com.example.battery.data.repository.BatteryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
    val graphData: List<GraphData> = emptyList(),
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
    val canConfigure: Boolean = false
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val batteryRepository: BatteryRepository,
    private val configDataStore: ConfigDataStore
) : ViewModel() {

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
        batteryRepository.getGraphDataFlow(2 * 60 * 1000L), // Last 2 minutes
        configDataStore.isConfiguredFlow
    ) { batteryData, graphData, isConfigured ->
        BatteryUiState(
            batteryData = batteryData,
            graphData = graphData,
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
                    canConfigure = false
                )
            } catch (e: Exception) {
                _configUiState.value = _configUiState.value.copy(
                    deviceInfo = "Error loading device info",
                    currentUnit = "Error detecting current unit"
                )
            }
        }
    }

    /**
     * Collect 5 current samples over 5 seconds
     * This helps establish a baseline for battery calculations
     */
    fun collectCurrentSamples() {
        // Prevent multiple collections
        if (_configUiState.value.isCollecting || _configUiState.value.collectedValues.isNotEmpty()) {
            return
        }

        viewModelScope.launch {
            try {
                _configUiState.value = _configUiState.value.copy(
                    isCollecting = true,
                    canConfigure = false
                )

                val samples = mutableListOf<Float>()

                repeat(5) { index ->
                    kotlinx.coroutines.delay(1000)
                    val sample = batteryRepository.collectCurrentSample()
                    samples.add(sample)

                    _configUiState.value = _configUiState.value.copy(
                        collectedValues = samples.toList(),
                        currentPattern = "Current Pattern: ${samples.joinToString(", ") { "%.3fA".format(it) }}",
                        isCollecting = true,
                        canConfigure = false
                    )
                }

                // Collection complete
                _configUiState.value = _configUiState.value.copy(
                    isCollecting = false,
                    canConfigure = true
                )
            } catch (e: Exception) {
                _configUiState.value = _configUiState.value.copy(
                    isCollecting = false,
                    canConfigure = false,
                    currentPattern = "Error collecting samples: ${e.message}"
                )
            }
        }
    }

    /**
     * Update battery capacity input
     */
    fun updateBatteryCapacity(capacity: String) {
        _configUiState.value = _configUiState.value.copy(batteryCapacity = capacity)
    }

    /**
     * Save configuration and mark app as configured
     * This triggers MainActivity to switch from ConfigScreen to MainScreen
     */
    fun saveConfiguration(capacity: Int) {
        viewModelScope.launch {
            try {
                batteryRepository.saveConfiguration(capacity, _configUiState.value.collectedValues)
                // isConfiguredFlow will automatically update uiState.isConfigured
            } catch (e: Exception) {
                // Handle error if needed
            }
        }
    }

    /**
     * Reset configuration state (for testing or reconfiguration)
     */
    fun resetConfigState() {
        _configUiState.value = ConfigUiState()
    }
}