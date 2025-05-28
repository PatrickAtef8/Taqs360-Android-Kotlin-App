package com.example.taqs360.alerts.model

import androidx.room.TypeConverter
import com.example.taqs360.map.model.LocationData
import com.google.gson.Gson

class LocationDataConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromLocationData(location: LocationData?): String? {
        return location?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toLocationData(json: String?): LocationData? {
        return json?.let { gson.fromJson(it, LocationData::class.java) }
    }
}