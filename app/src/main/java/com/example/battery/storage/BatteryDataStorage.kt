package com.example.battery.storage

import android.content.Context
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues

class BatteryDataStorage(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "BatteryConfig"
        private const val KEY_BATTERY_CAPACITY = "BatteryCapacity"
        private const val KEY_CURRENT_PATTERN = "CurrentPattern"
        private const val KEY_MIN_POWER = "MinPower"
        private const val KEY_MAX_POWER = "MaxPower"
        private const val KEY_AVG_POWER = "AvgPower"
        private const val KEY_LAST_CURRENT_NOW = "LastCurrentNow"
        private const val KEY_VOLTAGE = "Voltage"
        private const val KEY_TEMPERATURE = "Temperature"

        private const val DATABASE_NAME = "BatteryData.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_CHARGING_POWER = "ChargingPower"
        private const val COLUMN_ID = "id"
        private const val COLUMN_POWER = "power"
        private const val COLUMN_TIMESTAMP = "timestamp"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_CHARGING_POWER (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, 
                $COLUMN_POWER REAL, 
                $COLUMN_TIMESTAMP INTEGER
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CHARGING_POWER")
        onCreate(db)
    }

    fun resetMinMaxPower() {
        with(sharedPreferences.edit()) {
            putFloat(KEY_MIN_POWER, -1f)
            putFloat(KEY_MAX_POWER, -1f)
        }
    }


    fun saveChargingPower(power: Float) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_POWER, power)
            put(COLUMN_TIMESTAMP, System.currentTimeMillis())
        }
        db.insert(TABLE_CHARGING_POWER, null, values)
        db.close()
    }

    fun getChargingPowerData(duration: Long): List<Pair<Long, Float>> {
        val db = readableDatabase
        val cutoffTime = System.currentTimeMillis() - duration
        val cursor = db.rawQuery(
            "SELECT $COLUMN_TIMESTAMP, $COLUMN_POWER FROM $TABLE_CHARGING_POWER WHERE $COLUMN_TIMESTAMP >= ?",
            arrayOf(cutoffTime.toString())
        )
        val data = mutableListOf<Pair<Long, Float>>()
        while (cursor.moveToNext()) {
            val timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP))
            val power = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_POWER))
            data.add(timestamp to power)
        }
        cursor.close()
        db.close()
        return data
    }

    fun clearChargingPowerData() {
        val db = writableDatabase
        db.execSQL("DELETE FROM $TABLE_CHARGING_POWER")
        db.close()
    }


    fun saveBatteryCapacity(capacity: Int) {
        sharedPreferences.edit().putInt(KEY_BATTERY_CAPACITY, capacity).apply()
    }

    fun getBatteryCapacity(): Int {
        return sharedPreferences.getInt(KEY_BATTERY_CAPACITY, -1)
    }

    fun saveCurrentPattern(currentValues: List<Float>) {
        val formatted = currentValues.joinToString(",")
        sharedPreferences.edit().putString(KEY_CURRENT_PATTERN, formatted).apply()
    }

    fun getCurrentPattern(): List<Float> {
        val data = sharedPreferences.getString(KEY_CURRENT_PATTERN, null) ?: return emptyList()
        return data.split(",").mapNotNull { it.toFloatOrNull() }
    }

    fun saveMinPower(value: Float) {
        sharedPreferences.edit().putFloat(KEY_MIN_POWER, value).apply()
    }

    fun getMinPower(): Float {
        return sharedPreferences.getFloat(KEY_MIN_POWER, -1f)
    }

    fun saveMaxPower(value: Float) {
        sharedPreferences.edit().putFloat(KEY_MAX_POWER, value).apply()
    }

    fun getMaxPower(): Float {
        return sharedPreferences.getFloat(KEY_MAX_POWER, -1f)
    }

    fun saveAvgPower(value: Float) {
        sharedPreferences.edit().putFloat(KEY_AVG_POWER, value).apply()
    }

    fun getAvgPower(): Float {
        return sharedPreferences.getFloat(KEY_AVG_POWER, -1f)
    }

    fun saveLastCurrentNow(currentNow: Int) {
        sharedPreferences.edit().putInt(KEY_LAST_CURRENT_NOW, currentNow).apply()
    }

    fun getLastCurrentNow(): Int {
        return sharedPreferences.getInt(KEY_LAST_CURRENT_NOW, 0)
    }

    fun saveVoltage(voltage: Float) {
        sharedPreferences.edit().putFloat(KEY_VOLTAGE, voltage).apply()
    }

    fun getVoltage(): Float {
        return sharedPreferences.getFloat(KEY_VOLTAGE, -1f)
    }

    fun saveTemperature(temp: Float) {
        sharedPreferences.edit().putFloat(KEY_TEMPERATURE, temp).apply()
    }

    fun getTemperature(): Float {
        return sharedPreferences.getFloat(KEY_TEMPERATURE, -1f)
    }

    fun isConfigured(): Boolean {
        return getBatteryCapacity() != -1 && getCurrentPattern().isNotEmpty()
    }
}
