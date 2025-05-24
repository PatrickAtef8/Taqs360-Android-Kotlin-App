package com.example.taqs360.settings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.taqs360.R
import com.example.taqs360.databinding.ActivitySettingsBinding
import com.example.taqs360.home.view.WeatherActivity

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private val TAG = "SettingsActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
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
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }

    override fun onBackPressed() {
        Log.d(TAG, "Mobile back button pressed, backStackEntryCount: ${supportFragmentManager.backStackEntryCount}")
        if (supportFragmentManager.backStackEntryCount > 0) {
            // Pop fragments using superclass implementation
            super.onBackPressed()
        } else {
            // Recreate WeatherActivity with cleared back stack
            val intent = Intent(this, WeatherActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.toolbar.title = getString(R.string.settings_title)
    }
}