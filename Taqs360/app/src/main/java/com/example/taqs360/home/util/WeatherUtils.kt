package com.example.taqs360.home.util

import com.example.taqs360.R
import java.text.SimpleDateFormat
import java.util.*

object WeatherUtils {

    fun getWeatherIconResIdFromCode(iconCode: String): Int {
        return when (iconCode) {
            "01d" -> R.drawable.sunny // Clear sky (day)
            "01n" -> R.drawable.clearnight // Clear sky (night)
            "02d" -> R.drawable.cloudy // Few clouds (day)
            "02n" -> R.drawable.dark // Few clouds (night)
            "03d", "03n" -> R.drawable.scatteredclouds // Scattered clouds
            "04d", "04n" -> R.drawable.scatteredclouds // Broken clouds
            "09d", "09n" -> R.drawable.rainy // Shower rain
            "10d" -> R.drawable.rainyday // Rain (day)
            "10n" -> R.drawable.rainynight // Rain (night)
            "11d", "11n" -> R.drawable.thunderstorm // Thunderstorm
            "13d", "13n" -> R.drawable.snowy // Snow
            "50d", "50n" -> R.drawable.misty // Mist
            else -> R.drawable.cloudy // Default
        }
    }

    fun formatDate(timestamp: Long, pattern: String = "EEEE", timeZone: TimeZone = TimeZone.getTimeZone("UTC")): String {
        val formatter = SimpleDateFormat(pattern, Locale.getDefault()).apply {
            this.timeZone = timeZone
        }
        return formatter.format(Date(timestamp * 1000))
    }

    fun formatTime(timestamp: Long, timeZone: TimeZone = TimeZone.getDefault()): String {
        val formatter = SimpleDateFormat("h:mm a", Locale.getDefault()).apply {
            this.timeZone = timeZone
        }
        return formatter.format(Date(timestamp * 1000))
    }

    fun formatTemperature(temp: Float): String = "${temp.toInt()}°"

    fun formatVisibility(visibility: Int): String = "${visibility / 1000.0} km"

    fun formatWindSpeed(speed: Float): String = "$speed km/h"
}