# Taqs360-Android-Kotlin-App

# 🌦️ Taqs360 - Weather Forecast Android App

**Taqs360** is a feature-rich Android application developed using **Kotlin** that delivers accurate weather forecasts with support for **alarms, search, favorite locations, map integration, and more**. It follows the **MVVM (Model-View-ViewModel)** architecture and leverages modern Android development best practices, making it scalable and testable.

---

## 🚀 Features

- **📍 Location-based weather** forecast
- **📡 Real-time updates** using remote APIs
- **📅 Alarms & notifications** for extreme weather alerts
- **🔍 Search** for locations using Nominatim API
- **🗺️ Map view** for selecting and visualizing locations
- **❤️ Favorite Locations** for quick access
- **🧠 Clean Architecture** with repository pattern
- **💾 Local caching** via Room DB
- **📊 Graphs** for temperature trends
- **🌐 Network monitoring**
- **🛠️ MVVM + Repository Pattern** throughout

---

## 🧠 Architecture Overview

- **Model:** Handles data operations (local & remote)
- **View:** UI components and user interaction
- **ViewModel:** Exposes data to views & handles logic
- **Repository:** Mediates between data sources (local/db/network)

Each feature is **modularized** with its own:
- `model` (data classes, DAO, data sources, repository)
- `view` (Activities, Fragments, Adapters)
- `viewmodel` (ViewModel and Factory)

---

## 🗂️ Project Structure

