package com.example.taqs360.map.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.taqs360.R
import com.example.taqs360.databinding.FragmentMapBinding
import com.example.taqs360.home.view.WeatherActivity
import com.example.taqs360.location.LocationDataSource
import com.example.taqs360.location.LocationResult
import com.example.taqs360.map.model.LocationData
import com.example.taqs360.map.model.datasource.MapLocalDataSourceImpl
import com.example.taqs360.map.model.repository.MapRepositoryImpl
import com.example.taqs360.map.viewmodel.MapViewModel
import com.example.taqs360.map.viewmodel.MapViewModelFactory
import com.example.taqs360.search.view.SearchActivity
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.io.File

class MapFragment : Fragment() {
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private val mapViewModel: MapViewModel by activityViewModels {
        MapViewModelFactory(
            MapRepositoryImpl(
                MapLocalDataSourceImpl(requireContext()),
                LocationDataSource(requireContext())
            )
        )
    }
    private val TAG = "MapFragment"

    private val searchResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val latitude = data?.getDoubleExtra("latitude", 0.0) ?: 0.0
            val longitude = data?.getDoubleExtra("longitude", 0.0) ?: 0.0
            if (latitude != 0.0 && longitude != 0.0) {
                android.util.Log.d(TAG, "Search result: lat=$latitude, lon=$longitude")
                mapViewModel.setSelectedLocation(LocationData(latitude, longitude))
                if (requireActivity() is WeatherActivity) {
                    requireActivity().supportFragmentManager.popBackStack()
                } else {
                    val intent = Intent(requireContext(), WeatherActivity::class.java).apply {
                        putExtra("latitude", latitude)
                        putExtra("longitude", longitude)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    startActivity(intent)
                    requireActivity().finish()
                }
            } else {
                android.util.Log.w(TAG, "Invalid search result: lat=$latitude, lon=$longitude")
                Toast.makeText(requireContext(), "Invalid location selected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configure osmdroid
        Configuration.getInstance().apply {
            osmdroidBasePath = File(requireContext().cacheDir, "osmdroid")
            userAgentValue = requireContext().packageName
        }

        binding.mapView.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(15.0)
        }

        // Observe current location
        mapViewModel.currentLocation.observe(viewLifecycleOwner) { result ->
            when (result) {
                is LocationResult.Success -> {
                    val location = result.data
                    val geoPoint = GeoPoint(location.latitude, location.longitude)
                    binding.mapView.controller.setCenter(geoPoint)
                    addMarker(geoPoint)
                    android.util.Log.d(TAG, "Current location set: lat=${location.latitude}, lon=${location.longitude}")
                }
                is LocationResult.Failure -> {
                    val defaultGeoPoint = GeoPoint(30.0444, 31.2357) // Cairo
                    binding.mapView.controller.setCenter(defaultGeoPoint)
                    addMarker(defaultGeoPoint)
                    android.util.Log.w(TAG, "Location error: ${result.exception.message}")
                }
            }
        }

        // Setup map tap event
        val mapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                p?.let {
                    binding.mapView.overlays.removeAll { overlay -> overlay is Marker }
                    addMarker(p)
                    mapViewModel.setSelectedLocation(LocationData(p.latitude, p.longitude))
                    android.util.Log.d(TAG, "Map tapped: lat=${p.latitude}, lon=${p.longitude}")
                    if (requireActivity() is WeatherActivity) {
                        requireActivity().supportFragmentManager.popBackStack()
                    } else {
                        val intent = Intent(requireContext(), WeatherActivity::class.java).apply {
                            putExtra("latitude", p.latitude)
                            putExtra("longitude", p.longitude)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        startActivity(intent)
                        requireActivity().finish()
                    }
                }
                return true
            }

            override fun longPressHelper(p: GeoPoint?): Boolean = false
        }

        binding.mapView.overlays.add(MapEventsOverlay(mapEventsReceiver))

        // Setup search button
        binding.ivSearch.setOnClickListener {
            val intent = Intent(requireContext(), SearchActivity::class.java)
            searchResultLauncher.launch(intent)
            android.util.Log.d(TAG, "Search activity launched")
        }

        // Fetch current location
        mapViewModel.fetchCurrentLocation()
    }

    private fun addMarker(geoPoint: GeoPoint) {
        val marker = Marker(binding.mapView).apply {
            position = geoPoint
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }
        binding.mapView.overlays.add(marker)
        binding.mapView.invalidate()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.mapView.overlays.clear()
        _binding = null
    }
}