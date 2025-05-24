package com.example.taqs360.map.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taqs360.location.Location
import com.example.taqs360.location.LocationResult
import com.example.taqs360.map.model.LocationData
import com.example.taqs360.map.model.repository.MapRepository
import kotlinx.coroutines.launch

class MapViewModel(
    private val repository: MapRepository
) : ViewModel() {
    private val _selectedLocation = MutableLiveData<LocationData?>()
    val selectedLocation: LiveData<LocationData?> get() = _selectedLocation

    private val _currentLocation = MutableLiveData<LocationResult>()
    val currentLocation: LiveData<LocationResult> get() = _currentLocation

    init {
        fetchHomeLocation()
    }

    private fun fetchHomeLocation() {
        val homeLocation = repository.getHomeLocation()
        _currentLocation.value = LocationResult.Success(
            Location(homeLocation.latitude, homeLocation.longitude)
        )
    }

    fun fetchCurrentLocation() {
        viewModelScope.launch {
            _currentLocation.value = repository.getCurrentLocation()
        }
    }

    fun setSelectedLocation(location: LocationData) {
        _selectedLocation.value = location
        repository.saveHomeLocation(location)
    }

    fun clearSelectedLocation() {
        _selectedLocation.value = null
    }
}