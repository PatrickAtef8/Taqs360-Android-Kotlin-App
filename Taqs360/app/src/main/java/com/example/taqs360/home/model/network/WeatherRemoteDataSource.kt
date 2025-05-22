package com.example.taqs360.home.model.network

import com.example.taqs360.home.model.pojo.WeatherResponse

interface WeatherRemoteDataSource {
    suspend fun getFiveDayForecast(latitude: Double, longitude: Double, apiKey: String): WeatherResponse
}