package com.example.taqs360.favorite.model.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_locations")
data class FavoriteLocation(
    @PrimaryKey val id: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double
)