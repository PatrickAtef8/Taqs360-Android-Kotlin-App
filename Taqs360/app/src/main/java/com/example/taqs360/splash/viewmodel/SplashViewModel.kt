package com.example.taqs360.splash.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.taqs360.splash.model.repository.SplashRepository

class SplashViewModel : ViewModel() {
    private val repository = SplashRepository()
    private val _animationResource = MutableLiveData<Int>()
    val animationResource: LiveData<Int> get() = _animationResource

    private val _navigateToMain = MutableLiveData<Boolean>()
    val navigateToMain: LiveData<Boolean> get() = _navigateToMain

    fun loadAnimation() {
        _animationResource.value = repository.getAnimationResource()
    }

    fun triggerNavigation() {
        _navigateToMain.value = true
    }
}