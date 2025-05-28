package com.example.taqs360.home.model.remote

import com.example.taqs360.home.model.pojo.WeatherResponse

interface WeatherRemoteDataSource {
    suspend fun getFiveDayForecast(
        latitude: Double,
        longitude: Double,
        units: String,
        language: String,
        apiKey: String
    ): WeatherResponse

    fun getUnits(): String
    fun getLanguage(): String
}