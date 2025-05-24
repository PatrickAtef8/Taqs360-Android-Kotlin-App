package com.example.taqs360.home.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.taqs360.home.model.repository.WeatherRepository
import com.example.taqs360.location.LocationDataSource
import com.example.taqs360.settings.model.repository.SettingsRepository

class WeatherViewModelFactory(
    private val repository: WeatherRepository,
    private val locationDataSource: LocationDataSource,
    private val settingsRepository: SettingsRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
            return WeatherViewModel(repository, locationDataSource, settingsRepository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}