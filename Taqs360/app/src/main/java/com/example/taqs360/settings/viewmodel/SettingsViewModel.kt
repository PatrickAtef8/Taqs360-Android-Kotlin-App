package com.example.taqs360.settings.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import android.util.Log
import com.example.taqs360.settings.model.Settings
import com.example.taqs360.settings.model.repository.SettingsRepository
import java.util.*

class SettingsViewModel(
    private val repository: SettingsRepository
) : ViewModel() {
    private val _settings = MutableLiveData<Settings>()
    val settings: LiveData<Settings> get() = _settings

    private val _languageChanged = MutableLiveData<Unit>()
    val languageChanged: LiveData<Unit> get() = _languageChanged

    private val TAG = "SettingsViewModel"
    private var lastLanguage: String? = null

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val tempUnit = repository.getTemperatureUnit()
            val windUnit = repository.getWindSpeedUnit()
            val language = repository.getLanguage()
            val locationMode = repository.getLocationMode()
            Log.d(TAG, "Loaded settings: temp=$tempUnit, wind=$windUnit, lang=$language, location=$locationMode")
            lastLanguage = language
            _settings.postValue(Settings(tempUnit, windUnit, language, locationMode))
        }
    }

    fun saveTemperatureUnit(unit: String) {
        viewModelScope.launch {
            Log.d(TAG, "Saving temperature unit: $unit")
            repository.saveTemperatureUnit(unit)
            updateSettings()
        }
    }

    fun saveWindSpeedUnit(unit: String) {
        viewModelScope.launch {
            Log.d(TAG, "Saving wind speed unit: $unit")
            repository.saveWindSpeedUnit(unit)
            updateSettings()
        }
    }

    fun saveLanguage(language: String) {
        viewModelScope.launch {
            val currentLanguage = repository.getLanguage()
            Log.d(TAG, "Attempting to save language: $language, current: $currentLanguage")
            if (currentLanguage != language && lastLanguage != language) {
                repository.saveLanguage(language)
                lastLanguage = language
                updateSettings()
                _languageChanged.postValue(Unit)
                Log.d(TAG, "Language changed to: $language, notifying observers")
            } else {
                Log.d(TAG, "Language unchanged or already processed, skipping save")
            }
        }
    }

    fun saveLocationMode(mode: String) {
        viewModelScope.launch {
            Log.d(TAG, "Saving location mode: $mode")
            repository.saveLocationMode(mode)
            updateSettings()
        }
    }

    private fun updateSettings() {
        viewModelScope.launch {
            val tempUnit = repository.getTemperatureUnit()
            val windUnit = repository.getWindSpeedUnit()
            val language = repository.getLanguage()
            val locationMode = repository.getLocationMode()
            Log.d(TAG, "Updated settings: temp=$tempUnit, wind=$windUnit, lang=$language, location=$locationMode")
            _settings.postValue(Settings(tempUnit, windUnit, language, locationMode))
        }
    }

    fun getLocale(): Locale {
        val language = repository.getLanguage()
        Log.d(TAG, "Getting locale for language: $language")
        return when (language) {
            "system" -> Locale.getDefault()
            "ar" -> Locale("ar")
            else -> Locale("en")
        }
    }

    fun getLanguage(): String {
        val language = repository.getLanguage()
        Log.d(TAG, "Getting language: $language")
        return language
    }
}