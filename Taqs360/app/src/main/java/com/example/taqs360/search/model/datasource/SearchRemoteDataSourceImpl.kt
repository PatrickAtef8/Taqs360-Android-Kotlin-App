package com.example.taqs360.search.model.datasource

import com.example.taqs360.search.model.NominatimResult
import com.example.taqs360.search.util.RetrofitHelper

class SearchRemoteDataSourceImpl : SearchRemoteDataSource {
    private val service: NominatimService = RetrofitHelper.createService(NominatimService::class.java)

    override suspend fun search(query: String, format: String): List<NominatimResult> {
        return service.search(query, format)
    }
}