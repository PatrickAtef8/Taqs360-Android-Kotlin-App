package com.example.taqs360.alarm.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.taqs360.map.model.LocationData

@Entity(tableName = "alarms")
data class AlarmData(
    @PrimaryKey val id: String,
    val location: LocationData,
    val locationName: String,
    val timestamp: Long,
    val formattedDateTime: String,
    val weatherStatus: String = "Unknown"
)