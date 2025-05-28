package com.example.taqs360.alerts.view

import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.animation.ScaleAnimation
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taqs360.alerts.viewmodel.AlarmViewModel
import com.example.taqs360.alerts.viewmodel.AlarmViewModelFactory
import com.example.taqs360.databinding.ActivityWeatherAlertsBinding
import com.example.taqs360.home.view.WeatherActivity
import com.example.taqs360.map.model.LocationData
import com.google.android.material.snackbar.Snackbar
import java.util.Locale

class WeatherAlertsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWeatherAlertsBinding
    private lateinit var viewModel: AlarmViewModel
    private lateinit var alarmAdapter: AlarmAdapter
    private val TAG = "WeatherAlertsActivity"

    private val mapResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val latitude = data?.getDoubleExtra("latitude", 0.0) ?: 0.0
                val longitude = data?.getDoubleExtra("longitude", 0.0) ?: 0.0
                if (latitude != 0.0 && longitude != 0.0) {
                    val locationName =
                        getLocationName(latitude, longitude) ?: "$latitude, $longitude"
                    viewModel.setSelectedLocation(LocationData(latitude, longitude), locationName)
                } else {
                    Log.w(TAG, "Invalid location selected: lat=$latitude, lon=$longitude")
                    showToast("Invalid location selected")
                }
            }
        }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                showToast("Notification permission denied. Alarms may not work.")
            }
        }

    private val exactAlarmPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeatherAlertsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(
            this,
            AlarmViewModelFactory(this)
        ).get(AlarmViewModel::class.java)

        setupRecyclerView()

        // Request notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        // Request exact alarm permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            exactAlarmPermissionLauncher.launch(android.Manifest.permission.SCHEDULE_EXACT_ALARM)
        }

        // Request battery optimization exemption
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = android.net.Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
        }

        binding.fabAddAlarm.setOnClickListener {
            animateClick(binding.fabAddAlarm)
            val intent = Intent(this, WeatherActivity::class.java).apply {
                putExtra("open_map", true)
            }
            mapResultLauncher.launch(intent)
        }

        binding.toolbar.setOnClickListener {
            viewModel.testAlarm()
        }

        viewModel.alarms.observe(this) { alarms ->
            alarmAdapter.submitList(alarms)
            binding.tvEmptyState.visibility = if (alarms.isEmpty()) View.VISIBLE else View.GONE
            Log.d(TAG, "Updated UI with ${alarms.size} alarms")
        }

        viewModel.message.observe(this) { message ->
            showToast(message)
        }

        viewModel.openDatePickerEvent.observe(this) { open ->
            if (open == true) {
                DatePickerFragment().show(supportFragmentManager, "datePicker")
                viewModel.clearDatePickerEvent()
            }
        }

        viewModel.openTimePickerEvent.observe(this) { open ->
            if (open == true) {
                TimePickerFragment().show(supportFragmentManager, "timePicker")
                viewModel.clearTimePickerEvent()
            }
        }

        viewModel.alarmAddedEvent.observe(this) { alarm ->
            if (alarm != null) {
                viewModel.clearAlarmAddedEvent()
            }
        }

        viewModel.alarmDeletedEvent.observe(this) { alarm ->
            if (alarm != null) {
                Snackbar.make(
                    binding.root,
                    "Alarm for ${alarm.locationName} deleted",
                    Snackbar.LENGTH_LONG
                ).setAction("Undo") {
                    viewModel.restoreAlarm(alarm)
                }.show()
                viewModel.clearAlarmDeletedEvent()
            }
        }

        viewModel.loadAlarms()
    }

    private fun setupRecyclerView() {
        alarmAdapter = AlarmAdapter(
            onAlarmClick = { alarm ->
                val intent = Intent(this, WeatherActivity::class.java).apply {
                    putExtra("latitude", alarm.location.latitude)
                    putExtra("longitude", alarm.location.longitude)
                    putExtra("open_map", false)
                }
                startActivity(intent)
            },
            onDeleteClick = { alarm ->
                viewModel.deleteAlarm(alarm)
            }
        )
        binding.rvAlarms.layoutManager = LinearLayoutManager(this)
        binding.rvAlarms.adapter = alarmAdapter
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

    private fun showToast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }

    private fun getLocationName(latitude: Double, longitude: Double): String? {
        return try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                addresses[0].locality ?: addresses[0].featureName ?: "$latitude, $longitude"
            } else {
                "$latitude, $longitude"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Geocoder error: ${e.message}")
            "$latitude, $longitude"
        }
    }
}