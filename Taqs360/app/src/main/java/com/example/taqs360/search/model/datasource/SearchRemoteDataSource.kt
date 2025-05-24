package com.example.taqs360.search.model.datasource

import com.example.taqs360.search.model.NominatimResult

interface SearchRemoteDataSource {
    suspend fun search(query: String, format: String): List<NominatimResult>
}