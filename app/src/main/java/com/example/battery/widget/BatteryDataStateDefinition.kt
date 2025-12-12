package com.example.battery.widget

import android.content.Context
import android.util.Log
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStoreFile
import androidx.glance.state.GlanceStateDefinition
import com.example.battery.data.model.BatteryData
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.io.OutputStream

/**
 * GlanceStateDefinition for BatteryData
 *
 * This defines how Glance widgets store and retrieve battery data.
 * Each widget instance gets its own DataStore file managed by Glance.
 */
object BatteryDataStateDefinition : GlanceStateDefinition<BatteryData> {

    private const val DATA_STORE_FILENAME = "battery_widget_data"
    private const val TAG = "BatteryDataState"

    override suspend fun getDataStore(context: Context, fileKey: String): DataStore<BatteryData> {
        Log.d(TAG, "getDataStore called for fileKey: $fileKey")
        return context.getBatteryDataStore(fileKey)
    }

    override fun getLocation(context: Context, fileKey: String): File {
        val file = context.dataStoreFile("$DATA_STORE_FILENAME-$fileKey")
        Log.d(TAG, "DataStore location: ${file.absolutePath}")
        return file
    }

    object BatteryDataSerializer : Serializer<BatteryData> {
        override val defaultValue: BatteryData
            get() {
                Log.d(TAG, "Returning default BatteryData.EMPTY")
                return BatteryData.EMPTY
            }

        override suspend fun readFrom(input: InputStream): BatteryData {
            return try {
                val jsonString = input.readBytes().decodeToString()
                Log.d(TAG, "📖 Reading from DataStore, length: ${jsonString.length}")

                if (jsonString.isEmpty()) {
                    Log.d(TAG, "⚠️ Empty DataStore, returning default")
                    return defaultValue
                }

                val json = JSONObject(jsonString)
                val batteryData = BatteryData(
                    timestamp = json.optLong("timestamp", 0L),
                    batteryPercentage = json.optDouble("batteryPercentage", 0.0).toFloat(),
                    voltage = json.optDouble("voltage", 0.0).toFloat(),
                    currentAmps = json.optDouble("current", 0.0).toFloat(),
                    temperature = json.optDouble("temperature", 0.0).toFloat(),
                    chargingPower = json.optDouble("chargingPower", 0.0).toFloat(),
                    isCharging = json.optBoolean("isCharging", false),
                    chargingType = json.optString("chargingType", "Unknown"),
                    sourceType = json.optString("sourceType", ""),
                    timeRemainingCharging = json.optString("timeRemaining", "Calculating..."),
                    chargingStatus = json.optString("chargingStatus", "Unknown"),
//                    minPower = json.optDouble("minPower", 0.0).toFloat(),
//                    maxPower = json.optDouble("maxPower", 0.0).toFloat(),
//                    avgPower = json.optDouble("avgPower", 0.0).toFloat(),
                    pluggedType = json.optInt("pluggedType", -1)
                )

                Log.d(TAG, "✅ Successfully read: ${batteryData.batteryPercentage}%, ${batteryData.chargingPower}W, timestamp: ${batteryData.timestamp}")
                batteryData

            } catch (exception: Exception) {
                Log.e(TAG, "❌ Error reading battery data", exception)
                throw CorruptionException("Cannot read battery data", exception)
            }
        }

        override suspend fun writeTo(t: BatteryData, output: OutputStream) {
            try {
                Log.d(TAG, "💾 Writing to DataStore: ${t.batteryPercentage}%, ${t.chargingPower}W, timestamp: ${t.timestamp}")

                val json = JSONObject().apply {
                    put("timestamp", t.timestamp)
                    put("batteryPercentage", t.batteryPercentage.toDouble())
                    put("voltage", t.voltage.toDouble())
                    put("current", t.currentAmps.toDouble())
                    put("temperature", t.temperature.toDouble())
                    put("chargingPower", t.chargingPower.toDouble())
                    put("isCharging", t.isCharging)
                    put("chargingType", t.chargingType)
                    put("sourceType", t.sourceType)
                    put("timeRemaining", t.timeRemainingCharging)
                    put("chargingStatus", t.chargingStatus)
//                    put("minPower", t.minPower.toDouble())
//                    put("maxPower", t.maxPower.toDouble())
//                    put("avgPower", t.avgPower.toDouble())
                    put("pluggedType", t.pluggedType)
                }

                val jsonString = json.toString()
                output.write(jsonString.encodeToByteArray())
                Log.d(TAG, "✅ Successfully wrote ${jsonString.length} bytes to DataStore")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error writing battery data", e)
                throw e
            }
        }
    }
}

private fun Context.getBatteryDataStore(fileKey: String): DataStore<BatteryData> {
    Log.d("BatteryDataState", "Creating DataStore for fileKey: $fileKey")
    return androidx.datastore.core.DataStoreFactory.create(
        serializer = BatteryDataStateDefinition.BatteryDataSerializer,
        produceFile = {
            val file = this.dataStoreFile("battery_widget_data-$fileKey")
            Log.d("BatteryDataState", "DataStore file: ${file.absolutePath}")
            file
        }
    )
}