package com.example.taqs360.home.model.repository

import android.util.Log
import com.example.taqs360.home.model.local.WeatherEntity
import com.example.taqs360.home.model.local.WeatherLocalDataSource
import com.example.taqs360.home.model.pojo.WeatherData
import com.example.taqs360.home.model.remote.WeatherRemoteDataSource
import com.example.taqs360.home.model.pojo.WeatherResponse

class WeatherRepositoryImpl(
    private val remoteDataSource: WeatherRemoteDataSource,
    private val localDataSource: WeatherLocalDataSource
) : WeatherRepository {

    private val TAG = "WeatherRepositoryImpl"

    override suspend fun getFiveDayForecast(
        latitude: Double,
        longitude: Double,
        apiKey: String,
        isOnline: Boolean
    ): WeatherData {
        val locationKey = "$latitude,$longitude"
        val cacheExpiration = System.currentTimeMillis() - (24 * 60 * 60 * 1000)

        Log.d(TAG, "Fetching forecast for lat=$latitude, lon=$longitude, isOnline=$isOnline")
        localDataSource.clearExpiredWeather(cacheExpiration)

        val allCached = localDataSource.getAllWeather()
        Log.d(TAG, "Current cache entries: ${allCached.size}, keys=${allCached.map { it.locationKey }}")

        if (isOnline) {
            try {
                val units = remoteDataSource.getUnits()
                Log.d(TAG, "Fetching from remote with units=$units")
                val response = remoteDataSource.getFiveDayForecast(
                    latitude,
                    longitude,
                    units,
                    remoteDataSource.getLanguage(),
                    apiKey
                )
                localDataSource.insertWeather(
                    WeatherEntity(
                        locationKey = locationKey,
                        latitude = latitude,
                        longitude = longitude,
                        timestamp = System.currentTimeMillis(),
                        units = units,
                        weatherResponse = response
                    )
                )
                Log.d(TAG, "Successfully fetched and cached data with units=$units")
                return WeatherData(response, units)
            } catch (e: Exception) {
                Log.e(TAG, "Remote fetch failed: ${e.message}", e)
            }
        }

        Log.d(TAG, "Fetching from cache for locationKey=$locationKey")
        val cachedWeather = localDataSource.getWeather(locationKey)
        return if (cachedWeather != null) {
            Log.d(TAG, "Returning cached data with units=${cachedWeather.units}")
            WeatherData(cachedWeather.weatherResponse, cachedWeather.units)
        } else {
            Log.e(TAG, "No cached data available")
            throw Exception("No internet connection and no cached data available for this location")
        }
    }
}