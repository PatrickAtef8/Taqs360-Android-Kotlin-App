package com.example.taqs360.location

sealed class LocationResult {
    data class Success(val data: Location) : LocationResult()
    data class Failure(val exception: Exception) : LocationResult()
}

data class Location(val latitude: Double, val longitude: Double)