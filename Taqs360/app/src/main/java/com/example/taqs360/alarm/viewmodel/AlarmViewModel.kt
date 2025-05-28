package com.example.taqs360.alarm.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.taqs360.alarm.model.AlarmData
import com.example.taqs360.alarm.model.repository.AlarmRepository
import com.example.taqs360.alarm.work.AlarmWorker
import com.example.taqs360.home.model.local.WeatherDatabase
import com.example.taqs360.home.model.local.WeatherLocalDataSourceImpl
import com.example.taqs360.home.model.remote.RetrofitHelper
import com.example.taqs360.home.model.remote.WeatherRemoteDataSourceImpl
import com.example.taqs360.home.model.remote.WeatherService
import com.example.taqs360.home.model.repository.WeatherRepositoryImpl
import com.example.taqs360.home.viewmodel.WeatherViewModel
import com.example.taqs360.location.LocationDataSource
import com.example.taqs360.map.model.LocationData
import com.example.taqs360.network.NetworkMonitor
import com.example.taqs360.settings.model.local.SettingsLocalDataSourceImpl
import com.example.taqs360.settings.model.remote.SettingsRemoteDataSourceImpl
import com.example.taqs360.settings.model.repository.SettingsRepositoryImpl
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class AlarmViewModel(
    private val repository: AlarmRepository,
    private val context: Context
) : ViewModel() {
    private val _alarms = MutableLiveData<List<AlarmData>>()
    val alarms: LiveData<List<AlarmData>> = _alarms

    private val _selectedLocation = MutableLiveData<LocationData?>()
    private val _selectedLocationName = MutableLiveData<String?>()
    private val _selectedDate = MutableLiveData<Long?>()
    private val _selectedTime = MutableLiveData<Pair<Int, Int>?>()

    private val _openDatePickerEvent = MutableLiveData<Boolean>()
    val openDatePickerEvent: LiveData<Boolean> get() = _openDatePickerEvent

    private val _openTimePickerEvent = MutableLiveData<Boolean>()
    val openTimePickerEvent: LiveData<Boolean> get() = _openTimePickerEvent

    private val _alarmAddedEvent = MutableLiveData<AlarmData?>()
    val alarmAddedEvent: LiveData<AlarmData?> get() = _alarmAddedEvent

    private val _alarmDeletedEvent = MutableLiveData<AlarmData?>()
    val alarmDeletedEvent: LiveData<AlarmData?> get() = _alarmDeletedEvent

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private val weatherViewModel: WeatherViewModel by lazy {
        WeatherViewModel(
            WeatherRepositoryImpl(
                WeatherRemoteDataSourceImpl(
                    RetrofitHelper.getInstance().create(WeatherService::class.java),
                    SettingsRepositoryImpl(
                        SettingsLocalDataSourceImpl(context),
                        SettingsRemoteDataSourceImpl()
                    )
                ),
                WeatherLocalDataSourceImpl(WeatherDatabase.getDatabase(context).weatherDao())
            ),
            LocationDataSource(context),
            SettingsRepositoryImpl(
                SettingsLocalDataSourceImpl(context),
                SettingsRemoteDataSourceImpl()
            ),
            context,
            NetworkMonitor(context)
        )
    }

    fun loadAlarms() {
        viewModelScope.launch {
            try {
                val alarms = repository.getAlarms()
                _alarms.value = alarms
            } catch (e: Exception) {
                _message.value = "Failed to load alarms: ${e.message}"
                android.util.Log.e("AlarmViewModel", "Load alarms failed: ${e.message}", e)
            }
        }
    }

    fun setSelectedLocation(location: LocationData, locationName: String) {
        _selectedLocation.value = location
        _selectedLocationName.value = locationName
        openDatePicker()
    }

    fun setSelectedDate(timestamp: Long) {
        _selectedDate.value = timestamp
    }

    fun setSelectedTime(hour: Int, minute: Int) {
        _selectedTime.value = Pair(hour, minute)
    }

    fun openDatePicker() {
        _openDatePickerEvent.value = true
    }

    fun onDateConfirmed() {
        if (_selectedDate.value != null) {
            _openTimePickerEvent.value = true
        }
    }

    fun onTimeConfirmed() {
        val location = _selectedLocation.value ?: return
        val locationName = _selectedLocationName.value ?: repository.getLocationName(location)
        val date = _selectedDate.value ?: return
        val time = _selectedTime.value ?: return

        val calendar = Calendar.getInstance().apply {
            timeInMillis = date
            set(Calendar.HOUR_OF_DAY, time.first)
            set(Calendar.MINUTE, time.second)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val alarmTime = calendar.timeInMillis
        val formattedDateTime = formatDateTime(alarmTime)

        viewModelScope.launch {
            var weatherStatus = "Unknown"
            try {
                weatherViewModel.fetchWeatherForLocation(location.latitude, location.longitude)
                val weather = withTimeoutOrNull(5000) {
                    weatherViewModel.weatherUiData.asFlow().firstOrNull()
                }
                weatherStatus = weather?.description?.replaceFirstChar { it.uppercase(Locale.getDefault()) } ?: "Unknown"
                if (weather?.isFromCache == true) {
                    _message.value = "Using cached weather data for $locationName"
                }
            } catch (e: Exception) {
                android.util.Log.e("AlarmViewModel", "Weather fetch error: ${e.message}", e)
                _message.value = "Failed to fetch weather, using default status"
            }

            val alarm = AlarmData(
                id = java.util.UUID.randomUUID().toString(),
                location = location,
                locationName = locationName,
                timestamp = alarmTime,
                formattedDateTime = formattedDateTime,
                weatherStatus = weatherStatus
            )

            try {
                repository.saveAlarm(alarm)
                scheduleAlarm(alarm)
                _alarmAddedEvent.value = alarm
                _message.value = "Alarm set for ${alarm.locationName} at ${formatDateTime(alarmTime)}"
                loadAlarms()
            } catch (e: Exception) {
                _message.value = "Failed to save alarm: ${e.message}"
                android.util.Log.e("AlarmViewModel", "Save alarm failed: ${e.message}", e)
            }

            _selectedLocation.value = null
            _selectedLocationName.value = null
            _selectedDate.value = null
            _selectedTime.value = null
        }
    }

    fun deleteAlarm(alarm: AlarmData) {
        viewModelScope.launch {
            try {
                repository.deleteAlarm(alarm)
                cancelAlarm(alarm)
                _alarmDeletedEvent.value = alarm
                _message.value = "Alarm for ${alarm.locationName} deleted"
                loadAlarms()
            } catch (e: Exception) {
                _message.value = "Failed to delete alarm: ${e.message}"
                android.util.Log.e("AlarmViewModel", "Delete alarm failed: ${e.message}", e)
            }
        }
    }

    fun restoreAlarm(alarm: AlarmData) {
        viewModelScope.launch {
            try {
                repository.saveAlarm(alarm)
                scheduleAlarm(alarm)
                _message.value = "Alarm restored for ${alarm.locationName}"
                loadAlarms()
            } catch (e: Exception) {
                _message.value = "Failed to restore alarm: ${e.message}"
                android.util.Log.e("AlarmViewModel", "Restore alarm failed: ${e.message}", e)
            }
        }
    }

    fun clearDatePickerEvent() {
        _openDatePickerEvent.value = false
    }

    fun clearTimePickerEvent() {
        _openTimePickerEvent.value = false
    }

    fun clearAlarmAddedEvent() {
        _alarmAddedEvent.value = null
    }

    fun clearAlarmDeletedEvent() {
        _alarmDeletedEvent.value = null
    }

    fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        return sdf.format(timestamp)
    }

    private fun scheduleAlarm(alarm: AlarmData) {
        val currentTime = System.currentTimeMillis()
        if (alarm.timestamp <= currentTime) {
            android.util.Log.w("AlarmViewModel", "Cannot schedule alarm: timestamp ${alarm.timestamp} is in the past")
            _message.value = "Cannot schedule alarm: Time is in the past"
            return
        }

        val delay = alarm.timestamp - currentTime
        val workData = Data.Builder()
            .putString("location_name", alarm.locationName)
            .putString("alarm_id", alarm.id)
            .putDouble("latitude", alarm.location.latitude)
            .putDouble("longitude", alarm.location.longitude)
            .putString("weather_status", alarm.weatherStatus)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<AlarmWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workData)
            .addTag(alarm.id)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
        android.util.Log.d("AlarmViewModel", "Scheduled alarm ${alarm.id} for ${alarm.locationName} at ${alarm.timestamp}")
    }

    private fun cancelAlarm(alarm: AlarmData) {
        WorkManager.getInstance(context).cancelAllWorkByTag(alarm.id)
        android.util.Log.d("AlarmViewModel", "Cancelled alarm ${alarm.id}")
    }

    fun testAlarm(locationName: String = "Test Location") {
        val alarm = AlarmData(
            id = "test_${System.currentTimeMillis()}",
            location = LocationData(37.7749, -122.4194),
            locationName = locationName,
            timestamp = System.currentTimeMillis() + 5000,
            formattedDateTime = formatDateTime(System.currentTimeMillis() + 5000),
            weatherStatus = "Sunny"
        )
        viewModelScope.launch {
            scheduleAlarm(alarm)
            _message.value = "Test alarm scheduled for 5 seconds"
        }
    }
}