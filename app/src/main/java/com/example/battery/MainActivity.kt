package com.example.battery

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.drawable.AnimationDrawable
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.airbnb.lottie.LottieAnimationView
import com.example.battery.notification.BatteryNotificationService
import com.example.battery.storage.BatteryDataStorage
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {

    private lateinit var chargingPowerText: TextView
    private lateinit var minPowerText: TextView
//    private lateinit var avgPowerText: TextView
    private lateinit var maxPowerText: TextView
    private lateinit var voltageText: TextView
    private lateinit var currentText: TextView
    private lateinit var batteryLevelText: TextView
    private lateinit var temperatureText: TextView
    private lateinit var chargingStatusText: TextView
    private lateinit var timeremainingText: TextView
    private lateinit var lineChart: LineChart
    private var minPower: Float = -1f
//    private var avgPower: Float = -1f
    private var maxPower: Float = -1f
    private val handler = Handler(Looper.getMainLooper())
    private val currentSamples = mutableListOf<Float>()
    private var avgCurrentFor10Sec = 0f
    private var currentSampleCounter = 0

    private lateinit var batteryDataStorage: BatteryDataStorage
    private val allEntries = mutableListOf<Entry>() // Stores graph data
    private val updateInterval = 1000L // 1 second update interval
    private var startTime = 0f // Used for x-axis normalization

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        batteryDataStorage = BatteryDataStorage(this)

        if (!batteryDataStorage.isConfigured()) {
            startActivity(Intent(this, ConfigActivity::class.java))
            finish()
            return
        }

        val chargingAnimationView = findViewById<LottieAnimationView>(R.id.charging_animation)

        val batteryStatus = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        val animationFile = if (isCharging) "charging_bolt_green.json" else "charging_bolt_red.json"
        chargingAnimationView.setAnimation(animationFile)
        chargingAnimationView.playAnimation()

        registerReceiver(powerReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        val layout = findViewById<LinearLayout>(R.id.root_layout) // use your actual layout ID
        val animationDrawable = layout.background as? AnimationDrawable
        animationDrawable?.apply {
            setEnterFadeDuration(1000)
            setExitFadeDuration(1000)
            start()
        }

        // Initialize UI and chart
        initViews()
        setupChart()

        // Start monitoring
        startBatteryService()
        requestNotificationPermission()
        startBatteryUpdates()

        Log.d("MainActivityN", "Launched via notification: ${intent.action}")

    }

    private val powerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

            val animationFile = if (isCharging) "charging_bolt_green.json" else "charging_bolt_red.json"
            findViewById<LottieAnimationView>(R.id.charging_animation).apply {
                setAnimation(animationFile)
                playAnimation()
            }
        }
    }

    private fun startBatteryService() {
        val intent = Intent(this, BatteryNotificationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateRunnable)
        try {
            unregisterReceiver(powerReceiver)
        } catch (e: IllegalArgumentException) {
            Log.w("MainActivity", "Receiver was not registered: ${e.message}")
        }
    }

    private fun initViews() {
        chargingPowerText = findViewById(R.id.tv_charging_power)
        minPowerText = findViewById(R.id.tv_min_power_value)
        maxPowerText = findViewById(R.id.tv_max_power_value)
        voltageText = findViewById(R.id.tv_voltage)
        currentText = findViewById(R.id.tv_current)
        batteryLevelText = findViewById(R.id.tv_battery_level)
        temperatureText = findViewById(R.id.tv_battery_temperature)
        chargingStatusText = findViewById(R.id.tv_charging_status)
        lineChart = findViewById(R.id.line_chart)
        timeremainingText = findViewById(R.id.tv_time_remaining)
    }

    private fun startBatteryUpdates() {
        startTime = System.currentTimeMillis() / 1000f // Set the start time
        handler.post(updateRunnable)
    }

    private val updateRunnable = object : Runnable {
        override fun run() {
            registerBatteryReceiver()
            handler.postDelayed(this, updateInterval)
        }
    }

    private fun registerBatteryReceiver() {
        registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))?.let { updateBatteryInfo(it) }
    }

    private var chargerConnected = false

    private fun updateBatteryInfo(intent: Intent) {
        val rawVoltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0).toFloat()
        val voltage = if (rawVoltage > 100) rawVoltage / 1000f else rawVoltage
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val batteryPct = if (scale > 0) (level * 100 / scale.toFloat()) else 0f
        val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f

        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING

        // Fetch current in Amps
        val currentNow = (getSystemService(BATTERY_SERVICE) as? BatteryManager)
            ?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)?.toFloat()?.let {
                abs(it / if (abs(it) > 10000) 1_000_000f else 1_000f)
            } ?: batteryDataStorage.getLastCurrentNow().toFloat()

        // Store current samples
        currentSamples.add(currentNow)
        currentSampleCounter++

        if (currentSampleCounter >= 10) {
            avgCurrentFor10Sec = currentSamples.average().toFloat()
            currentSamples.clear()
            currentSampleCounter = 0
        }

        val chargingPower = voltage * currentNow

        // Reset min/max when charger is plugged in
        if (isCharging && !chargerConnected) {
            batteryDataStorage.resetMinMaxPower()
            chargerConnected = true
            minPower = 0.0f
            maxPower = 0.0f
            batteryDataStorage.saveMinPower(minPower)
            batteryDataStorage.saveMaxPower(maxPower)
        } else if (!isCharging) {
            chargerConnected = false
        }

        // Update min and max normally
        if (minPower == 0.0f || chargingPower < minPower) {
            minPower = max(0.1f, chargingPower)
            batteryDataStorage.saveMinPower(minPower)
        }

        if (chargingPower > maxPower) {
            maxPower = chargingPower
            batteryDataStorage.saveMaxPower(maxPower)
        }

        // ✅ Update UI
        chargingPowerText.text = "%.1f W".format(chargingPower)
        minPowerText.text = "%.1f W".format(minPower)
        maxPowerText.text = "%.1f W".format(maxPower)
        voltageText.text = "Voltage: %.1fV".format(voltage)
        currentText.text = "Current: %.3fA".format(currentNow)
        batteryLevelText.text = "Battery Level: %.1f%%".format(batteryPct)
        temperatureText.text = "Temp: %.1f°C".format(temperature)
        chargingStatusText.text = getChargingStatus(chargingPower, intent)

        // ✅ Update Time Remaining (optional)
         timeremainingText.text = calculateTimeRemaining(batteryPct, voltage, avgCurrentFor10Sec, isCharging)
        updateChart(chargingPower)
    }

    private fun getChargingStatus(chargingPower: Float, intent: Intent): String {
        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val isCharging = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1) in
                listOf(BatteryManager.BATTERY_STATUS_CHARGING, BatteryManager.BATTERY_STATUS_FULL)

        if (!isCharging) return "Charging: Discharging"

        val chargingType = when {
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

        return "Charging: $chargingType $sourceType"
    }
    private fun calculateTimeRemaining(
        batteryPct: Float, voltage: Float, avgCurrent: Float, isCharging: Boolean
    ): String {

        val batteryManager = getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        var chargeCounter =
            batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
                ?.toFloat()

        if (chargeCounter == null || chargeCounter <= 0) return "Calculating..."

        if (chargeCounter > 10000) {
            chargeCounter /= 1000f
        }

        if (batteryPct <= 0) return "Calculating..."

        val batteryCapacityMah = chargeCounter / (batteryPct / 100f)
        val batteryCapacityWh = (batteryCapacityMah * voltage) / 1000f

        val efficiencyFactor = 0.85f

        // ✅ **Compute average power over last 10 seconds**
        val avgPower = voltage * avgCurrent * efficiencyFactor
        if (avgPower <= 0) return "Calculating..."

        val remainingPercentage = (100f - batteryPct) / 100f
        val timeRemainingMinutes = if (isCharging) {
            // 🔌 **Charging Time Calculation**
            ((remainingPercentage * batteryCapacityWh) / avgPower * 60).toInt()
        } else {
            // 🔋 **Discharging Time Calculation**
            val dischargePower = 4.0f // Approximate average phone power usage (adjustable)
            ((batteryPct * batteryCapacityWh) / (dischargePower)).toInt()
        }

        return when {
            timeRemainingMinutes < 1 -> "Less than a minute"
            timeRemainingMinutes < 60 -> if (isCharging) "$timeRemainingMinutes min left" else "Discharging: $timeRemainingMinutes min left"
            else -> if (isCharging) "${timeRemainingMinutes / 60}h ${timeRemainingMinutes % 60}m left"
            else "Discharging: ${timeRemainingMinutes / 60}h ${timeRemainingMinutes % 60}m left"
        }

    }

    private fun setupChart() {
        lineChart.apply {
            description.isEnabled = false
            setTouchEnabled(false) // No manual interaction
            isDragEnabled = false
            setScaleEnabled(false)
            setDrawGridBackground(false)
            legend.isEnabled = false
            axisRight.isEnabled = false

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 10f // Labels every 10 seconds
                axisMinimum = 0f
                axisMaximum = 120f // Fixed 120s window
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return "${value.toInt()}s"
                    }
                }
            }

            axisLeft.apply {
                axisMinimum = 0f
                setDrawGridLines(true)
            }
        }
    }

    private fun updateChart(value: Float) {
        handler.post {
            val maxEntries = 120
            if (allEntries.size < maxEntries) {
                allEntries.add(Entry(allEntries.size.toFloat(), value))
            } else {
                for (i in 1 until allEntries.size) {
                    allEntries[i - 1] = Entry(allEntries[i].x - 1f, allEntries[i].y)
                }
                allEntries[allEntries.size - 1] = Entry(120f, value)
            }

            val lineColor = when {
                value > 20f -> android.graphics.Color.RED
                value in 12.0f..20.0f -> android.graphics.Color.MAGENTA
                value in 4.0f..12.0f -> android.graphics.Color.GREEN
                else -> android.graphics.Color.BLUE
            }

            val set = LineDataSet(allEntries, "Power Usage").apply {
                setDrawCircles(false)
                setDrawValues(false)
                mode = LineDataSet.Mode.CUBIC_BEZIER
                lineWidth = 2.5f
                color = lineColor
            }

            lineChart.data = LineData(set)
            lineChart.notifyDataSetChanged()
            lineChart.invalidate()
        }
    }
}
