package com.example.taqs360.alerts.model.datasource

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.taqs360.alerts.model.AlarmData
import com.example.taqs360.alerts.model.LocationDataConverter

@Database(entities = [AlarmData::class], version = 1, exportSchema = false)
@TypeConverters(LocationDataConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "taqs360_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}