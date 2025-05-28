package com.example.taqs360.home.model.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.taqs360.home.model.pojo.WeatherResponse

@Entity(tableName = "weather_cache")
data class WeatherEntity(
    @PrimaryKey val locationKey: String, // Format: "lat,lon"
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long, // Cache timestamp
    val units: String, // e.g., "metric", "imperial", "standard"
    val weatherResponse: WeatherResponse // Serialized response
)