package com.example.taqs360.home.model.repository

import com.example.taqs360.home.model.pojo.WeatherResponse
import com.example.taqs360.home.model.network.WeatherRemoteDataSource


class WeatherRepositoryImpl(private val remoteDataSource: WeatherRemoteDataSource) :
    WeatherRepository {
    override suspend fun getFiveDayForecast(latitude: Double, longitude: Double, apiKey: String): WeatherResponse =
        remoteDataSource.getFiveDayForecast(latitude, longitude, apiKey)
    }
