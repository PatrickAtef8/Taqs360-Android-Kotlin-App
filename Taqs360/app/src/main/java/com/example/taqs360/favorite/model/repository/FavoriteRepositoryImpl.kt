package com.example.taqs360.favorite.model.repository

import com.example.taqs360.favorite.model.data.FavoriteLocation
import com.example.taqs360.favorite.model.datasource.FavoriteLocalDataSource
import com.example.taqs360.map.model.LocationData

class FavoriteRepositoryImpl(
    private val localDataSource: FavoriteLocalDataSource
) : FavoriteRepository {
    override suspend fun saveFavorite(favorite: FavoriteLocation) {
        localDataSource.saveFavorite(favorite)
    }

    override suspend fun getFavorites(): List<FavoriteLocation> {
        return localDataSource.getFavorites()
    }

    override suspend fun deleteFavorite(favorite: FavoriteLocation) {
        localDataSource.deleteFavorite(favorite)
    }

    override fun getLocationName(location: LocationData): String {
        return localDataSource.getLocationName(location)
    }
}