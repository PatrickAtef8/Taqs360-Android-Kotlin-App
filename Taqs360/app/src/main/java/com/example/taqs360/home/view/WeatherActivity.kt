package com.example.taqs360.home.view

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.ScaleAnimation
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.lottie.LottieAnimationView
import com.example.taqs360.R
import com.example.taqs360.databinding.ActivityWeatherBinding
import com.example.taqs360.home.model.repository.WeatherRepositoryImpl
import com.example.taqs360.home.model.network.RetrofitHelper
import com.example.taqs360.home.model.network.WeatherRemoteDataSourceImpl
import com.example.taqs360.home.model.network.WeatherService
import com.example.taqs360.home.viewmodel.WeatherViewModel
import com.example.taqs360.home.viewmodel.WeatherViewModelFactory
import com.example.taqs360.location.LocationDataSource
import com.example.taqs360.map.model.datasource.MapLocalDataSourceImpl
import com.example.taqs360.map.model.repository.MapRepositoryImpl
import com.example.taqs360.map.view.MapFragment
import com.example.taqs360.map.viewmodel.MapViewModel
import com.example.taqs360.map.viewmodel.MapViewModelFactory
import com.example.taqs360.settings.SettingsActivity
import com.example.taqs360.settings.model.local.SettingsLocalDataSourceImpl
import com.example.taqs360.settings.model.remote.SettingsRemoteDataSourceImpl
import com.example.taqs360.settings.model.repository.SettingsRepositoryImpl
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class WeatherActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWeatherBinding
    private lateinit var viewModel: WeatherViewModel
    private lateinit var mapViewModel: MapViewModel
    private lateinit var forecastAdapter: ForecastAdapter
    private lateinit var threeHoursForecastAdapter: ThreeHoursForecastAdapter
    private val TAG = "WeatherActivity"
    private var isReturningFromMap = false

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        viewModel.onPermissionResult(isGranted, openMapFragment = viewModel.isMapRequestPending())
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeatherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize WeatherViewModel with updated factory
        viewModel = ViewModelProvider(
            this,
            WeatherViewModelFactory(
                WeatherRepositoryImpl(
                    WeatherRemoteDataSourceImpl(
                        RetrofitHelper.getInstance().create(WeatherService::class.java),
                        SettingsRepositoryImpl(
                            SettingsLocalDataSourceImpl(this),
                            SettingsRemoteDataSourceImpl()
                        )
                    )
                ),
                LocationDataSource(this),
                SettingsRepositoryImpl(
                    SettingsLocalDataSourceImpl(this),
                    SettingsRemoteDataSourceImpl()
                ),
                this // Pass context
            )
        ).get(WeatherViewModel::class.java)

        mapViewModel = ViewModelProvider(
            this,
            MapViewModelFactory(
                MapRepositoryImpl(
                    MapLocalDataSourceImpl(this),
                    LocationDataSource(this)
                )
            )
        ).get(MapViewModel::class.java)

        // Apply locale on creation
        val locale = viewModel.getLocale()
        Locale.setDefault(locale)
        val config = resources.configuration.apply {
            setLocale(locale)
            setLayoutDirection(locale)
        }
        resources.updateConfiguration(config, resources.displayMetrics)

        binding.ivMenu.visibility = View.GONE
        binding.ivMap.visibility = View.GONE

        setupLottieErrorHandling(binding.rainyAnimation, "Rainy Animation")
        setupLottieErrorHandling(binding.cloudyAnimation, "Cloudy Animation")

        binding.ivMenu.setOnClickListener {
            animateClick(binding.ivMenu)
            binding.drawerLayout.openDrawer(binding.navView)
        }

        binding.ivMap.setOnClickListener {
            animateClick(binding.ivMap)
            viewModel.openMapFragment()
        }

        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_settings -> {
                    Log.d(TAG, "Navigating to SettingsActivity")
                    binding.drawerLayout.closeDrawers()
                    startActivity(Intent(this, SettingsActivity::class.java))
                    // Removed finish() to keep WeatherActivity in back stack
                    true
                }
                else -> false
            }
        }

        setupAdapters()

        viewModel.weatherUiData.observe(this) { weather ->
            binding.progressBar.visibility = View.GONE
            binding.contentLayout.visibility = View.VISIBLE

            binding.tvLocation.text = weather.location
            binding.chipDate.text = "${weather.date}, ${weather.currentTime}"
            binding.tvTemp.text = viewModel.formatTemperature(weather.currentTemp.toFloat())
            binding.tvDescription.text = weather.description
            binding.tvFeelsLike.text = getString(
                R.string.feels_like,
                viewModel.formatTemperature(weather.feelsLike.toFloat()),
                viewModel.formatTemperature(weather.currentTemp.toFloat()),
                weather.description
            )
            binding.tvHumidity.text = getString(R.string.humidity, weather.humidity)
            binding.tvWind.text = viewModel.formatWindSpeed(weather.windSpeed.toFloat())
            binding.tvVisibility.text = viewModel.formatVisibility(weather.visibility)
            binding.ivWeatherIconCurrent.setImageResource(weather.currentWeatherIcon)

            forecastAdapter.submitList(weather.forecasts)
            if (weather.forecasts.isNotEmpty()) {
                threeHoursForecastAdapter.submitList(
                    weather.forecasts[0].forecastsForDay,
                    weather.timezoneId
                )
                binding.temperatureGraph.setForecasts(weather.forecasts[0].forecastsForDay)
                binding.tvHourlyTitle.text = getString(R.string.three_hours_forecast_today)
            }

            val timeZone = TimeZone.getTimeZone(weather.timezoneId)
            val calendar = Calendar.getInstance(timeZone)
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val isNight = hour !in 5..16
            val backgroundRes = when (hour) {
                in 5..7, in 17..19 -> R.drawable.bg_gradient_sunrise_sunset
                in 8..16 -> R.drawable.bg_day
                else -> R.drawable.bg_night
            }
            binding.root.setBackgroundResource(backgroundRes)

            binding.sunlightAnimation.visibility = View.GONE
            binding.sunlightAnimation.clearAnimation()
            binding.rainyAnimation.visibility = View.GONE
            binding.cloudyAnimation.visibility = View.GONE
            binding.snowyAnimation.visibility = View.GONE
            binding.snowyAnimation.clearAnimation()
            binding.starsAnimation.visibility = View.GONE
            binding.starsAnimation.clearAnimation()

            when {
                weather.description.contains("clear", ignoreCase = true) -> {
                    binding.sunlightAnimation.visibility = View.VISIBLE
                    val sunlightAnimation = AnimationUtils.loadAnimation(this, R.anim.sunlight_flash_anim)
                    binding.sunlightAnimation.startAnimation(sunlightAnimation)
                }
                weather.description.contains("rain", ignoreCase = true) -> {
                    binding.rainyAnimation.visibility = View.VISIBLE
                }
                weather.description.contains("cloud", ignoreCase = true) -> {
                    binding.cloudyAnimation.visibility = View.VISIBLE
                }
                weather.description.contains("snow", ignoreCase = true) -> {
                    binding.snowyAnimation.visibility = View.VISIBLE
                    val snowyAnimation = AnimationUtils.loadAnimation(this, R.anim.snowy_anim)
                    binding.snowyAnimation.startAnimation(snowyAnimation)
                }
            }

            if (isNight) {
                binding.starsAnimation.visibility = View.VISIBLE
                val starsAnimation = AnimationUtils.loadAnimation(this, R.anim.stars_anim)
                binding.starsAnimation.startAnimation(starsAnimation)
            }

            binding.ivMenu.visibility = View.VISIBLE
            binding.ivMap.visibility = View.VISIBLE

            if (savedInstanceState == null && !isReturningFromMap) {
                animateViews(
                    binding.ivMenu,
                    binding.ivMap,
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
            }
            isReturningFromMap = false
        }

        mapViewModel.selectedLocation.observe(this) { location ->
            if (location != null) {
                lifecycleScope.launch {
                    viewModel.saveLocationMode("map")
                }
                viewModel.fetchWeatherForLocation(location.latitude, location.longitude)
                mapViewModel.clearSelectedLocation()
                isReturningFromMap = true
            }
        }

        viewModel.error.observe(this) { error ->
            if (error != null) {
                binding.progressBar.visibility = View.GONE
                binding.contentLayout.visibility = View.GONE
                binding.ivMenu.visibility = View.GONE
                binding.ivMap.visibility = View.GONE
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                if (error.contains("Location services are disabled")) {
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
                viewModel.clearError()
            }
        }

        viewModel.requestPermissionEvent.observe(this) { request ->
            if (request) {
                requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                viewModel.clearPermissionRequest()
            }
        }

        viewModel.openMapFragmentEvent.observe(this) { open ->
            if (open) {
                openMapFragment()
                viewModel.clearMapFragmentEvent()
            }
        }

        if (savedInstanceState == null) {
            if (viewModel.getLocationMode() == "gps") {
                viewModel.requestLocationAndFetchWeather()
            } else {
                viewModel.lastLocation.value?.let { location ->
                    viewModel.fetchWeatherForLocation(location.latitude, location.longitude)
                }
            }
        } else {
            viewModel.lastLocation.value?.let { location ->
                viewModel.fetchWeatherForLocation(location.latitude, location.longitude)
            }
        }
    }

    override fun onBackPressed() {
        Log.d(TAG, "Mobile back button pressed, backStackEntryCount: ${supportFragmentManager.backStackEntryCount}")
        if (supportFragmentManager.backStackEntryCount > 0) {
            // Pop fragments (e.g., MapFragment) if present
            super.onBackPressed()
        } else if (binding.drawerLayout.isDrawerOpen(binding.navView)) {
            // Close drawer if open
            binding.drawerLayout.closeDrawer(binding.navView)
        } else {
            // Neutral behavior: minimize app to background
            moveTaskToBack(true)
        }
    }

    override fun onResume() {
        super.onResume()
        // Apply locale in case it's changed externally
        val locale = viewModel.getLocale()
        Locale.setDefault(locale)
        val config = resources.configuration.apply {
            setLocale(locale)
            setLayoutDirection(locale)
        }
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    private fun setupAdapters() {
        forecastAdapter = ForecastAdapter(
            tempUnit = viewModel.getTemperatureUnit(),
            isArabic = viewModel.getLocale().language == "ar"
        ).apply {
            setOnDayClickListener { position, forecastsForDay, itemView ->
                animateClick(itemView)
                threeHoursForecastAdapter.submitList(
                    forecastsForDay,
                    viewModel.weatherUiData.value?.timezoneId ?: "UTC"
                )
                binding.temperatureGraph.setForecasts(forecastsForDay)
                val title = if (position == 0) getString(R.string.three_hours_forecast_today)
                else getString(R.string.three_hours_forecast, viewModel.formatDate(
                    forecastsForDay[0].dt,
                    "EEEE",
                    TimeZone.getTimeZone(viewModel.weatherUiData.value?.timezoneId ?: "UTC")
                ))
                binding.tvHourlyTitle.text = title
                animateViews(
                    binding.tvHourlyTitle,
                    binding.rvHourlyForecast,
                    binding.temperatureGraph
                )
            }
        }
        binding.rvForecast.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvForecast.adapter = forecastAdapter

        threeHoursForecastAdapter = ThreeHoursForecastAdapter(
            tempUnit = viewModel.getTemperatureUnit(),
            isArabic = viewModel.getLocale().language == "ar"
        )
        binding.rvHourlyForecast.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvHourlyForecast.adapter = threeHoursForecastAdapter
    }

    private fun setupLottieErrorHandling(animationView: LottieAnimationView, animationName: String) {
        animationView.addLottieOnCompositionLoadedListener { composition ->
            if (composition == null) {
                animationView.visibility = View.GONE
            }
        }
    }

    private fun openMapFragment() {
        supportFragmentManager.commit {
            setCustomAnimations(
                R.anim.slide_in_from_right,
                R.anim.slide_out_to_left,
                R.anim.slide_in_from_left,
                R.anim.slide_out_to_right
            )
            replace(R.id.weather_root, MapFragment())
            addToBackStack(null)
        }
    }

    private fun animateViews(vararg views: View) {
        views.forEachIndexed { index, view ->
            val animation = AnimationUtils.loadAnimation(this, R.anim.slide_in_from_top_wave).apply {
                startOffset = (index * 100).toLong()
            }
            view.startAnimation(animation)
        }
    }

    private fun animateClick(view: View) {
        val scaleAnim = ScaleAnimation(
            0.9f, 1f, 0.9f, 1f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 150
            fillAfter = true
        }
        view.startAnimation(scaleAnim)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isReturningFromMap", isReturningFromMap)
    }
}