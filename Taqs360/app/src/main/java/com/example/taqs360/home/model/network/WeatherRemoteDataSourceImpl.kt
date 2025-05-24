package com.example.taqs360.home.model.network

import com.example.taqs360.home.model.pojo.WeatherResponse
import com.example.taqs360.settings.model.repository.SettingsRepository

class WeatherRemoteDataSourceImpl(
    private val weatherService: WeatherService,
    private val settingsRepository: SettingsRepository // Inject SettingsRepository
) : WeatherRemoteDataSource {
    override suspend fun getFiveDayForecast(latitude: Double, longitude: Double, apiKey: String): WeatherResponse {
        val units = settingsRepository.getApiUnits()
        val language = settingsRepository.getLanguage()
        return weatherService.fetchFiveDayForecast(latitude, longitude, apiKey, units, language)
    }
}