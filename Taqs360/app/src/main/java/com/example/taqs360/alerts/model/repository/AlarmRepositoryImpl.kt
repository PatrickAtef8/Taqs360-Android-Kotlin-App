package com.example.taqs360.alerts.model.repository

import com.example.taqs360.alerts.model.AlarmData
import com.example.taqs360.alerts.model.datasource.AlarmLocalDataSource
import com.example.taqs360.map.model.LocationData

class AlarmRepositoryImpl(
    private val localDataSource: AlarmLocalDataSource
) : AlarmRepository {
    override suspend fun saveAlarm(alarm: AlarmData) {
        localDataSource.saveAlarm(alarm)
    }

    override suspend fun getAlarms(): List<AlarmData> {
        return localDataSource.getAlarms()
    }

    override suspend fun deleteAlarm(alarm: AlarmData) {
        localDataSource.deleteAlarm(alarm)
    }

    override fun getLocationName(location: LocationData): String {
        return localDataSource.getLocationName(location)
    }
}