package com.example.taqs360.favorite.model.datasource

import com.example.taqs360.favorite.model.data.FavoriteLocation
import com.example.taqs360.map.model.LocationData

interface FavoriteLocalDataSource {
    suspend fun saveFavorite(favorite: FavoriteLocation)
    suspend fun getFavorites(): List<FavoriteLocation>
    suspend fun deleteFavorite(favorite: FavoriteLocation)
    fun getLocationName(location: LocationData): String
}