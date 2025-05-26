package com.example.taqs360.favorite.model.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.taqs360.favorite.model.data.FavoriteLocation

@Dao
interface FavoriteLocationDao {
    @Insert
    suspend fun insert(favorite: FavoriteLocation)

    @Delete
    suspend fun delete(favorite: FavoriteLocation)

    @Query("SELECT * FROM favorite_locations")
    suspend fun getAllFavorites(): List<FavoriteLocation>
}