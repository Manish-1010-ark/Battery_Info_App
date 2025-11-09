package com.example.battery.data.repository

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import com.example.battery.data.datastore.ConfigDataStore
import com.example.battery.data.db.GraphData
import com.example.battery.data.db.GraphDataDao
import com.example.battery.data.model.BatteryData
import com.example.battery.widget.BatteryWidgetProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.max

data class ConfigData(
    val manufacturer: String,
    val model: String,
    val androidVersion: String,
    val currentUnit: String,
    val detectedCapacity: Int,
    val currentSamples: List<Float> = emptyList()
)

@Singleton
class BatteryRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val graphDataDao: GraphDataDao,
    private val configDataStore: ConfigDataStore
) {
    private val batteryManager: BatteryManager by lazy {
        context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    }

    // THE SINGLE SOURCE OF TRUTH - Repository owns the StateFlow
    private val _batteryDataFlow = MutableStateFlow(BatteryData.EMPTY)
    val batteryDataFlow: StateFlow<BatteryData> = _batteryDataFlow.asStateFlow()

    // Current sampling for averaging
    private val currentSamples = mutableListOf<Float>()
    private var currentSampleCounter = 0
    private var avgCurrentFor10Sec = 0f

    // Power tracking
    private var minPower: Float = -1f
    private var maxPower: Float = -1f
    private var chargerConnected = false

    private var lastDbInsertTime = 0L

    /**
     * Main update function called by BatteryMonitorService.
     * This updates the StateFlow and broadcasts to widget.
     */
    suspend fun updateCurrentBatteryData() = withContext(Dispatchers.IO) {
        val batteryData = getCurrentBatteryData()
        _batteryDataFlow.value = batteryData

        // Broadcast to widget
        broadcastWidgetUpdate()
    }

    private suspend fun getCurrentBatteryData(): BatteryData {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            ?: return BatteryData.EMPTY

        // Extract raw battery info
        val rawVoltage = batteryIntent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0).toFloat()
        val voltage = if (rawVoltage > 100) rawVoltage / 1000f else rawVoltage

        val level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val batteryPct = if (scale > 0) (level * 100 / scale.toFloat()) else 0f

        val temperature = batteryIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f

        val status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val plugged = batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)

        // Get current in Amps
        var currentNow = try {
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        } catch (e: Exception) {
            configDataStore.getLastCurrentNow()
        }

        if (currentNow == Integer.MIN_VALUE || currentNow == 0) {
            currentNow = configDataStore.getLastCurrentNow()
        } else {
            configDataStore.saveLastCurrentNow(currentNow)
        }

        val currentAmps = abs(
            if (abs(currentNow) > 10000) currentNow / 1_000_000f
            else currentNow / 1_000f
        )

        // Update current samples for averaging
        currentSamples.add(currentAmps)
        currentSampleCounter++

        if (currentSampleCounter >= 10) {
            avgCurrentFor10Sec = currentSamples.average().toFloat()
            currentSamples.clear()
            currentSampleCounter = 0
        }

        val chargingPower = voltage * currentAmps

        // Reset min/max when charger is plugged in
        if (isCharging && !chargerConnected) {
            resetMinMaxPower()
            chargerConnected = true
        } else if (!isCharging) {
            chargerConnected = false
        }

        // Update min and max power
        if (minPower <= 0f || chargingPower < minPower) {
            minPower = max(0.1f, chargingPower)
            configDataStore.saveMinPower(minPower)
        }

        if (chargingPower > maxPower) {
            maxPower = chargingPower
            configDataStore.saveMaxPower(maxPower)
        }

        // Save charging power to database for graph
        if (isCharging && chargingPower > 0) {
            graphDataDao.insertChargingPower(
                GraphData(power = chargingPower, timestamp = System.currentTimeMillis())
            )
        }

        if (isCharging && chargingPower > 0) {
            val now = System.currentTimeMillis()
            if (now - lastDbInsertTime >= 5000L) { // 5 seconds
                graphDataDao.insertChargingPower(
                    GraphData(power = chargingPower, timestamp = now)
                )
                lastDbInsertTime = now
            }
        }

        // Calculate charging status and type
        val chargingType = when {
            !isCharging -> "Discharging"
            chargingPower > 20 -> "Super Fast Charging ⚡⚡⚡"
            chargingPower in 12.0..20.0 -> "Fast Charging ⚡⚡"
            chargingPower in 4.0..12.0 -> "Normal Charging ⚡"
            chargingPower < 4.0 -> "Slow Charging"
            else -> "Not Charging"
        }

        val sourceType = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> "(AC Charging)"
            BatteryManager.BATTERY_PLUGGED_USB -> "(USB Charging)"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "(Wireless Charging)"
            else -> ""
        }

        val chargingStatus = if (isCharging) "Charging: $chargingType $sourceType" else "Discharging"

        // Calculate time remaining
        val timeRemaining = calculateTimeRemaining(batteryPct, voltage, avgCurrentFor10Sec, isCharging)

        return BatteryData(
            batteryPercentage = batteryPct,
            voltage = voltage,
            currentAmps = currentAmps,
            temperature = temperature,
            isCharging = isCharging,
            chargingPower = chargingPower,
            chargingStatus = chargingStatus,
            chargingType = chargingType,
            sourceType = sourceType,
            minPower = minPower,
            maxPower = maxPower,
            avgPower = avgCurrentFor10Sec * voltage,
            timeRemaining = timeRemaining,
            pluggedType = plugged,
            timestamp = System.currentTimeMillis()
        )
    }

    private suspend fun resetMinMaxPower() {
        minPower = 0f
        maxPower = 0f
        configDataStore.resetMinMaxPower()
    }

    private suspend fun calculateTimeRemaining(
        batteryPct: Float,
        voltage: Float,
        avgCurrent: Float,
        isCharging: Boolean
    ): String {
        var chargeCounter = try {
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER).toFloat()
        } catch (e: Exception) {
            0f
        }

        if (chargeCounter <= 0 || batteryPct <= 0) {
            return "Calculating..."
        }

        if (chargeCounter == null || chargeCounter <= 0 || batteryPct <= 0) {
            return "Calculating..."
        }

        if (chargeCounter > 10000) {
            chargeCounter /= 1000f
        }

        val batteryCapacityMah = chargeCounter / (batteryPct / 100f)
        val batteryCapacityWh = (batteryCapacityMah * voltage) / 1000f
        val efficiencyFactor = 0.85f

        val avgPower = voltage * avgCurrent * efficiencyFactor
        if (avgPower <= 0) return "Calculating..."

        val timeRemainingMinutes = if (isCharging) {
            val remainingPercentage = (100f - batteryPct) / 100f
            ((remainingPercentage * batteryCapacityWh) / avgPower * 60).toInt()
        } else {
            val dischargePower = 4.0f
            ((batteryPct * batteryCapacityWh) / dischargePower).toInt()
        }

        return when {
            timeRemainingMinutes < 1 -> "Less than a minute"
            timeRemainingMinutes < 60 -> if (isCharging)
                "$timeRemainingMinutes min left"
            else
                "Discharging: $timeRemainingMinutes min left"
            else -> if (isCharging)
                "${timeRemainingMinutes / 60}h ${timeRemainingMinutes % 60}m left"
            else
                "Discharging: ${timeRemainingMinutes / 60}h ${timeRemainingMinutes % 60}m left"
        }
    }

    private fun broadcastWidgetUpdate() {
        val intent = Intent(BatteryWidgetProvider.ACTION_UPDATE_WIDGET).apply {
            setPackage(context.packageName)
        }
        context.sendBroadcast(intent)
    }

    // Graph data as Flow for reactive UI
    fun getGraphDataFlow(durationMillis: Long): Flow<List<GraphData>> {
        val cutoffTime = System.currentTimeMillis() - durationMillis
        return graphDataDao.getChargingPowerDataFlow(cutoffTime)
    }

    suspend fun clearGraphData() = withContext(Dispatchers.IO) {
        graphDataDao.clearChargingPowerData()
    }

    suspend fun isConfigured(): Boolean {
        return configDataStore.isConfigured()
    }

    suspend fun loadStoredPowerStats() {
        minPower = configDataStore.getMinPower()
        maxPower = configDataStore.getMaxPower()
    }

    // Config screen helper functions
    suspend fun getInitialConfigData(): ConfigData = withContext(Dispatchers.IO) {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        val androidVersion = Build.VERSION.RELEASE

        // Detect current unit
        val currentNow = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
            .toFloat()
        val unit = if (abs(currentNow) > 10000) "µA" else "mA"

        // Auto-detect battery capacity
        val detectedCapacity = getBatteryCapacity()

        ConfigData(
            manufacturer = manufacturer,
            model = model,
            androidVersion = androidVersion,
            currentUnit = unit,
            detectedCapacity = detectedCapacity
        )
    }

    suspend fun collectCurrentSample(): Float = withContext(Dispatchers.IO) {
        val currentNow = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
            .toFloat()
        abs(
            if (abs(currentNow) > 10000) currentNow / 1_000_000f
            else currentNow / 1_000f
        )
    }

    suspend fun saveConfiguration(capacity: Int, currentPattern: List<Float>) {
        configDataStore.saveBatteryCapacity(capacity)
        if (currentPattern.isNotEmpty()) {
            configDataStore.saveCurrentPattern(currentPattern)
        }
    }

    private fun getBatteryCapacity(): Int {
        return try {
            val powerProfileClass = Class.forName("com.android.internal.os.PowerProfile")
            val constructor = powerProfileClass.getConstructor(Context::class.java)
            val powerProfile = constructor.newInstance(context)
            val method = powerProfileClass.getMethod("getBatteryCapacity")
            (method.invoke(powerProfile) as Double).toInt()
        } catch (e: Exception) {
            0
        }
    }
}