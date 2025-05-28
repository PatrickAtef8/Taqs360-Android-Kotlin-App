package com.example.taqs360.home.model.local

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherLocalDataSourceImpl(
    private val weatherDao: WeatherDao
) : WeatherLocalDataSource {

    override suspend fun insertWeather(weather: WeatherEntity) {
        withContext(Dispatchers.IO) {
            weatherDao.insertWeather(weather)
        }
    }

    override suspend fun getWeather(locationKey: String): WeatherEntity? {
        return withContext(Dispatchers.IO) {
            weatherDao.getWeather(locationKey)
        }
    }

    override suspend fun clearExpiredWeather(expiration: Long) {
        withContext(Dispatchers.IO) {
            weatherDao.clearExpiredWeather(expiration)
        }
    }

    override suspend fun getAllWeather(): List<WeatherEntity> {
        return withContext(Dispatchers.IO) {
            weatherDao.getAllWeather()
        }
    }

    override suspend fun deleteWeatherDataById(locationKey: String) {
        withContext(Dispatchers.IO) {
            weatherDao.deleteWeatherById(locationKey)
        }
    }
}