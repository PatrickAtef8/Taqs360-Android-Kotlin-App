package com.example.taqs360.home.model.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WeatherDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weather: WeatherEntity)

    @Query("SELECT * FROM weather_cache WHERE locationKey = :locationKey")
    suspend fun getWeather(locationKey: String): WeatherEntity?

    @Query("DELETE FROM weather_cache WHERE timestamp < :expirationTime")
    suspend fun clearExpiredWeather(expirationTime: Long)

    @Query("DELETE FROM weather_cache WHERE locationKey = :locationKey")
    suspend fun deleteWeatherById(locationKey: String)


    @Query("SELECT * FROM weather_cache")
    suspend fun getAllWeather(): List<WeatherEntity>
}