package com.example.taqs360.settings.model.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.taqs360.map.model.LocationData
import com.example.taqs360.settings.model.Settings
import com.example.taqs360.settings.model.local.SettingsLocalDataSource
import com.example.taqs360.settings.model.remote.SettingsRemoteDataSource
import java.util.Locale

class SettingsRepositoryImpl(
    private val localDataSource: SettingsLocalDataSource,
    private val remoteDataSource: SettingsRemoteDataSource
) : SettingsRepository {
    private val _settings = MutableLiveData<Settings>()
    override val settings: LiveData<Settings> get() = _settings
    private val TAG = "SettingsRepositoryImpl"

    init {
        updateSettings()
    }

    override suspend fun saveTemperatureUnit(unit: String) {
        Log.d(TAG, "Saving temperature unit: $unit")
        localDataSource.saveTemperatureUnit(unit)
        updateSettings()
    }

    override suspend fun saveWindSpeedUnit(unit: String) {
        Log.d(TAG, "Saving wind speed unit: $unit")
        localDataSource.saveWindSpeedUnit(unit)
        updateSettings()
    }

    override suspend fun saveLanguage(language: String) {
        Log.d(TAG, "Saving language: $language")
        localDataSource.saveLanguage(language)
        updateSettings()
    }

    override suspend fun saveLocationMode(mode: String) {
        Log.d(TAG, "Saving location mode: $mode")
        localDataSource.saveLocationMode(mode)
        updateSettings()
    }

    override suspend fun saveLastLocation(location: LocationData) {
        Log.d(TAG, "Saving location: $location")
        localDataSource.saveLastLocation(location)
    }

    override suspend fun getLastLocation(): LocationData? {
        val location = localDataSource.getLastLocation()
        Log.d(TAG, "Retrieved last location: $location")
        return location
    }

    override fun getTemperatureUnit(): String {
        val unit = localDataSource.getTemperatureUnit()
        Log.d(TAG, "Retrieved temperature unit: $unit")
        return unit
    }

    override fun getWindSpeedUnit(): String {
        val unit = localDataSource.getWindSpeedUnit()
        Log.d(TAG, "Retrieved wind speed unit: $unit")
        return unit
    }

    override fun getLanguage(): String {
        val language = localDataSource.getLanguage()
        Log.d(TAG, "Retrieved language: $language")
        return language
    }

    override fun getLocationMode(): String {
        val mode = localDataSource.getLocationMode()
        Log.d(TAG, "Retrieved location mode: $mode")
        return mode
    }

    override fun getApiUnits(): String {
        val apiUnits = remoteDataSource.getApiUnits(localDataSource.getTemperatureUnit())
        Log.d(TAG, "Retrieved API units: $apiUnits")
        return apiUnits
    }

    override fun getLanguageSync(): String {
        val language = localDataSource.getLanguageSync()
        Log.d(TAG, "Retrieved language sync: $language")
        return language
    }

    override fun getLocale(): Locale {
        val language = getLanguage()
        Log.d(TAG, "Retrieved locale: $language")
        return when (language) {
            "system" -> Locale.getDefault()
            "ar" -> Locale("ar")
            else -> Locale("en")
        }
    }

    private fun updateSettings() {
        _settings.postValue(
            Settings(
                temperatureUnit = getTemperatureUnit(),
                windSpeedUnit = getWindSpeedUnit(),
                language = getLanguage(),
                locationMode = getLocationMode()
            )
        )
    }
}