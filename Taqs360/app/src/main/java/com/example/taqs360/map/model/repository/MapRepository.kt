package com.example.taqs360.map.model.repository

import com.example.taqs360.location.LocationResult
import com.example.taqs360.map.model.LocationData

interface MapRepository {
    suspend fun getCurrentLocation(): LocationResult
    fun getHomeLocation(): LocationData
    fun saveHomeLocation(location: LocationData)
}