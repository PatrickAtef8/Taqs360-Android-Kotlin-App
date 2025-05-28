package com.example.taqs360.alarm.model.datasource

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.taqs360.alarm.model.AlarmData

@Dao
interface AlarmDao {
    @Insert
    suspend fun insert(alarm: AlarmData)

    @Delete
    suspend fun delete(alarm: AlarmData)

    @Query("SELECT * FROM alarms")
    suspend fun getAllAlarms(): List<AlarmData>

}