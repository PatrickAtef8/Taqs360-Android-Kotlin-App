package com.example.taqs360.settings.model.repository

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

    init {
        updateSettings()
    }

    override suspend fun saveTemperatureUnit(unit: String) {
        localDataSource.saveTemperatureUnit(unit)
        updateSettings()
    }

    override suspend fun saveWindSpeedUnit(unit: String) {
        localDataSource.saveWindSpeedUnit(unit)
        updateSettings()
    }

    override suspend fun saveLanguage(language: String) {
        localDataSource.saveLanguage(language)
        updateSettings()
    }

    override suspend fun saveLocationMode(mode: String) {
        localDataSource.saveLocationMode(mode)
        updateSettings()
    }

    override suspend fun saveLastLocation(location: LocationData) {
        localDataSource.saveLastLocation(location)
    }

    override suspend fun getLastLocation(): LocationData? {
        return localDataSource.getLastLocation()
    }

    override fun getTemperatureUnit(): String {
        return localDataSource.getTemperatureUnit()
    }

    override fun getWindSpeedUnit(): String {
        return localDataSource.getWindSpeedUnit()
    }

    override fun getLanguage(): String {
        return localDataSource.getLanguage()
    }

    override fun getLocationMode(): String {
        return localDataSource.getLocationMode()
    }

    override fun getApiUnits(): String {
        return remoteDataSource.getApiUnits(localDataSource.getTemperatureUnit())
    }

    override fun getLanguageSync(): String {
        return localDataSource.getLanguageSync()
    }

    override fun getLocale(): Locale {
        val language = getLanguage()
        return when (language) {
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

