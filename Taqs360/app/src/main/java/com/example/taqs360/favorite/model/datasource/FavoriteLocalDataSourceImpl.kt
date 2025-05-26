package com.example.taqs360.favorite.model.datasource

import android.content.Context
import android.util.Log
import com.example.taqs360.favorite.model.database.AppDatabase
import com.example.taqs360.favorite.model.data.FavoriteLocation
import com.example.taqs360.map.model.LocationData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class FavoriteLocalDataSourceImpl(private val appContext: Context) : FavoriteLocalDataSource {
    private val favoriteDao = AppDatabase.getDatabase(appContext).favoriteLocationDao()

    override suspend fun saveFavorite(favorite: FavoriteLocation) {
        withContext(Dispatchers.IO) {
            favoriteDao.insert(favorite)
            Log.d("FavoriteLocalDataSource", "Inserted favorite: ${favorite.locationName}")
        }
    }

    override suspend fun getFavorites(): List<FavoriteLocation> {
        return withContext(Dispatchers.IO) {
            val favorites = favoriteDao.getAllFavorites()
            Log.d("FavoriteLocalDataSource", "Retrieved ${favorites.size} favorites from database")
            favorites
        }
    }

    override suspend fun deleteFavorite(favorite: FavoriteLocation) {
        withContext(Dispatchers.IO) {
            favoriteDao.delete(favorite)
            Log.d("FavoriteLocalDataSource", "Deleted favorite: ${favorite.locationName}")
        }
    }

    override fun getLocationName(location: LocationData): String {
        return try {
            val geocoder = android.location.Geocoder(appContext, Locale.getDefault())
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                addresses[0].locality ?: addresses[0].featureName
                ?: "${location.latitude}, ${location.longitude}"
            } else {
                "${location.latitude}, ${location.longitude}"
            }
        } catch (e: Exception) {
            Log.e("FavoriteLocalDataSource", "Geocoder error: ${e.message}")
            "${location.latitude}, ${location.longitude}"
        }
    }
}