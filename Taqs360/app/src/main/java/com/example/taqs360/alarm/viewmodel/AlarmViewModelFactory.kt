package com.example.taqs360.alarm.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.taqs360.alarm.model.datasource.AlarmLocalDataSourceImpl
import com.example.taqs360.alarm.model.repository.AlarmRepositoryImpl

class AlarmViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlarmViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AlarmViewModel(AlarmRepositoryImpl(AlarmLocalDataSourceImpl(context)), context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}