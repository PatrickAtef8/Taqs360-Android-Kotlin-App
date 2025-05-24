package com.example.taqs360.map.model.datasource

import com.example.taqs360.map.model.LocationData

interface MapLocalDataSource {
    fun getHomeLocation(): LocationData
    fun saveHomeLocation(location: LocationData)
}