package com.example.taqs360.home.model.local

import androidx.room.TypeConverter
import com.example.taqs360.home.model.pojo.WeatherResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class WeatherTypeConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromWeatherResponse(response: WeatherResponse?): String? {
        return response?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toWeatherResponse(json: String?): WeatherResponse? {
        return json?.let {
            val type = object : TypeToken<WeatherResponse>() {}.type
            gson.fromJson(it, type)
        }
    }
}