package com.example.taqs360.home.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taqs360.BuildConfig
import com.example.taqs360.R
import com.example.taqs360.home.model.pojo.WeatherResponse
import com.example.taqs360.home.model.repository.WeatherRepository
import com.example.taqs360.home.model.uidata.ForecastUiModel
import com.example.taqs360.home.model.uidata.WeatherUiData
import com.example.taqs360.home.util.WeatherUtils
import com.example.taqs360.location.Location
import com.example.taqs360.location.LocationDataSource
import com.example.taqs360.location.LocationResult
import com.example.taqs360.map.model.LocationData
import com.example.taqs360.settings.model.repository.SettingsRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.text.SimpleDateFormat
import java.util.*

class WeatherViewModel(
    private val repository: WeatherRepository,
    private val locationDataSource: LocationDataSource,
    private val settingsRepository: SettingsRepository,
    private val context: Context
) : ViewModel() {

    private val _weatherUiData = MutableLiveData<WeatherUiData>()
    val weatherUiData: LiveData<WeatherUiData> get() = _weatherUiData

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val _requestPermissionEvent = MutableLiveData<Boolean>()
    val requestPermissionEvent: LiveData<Boolean> get() = _requestPermissionEvent

    private val _openMapFragmentEvent = MutableLiveData<Boolean>()
    val openMapFragmentEvent: LiveData<Boolean> get() = _openMapFragmentEvent

    private val _lastLocation = MutableLiveData<LocationData?>()
    val lastLocation: LiveData<LocationData?> get() = _lastLocation

    private var isMapRequestPending = false
    private val apiKey = BuildConfig.WEATHER_API_KEY
    private val TAG = "WeatherViewModel"

    init {
        // Restore last location and trigger fetch if needed
        viewModelScope.launch {
            try {
                Log.d(TAG, "Initializing WeatherViewModel")
                val savedLocation = settingsRepository.getLastLocation()
                if (savedLocation != null) {
                    Log.d(TAG, "Restored last location: lat=${savedLocation.latitude}, lon=${savedLocation.longitude}")
                    _lastLocation.value = savedLocation
                    // Trigger fetch to ensure data is loaded
                    fetchWeatherForLocation(savedLocation.latitude, savedLocation.longitude)
                } else {
                    Log.d(TAG, "No saved last location")
                    // Trigger fetch for both gps and map modes
                    when (settingsRepository.getLocationMode()) {
                        "gps" -> {
                            Log.d(TAG, "Location mode is gps, fetching weather")
                            requestLocationAndFetchWeather()
                        }
                        "map" -> {
                            Log.d(TAG, "Location mode is map, fetching weather with fallback")
                            fetchWeather() // Fallback to GPS or Cairo
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to restore last location", e)
                _error.value = "Failed to restore location: ${e.message}"
            }
        }
    }

    fun requestLocationAndFetchWeather() {
        viewModelScope.launch {
            if (locationDataSource.hasLocationPermissions()) {
                val persistedLocation = _lastLocation.value
                if (persistedLocation != null) {
                    Log.d(TAG, "Using persisted location: lat=${persistedLocation.latitude}, lon=${persistedLocation.longitude}")
                    fetchWeatherForLocation(persistedLocation.latitude, persistedLocation.longitude)
                } else {
                    fetchWeather()
                }
            } else {
                isMapRequestPending = false
                Log.d(TAG, "No location permissions, requesting")
                _requestPermissionEvent.value = true
            }
        }
    }

    fun onPermissionResult(isGranted: Boolean, openMapFragment: Boolean) {
        if (isGranted) {
            if (openMapFragment) {
                _openMapFragmentEvent.value = true
            } else {
                fetchWeather()
            }
        } else {
            Log.d(TAG, "Permission denied")
            _error.value = "Location permission required for weather data"
        }
        isMapRequestPending = false
    }

    fun isMapRequestPending(): Boolean = isMapRequestPending

    fun clearPermissionRequest() {
        _requestPermissionEvent.value = false
    }

    fun clearMapFragmentEvent() {
        _openMapFragmentEvent.value = false
    }

    fun clearError() {
        _error.value = null
    }

    fun fetchWeather() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting weather fetch")
                if (!locationDataSource.hasLocationPermissions()) {
                    throw Exception("Location permissions not granted")
                }

                val location = when (val result = locationDataSource.getCurrentLocation()) {
                    is LocationResult.Success -> result.data
                    is LocationResult.Failure -> {
                        Log.w(TAG, "Using fallback location: ${result.exception.message}")
                        Location(30.0444, 31.2357) // Cairo
                    }
                }

                _lastLocation.value = LocationData(location.latitude, location.longitude)
                settingsRepository.saveLastLocation(LocationData(location.latitude, location.longitude))
                fetchWeatherForLocation(location.latitude, location.longitude)
            } catch (e: Exception) {
                Log.e(TAG, "Weather fetch failed", e)
                _error.value = "Failed to fetch weather: ${e.message}"
            }
        }
    }

    fun fetchWeatherForLocation(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Fetching weather for lat=$latitude, lon=$longitude")
                // Validate location
                if (latitude == 0.0 && longitude == 0.0) {
                    throw Exception("Invalid location coordinates")
                }
                _lastLocation.value = LocationData(latitude, longitude)
                settingsRepository.saveLastLocation(LocationData(latitude, longitude))
                val response = withTimeoutOrNull(10000) { // 10s timeout
                    repository.getFiveDayForecast(latitude, longitude, apiKey)
                } ?: throw Exception("Weather fetch timed out")
                Log.d(TAG, "Received ${response.list.size} forecast points")
                val uiData = processWeatherData(response)
                _weatherUiData.value = uiData
            } catch (e: Exception) {
                Log.e(TAG, "Weather fetch failed", e)
                _error.value = "Failed to fetch weather: ${e.message}"
            }
        }
    }

    fun openMapFragment() {
        if (locationDataSource.hasLocationPermissions()) {
            _openMapFragmentEvent.value = true
        } else {
            isMapRequestPending = true
            _requestPermissionEvent.value = true
        }
    }

    fun getLocale(): Locale {
        return settingsRepository.getLocale()
    }

    fun shouldShowArabicNumerals(): Boolean {
        return settingsRepository.getLanguage() == "ar"
    }

    fun getTemperatureUnit(): String {
        return settingsRepository.getTemperatureUnit()
    }

    fun getWindSpeedUnit(): String {
        return settingsRepository.getWindSpeedUnit()
    }

    fun getLocationMode(): String {
        return settingsRepository.getLocationMode()
    }

    suspend fun saveLocationMode(mode: String) {
        settingsRepository.saveLocationMode(mode)
    }

    fun formatTemperature(temp: Float): String {
        val unitSymbol = when (getTemperatureUnit()) {
            "metric" -> "°C"
            "imperial" -> "°F"
            "standard" -> "K"
            else -> "°C"
        }
        return WeatherUtils.formatTemperature(temp, unitSymbol).let {
            if (shouldShowArabicNumerals()) WeatherUtils.toArabicNumerals(it, true) else it
        }
    }

    fun formatWindSpeed(speed: Float): String {
        return WeatherUtils.formatWindSpeed(speed, getWindSpeedUnit()).let {
            if (shouldShowArabicNumerals()) WeatherUtils.toArabicNumerals(it, true) else it
        }
    }

    fun formatVisibility(visibility: Int): String {
        return WeatherUtils.formatVisibility(visibility).let {
            if (shouldShowArabicNumerals()) WeatherUtils.toArabicNumerals(it, true) else it
        }
    }

    fun formatDate(timestamp: Long, pattern: String, timeZone: TimeZone): String {
        return WeatherUtils.formatDate(timestamp, pattern, timeZone).let {
            if (shouldShowArabicNumerals()) WeatherUtils.toArabicNumerals(it, true) else it
        }
    }

    private fun processWeatherData(response: WeatherResponse): WeatherUiData {
        val timeZoneId = response.city.timezone.let { offset ->
            TimeZone.getAvailableIDs(offset * 1000).firstOrNull() ?: "UTC"
        }
        val timeZone = TimeZone.getTimeZone(timeZoneId)

        val currentTime = System.currentTimeMillis() / 1000
        val currentCalendar = Calendar.getInstance(timeZone).apply {
            timeInMillis = currentTime * 1000
        }
        val tomorrowCalendar = Calendar.getInstance(timeZone).apply {
            timeInMillis = currentTime * 1000
            add(Calendar.DAY_OF_YEAR, 1)
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
            this.timeZone = timeZone
        }
        val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault()).apply {
            this.timeZone = timeZone
        }
        val displayDateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault()).apply {
            this.timeZone = timeZone
        }
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault()).apply {
            this.timeZone = timeZone
        }

        val groupedForecasts = response.list.groupBy { forecast ->
            val forecastCalendar = Calendar.getInstance(timeZone).apply {
                timeInMillis = forecast.dt * 1000
            }
            dateFormat.format(forecastCalendar.time)
        }.toSortedMap()

        val forecastUiModels = groupedForecasts.entries.take(5).mapIndexed { index, (date, forecasts) ->
            val forecastDate = dateFormat.parse(date)!!
            val forecastCalendar = Calendar.getInstance(timeZone).apply {
                time = forecastDate
            }

            val isToday = currentCalendar.get(Calendar.YEAR) == forecastCalendar.get(Calendar.YEAR) &&
                    currentCalendar.get(Calendar.DAY_OF_YEAR) == forecastCalendar.get(Calendar.DAY_OF_YEAR)

            val isTomorrow = tomorrowCalendar.get(Calendar.YEAR) == forecastCalendar.get(Calendar.YEAR) &&
                    tomorrowCalendar.get(Calendar.DAY_OF_YEAR) == forecastCalendar.get(Calendar.DAY_OF_YEAR)

            val representative = forecasts.minByOrNull { forecast ->
                val forecastHour = Calendar.getInstance(timeZone).apply {
                    timeInMillis = forecast.dt * 1000
                }.get(Calendar.HOUR_OF_DAY)
                Math.abs(forecastHour - 12)
            } ?: forecasts.first()

            ForecastUiModel(
                day = when {
                    isToday -> context.getString(R.string.today)
                    isTomorrow -> context.getString(R.string.tomorrow)
                    else -> dayFormat.format(forecastDate)
                },
                minTemp = forecasts.minOf { it.main.temp }.toInt(),
                maxTemp = forecasts.maxOf { it.main.temp }.toInt(),
                weatherDescription = representative.weather[0].description.replaceFirstChar { it.uppercase() },
                iconResId = WeatherUtils.getWeatherIconResIdFromCode(representative.weather[0].icon),
                forecastsForDay = forecasts.sortedBy { it.dt },
                isToday = isToday,
                isTomorrow = isTomorrow
            )
        }

        val currentForecast = response.list.minByOrNull { Math.abs(it.dt - currentTime) } ?: response.list.first()

        return WeatherUiData(
            location = "${response.city.name}, ${response.city.country}",
            currentTemp = currentForecast.main.temp.toInt(),
            feelsLike = currentForecast.main.feels_like.toInt(),
            description = currentForecast.weather[0].description.replaceFirstChar { it.uppercase() },
            humidity = currentForecast.main.humidity,
            windSpeed = currentForecast.wind.speed,
            visibility = currentForecast.visibility,
            date = displayDateFormat.format(currentCalendar.time),
            currentTime = timeFormat.format(currentCalendar.time),
            currentWeatherIcon = WeatherUtils.getWeatherIconResIdFromCode(currentForecast.weather[0].icon),
            forecasts = forecastUiModels,
            timezoneOffset = response.city.timezone,
            timezoneId = timeZoneId
        )
    }
}