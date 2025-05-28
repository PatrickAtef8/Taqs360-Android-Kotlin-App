package com.example.taqs360.home.model.local

interface WeatherLocalDataSource {
    suspend fun insertWeather(weather: WeatherEntity)
    suspend fun getWeather(locationKey: String): WeatherEntity?
    suspend fun clearExpiredWeather(expiration: Long)
    suspend fun getAllWeather(): List<WeatherEntity>
    suspend fun deleteWeatherDataById(locationKey: String)
}