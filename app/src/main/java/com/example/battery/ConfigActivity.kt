package com.example.battery

import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.battery.storage.BatteryDataStorage
import kotlin.math.abs

class ConfigActivity : AppCompatActivity() {

    private lateinit var batteryStorage: BatteryDataStorage

    private lateinit var deviceInfoText: TextView
    private lateinit var unitText: TextView
    private lateinit var currentPatternText: TextView
    private lateinit var capacityInput: EditText
    private lateinit var instructionsText: TextView
    private lateinit var configureButton: Button

    private val collectedCurrentValues = mutableListOf<Float>()
    private var batteryManager: BatteryManager? = null
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config)

        batteryStorage = BatteryDataStorage(this)

        deviceInfoText = findViewById(R.id.tv_device_info)
        unitText = findViewById(R.id.tv_unit)
        currentPatternText = findViewById(R.id.tv_current_pattern)
        capacityInput = findViewById(R.id.et_capacity)
        instructionsText = findViewById(R.id.tv_instructions)
        configureButton = findViewById(R.id.btn_configure)

        loadData()
        collectCurrentValues()

        configureButton.setOnClickListener {
            saveData()
            finish()
        }
    }

    private fun loadData() {
        // Device Info
        val deviceName = "${Build.MANUFACTURER} ${Build.MODEL}"
        val androidVersion = "Android ${Build.VERSION.RELEASE}"
        deviceInfoText.text = "Device: $deviceName\n$androidVersion"

        // Detect Current Unit
        val currentNow = getBatteryCurrent()
        val unit = if (currentNow > 10) "mA" else "A"
        unitText.text = "Current Unit: $unit"

        // Show Last 5 Current Readings
        val currentList = batteryStorage.getCurrentPattern()
        if (currentList.isNotEmpty()) {
            currentPatternText.text = "Current Pattern: ${currentList.joinToString(", ")}"
            configureButton.isEnabled = true // Enable button if data is present
        } else {
            currentPatternText.text = "Current Pattern: Collecting..."
            configureButton.isEnabled = false // Disable button while collecting
        }

        // 🔹 Get Battery Capacity using PowerProfile
        val detectedCapacity = getBatteryCapacity()
        val savedCapacity = batteryStorage.getBatteryCapacity()

        // 🔹 Use the best available battery capacity
        when {
            savedCapacity > 0 -> {
                // If stored capacity is available, use it
                capacityInput.setText(savedCapacity.toString())
            }
            detectedCapacity > 0 -> {
                // If detected capacity is available, use it
                capacityInput.setText(detectedCapacity.toInt().toString())
            }
            else -> {
                // If no data is available, leave input empty
                capacityInput.setText("")
            }
        }

        // Instructions
        instructionsText.text = "Please wait for data collection. Ensure the device is charging."
    }


    private fun getBatteryCapacity(): Double {
        return try {
            val powerProfileClass = Class.forName("com.android.internal.os.PowerProfile")
            val constructor = powerProfileClass.getConstructor(Context::class.java)
            val powerProfile = constructor.newInstance(this)

            val method = powerProfileClass.getMethod("getBatteryCapacity")
            method.invoke(powerProfile) as Double
        } catch (e: Exception) {
            e.printStackTrace()
            -1.0
        }
    }

    private fun collectCurrentValues() {
        batteryManager = getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        collectedCurrentValues.clear()

        val runnable = object : Runnable {
            override fun run() {
                val currentNow = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)?.toFloat()
                if (currentNow != null) {
                    val normalizedCurrent = abs(currentNow / if (abs(currentNow) > 10000) 1_000_000f else 1_000f)
                    collectedCurrentValues.add(normalizedCurrent)
                }

                if (collectedCurrentValues.size < 5) {
                    handler.postDelayed(this, 1000) // Collect every second
                } else {
                    showCollectedCurrent()
                }
            }
        }

        handler.post(runnable)
    }

    private fun showCollectedCurrent() {
        runOnUiThread {
            currentPatternText.text = "Current Pattern: ${collectedCurrentValues.joinToString(", ") { "%.3fA".format(it) }}"
            configureButton.isEnabled = collectedCurrentValues.isNotEmpty()
        }
    }

    private fun saveData() {
        val capacity = capacityInput.text.toString().toIntOrNull()
        if (capacity != null) {
            batteryStorage.saveBatteryCapacity(capacity)
        }

        // 🔹 Save collected current values
        if (collectedCurrentValues.isNotEmpty()) {
            batteryStorage.saveCurrentPattern(collectedCurrentValues)
        }

        // 🔹 Open MainActivity after saving data
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }


    private fun getBatteryCurrent(): Float {
        val batteryManager = getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        return batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)?.toFloat()?.let {
            abs(it / if (abs(it) > 10000) 1_000_000f else 1_000f)
        } ?: 0f
    }
}
