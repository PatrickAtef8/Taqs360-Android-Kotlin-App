package com.example.taqs360.settings.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import com.example.taqs360.R
import com.example.taqs360.databinding.FragmentSettingsBinding
import com.example.taqs360.map.view.MapFragment
import com.example.taqs360.settings.model.local.SettingsLocalDataSourceImpl
import com.example.taqs360.settings.model.remote.SettingsRemoteDataSourceImpl
import com.example.taqs360.settings.model.repository.SettingsRepositoryImpl
import com.example.taqs360.settings.viewmodel.SettingsViewModel
import com.example.taqs360.settings.viewmodel.SettingsViewModelFactory

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: SettingsViewModel
    private var lastUserSelectedLocationMode: String? = null
    private var isInitialSpinnerSetup = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(
            this,
            SettingsViewModelFactory(SettingsRepositoryImpl(
                SettingsLocalDataSourceImpl(requireContext()),
                SettingsRemoteDataSourceImpl()
            ))
        ).get(SettingsViewModel::class.java)

        setupSpinners()
        observeSettings()
        observeLanguageChange()
    }

    private fun setupSpinners() {
        val tempUnits = listOf(
            getString(R.string.celsius) to "metric",
            getString(R.string.fahrenheit) to "imperial",
            getString(R.string.kelvin) to "standard"
        )

        val windUnits = listOf(
            getString(R.string.meters_sec) to "meters_sec",
            getString(R.string.miles_hour) to "miles_hour"
        )

        val languages = listOf(
            getString(R.string.system_default) to "system",
            getString(R.string.english) to "en",
            getString(R.string.arabic) to "ar"
        )

        val locationModes = listOf(
            getString(R.string.gps) to "gps",
            getString(R.string.map) to "map"
        )

        val tempAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            tempUnits.map { it.first }
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinnerTempUnits.adapter = tempAdapter
        binding.spinnerTempUnits.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (binding.spinnerTempUnits.tag != "programmatic") {
                    Log.d("SettingsFragment", "User selected temp unit: ${tempUnits[position].second}")
                    viewModel.saveTemperatureUnit(tempUnits[position].second)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        val windAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            windUnits.map { it.first }
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinnerWindUnits.adapter = windAdapter
        binding.spinnerWindUnits.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (binding.spinnerWindUnits.tag != "programmatic") {
                    Log.d("SettingsFragment", "User selected wind unit: ${windUnits[position].second}")
                    viewModel.saveWindSpeedUnit(windUnits[position].second)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        val langAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            languages.map { it.first }
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinnerLanguage.adapter = langAdapter
        binding.spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (binding.spinnerLanguage.tag != "programmatic") {
                    Log.d("SettingsFragment", "User selected language: ${languages[position].second}")
                    viewModel.saveLanguage(languages[position].second)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        val locationAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            locationModes.map { it.first }
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinnerLocationMode.adapter = locationAdapter
        binding.spinnerLocationMode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (!isInitialSpinnerSetup && binding.spinnerLocationMode.tag != "programmatic") {
                    val selectedMode = locationModes[position].second
                    Log.d("SettingsFragment", "User selected location mode: $selectedMode")
                    viewModel.saveLocationMode(selectedMode)
                    Log.d("SettingsFragment", "Saved location mode: $selectedMode")
                    if (selectedMode == "map" && lastUserSelectedLocationMode != "map") {
                        lastUserSelectedLocationMode = selectedMode
                        openMapFragment()
                    } else {
                        lastUserSelectedLocationMode = selectedMode
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun observeSettings() {
        viewModel.settings.observe(viewLifecycleOwner) { settings ->
            Log.d("SettingsFragment", "Updating spinners with settings: $settings")
            if (lastUserSelectedLocationMode == null) {
                lastUserSelectedLocationMode = settings.locationMode
                Log.d("SettingsFragment", "Initialized lastUserSelectedLocationMode: $lastUserSelectedLocationMode")
            }

            val tempUnits = listOf(
                getString(R.string.celsius) to "metric",
                getString(R.string.fahrenheit) to "imperial",
                getString(R.string.kelvin) to "standard"
            )
            val windUnits = listOf(
                getString(R.string.meters_sec) to "meters_sec",
                getString(R.string.miles_hour) to "miles_hour"
            )
            val languages = listOf(
                getString(R.string.system_default) to "system",
                getString(R.string.english) to "en",
                getString(R.string.arabic) to "ar"
            )
            val locationModes = listOf(
                getString(R.string.gps) to "gps",
                getString(R.string.map) to "map"
            )

            binding.spinnerTempUnits.tag = "programmatic"
            binding.spinnerWindUnits.tag = "programmatic"
            binding.spinnerLanguage.tag = "programmatic"
            binding.spinnerLocationMode.tag = "programmatic"

            binding.spinnerTempUnits.onItemSelectedListener = null
            binding.spinnerWindUnits.onItemSelectedListener = null
            binding.spinnerLanguage.onItemSelectedListener = null
            binding.spinnerLocationMode.onItemSelectedListener = null

            binding.spinnerTempUnits.setSelection(
                tempUnits.indexOfFirst { it.second == settings.temperatureUnit }.coerceAtLeast(0),
                false
            )
            binding.spinnerWindUnits.setSelection(
                windUnits.indexOfFirst { it.second == settings.windSpeedUnit }.coerceAtLeast(0),
                false
            )
            binding.spinnerLanguage.setSelection(
                languages.indexOfFirst { it.second == settings.language }.coerceAtLeast(0),
                false
            )
            binding.spinnerLocationMode.setSelection(
                locationModes.indexOfFirst { it.second == settings.locationMode }.coerceAtLeast(0),
                false
            )

            binding.spinnerTempUnits.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    if (binding.spinnerTempUnits.tag != "programmatic") {
                        Log.d("SettingsFragment", "User selected temp unit: ${tempUnits[position].second}")
                        viewModel.saveTemperatureUnit(tempUnits[position].second)
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
            binding.spinnerWindUnits.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    if (binding.spinnerWindUnits.tag != "programmatic") {
                        Log.d("SettingsFragment", "User selected wind unit: ${windUnits[position].second}")
                        viewModel.saveWindSpeedUnit(windUnits[position].second)
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
            binding.spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    if (binding.spinnerLanguage.tag != "programmatic") {
                        Log.d("SettingsFragment", "User selected language: ${languages[position].second}")
                        viewModel.saveLanguage(languages[position].second)
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
            binding.spinnerLocationMode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    if (!isInitialSpinnerSetup && binding.spinnerLocationMode.tag != "programmatic") {
                        val selectedMode = locationModes[position].second
                        Log.d("SettingsFragment", "User selected location mode: $selectedMode")
                        viewModel.saveLocationMode(selectedMode)
                        Log.d("SettingsFragment", "Saved location mode: $selectedMode")
                        if (selectedMode == "map" && lastUserSelectedLocationMode != "map") {
                            lastUserSelectedLocationMode = selectedMode
                            openMapFragment()
                        } else {
                            lastUserSelectedLocationMode = selectedMode
                        }
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

            binding.spinnerTempUnits.tag = null
            binding.spinnerWindUnits.tag = null
            binding.spinnerLanguage.tag = null
            binding.spinnerLocationMode.tag = null

            isInitialSpinnerSetup = false
        }
    }

    private fun observeLanguageChange() {
        viewModel.languageChanged.observe(viewLifecycleOwner) { _ ->
            Log.d("SettingsFragment", "Language changed, restarting activity")
            val intent = Intent(requireContext(), SettingsActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun openMapFragment() {
        viewModel.saveLocationMode("map")
        Log.d("SettingsFragment", "Opening MapFragment with location mode set to map")
        requireActivity().supportFragmentManager.commit {
            setCustomAnimations(
                R.anim.slide_in_from_right,
                R.anim.slide_out_to_left,
                R.anim.slide_in_from_left,
                R.anim.slide_out_to_right
            )
            replace(R.id.settings_container, MapFragment())
            addToBackStack(null)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}