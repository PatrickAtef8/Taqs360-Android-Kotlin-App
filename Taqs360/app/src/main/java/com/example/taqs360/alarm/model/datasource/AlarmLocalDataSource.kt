package com.example.taqs360.alarm.model.datasource

import com.example.taqs360.alarm.model.AlarmData
import com.example.taqs360.map.model.LocationData

interface AlarmLocalDataSource {
    suspend fun saveAlarm(alarm: AlarmData)
    suspend fun getAlarms(): List<AlarmData>
    suspend fun deleteAlarm(alarm: AlarmData)
    fun getLocationName(location: LocationData): String
}