package com.example.taqs360.favorite.model.repository

import com.example.taqs360.favorite.model.data.FavoriteLocation
import com.example.taqs360.favorite.model.datasource.FavoriteLocalDataSource
import com.example.taqs360.map.model.LocationData

class FakeFavoriteLocalDataSource : FavoriteLocalDataSource {
    private val favorites = mutableListOf<FavoriteLocation>()

    override suspend fun saveFavorite(favorite: FavoriteLocation) {
        favorites.add(favorite)
    }

    override suspend fun getFavorites(): List<FavoriteLocation> {
        return favorites.toList()
    }

    override suspend fun deleteFavorite(favorite: FavoriteLocation) {
        favorites.removeAll { it.id == favorite.id }
    }

    override fun getLocationName(location: LocationData): String {
        return "Dummy Location"
    }
}