package com.example.taqs360.home.model.repository

import com.example.taqs360.home.model.pojo.WeatherResponse

interface WeatherRepository {
    suspend fun getFiveDayForecast(latitude: Double, longitude: Double, apiKey: String): WeatherResponse
}