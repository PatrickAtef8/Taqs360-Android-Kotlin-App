package com.example.taqs360.settings.model.remote

interface SettingsRemoteDataSource {
    fun getApiUnits(temperatureUnit: String): String
}