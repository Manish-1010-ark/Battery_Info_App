package com.example.battery.data.repository

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.util.Log
import com.example.battery.data.datastore.ConfigDataStore
import com.example.battery.data.db.ChargingPowerDao
import com.example.battery.data.db.DischargingPowerDao
import com.example.battery.data.db.TemperatureDao
import com.example.battery.data.db.ChargingPower
import com.example.battery.data.db.DischargingPower
import com.example.battery.data.db.TemperatureSample
import com.example.battery.data.model.BatteryData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

data class ConfigData(
    val manufacturer: String,
    val model: String,
    val androidVersion: String,
    val currentUnit: String,
    val detectedCapacity: Int,
    val currentSamples: List<Float> = emptyList()
)

data class TimeRemainingResult(
    val chargingModeText: String?,
    val dischargingOnText: String?,
    val dischargingOffText: String?,
    val state: TimeState
)

enum class TimeState {
    FULL, CALCULATING, STABILIZING, FINISHING, NORMAL
}

enum class TimeRange(val milliseconds: Long) {
    MIN_5(5 * 60 * 1000L),
    HOUR_1(60 * 60 * 1000L),
    DAY_1(24 * 60 * 60 * 1000L)
}

@Singleton
class BatteryRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val chargingPowerDao: ChargingPowerDao,
    private val dischargingPowerDao: DischargingPowerDao,
    private val temperatureDao: TemperatureDao,
    private val configDataStore: ConfigDataStore
) {
    private val batteryManager: BatteryManager by lazy {
        context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    }

    private val _batteryDataFlow = MutableStateFlow(BatteryData.EMPTY)
    val batteryDataFlow: StateFlow<BatteryData> = _batteryDataFlow.asStateFlow()

    private var chargerConnected = false
    private var lastChargingInsertTime = 0L
    private var lastDischargingInsertTime = 0L
    private var lastTemperatureInsertTime = 0L
    private var sampleCounter = 0
    private var isScreenOn: Boolean = true
    private var screenStateRegistered = false

    private var emaCurrentGeneral: Float = 0f
    private var emaCurrentScreenOn: Float = 0f
    private var emaCurrentScreenOff: Float = 0f
    private var emaInitialized = false
    private var stabilizationCounter = 0

    private val EMA_ALPHA = 0.20f
    private val STABILIZATION_SAMPLES = 5

    // BATCH 6: Analytics sampling intervals (UPDATED)
    private val CHARGING_SAMPLE_INTERVAL = 1_000L      // 1 second (was 5 seconds)
    private val DISCHARGING_SAMPLE_INTERVAL = 1_000L   // 1 second (was 15 seconds)
    private val TEMPERATURE_SAMPLE_INTERVAL = 10_000L  // 10 seconds (was 15 seconds)

    // Cleanup interval - every 30 minutes
    private val CLEANUP_INTERVAL = 30 * 60 * 1000L
    private var lastCleanupTime = 0L

    // Repository scope for background operations
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        registerScreenStateReceiver()
        schedulePeriodicCleanup()
    }

    /**
     * SECTION A - Calculate power from voltage and current
     */
    private fun calculatePower(voltage: Float, currentA: Float): Float {
        return voltage * currentA
    }

    private fun registerScreenStateReceiver() {
        if (screenStateRegistered) return

        try {
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    when (intent?.action) {
                        Intent.ACTION_SCREEN_ON -> {
                            isScreenOn = true
                            Log.d(TAG, "📱 Screen ON")
                        }
                        Intent.ACTION_SCREEN_OFF -> {
                            isScreenOn = false
                            Log.d(TAG, "📱 Screen OFF")
                        }
                    }
                }
            }

            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_SCREEN_OFF)
            }

            context.registerReceiver(receiver, filter)
            screenStateRegistered = true
            Log.d(TAG, "✅ Screen state receiver registered")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to register screen state receiver", e)
        }
    }

    /**
     * SECTION B - Schedule periodic cleanup with isActive check
     */
    private fun schedulePeriodicCleanup() {
        repositoryScope.launch {
            while (isActive) {
                kotlinx.coroutines.delay(CLEANUP_INTERVAL)
                cleanupOldData()
            }
        }
    }

    /**
     * Clean up database entries older than 24 hours
     */
    private suspend fun cleanupOldData() {
        val now = System.currentTimeMillis()
        if (now - lastCleanupTime < CLEANUP_INTERVAL) return

        try {
            val cutoffTime = now - TimeRange.DAY_1.milliseconds

            chargingPowerDao.deleteOlderThan(cutoffTime)
            dischargingPowerDao.deleteOlderThan(cutoffTime)
            temperatureDao.deleteOlderThan(cutoffTime)

            lastCleanupTime = now
            Log.d(TAG, "🧹 Cleaned up analytics data older than 24 hours")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to cleanup old data", e)
        }
    }

    suspend fun updateCurrentBatteryData() = withContext(Dispatchers.IO) {
        try {
            val batteryData = getCurrentBatteryData()
            sampleCounter++

            if (batteryData.isCharging != _batteryDataFlow.value.isCharging || sampleCounter % 5 == 0) {
                Log.d(TAG, "🔌 [Update $sampleCounter] CHARGING: ${batteryData.isCharging}, " +
                        "power=${batteryData.chargingPower}W, " +
                        "current=${batteryData.currentAmps}A, " +
                        "voltage=${batteryData.voltage}V")
            }

            _batteryDataFlow.value = batteryData

            // Log analytics data asynchronously
            logAnalyticsData(batteryData)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error updating battery data", e)
        }
    }

    /**
     * Log analytics data (charging/discharging power + temperature)
     * BATCH 6: Updated intervals - 1s for power, 10s for temperature
     */
    private fun logAnalyticsData(batteryData: BatteryData) {
        repositoryScope.launch {
            try {
                val now = System.currentTimeMillis()

                // 1. Log charging power (every 1 second - UPDATED)
                if (batteryData.isCharging &&
                    batteryData.chargingPower > 0 &&
                    now - lastChargingInsertTime >= CHARGING_SAMPLE_INTERVAL
                ) {
                    chargingPowerDao.insert(
                        ChargingPower(
                            power = batteryData.chargingPower,
                            timestamp = now
                        )
                    )
                    lastChargingInsertTime = now
                }

                // 2. Log discharging power (every 1 second - UPDATED)
                if (!batteryData.isCharging &&
                    now - lastDischargingInsertTime >= DISCHARGING_SAMPLE_INTERVAL
                ) {
                    val dischargingPower = calculatePower(batteryData.voltage, batteryData.currentAmps)

                    if (dischargingPower > 0) {
                        dischargingPowerDao.insert(
                            DischargingPower(
                                power = dischargingPower,
                                timestamp = now
                            )
                        )
                        lastDischargingInsertTime = now

                        if (sampleCounter % 3 == 0) {
                            Log.d(TAG, "📉 Discharging: ${dischargingPower}W " +
                                    "(${batteryData.voltage}V × ${batteryData.currentAmps}A)")
                        }
                    }
                }

                // 3. Log temperature (every 10 seconds - UPDATED from 15s)
                if (now - lastTemperatureInsertTime >= TEMPERATURE_SAMPLE_INTERVAL) {
                    temperatureDao.insert(
                        TemperatureSample(
                            temp = batteryData.temperature,
                            timestamp = now
                        )
                    )
                    lastTemperatureInsertTime = now

                    if (sampleCounter % 3 == 0) {
                        Log.d(TAG, "🌡️ Temperature: ${batteryData.temperature}°C")
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to log analytics data", e)
            }
        }
    }

    private suspend fun getCurrentBatteryData(): BatteryData {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            ?: return BatteryData.EMPTY

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

        val healthCode = batteryIntent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
        val batteryHealth = getBatteryHealthStatus(healthCode)

        val chemistry = batteryIntent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"

        val cycleCount = if (Build.VERSION.SDK_INT >= 34) {
            val rawCycleCount = batteryIntent.getIntExtra("android.os.extra.CYCLE_COUNT", 0)
            Log.d(TAG, "🔄 Raw android.os.extra.CYCLE_COUNT = $rawCycleCount")
            rawCycleCount
        } else {
            Log.d(TAG, "🔄 Cycle Count not supported (SDK < 34)")
            0
        }

        val now = System.currentTimeMillis()

        var currentNow = try {
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get current, using cached value", e)
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

        updateEmaCurrents(currentAmps, isCharging)

        val chargingPower = calculatePower(voltage, currentAmps)

        if (isCharging && !chargerConnected) {
            chargerConnected = true
        } else if (!isCharging) {
            chargerConnected = false
        }

        val chargingType = when {
            !isCharging -> "Discharging"
            chargingPower >= 40.0f -> "Hyper Charging 🚀"
            chargingPower >= 20.0f -> "Super Fast Charging ⚡⚡"
            chargingPower >= 10.0f -> "Fast Charging ⚡"
            chargingPower >= 5.0f -> "Normal Charging"
            chargingPower > 0.1f -> "Trickle Charging ⏳"
            else -> "Connected / Idle"
        }

        val sourceType = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> "(AC Charging)"
            BatteryManager.BATTERY_PLUGGED_USB -> "(USB Charging)"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "(Wireless Charging)"
            else -> ""
        }

        val chargingStatus = if (isCharging) "Charging: $chargingType $sourceType" else "Discharging"

        val (thermalStatus, thermalLevel) = getThermalStatus(temperature)

        val designCapacityMah = getBatteryCapacityFromAPI()

        val timeResult = computeTimeRemaining(
            batteryPct,
            currentAmps,
            designCapacityMah,
            isCharging,
            isScreenOn
        )

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
            timeRemainingCharging = timeResult.chargingModeText,
            timeRemainingOnScreen = timeResult.dischargingOnText,
            timeRemainingOffScreen = timeResult.dischargingOffText,
            timeRemainingState = timeResult.state.name,
            pluggedType = plugged,
            thermalStatus = thermalStatus,
            thermalLevel = thermalLevel,
            batteryHealth = batteryHealth,
            designCapacityMah = designCapacityMah,
            cycleCount = cycleCount,
            chemistry = chemistry,
            timestamp = now
        )
    }

    private fun updateEmaCurrents(currentAmps: Float, isCharging: Boolean) {
        if (!emaInitialized) {
            emaCurrentGeneral = currentAmps
            emaCurrentScreenOn = currentAmps
            emaCurrentScreenOff = currentAmps
            emaInitialized = true
            stabilizationCounter = 0
            return
        }

        if (stabilizationCounter < STABILIZATION_SAMPLES) {
            stabilizationCounter++
        }

        emaCurrentGeneral = EMA_ALPHA * currentAmps + (1 - EMA_ALPHA) * emaCurrentGeneral

        if (!isCharging) {
            if (isScreenOn) {
                emaCurrentScreenOn = EMA_ALPHA * currentAmps + (1 - EMA_ALPHA) * emaCurrentScreenOn
            } else {
                emaCurrentScreenOff = EMA_ALPHA * currentAmps + (1 - EMA_ALPHA) * emaCurrentScreenOff
            }
        }
    }

    private fun computeTimeRemaining(
        percentage: Float,
        currentNowA: Float,
        designCapacityMah: Int,
        isCharging: Boolean,
        screenOn: Boolean
    ): TimeRemainingResult {
        if (percentage >= 100f) {
            return TimeRemainingResult(
                chargingModeText = null,
                dischargingOnText = null,
                dischargingOffText = null,
                state = TimeState.FULL
            )
        }

        if (stabilizationCounter < STABILIZATION_SAMPLES) {
            return TimeRemainingResult(
                chargingModeText = "Stabilizing…",
                dischargingOnText = "Stabilizing…",
                dischargingOffText = "Stabilizing…",
                state = TimeState.STABILIZING
            )
        }

        if (emaCurrentGeneral < 0.05f) {
            return TimeRemainingResult(
                chargingModeText = "Calculating…",
                dischargingOnText = "Calculating…",
                dischargingOffText = "Calculating…",
                state = TimeState.CALCULATING
            )
        }

        if (designCapacityMah <= 0) {
            return TimeRemainingResult(
                chargingModeText = "Calculating…",
                dischargingOnText = "Calculating…",
                dischargingOffText = "Calculating…",
                state = TimeState.CALCULATING
            )
        }

        if (isCharging) {
            if (emaCurrentGeneral < 0.1f && percentage > 98f) {
                return TimeRemainingResult(
                    chargingModeText = "Finishing…",
                    dischargingOnText = null,
                    dischargingOffText = null,
                    state = TimeState.FINISHING
                )
            }

            val remainingToFullMah = designCapacityMah * (1 - percentage / 100f)
            val timeHours = remainingToFullMah / (emaCurrentGeneral * 1000f)
            val timeMinutes = (timeHours * 60).toInt()

            val chargingText = formatTimeRemaining(timeMinutes, true)

            return TimeRemainingResult(
                chargingModeText = chargingText,
                dischargingOnText = null,
                dischargingOffText = null,
                state = TimeState.NORMAL
            )
        } else {
            val remainingMah = designCapacityMah * (percentage / 100f)

            val onHours = if (emaCurrentScreenOn > 0.05f) {
                remainingMah / (emaCurrentScreenOn * 1000f)
            } else {
                0f
            }

            val offHours = if (emaCurrentScreenOff > 0.05f) {
                remainingMah / (emaCurrentScreenOff * 1000f)
            } else {
                0f
            }

            val onMinutes = (onHours * 60).toInt()
            val offMinutes = (offHours * 60).toInt()

            val onText = if (onMinutes > 0) formatTimeRemaining(onMinutes, false) else null
            val offText = if (offMinutes > 0) formatTimeRemaining(offMinutes, false) else null

            return TimeRemainingResult(
                chargingModeText = null,
                dischargingOnText = onText,
                dischargingOffText = offText,
                state = TimeState.NORMAL
            )
        }
    }

    private fun formatTimeRemaining(minutes: Int, isCharging: Boolean): String {
        return when {
            minutes < 1 -> if (isCharging) "Less than a minute" else "Less than a minute"
            minutes < 60 -> {
                if (isCharging) "$minutes min to full charge" else "$minutes min"
            }
            else -> {
                val hours = minutes / 60
                val mins = minutes % 60
                if (isCharging) {
                    if (mins == 0) "$hours hour${if (hours != 1) "s" else ""} to full charge"
                    else "$hours hour${if (hours != 1) "s" else ""} $mins min to full charge"
                } else {
                    if (mins == 0) "$hours hour${if (hours != 1) "s" else ""}"
                    else "$hours hour${if (hours != 1) "s" else ""} $mins min"
                }
            }
        }
    }

    private fun getThermalStatus(temperature: Float): Pair<String, Int> {
        return when {
            temperature < 22f -> "Cool" to 0
            temperature < 35f -> "Normal" to 1
            temperature < 42f -> "Warm" to 2
            temperature < 48f -> "Hot" to 3
            else -> "Critical" to 4
        }
    }

    private fun getBatteryHealthStatus(healthCode: Int): String {
        return when (healthCode) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheating"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Unspecified Failure"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            else -> "Unknown"
        }
    }

    private fun getBatteryCapacityFromAPI(): Int {
        return try {
            val powerProfileClass = Class.forName("com.android.internal.os.PowerProfile")
            val constructor = powerProfileClass.getConstructor(Context::class.java)
            val powerProfile = constructor.newInstance(context)
            val method = powerProfileClass.getMethod("getBatteryCapacity")
            (method.invoke(powerProfile) as Double).toInt()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get battery capacity from API", e)
            0
        }
    }

    // ====== ANALYTICS DATA ACCESS METHODS ======

    /**
     * Get charging power data flow for analytics
     */
    fun getChargingPowerFlow(cutoffTime: Long): Flow<List<ChargingPower>> {
        return chargingPowerDao.getFlow(cutoffTime)
    }

    /**
     * Get discharging power data flow for analytics
     */
    fun getDischargingPowerFlow(cutoffTime: Long): Flow<List<DischargingPower>> {
        return dischargingPowerDao.getFlow(cutoffTime)
    }

    /**
     * Get temperature data flow for analytics
     */
    fun getTemperatureFlow(cutoffTime: Long): Flow<List<TemperatureSample>> {
        return temperatureDao.getFlow(cutoffTime)
    }

    // ====== CONFIGURATION METHODS ======

    suspend fun isConfigured(): Boolean {
        return try {
            configDataStore.isConfigured()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check config status", e)
            false
        }
    }

    suspend fun loadStoredPowerStats() {
        Log.d(TAG, "📊 Power stats loading skipped (min/max removed)")
    }

    suspend fun getInitialConfigData(): ConfigData = withContext(Dispatchers.IO) {
        val currentNow = try {
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW).toFloat()
        } catch (e: Exception) {
            0f
        }

        ConfigData(
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            androidVersion = Build.VERSION.RELEASE,
            currentUnit = if (abs(currentNow) > 10000) "µA" else "mA",
            detectedCapacity = getBatteryCapacityFromAPI()
        )
    }

    suspend fun collectCurrentSample(): Float = withContext(Dispatchers.IO) {
        val currentNow = try {
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW).toFloat()
        } catch (e: Exception) {
            0f
        }

        abs(if (abs(currentNow) > 10000) currentNow / 1_000_000f else currentNow / 1_000f)
    }

    suspend fun saveConfiguration(capacity: Int, currentPattern: List<Float>) {
        try {
            configDataStore.saveBatteryCapacity(capacity)
            if (currentPattern.isNotEmpty()) {
                configDataStore.saveCurrentPattern(currentPattern)
            }
            Log.d(TAG, "✅ Configuration saved: capacity=$capacity mAh")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to save configuration", e)
            throw e
        }
    }

    companion object {
        private const val TAG = "BatteryRepository"
    }
}