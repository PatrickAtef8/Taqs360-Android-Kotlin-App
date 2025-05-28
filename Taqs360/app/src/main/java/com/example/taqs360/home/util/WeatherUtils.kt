package com.example.taqs360.home.util

import com.example.taqs360.R
import java.text.SimpleDateFormat
import java.util.*

object WeatherUtils {

    fun getWeatherIconResIdFromCode(iconCode: String): Int {
        return when (iconCode) {
            "01d" -> R.drawable.sunny
            "01n" -> R.drawable.clearnight
            "02d" -> R.drawable.cloudy
            "02n" -> R.drawable.dark
            "03d", "03n" -> R.drawable.scatteredclouds
            "04d", "04n" -> R.drawable.scatteredclouds
            "09d", "09n" -> R.drawable.rainy
            "10d" -> R.drawable.rainyday
            "10n" -> R.drawable.rainynight
            "11d", "11n" -> R.drawable.thunderstorm
            "13d", "13n" -> R.drawable.snowy
            "50d", "50n" -> R.drawable.misty
            else -> R.drawable.cloudy
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

    fun formatTemperature(temp: Float, unitSymbol: String): String {
        return "${temp.toInt()}$unitSymbol"
    }

    fun formatVisibility(visibility: Int): String {
        return "${(visibility / 1000.0).let { "%.1f".format(it) }} km"
    }

    fun formatWindSpeed(speed: Float, windUnit: String): String {
        return when (windUnit) {
            "meters_sec" -> "%.2f m/s".format(speed)
            "miles_hour" -> "%.2f mph".format(speed)
            else -> "%.2f m/s".format(speed)
        }
    }

    fun toArabicNumerals(input: String, isArabic: Boolean): String {
        if (!isArabic) return input
        val arabicNumerals = mapOf(
            '0' to '٠', '1' to '١', '2' to '٢', '3' to '٣', '4' to '٤',
            '5' to '٥', '6' to '٦', '7' to '٧', '8' to '٨', '9' to '٩'
        )
        return input.map { arabicNumerals[it] ?: it }.joinToString("")
    }
}