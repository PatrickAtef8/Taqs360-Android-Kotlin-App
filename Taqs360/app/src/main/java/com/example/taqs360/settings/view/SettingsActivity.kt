package com.example.taqs360.settings.view

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.taqs360.R
import com.example.taqs360.databinding.ActivitySettingsBinding
import com.example.taqs360.home.view.WeatherActivity
import com.example.taqs360.settings.model.local.SettingsLocalDataSourceImpl
import com.example.taqs360.settings.model.remote.SettingsRemoteDataSourceImpl
import com.example.taqs360.settings.model.repository.SettingsRepository
import com.example.taqs360.settings.model.repository.SettingsRepositoryImpl
import com.example.taqs360.settings.viewmodel.SettingsViewModel
import com.example.taqs360.settings.viewmodel.SettingsViewModelFactory
import java.util.*

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var viewModel: SettingsViewModel
    private lateinit var settingsRepository: SettingsRepository
    private val TAG = "SettingsActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        settingsRepository = SettingsRepositoryImpl(
            SettingsLocalDataSourceImpl(this),
            SettingsRemoteDataSourceImpl()
        )

        viewModel = ViewModelProvider(
            this,
            SettingsViewModelFactory(SettingsRepositoryImpl(
                SettingsLocalDataSourceImpl(this),
                SettingsRemoteDataSourceImpl()
            ))
        ).get(SettingsViewModel::class.java)

        val locale = viewModel.getLocale()
        Log.d(TAG, "Setting locale to: $locale")
        Locale.setDefault(locale)
        val config = resources.configuration.apply {
            setLocale(locale)
            setLayoutDirection(locale)
        }
        resources.updateConfiguration(config, resources.displayMetrics)

        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.title = getString(R.string.settings_title)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.settings_container, SettingsFragment())
                .commit()
        }

        binding.toolbar.setNavigationOnClickListener {
            Log.d(TAG, "Toolbar back button clicked")
            val intent = Intent(this, WeatherActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("force_gps_refresh", settingsRepository.getLocationMode() == "gps")
            }
            startActivity(intent)
            finish()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.d(TAG, "Configuration changed, checking language setting")
        if (viewModel.getLanguage() == "system") {
            Log.d(TAG, "System language selected, restarting activity")
            val intent = Intent(this, SettingsActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            startActivity(intent)
            finish()
        } else {
            Log.d(TAG, "Non-system language selected, retaining current locale")
            val locale = viewModel.getLocale()
            Locale.setDefault(locale)
            val config = resources.configuration.apply {
                setLocale(locale)
                setLayoutDirection(locale)
            }
            resources.updateConfiguration(config, resources.displayMetrics)
        }
    }

    override fun onBackPressed() {
        Log.d(TAG, "Mobile back button pressed, backStackEntryCount: ${supportFragmentManager.backStackEntryCount}")
        if (supportFragmentManager.backStackEntryCount > 0) {
            super.onBackPressed()
        } else {
            val intent = Intent(this, WeatherActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("force_gps_refresh", settingsRepository.getLocationMode() == "gps")
            }
            startActivity(intent)
            finish()
        }
    }
}