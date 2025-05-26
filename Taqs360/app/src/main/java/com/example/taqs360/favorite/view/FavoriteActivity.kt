package com.example.taqs360.favorite.view

import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.ScaleAnimation
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taqs360.R
import com.example.taqs360.databinding.ActivityFavoriteBinding
import com.example.taqs360.favorite.model.data.FavoriteLocation
import com.example.taqs360.favorite.model.datasource.FavoriteLocalDataSourceImpl
import com.example.taqs360.favorite.model.repository.FavoriteRepositoryImpl
import com.example.taqs360.favorite.viewmodel.FavoriteViewModel
import com.example.taqs360.favorite.viewmodel.FavoriteViewModelFactory
import com.example.taqs360.home.view.WeatherActivity
import com.example.taqs360.map.model.LocationData
import com.google.android.material.snackbar.Snackbar
import java.util.Locale

class FavoriteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFavoriteBinding
    private lateinit var viewModel: FavoriteViewModel
    private lateinit var favoriteAdapter: FavoriteAdapter
    private val TAG = "FavoriteActivity"

    private val mapResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val latitude = data?.getDoubleExtra("latitude", 0.0) ?: 0.0
                val longitude = data?.getDoubleExtra("longitude", 0.0) ?: 0.0
                if (latitude != 0.0 && longitude != 0.0) {
                    val locationName =
                        getLocationName(latitude, longitude) ?: "$latitude, $longitude"
                    viewModel.addFavorite(LocationData(latitude, longitude), locationName)
                } else {
                    Log.w(TAG, "Invalid location selected: lat=$latitude, lon=$longitude")
                    showToast("Invalid location selected")
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(
            this,
            FavoriteViewModelFactory(
                FavoriteRepositoryImpl(
                    FavoriteLocalDataSourceImpl(this)
                )
            )
        )[FavoriteViewModel::class.java]

        setupRecyclerView()

        binding.fabAddFavorite.setOnClickListener {
            animateClick(binding.fabAddFavorite)
            val intent = Intent(this, WeatherActivity::class.java).apply {
                putExtra("open_map", true)
            }
            mapResultLauncher.launch(intent)
        }

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        viewModel.favorites.observe(this) { favorites ->
            favoriteAdapter.submitList(favorites)
            binding.tvEmptyState.visibility = if (favorites.isEmpty()) View.VISIBLE else View.GONE
            Log.d(TAG, "Updated UI with ${favorites.size} favorite locations")
        }

        viewModel.message.observe(this) { message ->
            showToast(message)
        }

        viewModel.loadFavorites()
    }

    private fun setupRecyclerView() {
        favoriteAdapter = FavoriteAdapter(
            onItemClick = { location ->
                val intent = Intent(this, WeatherActivity::class.java).apply {
                    putExtra("latitude", location.latitude)
                    putExtra("longitude", location.longitude)
                    putExtra("open_map", false)
                }
                startActivity(intent)
            },
            onRemoveClick = { location ->
                viewModel.removeFavorite(location)
                Snackbar.make(
                    binding.root,
                    getString(R.string.favorite_removed, location.locationName),
                    Snackbar.LENGTH_LONG
                ).setAction(R.string.undo) {
                    viewModel.restoreFavorite(location)
                }.show()
            }
        )
        binding.rvFavorites.layoutManager = LinearLayoutManager(this)
        binding.rvFavorites.adapter = favoriteAdapter
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