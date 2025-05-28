package com.example.taqs360.home.view

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.ScaleAnimation
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
import com.example.taqs360.home.model.local.WeatherDatabase
import com.example.taqs360.home.model.local.WeatherLocalDataSourceImpl
import com.example.taqs360.home.model.remote.RetrofitHelper
import com.example.taqs360.home.model.remote.WeatherRemoteDataSourceImpl
import com.example.taqs360.home.model.remote.WeatherService
import com.example.taqs360.home.model.repository.WeatherRepositoryImpl
import com.example.taqs360.home.viewmodel.WeatherViewModel
import com.example.taqs360.home.viewmodel.WeatherViewModelFactory
import com.example.taqs360.location.LocationDataSource
import com.example.taqs360.map.model.datasource.MapLocalDataSourceImpl
import com.example.taqs360.map.model.repository.MapRepositoryImpl
import com.example.taqs360.map.view.MapFragment
import com.example.taqs360.map.viewmodel.MapViewModel
import com.example.taqs360.map.viewmodel.MapViewModelFactory
import com.example.taqs360.network.NetworkMonitor
import com.example.taqs360.settings.model.local.SettingsLocalDataSourceImpl
import com.example.taqs360.settings.model.remote.SettingsRemoteDataSourceImpl
import com.example.taqs360.settings.model.repository.SettingsRepository
import com.example.taqs360.settings.model.repository.SettingsRepositoryImpl
import com.example.taqs360.alarm.view.WeatherAlertsActivity
import com.example.taqs360.favorite.view.FavoriteActivity
import com.example.taqs360.settings.view.SettingsActivity
import kotlinx.coroutines.launch
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.core.content.ContextCompat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class WeatherActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWeatherBinding
    private lateinit var viewModel: WeatherViewModel
    private lateinit var mapViewModel: MapViewModel
    private lateinit var forecastAdapter: ForecastAdapter
    private lateinit var threeHoursForecastAdapter: ThreeHoursForecastAdapter
    private lateinit var networkMonitor: NetworkMonitor
    private lateinit var settingsRepository: SettingsRepository
    private val TAG = "WeatherActivity"
    private var isReturningFromMap = false

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        viewModel.onPermissionResult(isGranted, openMapFragment = viewModel.isMapRequestPending())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeatherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        networkMonitor = NetworkMonitor(this)

        settingsRepository = SettingsRepositoryImpl(
            SettingsLocalDataSourceImpl(this),
            SettingsRemoteDataSourceImpl()
        )

        viewModel = ViewModelProvider(
            this,
            WeatherViewModelFactory(
                WeatherRepositoryImpl(
                    WeatherRemoteDataSourceImpl(
                        RetrofitHelper.getInstance().create(WeatherService::class.java),
                        settingsRepository
                    ),
                    WeatherLocalDataSourceImpl(WeatherDatabase.getDatabase(this).weatherDao())
                ),
                LocationDataSource(this),
                settingsRepository,
                this,
                networkMonitor
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

        val locale = viewModel.getLocale()
        Locale.setDefault(locale)
        val config = resources.configuration.apply {
            setLocale(locale)
            setLayoutDirection(locale)
        }
        resources.updateConfiguration(config, resources.displayMetrics)

        binding.ivMenu.visibility = View.GONE
        binding.ivMap.visibility = View.GONE

        setupLottieErrorHandling(binding.rainyAnimation)
        setupLottieErrorHandling(binding.cloudyAnimation)

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
                R.id.nav_favorites -> {
                    Log.d(TAG, "Navigating to FavoriteActivity")
                    binding.drawerLayout.closeDrawers()
                    startActivity(Intent(this, FavoriteActivity::class.java))
                    true
                }
                R.id.nav_alerts -> {
                    Log.d(TAG, "Navigating to WeatherAlertsActivity")
                    binding.drawerLayout.closeDrawers()
                    startActivity(Intent(this, WeatherAlertsActivity::class.java))
                    true
                }
                R.id.nav_settings -> {
                    Log.d(TAG, "Navigating to SettingsActivity")
                    binding.drawerLayout.closeDrawers()
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }

        setupAdapters()

        binding.swipeRefreshLayout.setOnRefreshListener {
            Log.d(TAG, "Swipe-to-refresh triggered, forcing real-time GPS location fetch")
            lifecycleScope.launch {
                viewModel.saveLocationMode("gps")
                viewModel.clearLastLocation()
                Log.d(TAG, "Location mode set to gps, cleared last location")
                viewModel.requestLocationAndFetchWeather()
            }
        }

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
            binding.tvWind.text = viewModel.formatWindSpeed(weather.windSpeed)
            binding.tvVisibility.text = viewModel.formatVisibility(weather.visibility)
            binding.ivWeatherIconCurrent.setImageResource(weather.currentWeatherIcon)

            forecastAdapter.submitList(weather.forecasts)
            forecastAdapter.updateSettings(viewModel.getTemperatureUnit(), viewModel.getLocale().language == "ar")
            if (weather.forecasts.isNotEmpty()) {
                threeHoursForecastAdapter.submitList(
                    weather.forecasts[0].forecastsForDay,
                    weather.timezoneId,
                    weather.cachedUnits,
                    viewModel.getWindSpeedUnit()
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
            val navHeader = binding.navView.getHeaderView(0)
            navHeader?.setBackgroundResource(backgroundRes)
            binding.navView.setBackgroundResource(backgroundRes)

            if (isNight) {
                binding.navView.itemTextColor = ColorStateList.valueOf(Color.WHITE)
                binding.navView.itemIconTintList = ColorStateList.valueOf(Color.WHITE)
            } else {
                binding.navView.itemTextColor = ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.colorOnSurface)
                )
                binding.navView.itemIconTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.colorOnSurface)
                )
            }

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

            if (binding.swipeRefreshLayout.isRefreshing) {
                Log.d(TAG, "Weather data updated for location: ${weather.location}, stopping refresh indicator")
                binding.swipeRefreshLayout.isRefreshing = false
            }

            if (weather.isFromCache) {
                Toast.makeText(this, "Showing cached weather data", Toast.LENGTH_SHORT).show()
            }
        }

        networkMonitor.isConnected.observe(this) { isConnected ->
            binding.swipeRefreshLayout.isEnabled = isConnected
            if (!isConnected) {
                Toast.makeText(this, "No internet connection. Using cached data.", Toast.LENGTH_SHORT).show()
            }
        }

        mapViewModel.selectedLocation.observe(this) { location ->
            if (location != null) {
                lifecycleScope.launch {
                    viewModel.saveLocationMode("map")
                    if (intent.getBooleanExtra("open_map", false)) {
                        val resultIntent = Intent().apply {
                            putExtra("latitude", location.latitude)
                            putExtra("longitude", location.longitude)
                        }
                        setResult(RESULT_OK, resultIntent)
                        finish()
                    } else {
                        viewModel.fetchWeatherForLocation(location.latitude, location.longitude)
                    }
                }
                mapViewModel.clearSelectedLocation()
                isReturningFromMap = true
            }
        }

        viewModel.error.observe(this) { error ->
            if (error.isNullOrEmpty()) return@observe
            binding.progressBar.visibility = View.GONE
            binding.contentLayout.visibility = View.GONE
            binding.ivMenu.visibility = View.GONE
            binding.ivMap.visibility = View.GONE
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            if (error.contains("Location services are disabled")) {
                Log.d(TAG, "Prompting user to enable location services")
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            viewModel.clearError()
            if (binding.swipeRefreshLayout.isRefreshing) {
                Log.d(TAG, "Error occurred: $error, stopping refresh indicator")
                binding.swipeRefreshLayout.isRefreshing = false
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

        settingsRepository.settings.observe(this) { settings ->
            forecastAdapter.updateSettings(settings.temperatureUnit, viewModel.getLocale().language == "ar")
            threeHoursForecastAdapter.updateSettings(
                settings.temperatureUnit,
                viewModel.getLocale().language == "ar",
                settings.windSpeedUnit
            )
            viewModel.lastLocation.value?.let { location ->
                viewModel.fetchWeatherForLocation(location.latitude, location.longitude)
            }
        }

        if (savedInstanceState == null) {
            handleIntent(intent)
        } else {
            if (!intent.hasExtra("latitude") || !intent.hasExtra("longitude")) {
                viewModel.lastLocation.value?.let { location: com.example.taqs360.map.model.LocationData ->
                    viewModel.fetchWeatherForLocation(location.latitude, location.longitude)
                }
            } else {
                handleIntent(intent)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent?.let {
            if (it.getBooleanExtra("force_gps_refresh", false)) {
                lifecycleScope.launch {
                    viewModel.saveLocationMode("gps")
                    viewModel.clearLastLocation()
                    Log.d(TAG, "Forcing GPS refresh: cleared last location and fetching current GPS")
                    viewModel.requestLocationAndFetchWeather()
                }
            } else if (it.getBooleanExtra("open_map", false)) {
                viewModel.openMapFragment()
                Log.d(TAG, "Opening MapFragment for location selection")
            } else if (it.hasExtra("latitude") && it.hasExtra("longitude")) {
                val latitude = it.getDoubleExtra("latitude", 0.0)
                val longitude = it.getDoubleExtra("longitude", 0.0)
                if (latitude != 0.0 && longitude != 0.0) {
                    lifecycleScope.launch {
                        viewModel.saveLocationMode("map")
                    }
                    viewModel.fetchWeatherForLocation(latitude, longitude)
                    Log.d(TAG, "Fetching weather for location: lat=$latitude, lon=$longitude")
                } else {
                    Log.w(TAG, "Invalid location: lat=$latitude, lon=$longitude")
                    proceedWithDefaultLocation()
                }
            } else if (viewModel.getLocationMode() == "gps") {
                viewModel.requestLocationAndFetchWeather()
            } else {
                viewModel.lastLocation.value?.let { location: com.example.taqs360.map.model.LocationData ->
                    viewModel.fetchWeatherForLocation(location.latitude, location.longitude)
                }
            }
        }
    }

    override fun onBackPressed() {
        Log.d(TAG, "Mobile back button pressed, backStackEntryCount: ${supportFragmentManager.backStackEntryCount}")
        if (supportFragmentManager.backStackEntryCount > 0) {
            super.onBackPressed()
        } else if (binding.drawerLayout.isDrawerOpen(binding.navView)) {
            binding.drawerLayout.closeDrawer(binding.navView)
        } else {
            moveTaskToBack(true)
        }
    }

    override fun onResume() {
        super.onResume()
        val locale = viewModel.getLocale()
        Locale.setDefault(locale)
        val config = resources.configuration.apply {
            setLocale(locale)
            setLayoutDirection(locale)
        }
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    override fun onDestroy() {
        super.onDestroy()
        networkMonitor.cleanup()
    }

    private fun proceedWithDefaultLocation() {
        if (viewModel.getLocationMode() == "gps") {
            viewModel.requestLocationAndFetchWeather()
        } else {
            viewModel.lastLocation.value?.let { location: com.example.taqs360.map.model.LocationData ->
                viewModel.fetchWeatherForLocation(location.latitude, location.longitude)
            }
        }
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
                    viewModel.weatherUiData.value?.timezoneId ?: "UTC",
                    viewModel.weatherUiData.value?.cachedUnits ?: viewModel.getTemperatureUnit(),
                    viewModel.getWindSpeedUnit()
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

    private fun setupLottieErrorHandling(animationView: LottieAnimationView) {
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
        ScaleAnimation(
            0.9f, 1f, 0.9f, 1f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 150
            fillAfter = true
            view.startAnimation(this)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isReturningFromMap", isReturningFromMap)
    }
}