package com.example.battery.ui.screens.analytics.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.battery.data.db.ChargingPower
import com.example.battery.data.db.DischargingPower
import com.example.battery.data.db.TemperatureSample
import com.example.battery.data.repository.BatteryRepository
import com.example.battery.data.repository.TimeRange
import com.example.battery.util.calculateDuration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

// ====== ENUMS ======

enum class AnalyticsTab {
    POWER,
    TEMPERATURE,
    USAGE
}

enum class PowerSubTab {
    CHARGING,
    DISCHARGING
}

// ====== DATA CLASSES ======

/**
 * Represents a point on the graph
 */
data class GraphPoint(
    val timestamp: Long,
    val value: Float
)

/**
 * Statistics for power data (charging or discharging)
 */
data class PowerStats(
    val minPower: Float,
    val maxPower: Float,
    val avgPower: Float,
    val dataPoints: Int,
    val duration: Long // Duration in milliseconds
) {
    companion object {
        val EMPTY = PowerStats(
            minPower = 0f,
            maxPower = 0f,
            avgPower = 0f,
            dataPoints = 0,
            duration = 0L
        )
    }
}

/**
 * Statistics for temperature data
 */
data class TempStats(
    val minTemp: Float,
    val maxTemp: Float,
    val avgTemp: Float,
    val dataPoints: Int,
    val duration: Long
) {
    companion object {
        val EMPTY = TempStats(
            minTemp = 0f,
            maxTemp = 0f,
            avgTemp = 0f,
            dataPoints = 0,
            duration = 0L
        )
    }
}

/**
 * Complete UI state for Analytics screen
 */
data class AnalyticsUiState(
    val chargingPower: List<GraphPoint> = emptyList(),
    val dischargingPower: List<GraphPoint> = emptyList(),
    val temperature: List<GraphPoint> = emptyList(),

    val statsPowerCharging: PowerStats = PowerStats.EMPTY,
    val statsPowerDischarging: PowerStats = PowerStats.EMPTY,
    val statsTemperature: TempStats = TempStats.EMPTY,

    val selectedTab: AnalyticsTab = AnalyticsTab.POWER,
    val selectedPowerSubTab: PowerSubTab = PowerSubTab.CHARGING,
    val selectedRange: TimeRange = TimeRange.HOUR_1,

    val isLoading: Boolean = false,
    val error: String? = null
)

// ====== VIEW MODEL ======

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val batteryRepository: BatteryRepository
) : ViewModel() {

    // User-controlled selections
    private val _selectedTab = MutableStateFlow(AnalyticsTab.POWER)
    private val _selectedPowerSubTab = MutableStateFlow(PowerSubTab.CHARGING)
    private val _selectedRange = MutableStateFlow(TimeRange.HOUR_1)

    /**
     * Compute cutoff time once per range change
     */
    private fun cutoff(range: TimeRange): Long {
        return System.currentTimeMillis() - range.milliseconds
    }

    // Reactive data flows from repository (switch on range changes)
    private val chargingPowerFlow = _selectedRange.flatMapLatest { range ->
        batteryRepository.getChargingPowerFlow(cutoff(range))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val dischargingPowerFlow = _selectedRange.flatMapLatest { range ->
        batteryRepository.getDischargingPowerFlow(cutoff(range))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val temperatureFlow = _selectedRange.flatMapLatest { range ->
        batteryRepository.getTemperatureFlow(cutoff(range))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Combined UI State - reactively updates when any flow changes
    val uiState: StateFlow<AnalyticsUiState> = combine(
        _selectedTab,
        _selectedPowerSubTab,
        _selectedRange,
        chargingPowerFlow,
        dischargingPowerFlow,
        temperatureFlow
    ) { flows: Array<Any?> ->
        val selectedTab = flows[0] as AnalyticsTab
        val selectedPowerSubTab = flows[1] as PowerSubTab
        val selectedRange = flows[2] as TimeRange
        val charging = flows[3] as List<ChargingPower>
        val discharging = flows[4] as List<DischargingPower>
        val temp = flows[5] as List<TemperatureSample>

        // Convert to graph points
        val chargingPoints = charging.map { GraphPoint(it.timestamp, it.power) }
        val dischargingPoints = discharging.map { GraphPoint(it.timestamp, it.power) }
        val temperaturePoints = temp.map { GraphPoint(it.timestamp, it.temp) }

        // Calculate statistics
        val chargingStats = calculatePowerStatsFromCharging(charging)
        val dischargingStats = calculatePowerStatsFromDischarging(discharging)
        val tempStats = calculateTempStats(temp)

        AnalyticsUiState(
            chargingPower = chargingPoints,
            dischargingPower = dischargingPoints,
            temperature = temperaturePoints,
            statsPowerCharging = chargingStats,
            statsPowerDischarging = dischargingStats,
            statsTemperature = tempStats,
            selectedTab = selectedTab,
            selectedPowerSubTab = selectedPowerSubTab,
            selectedRange = selectedRange,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnalyticsUiState(isLoading = true)
    )

    // ====== USER ACTIONS ======

    fun selectTab(tab: AnalyticsTab) {
        _selectedTab.update { tab }
    }

    fun selectPowerSubTab(subTab: PowerSubTab) {
        _selectedPowerSubTab.update { subTab }
    }

    fun selectTimeRange(range: TimeRange) {
        _selectedRange.update { range }
    }

    // ====== STATISTICS CALCULATION ======

    /**
     * Calculate power statistics from charging power data
     */
    private fun calculatePowerStatsFromCharging(data: List<ChargingPower>): PowerStats {
        if (data.isEmpty()) return PowerStats.EMPTY

        val values = data.map { it.power }
        val timestamps = data.map { it.timestamp }

        val min = values.minOrNull() ?: 0f
        val max = values.maxOrNull() ?: 0f
        val avg = values.average().toFloat()
        val count = values.size

        val duration = calculateDuration(timestamps)

        return PowerStats(
            minPower = min,
            maxPower = max,
            avgPower = avg,
            dataPoints = count,
            duration = duration
        )
    }

    /**
     * Calculate power statistics from discharging power data
     */
    private fun calculatePowerStatsFromDischarging(data: List<DischargingPower>): PowerStats {
        if (data.isEmpty()) return PowerStats.EMPTY

        val values = data.map { it.power }
        val timestamps = data.map { it.timestamp }

        val min = values.minOrNull() ?: 0f
        val max = values.maxOrNull() ?: 0f
        val avg = values.average().toFloat()
        val count = values.size

        val duration = calculateDuration(timestamps)

        return PowerStats(
            minPower = min,
            maxPower = max,
            avgPower = avg,
            dataPoints = count,
            duration = duration
        )
    }

    /**
     * Calculate temperature statistics from temperature data
     */
    private fun calculateTempStats(data: List<TemperatureSample>): TempStats {
        if (data.isEmpty()) return TempStats.EMPTY

        val values = data.map { it.temp }
        val timestamps = data.map { it.timestamp }

        val min = values.minOrNull() ?: 0f
        val max = values.maxOrNull() ?: 0f
        val avg = values.average().toFloat()
        val count = values.size

        val duration = calculateDuration(timestamps)

        return TempStats(
            minTemp = min,
            maxTemp = max,
            avgTemp = avg,
            dataPoints = count,
            duration = duration
        )
    }
}