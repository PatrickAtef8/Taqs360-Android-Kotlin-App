package com.example.taqs360.favorite.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taqs360.favorite.model.data.FavoriteLocation
import com.example.taqs360.favorite.model.repository.FavoriteRepository
import com.example.taqs360.map.model.LocationData
import kotlinx.coroutines.launch
import java.util.UUID

class FavoriteViewModel(private val repository: FavoriteRepository) : ViewModel() {
    private val _favorites = MutableLiveData<List<FavoriteLocation>>()
    val favorites: LiveData<List<FavoriteLocation>> = _favorites

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    fun loadFavorites() {
        viewModelScope.launch {
            val favorites = repository.getFavorites()
            _favorites.value = favorites
        }
    }

    fun addFavorite(locationData: LocationData, name: String) {
        viewModelScope.launch {
            val favorite = FavoriteLocation(
                id = UUID.randomUUID().toString(),
                locationName = name,
                latitude = locationData.latitude,
                longitude = locationData.longitude
            )
            repository.saveFavorite(favorite)
            loadFavorites()
            _message.value = "Favorite added for $name"
        }
    }

    fun removeFavorite(location: FavoriteLocation) {
        viewModelScope.launch {
            val existingFavorites = repository.getFavorites()
            if (existingFavorites.any { it.id == location.id }) {
                repository.deleteFavorite(location)
                loadFavorites()
                _message.value = "${location.locationName} removed from favorites"
            } else {
                _message.value = "${location.locationName} not found in favorites"
            }
        }
    }

    fun restoreFavorite(location: FavoriteLocation) {
        viewModelScope.launch {
            repository.saveFavorite(location)
            loadFavorites()
            _message.value = "Favorite restored for ${location.locationName}"
        }
    }
}