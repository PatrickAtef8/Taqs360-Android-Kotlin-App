package com.example.taqs360.alarm.model.datasource

import android.content.Context
import android.util.Log
import com.example.taqs360.alarm.model.AlarmData
import com.example.taqs360.map.model.LocationData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class AlarmLocalDataSourceImpl(private val appContext: Context) : AlarmLocalDataSource {
    private val alarmDao = AppDatabase.getDatabase(appContext).alarmDao()

    override suspend fun saveAlarm(alarm: AlarmData) {
        withContext(Dispatchers.IO) {
            alarmDao.insert(alarm)
            Log.d("AlarmLocalDataSource", "Inserted alarm: ${alarm.id} for ${alarm.locationName}")
        }
    }

    override suspend fun getAlarms(): List<AlarmData> {
        return withContext(Dispatchers.IO) {
            val alarms = alarmDao.getAllAlarms()
            Log.d("AlarmLocalDataSource", "Retrieved ${alarms.size} alarms from database")
            alarms
        }
    }

    override suspend fun deleteAlarm(alarm: AlarmData) {
        withContext(Dispatchers.IO) {
            alarmDao.delete(alarm)
            Log.d("AlarmLocalDataSource", "Deleted alarm: ${alarm.id} for ${alarm.locationName}")
        }
    }

    override fun getLocationName(location: LocationData): String {
        return try {
            val geocoder = android.location.Geocoder(appContext, Locale.getDefault())
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                addresses[0].locality ?: addresses[0].featureName
                ?: "${location.latitude}, ${location.longitude}"
            } else {
                "${location.latitude}, ${location.longitude}"
            }
        } catch (e: Exception) {
            Log.e("AlarmLocalDataSource", "Geocoder error: ${e.message}")
            "${location.latitude}, ${location.longitude}"
        }
    }
}