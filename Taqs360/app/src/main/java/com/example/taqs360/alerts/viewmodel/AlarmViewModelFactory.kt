package com.example.taqs360.alerts.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.taqs360.alerts.model.datasource.AlarmLocalDataSourceImpl
import com.example.taqs360.alerts.model.repository.AlarmRepositoryImpl

class AlarmViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlarmViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AlarmViewModel(AlarmRepositoryImpl(AlarmLocalDataSourceImpl(context)), context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}