package com.example.taqs360.settings.model.local

import com.example.taqs360.map.model.LocationData


interface SettingsLocalDataSource {
    suspend fun saveTemperatureUnit(unit: String)
    suspend fun saveWindSpeedUnit(unit: String)
    suspend fun saveLanguage(language: String)
    suspend fun saveLocationMode(mode: String)
    suspend fun saveLastLocation(location: LocationData)
    suspend fun getLastLocation(): LocationData?
    fun getTemperatureUnit(): String
    fun getWindSpeedUnit(): String
    fun getLanguage(): String
    fun getLocationMode(): String
    fun getLanguageSync(): String
}