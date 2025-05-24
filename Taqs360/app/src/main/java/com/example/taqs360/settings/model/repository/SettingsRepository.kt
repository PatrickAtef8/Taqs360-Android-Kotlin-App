package com.example.taqs360.settings.model.repository

import androidx.lifecycle.LiveData
import com.example.taqs360.map.model.LocationData
import com.example.taqs360.settings.model.Settings
import java.util.Locale

interface SettingsRepository {
        val settings: LiveData<Settings>
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
        fun getApiUnits(): String
        fun getLanguageSync(): String
        fun getLocale(): Locale
}