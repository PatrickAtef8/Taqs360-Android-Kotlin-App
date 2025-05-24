package com.example.taqs360.search.model.repository

import com.example.taqs360.search.model.Place

interface SearchRepository {
    suspend fun searchPlaces(query: String): Result<List<Place>>
}