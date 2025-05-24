package com.example.taqs360.map.model.repository

import com.example.taqs360.location.LocationDataSource
import com.example.taqs360.location.LocationResult
import com.example.taqs360.map.model.LocationData
import com.example.taqs360.map.model.datasource.MapLocalDataSource

class MapRepositoryImpl(
    private val localDataSource: MapLocalDataSource,
    private val locationDataSource: LocationDataSource
) : MapRepository {

    override suspend fun getCurrentLocation(): LocationResult {
        return if (locationDataSource.hasLocationPermissions()) {
            locationDataSource.getCurrentLocation()
        } else {
            LocationResult.Failure(Exception("Location permissions not granted"))
        }
    }

    override fun getHomeLocation(): LocationData {
        return localDataSource.getHomeLocation()
    }

    override fun saveHomeLocation(location: LocationData) {
        localDataSource.saveHomeLocation(location)
    }
}