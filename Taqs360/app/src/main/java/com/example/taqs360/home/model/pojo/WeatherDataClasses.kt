package com.example.taqs360.home.model.pojo

data class WeatherResponse(
    val cod: String,
    val message: Int,
    val cnt: Int,
    val list: List<Forecast>,
    val city: City,
    val temperatureUnit: String = "Celsius",
    val windSpeedUnit: String = "meters_sec"
)

data class Forecast(
    val dt: Long,
    val main: Main,
    val weather: List<Weather>,
    val wind: Wind,
    val visibility: Int,
    val dt_txt: String
)

data class Main(
    val temp: Float,
    val feels_like: Float,
    val humidity: Int
)

data class Weather(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)

data class Wind(
    val speed: Float
)

data class City(
    val name: String,
    val country: String,
    val timezone: Int
)