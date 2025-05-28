package com.example.taqs360.search.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taqs360.databinding.ActivitySearchBinding
import com.example.taqs360.search.model.datasource.SearchRemoteDataSourceImpl
import com.example.taqs360.search.model.repository.SearchRepositoryImpl
import com.example.taqs360.search.viewmodel.SearchViewModel
import com.example.taqs360.search.viewmodel.SearchViewModelFactory
import kotlinx.coroutines.*

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    private lateinit var viewModel: SearchViewModel
    private lateinit var searchAdapter: SearchAdapter
    private val searchScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)



        // Initialize ViewModel with custom factory
        viewModel = ViewModelProvider(this, SearchViewModelFactory(
            SearchRepositoryImpl(
            SearchRemoteDataSourceImpl()
        )
        )
        )
            .get(SearchViewModel::class.java)

        // Setup RecyclerView
        searchAdapter = SearchAdapter { place ->
            // Return selected place to MapFragment
            val result = Intent().apply {
                putExtra("latitude", place.latitude)
                putExtra("longitude", place.longitude)
            }
            setResult(Activity.RESULT_OK, result)
            finish()
        }
        binding.rvSearchResults.layoutManager = LinearLayoutManager(this)
        binding.rvSearchResults.adapter = searchAdapter

        // Setup search input
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchScope.launch {
                    delay(300) // Debounce
                    viewModel.searchPlaces(s.toString())
                }
            }
        })

        // Observe search results
        viewModel.searchResults.observe(this) { places ->
            binding.progressBar.visibility = View.GONE
            if (places.isEmpty()) {
                binding.tvNoResults.visibility = View.VISIBLE
                binding.rvSearchResults.visibility = View.GONE
            } else {
                binding.tvNoResults.visibility = View.GONE
                binding.rvSearchResults.visibility = View.VISIBLE
                searchAdapter.submitList(places)
            }
        }

        // Observe errors
        viewModel.error.observe(this) { error ->
            binding.progressBar.visibility = View.GONE
            binding.tvNoResults.text = error
            binding.tvNoResults.visibility = View.VISIBLE
            binding.rvSearchResults.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        searchScope.cancel()
    }
}