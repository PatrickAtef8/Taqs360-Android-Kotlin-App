package com.example.taqs360.home.view

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taqs360.R
import com.example.taqs360.databinding.ActivityWeatherBinding
import com.example.taqs360.home.model.repository.WeatherRepositoryImpl
import com.example.taqs360.home.model.network.RetrofitHelper
import com.example.taqs360.home.model.network.WeatherRemoteDataSourceImpl
import com.example.taqs360.home.model.network.WeatherService
import com.example.taqs360.home.util.WeatherUtils
import com.example.taqs360.home.viewmodel.WeatherViewModel
import com.example.taqs360.home.viewmodel.WeatherViewModelFactory
import com.example.taqs360.location.LocationDataSource
import com.example.taqs360.location.PermissionHandler

class WeatherActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWeatherBinding
    private lateinit var viewModel: WeatherViewModel
    private lateinit var forecastAdapter: ForecastAdapter
    private lateinit var threeHoursForecastAdapter: ThreeHoursForecastAdapter
    private val permissionHandler = PermissionHandler()
    private val TAG = "WeatherActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeatherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(binding.navView)
        }

        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_settings -> {
                    binding.drawerLayout.closeDrawers()
                    true
                }
                else -> false
            }
        }

        viewModel = ViewModelProvider(
            this,
            WeatherViewModelFactory(
                WeatherRepositoryImpl(
                    WeatherRemoteDataSourceImpl(
                        RetrofitHelper.getInstance().create(WeatherService::class.java)
                    )
                ),
                LocationDataSource(this)
            )
        ).get(WeatherViewModel::class.java)

        forecastAdapter = ForecastAdapter().apply {
            setOnDayClickListener { position, forecastsForDay ->
                threeHoursForecastAdapter.submitList(forecastsForDay, viewModel.weatherUiData.value?.timezoneOffset ?: 0)
                binding.temperatureGraph.setForecasts(forecastsForDay)
                val title = if (position == 0) "Three-Hours Forecast (Today)"
                else "Three-Hours Forecast (${viewModel.getFormattedDate(forecastsForDay[0].dt)})"
                binding.tvHourlyTitle.text = title
            }
        }
        binding.rvForecast.apply {
            layoutManager = LinearLayoutManager(this@WeatherActivity, LinearLayoutManager.VERTICAL, false)
            adapter = forecastAdapter
        }

        threeHoursForecastAdapter = ThreeHoursForecastAdapter()
        binding.rvHourlyForecast.apply {
            layoutManager = LinearLayoutManager(this@WeatherActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = threeHoursForecastAdapter
        }

        viewModel.weatherUiData.observe(this) { weather ->
            Log.d(TAG, "Weather data received: ${weather.location}")
            binding.progressBar.visibility = android.view.View.GONE
            binding.contentLayout.visibility = android.view.View.VISIBLE
            binding.tvLocation.text = weather.location
            binding.chipDate.text = "${weather.date}, ${weather.currentTime}" // Date and time in single line
            binding.tvTemp.text = WeatherUtils.formatTemperature(weather.currentTemp.toFloat())
            binding.tvDescription.text = weather.description
            binding.tvFeelsLike.text = "Feels like ${WeatherUtils.formatTemperature(weather.feelsLike.toFloat())}, actual ${WeatherUtils.formatTemperature(weather.currentTemp.toFloat())}. ${weather.description}."
            binding.tvHumidity.text = "${weather.humidity}%"
            binding.tvWind.text = WeatherUtils.formatWindSpeed(weather.windSpeed)
            binding.tvVisibility.text = WeatherUtils.formatVisibility(weather.visibility)
            binding.ivWeatherIconCurrent.setImageResource(weather.currentWeatherIcon)

            forecastAdapter.submitList(weather.forecasts)
            if (weather.forecasts.isNotEmpty()) {
                threeHoursForecastAdapter.submitList(weather.forecasts[0].forecastsForDay, weather.timezoneOffset)
                binding.temperatureGraph.setForecasts(weather.forecasts[0].forecastsForDay)
                binding.tvHourlyTitle.text = "Three-Hours Forecast (Today)"
            }

            if (savedInstanceState == null) {
                val viewsToAnimate = listOf(
                    binding.tvLocation,
                    binding.chipDate,
                    binding.tvTemp,
                    binding.ivWeatherIconCurrent,
                    binding.tvDescription,
                    binding.tvFeelsLike,
                    binding.cardStats,
                    binding.cardForecast,
                    binding.cardHourlyForecast
                )
                viewsToAnimate.forEachIndexed { index, view ->
                    val animation = AnimationUtils.loadAnimation(this, R.anim.slide_in_from_top_wave).apply {
                        startOffset = (index * 100).toLong()
                    }
                    view.startAnimation(animation)
                }
            }
        }

        viewModel.error.observe(this) { error ->
            Log.d(TAG, "Error received: $error")
            binding.progressBar.visibility = android.view.View.GONE
            binding.contentLayout.visibility = android.view.View.GONE
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            if (error.contains("Location services are disabled")) {
                Log.d(TAG, "Prompting user to enable location services")
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
        }

        Log.d(TAG, "Checking initial permissions")
        if (permissionHandler.hasLocationPermissions(this)) {
            Log.d(TAG, "Permissions granted, fetching weather")
            binding.progressBar.visibility = android.view.View.VISIBLE
            binding.contentLayout.visibility = android.view.View.GONE
            viewModel.fetchWeather()
        } else {
            Log.d(TAG, "Requesting permissions")
            binding.progressBar.visibility = android.view.View.GONE
            binding.contentLayout.visibility = android.view.View.GONE
            permissionHandler.requestLocationPermissions(this)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "onRequestPermissionsResult: requestCode=$requestCode")
        permissionHandler.handlePermissionsResult(
            requestCode,
            grantResults,
            onGranted = {
                Log.d(TAG, "Permissions granted, fetching weather")
                binding.progressBar.visibility = android.view.View.VISIBLE
                binding.contentLayout.visibility = android.view.View.GONE
                viewModel.fetchWeather()
            },
            onDenied = {
                Log.d(TAG, "Permissions denied")
                binding.progressBar.visibility = android.view.View.GONE
                binding.contentLayout.visibility = android.view.View.GONE
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_LONG).show()
            }
        )
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: checking permissions")
        if (permissionHandler.hasLocationPermissions(this)) {
            Log.d(TAG, "Permissions granted in onResume, fetching weather")
            binding.progressBar.visibility = android.view.View.VISIBLE
            binding.contentLayout.visibility = android.view.View.GONE
            viewModel.fetchWeather()
        }
    }
}