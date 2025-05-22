package com.example.taqs360.home.model.network

import com.example.taqs360.home.model.pojo.WeatherResponse


class WeatherRemoteDataSourceImpl(private val weatherService: WeatherService) :
    WeatherRemoteDataSource {
    override suspend fun getFiveDayForecast(latitude: Double, longitude: Double, apiKey: String): WeatherResponse =
         weatherService.fetchFiveDayForecast(latitude, longitude, apiKey)
    }
