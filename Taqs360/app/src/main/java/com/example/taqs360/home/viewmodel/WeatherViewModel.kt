package com.example.taqs360.home.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taqs360.home.model.pojo.WeatherResponse
import com.example.taqs360.home.model.repository.WeatherRepository
import com.example.taqs360.home.model.uidata.WeatherUiData
import com.example.taqs360.home.model.uidata.ForecastUiModel
import com.example.taqs360.home.util.WeatherUtils
import com.example.taqs360.location.LocationDataSource
import com.example.taqs360.location.LocationResult
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.example.taqs360.BuildConfig

class WeatherViewModel(
    private val repository: WeatherRepository,
    private val locationDataSource: LocationDataSource
) : ViewModel() {
    private val _weatherUiData = MutableLiveData<WeatherUiData>()
    val weatherUiData: LiveData<WeatherUiData> get() = _weatherUiData

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val apiKey = BuildConfig.WEATHER_API_KEY


    private val TAG = "WeatherViewModel"

    fun fetchWeather() {
        viewModelScope.launch {
            Log.d(TAG, "Starting fetchWeather")
            if (!locationDataSource.hasLocationPermissions()) {
                Log.e(TAG, "Permissions not granted")
                _error.postValue("Location permissions not granted")
                return@launch
            }

            when (val locationResult = locationDataSource.getCurrentLocation()) {
                is LocationResult.Success -> {
                    Log.d(TAG, "Location success: lat=${locationResult.data.latitude}, lon=${locationResult.data.longitude}")
                    try {
                        val response = repository.getFiveDayForecast(
                            locationResult.data.latitude,
                            locationResult.data.longitude,
                            apiKey
                        )
                        Log.d(TAG, "Weather data fetched for ${response.city.name}")
                        val uiData = processWeatherData(response)
                        _weatherUiData.postValue(uiData)
                    } catch (e: Exception) {
                        Log.e(TAG, "Weather fetch error: ${e.message}", e)
                        _error.postValue("Failed to fetch weather data: ${e.message}")
                    }
                }
                is LocationResult.Failure -> {
                    Log.e(TAG, "Location failure: ${locationResult.exception.message}")
                    _error.postValue(locationResult.exception.message)
                    // Fallback to Cairo
                    try {
                        Log.d(TAG, "Attempting fallback to Cairo (30.0444, 31.2357)")
                        val response = repository.getFiveDayForecast(
                            30.0444, // Cairo latitude
                            31.2357, // Cairo longitude
                            apiKey
                        )
                        Log.d(TAG, "Fallback weather data fetched for ${response.city.name}")
                        val uiData = processWeatherData(response)
                        _weatherUiData.postValue(uiData)
                        _error.postValue("Using default location (Cairo) due to: ${locationResult.exception.message}")
                    } catch (e: Exception) {
                        Log.e(TAG, "Fallback fetch error: ${e.message}", e)
                        _error.postValue("Failed to fetch Cairo weather data: ${e.message}")
                    }
                }
            }
            Log.d(TAG, "fetchWeather completed")
        }
    }

    private fun processWeatherData(response: WeatherResponse): WeatherUiData {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val localDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val displayDateFormat = SimpleDateFormat("EEE, d MMM", Locale.getDefault()) // Shortened month for conciseness
        var timeZone = TimeZone.getTimeZone("UTC").apply {
            rawOffset = response.city.timezone * 1000 // Set timezone offset from API
        }

        val currentTime = System.currentTimeMillis() / 1000
        val twentyFourHoursAgo = currentTime - 24 * 60 * 60
        val localTodayStr = localDateFormat.format(Date())
        val utcTodayStr = dateFormat.format(Date(currentTime * 1000))

        val groupedForecasts = response.list
            .groupBy { dateFormat.format(Date(it.dt * 1000)) }
            .toMutableMap()

        val todayForecasts = response.list.filter {
            it.dt in twentyFourHoursAgo..currentTime
        }
        if (todayForecasts.isNotEmpty()) {
            groupedForecasts[utcTodayStr] = todayForecasts
        } else {
            groupedForecasts.remove(utcTodayStr)
        }

        val forecastUiModels = groupedForecasts.entries
            .sortedBy { it.key }
            .take(5)
            .mapIndexed { index, entry ->
                val forecastsForDay = entry.value
                val representativeForecast = forecastsForDay.minByOrNull { forecast ->
                    val date = Date(forecast.dt * 1000)
                    val hourFormat = SimpleDateFormat("HH", Locale.getDefault()).apply {
                        timeZone = timeZone
                    }
                    val hour = hourFormat.format(date).toInt()
                    kotlin.math.abs(hour - 12)
                } ?: forecastsForDay.first()

                val minTemp = forecastsForDay.minOfOrNull { it.main.temp }?.toInt() ?: 0
                val maxTemp = forecastsForDay.maxOfOrNull { it.main.temp }?.toInt() ?: 0
                val weatherDescription = representativeForecast.weather[0].description.lowercase()

                ForecastUiModel(
                    day = if (index == 0) "Today" else WeatherUtils.formatDate(representativeForecast.dt, timeZone = timeZone),
                    minTemp = minTemp,
                    maxTemp = maxTemp,
                    weatherDescription = weatherDescription.replaceFirstChar { it.uppercase() },
                    iconResId = WeatherUtils.getWeatherIconResIdFromCode(representativeForecast.weather[0].icon),
                    forecastsForDay = forecastsForDay,
                    isToday = index == 0
                )
            }

        val current = response.list.first()
        return WeatherUiData(
            location = "${response.city.name}, ${response.city.country}",
            currentTemp = current.main.temp.toInt(),
            feelsLike = current.main.feels_like.toInt(),
            description = current.weather[0].description.replaceFirstChar { it.uppercase() },
            humidity = current.main.humidity,
            windSpeed = current.wind.speed,
            visibility = current.visibility,
            date = "Today, ${displayDateFormat.format(Date())}",
            currentTime = WeatherUtils.formatTime(currentTime, timeZone), // Use city timezone
            currentWeatherIcon = WeatherUtils.getWeatherIconResIdFromCode(current.weather[0].icon),
            forecasts = forecastUiModels,
            timezoneOffset = response.city.timezone
        )
    }

    fun getFormattedDate(timestamp: Long): String {
        return WeatherUtils.formatDate(timestamp)
    }
}