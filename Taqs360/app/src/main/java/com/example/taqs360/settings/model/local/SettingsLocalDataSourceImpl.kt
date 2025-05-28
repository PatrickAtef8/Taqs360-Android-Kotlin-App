package com.example.taqs360.settings.model.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.example.taqs360.map.model.LocationData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SettingsLocalDataSourceImpl(private val context: Context) : SettingsLocalDataSource {
    private val prefs: SharedPreferences = context.getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)
    private val TAG = "SettingsLocalDataSourceImpl"

    companion object {
        private const val KEY_TEMP_UNIT = "temperature_unit"
        private const val KEY_WIND_SPEED_UNIT = "wind_speed_unit"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_LOCATION_MODE = "location_mode"
        private const val KEY_LAST_LATITUDE = "last_latitude"
        private const val KEY_LAST_LONGITUDE = "last_longitude"
        private const val DEFAULT_TEMP_UNIT = "metric"
        private const val DEFAULT_WIND_SPEED_UNIT = "meters_sec"
        private const val DEFAULT_LANGUAGE = "en"
        private const val DEFAULT_LOCATION_MODE = "gps"
    }

    override suspend fun saveTemperatureUnit(unit: String) {
        withContext(Dispatchers.IO) {
            Log.d(TAG, "Saving temperature unit: $unit")
            prefs.edit(commit = true) {
                putString(KEY_TEMP_UNIT, unit)
            }
        }
    }

    override suspend fun saveWindSpeedUnit(unit: String) {
        withContext(Dispatchers.IO) {
            Log.d(TAG, "Saving wind speed unit: $unit")
            prefs.edit(commit = true) {
                putString(KEY_WIND_SPEED_UNIT, unit)
            }
        }
    }

    override suspend fun saveLanguage(language: String) {
        withContext(Dispatchers.IO) {
            Log.d(TAG, "Saving language: $language")
            prefs.edit(commit = true) {
                putString(KEY_LANGUAGE, language)
            }
        }
    }

    override suspend fun saveLocationMode(mode: String) {
        withContext(Dispatchers.IO) {
            Log.d(TAG, "Saving location mode: $mode")
            prefs.edit(commit = true) {
                putString(KEY_LOCATION_MODE, mode)
            }
        }
    }

    override suspend fun saveLastLocation(location: LocationData) {
        withContext(Dispatchers.IO) {
            Log.d(TAG, "Saving location: lat=${location.latitude}, lon=${location.longitude}")
            prefs.edit(commit = true) {
                putFloat(KEY_LAST_LATITUDE, location.latitude.toFloat())
                putFloat(KEY_LAST_LONGITUDE, location.longitude.toFloat())
            }
        }
    }

    override suspend fun getLastLocation(): LocationData? {
        return withContext(Dispatchers.IO) {
            val latitude = prefs.getFloat(KEY_LAST_LATITUDE, Float.MIN_VALUE)
            val longitude = prefs.getFloat(KEY_LAST_LONGITUDE, Float.MIN_VALUE)
            Log.d(TAG, "Retrieved location: lat=$latitude, lon=$longitude")
            if (latitude != Float.MIN_VALUE && longitude != Float.MIN_VALUE) {
                LocationData(latitude.toDouble(), longitude.toDouble())
            } else {
                null
            }
        }
    }

    override fun getTemperatureUnit(): String {
        val unit = prefs.getString(KEY_TEMP_UNIT, DEFAULT_TEMP_UNIT) ?: DEFAULT_TEMP_UNIT
        Log.d(TAG, "Retrieved temperature unit: $unit")
        return unit
    }

    override fun getWindSpeedUnit(): String {
        val unit = prefs.getString(KEY_WIND_SPEED_UNIT, DEFAULT_WIND_SPEED_UNIT) ?: DEFAULT_WIND_SPEED_UNIT
        Log.d(TAG, "Retrieved wind speed unit: $unit")
        return unit
    }

    override fun getLanguage(): String {
        val language = prefs.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
        Log.d(TAG, "Retrieved language: $language")
        return language
    }

    override fun getLocationMode(): String {
        val mode = prefs.getString(KEY_LOCATION_MODE, DEFAULT_LOCATION_MODE) ?: DEFAULT_LOCATION_MODE
        Log.d(TAG, "Retrieved location mode: $mode")
        return mode
    }

    override fun getLanguageSync(): String {
        val language = prefs.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
        Log.d(TAG, "Retrieved language sync: $language")
        return language
    }
}