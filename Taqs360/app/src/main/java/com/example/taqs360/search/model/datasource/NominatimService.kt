package com.example.taqs360.search.model.datasource

import com.example.taqs360.search.model.NominatimResult
import retrofit2.http.GET
import retrofit2.http.Query

interface NominatimService {
    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("format") format: String
    ): List<NominatimResult>
}
