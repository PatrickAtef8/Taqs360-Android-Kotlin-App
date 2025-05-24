package com.example.taqs360.search.model.repository

import com.example.taqs360.search.model.datasource.SearchRemoteDataSource
import com.example.taqs360.search.model.Place
import java.lang.Exception

class SearchRepositoryImpl(
    private val remoteDataSource: SearchRemoteDataSource
) : SearchRepository {
    override suspend fun searchPlaces(query: String): Result<List<Place>> {
        return try {
            val response = remoteDataSource.search(query, "json")
            val places = response.map {
                Place(
                    name = it.displayName,
                    address = it.displayName,
                    latitude = it.lat.toDouble(),
                    longitude = it.lon.toDouble()
                )
            }
            Result.success(places)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}