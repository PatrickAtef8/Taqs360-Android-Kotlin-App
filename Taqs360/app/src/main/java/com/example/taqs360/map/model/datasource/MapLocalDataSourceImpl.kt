package com.example.taqs360.map.model.datasource

import android.content.Context
import com.example.taqs360.map.model.LocationData

class MapLocalDataSourceImpl(
    private val context: Context
) : MapLocalDataSource {
    private val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    private val defaultHomeLocation = LocationData(30.0444, 31.2357)

    override fun getHomeLocation(): LocationData {
        val lat = prefs.getFloat("home_lat", defaultHomeLocation.latitude.toFloat()).toDouble()
        val lon = prefs.getFloat("home_lon", defaultHomeLocation.longitude.toFloat()).toDouble()
        return LocationData(lat, lon)
    }

    override fun saveHomeLocation(location: LocationData) {
        prefs.edit()
            .putFloat("home_lat", location.latitude.toFloat())
            .putFloat("home_lon", location.longitude.toFloat())
            .apply()
    }
}