package com.example.taqs360.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.taqs360.home.model.repository.WeatherRepository
import com.example.taqs360.location.LocationDataSource

class WeatherViewModelFactory(
    private val repository: WeatherRepository,
    private val locationDataSource: LocationDataSource
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WeatherViewModel(repository, locationDataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}