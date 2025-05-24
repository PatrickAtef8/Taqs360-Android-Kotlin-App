package com.example.taqs360.settings.model.remote

class SettingsRemoteDataSourceImpl : SettingsRemoteDataSource {
    override fun getApiUnits(temperatureUnit: String): String {
        return when (temperatureUnit) {
            "standard", "metric", "imperial" -> temperatureUnit
            else -> "metric" // Default to Celsius
        }
    }
}