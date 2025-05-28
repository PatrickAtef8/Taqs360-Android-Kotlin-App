package com.example.taqs360.home.model.remote

import com.example.taqs360.home.model.pojo.WeatherResponse
import com.example.taqs360.settings.model.repository.SettingsRepository

class WeatherRemoteDataSourceImpl(
    private val weatherService: WeatherService,
    private val settingsRepository: SettingsRepository
) : WeatherRemoteDataSource {

    override suspend fun getFiveDayForecast(
        latitude: Double,
        longitude: Double,
        units: String,
        language: String,
        apiKey: String
    ): WeatherResponse {
        return weatherService.fetchFiveDayForecast(latitude, longitude, apiKey, units, language)
    }

    override fun getUnits(): String {
        return settingsRepository.getApiUnits()
    }

    override fun getLanguage(): String {
        return settingsRepository.getLanguage()
    }
}