```bash
.
├── alarm
│   ├── model
│   │   ├── AlarmData.kt
│   │   ├── datasource
│   │   │   ├── AlarmDao.kt
│   │   │   ├── AlarmLocalDataSourceImpl.kt
│   │   │   ├── AlarmLocalDataSource.kt
│   │   │   └── AppDatabase.kt
│   │   ├── LocationDataConverter.kt
│   │   └── repository
│   │       ├── AlarmRepositoryImpl.kt
│   │       └── AlarmRepository.kt
│   ├── receiver
│   │   └── DismissAlarmReceiver.kt
│   ├── view
│   │   ├── AlarmAdapter.kt
│   │   ├── DatePickerFragment.kt
│   │   ├── TimePickerFragment.kt
│   │   └── WeatherAlertsActivity.kt
│   └── viewmodel
│       ├── AlarmViewModelFactory.kt
│       ├── AlarmViewModel.kt
│       └── AlarmWorker.kt
├── favorite
│   ├── model
│   │   ├── dao
│   │   │   └── FavoriteLocationDao.kt
│   │   ├── data
│   │   │   └── FavoriteLocation.kt
│   │   ├── database
│   │   │   └── AppDatabase.kt
│   │   ├── datasource
│   │   │   ├── FavoriteLocalDataSourceImpl.kt
│   │   │   └── FavoriteLocalDataSource.kt
│   │   └── repository
│   │       ├── FavoriteRepositoryImpl.kt
│   │       └── FavoriteRepository.kt
│   ├── view
│   │   ├── FavoriteActivity.kt
│   │   └── FavoriteAdapter.kt
│   └── viewmodel
│       ├── FavoriteViewModelFactory.kt
│       └── FavoriteViewModel.kt
├── home
│   ├── model
│   │   ├── local
│   │   │   ├── WeatherDao.kt
│   │   │   ├── WeatherDatbase.kt
│   │   │   ├── WeatherEntity.kt
│   │   │   ├── WeatherLocalDataSourceImpl.kt
│   │   │   ├── WeatherLocalDataSource.kt
│   │   │   └── WeatherTypeConverters.kt
│   │   ├── pojo
│   │   │   ├── WeatherDataClasses.kt
│   │   │   └── WeatherData.kt
│   │   ├── remote
│   │   │   ├── RetrofitHelper.kt
│   │   │   ├── WeatherRemoteDataSourceImpl.kt
│   │   │   ├── WeatherRemoteDataSource.kt
│   │   │   └── WeatherService.kt
│   │   ├── repository
│   │   │   ├── WeatherRepositoryImpl.kt
│   │   │   └── WeatherRepository.kt
│   │   └── uidata
│   │       └── WeatherUiData.kt
│   ├── util
│   │   ├── WeatherUnitConverter.java
│   │   └── WeatherUtils.kt
│   ├── view
│   │   ├── ForecastAdapter.kt
│   │   ├── GraphDrawer.java
│   │   ├── TemperatureGraphView.kt
│   │   ├── ThreeHoursForecastAdapter.kt
│   │   └── WeatherActivity.kt
│   └── viewmodel
│       ├── WeatherViewModelFactory.kt
│       └── WeatherViewModel.kt
├── location
│   ├── LocationDataSource.kt
│   ├── LocationResult.kt
│   └── PermissionHandler.kt
├── map
│   ├── model
│   │   ├── datasource
│   │   │   ├── MapLocalDataSourceImpl.kt
│   │   │   └── MapLocalDataSource.kt
│   │   ├── LocationData.kt
│   │   └── repository
│   │       ├── MapRepositoryImpl.kt
│   │       └── MapRepository.kt
│   ├── util
│   │   └── MapUtils.kt
│   ├── view
│   │   └── MapFragment.kt
│   └── viewmodel
│       ├── MapViewModelFactory.kt
│       └── MapViewModel.kt
├── network
│   └── NetworkMonitor.kt
├── search
│   ├── model
│   │   ├── datasource
│   │   │   ├── NominatimService.kt
│   │   │   ├── SearchRemoteDataSourceImpl.kt
│   │   │   └── SearchRemoteDataSource.kt
│   │   ├── NominatimResult.kt
│   │   ├── Place.kt
│   │   └── repository
│   │       ├── SearchRepositoryImpl.kt
│   │       └── SearchRepository.kt
│   ├── util
│   │   └── RetrofitHelper.kt
│   ├── view
│   │   ├── SearchActivity.kt
│   │   └── SearchAdapter.kt
│   └── viewmodel
│       ├── SearchViewModelFactory.kt
│       └── SearchViewModel.kt
├── settings
│   ├── model
│   │   ├── local
│   │   │   ├── SettingsLocalDataSourceImpl.kt
│   │   │   └── SettingsLocalDataSource.kt
│   │   ├── remote
│   │   │   ├── SettingsRemoteDataSourceImpl.kt
│   │   │   └── SettingsRemoteDataSource.kt
│   │   ├── repository
│   │   │   ├── SettingsRepositoryImpl.kt
│   │   │   └── SettingsRepository.kt
│   │   └── Settings.kt
│   ├── view
│   │   ├── SettingsActivity.kt
│   │   └── SettingsFragment.kt
│   └── viewmodel
│       ├── SettingsViewModelFactory.kt
│       └── SettingsViewModel.kt
└── splash
    ├── model
    │   └── repository
    │       └── SplashRepository.kt
    ├── view
    │   └── SplashActivity.kt
    └── viewmodel
        └── SplashViewModel.kt


        # 🌦️ Weather App - Full Architecture

```mermaid
classDiagram
    %% ====================== CORE COMPONENTS ======================
    class NetworkMonitor
    class LocationDataSource
    class PermissionHandler
    NetworkMonitor --> NetworkStatus
    LocationDataSource --> LocationResult
    LocationDataSource --> PermissionHandler

    %% ====================== WEATHER FEATURE ======================
    class WeatherActivity
    class WeatherViewModel
    class WeatherRepository
    class WeatherRemoteDataSource
    class WeatherLocalDataSource
    WeatherActivity --> WeatherViewModel
    WeatherViewModel --> WeatherRepository
    WeatherRepository --> WeatherRemoteDataSource
    WeatherRepository --> WeatherLocalDataSource

    %% ====================== ALARM FEATURE ======================
    class WeatherAlertsActivity
    class AlarmViewModel
    class AlarmRepository
    class AlarmLocalDataSource
    WeatherAlertsActivity --> AlarmViewModel
    AlarmViewModel --> AlarmRepository
    AlarmRepository --> AlarmLocalDataSource

    %% ====================== FAVORITES FEATURE ======================
    class FavoriteActivity
    class FavoriteViewModel
    class FavoriteRepository
    class FavoriteLocalDataSource
    FavoriteActivity --> FavoriteViewModel
    FavoriteViewModel --> FavoriteRepository
    FavoriteRepository --> FavoriteLocalDataSource

    %% ====================== MAP FEATURE ======================
    class MapFragment
    class MapViewModel
    class MapRepository
    MapFragment --> MapViewModel
    MapViewModel --> MapRepository

    %% ====================== SEARCH FEATURE ======================
    class SearchActivity
    class SearchViewModel
    class SearchRepository
    SearchActivity --> SearchViewModel
    SearchViewModel --> SearchRepository

    %% ====================== SETTINGS FEATURE ======================
    class SettingsActivity
    class SettingsViewModel
    class SettingsRepository
    SettingsActivity --> SettingsViewModel
    SettingsViewModel --> SettingsRepository

    %% ====================== SPLASH FEATURE ======================
    class SplashActivity
    class SplashViewModel
    SplashActivity --> SplashViewModel
