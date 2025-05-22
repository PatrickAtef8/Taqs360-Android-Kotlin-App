package com.example.taqs360.home.model.uidata

import com.example.taqs360.home.model.pojo.Forecast

data class WeatherUiData(
    val location: String,
    val currentTemp: Int,
    val feelsLike: Int,
    val description: String,
    val humidity: Int,
    val windSpeed: Float,
    val visibility: Int,
    val date: String,
    val currentTime: String,
    val currentWeatherIcon: Int,
    val forecasts: List<ForecastUiModel>,
    val timezoneOffset: Int
)


data class ForecastUiModel(
    val day: String,
    val minTemp: Int,
    val maxTemp: Int,
    val weatherDescription: String,
    val iconResId: Int,
    val forecastsForDay: List<Forecast>,
    val isToday: Boolean
)