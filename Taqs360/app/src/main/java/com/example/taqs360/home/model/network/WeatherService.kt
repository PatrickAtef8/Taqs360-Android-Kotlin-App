package com.example.taqs360.home.model.network

import com.example.taqs360.home.model.pojo.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    @GET("forecast")
    suspend fun fetchFiveDayForecast(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): WeatherResponse
}