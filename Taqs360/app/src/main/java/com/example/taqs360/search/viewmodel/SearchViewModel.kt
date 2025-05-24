package com.example.taqs360.search.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taqs360.search.model.Place
import com.example.taqs360.search.model.repository.SearchRepository
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchRepository: SearchRepository
) : ViewModel() {
    private val _searchResults = MutableLiveData<List<Place>>()
    val searchResults: LiveData<List<Place>> get() = _searchResults

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    fun searchPlaces(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        viewModelScope.launch {
            val result = searchRepository.searchPlaces(query)
            result.getOrNull()?.let { places ->
                _searchResults.value = places
            } ?: run {
                _error.value = "Failed to search: ${result.exceptionOrNull()?.message ?: "Unknown error"}"
            }
        }
    }
}