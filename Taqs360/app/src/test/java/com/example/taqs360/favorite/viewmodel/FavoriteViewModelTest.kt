package com.example.taqs360.favorite.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.taqs360.favorite.model.data.FavoriteLocation
import com.example.taqs360.favorite.model.repository.FavoriteRepository
import com.example.taqs360.map.model.LocationData
import getOrAwaitValue
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FavoriteViewModelTest {

 private lateinit var repository: FavoriteRepository
 private lateinit var viewModel: FavoriteViewModel
 private val testScheduler = TestCoroutineScheduler()
 private val testDispatcher = StandardTestDispatcher(testScheduler)

 @get:Rule
 val instantTaskExecutorRule = InstantTaskExecutorRule()

 @Before
 fun setup() {
  // Set the test dispatcher as the Main dispatcher
  Dispatchers.setMain(testDispatcher)
  repository = mockk(relaxed = true)
  viewModel = FavoriteViewModel(repository)
 }

 @After
 fun tearDown() {
  // Reset the Main dispatcher to avoid affecting other tests
  Dispatchers.resetMain()
 }

 @Test
 fun addFavorite_addsAndUpdates() {
  // Given: A mock repository with an empty list
  val favoritesList = mutableListOf<FavoriteLocation>()
  coEvery { repository.getFavorites() } returns favoritesList
  coEvery { repository.saveFavorite(any()) } answers {
   val favorite = firstArg<FavoriteLocation>()
   favoritesList.add(favorite)
  }

  // When: Adding a favorite
  val locationData = LocationData(latitude = 37.7749, longitude = -122.4194)
  val name = "Home"
  viewModel.addFavorite(locationData, name)
  testScheduler.advanceUntilIdle() // Process all queued coroutines

  // Then: Verify the favorites list and message are updated
  val favorites = viewModel.favorites.getOrAwaitValue()
  assertThat(favorites, not(emptyList()))
  assertThat(favorites[0].locationName, equalTo("Home"))
  assertThat(favorites[0].latitude, equalTo(37.7749))
  assertThat(favorites[0].longitude, equalTo(-122.4194))

  val message = viewModel.message.getOrAwaitValue()
  assertThat(message, equalTo("Favorite added for Home"))
 }

 @Test
 fun removeFavorite_removesAndUpdates() {
  // Given: A mock repository with a pre-populated favorite
  val favorite = FavoriteLocation(
   id = "1",
   locationName = "Home",
   latitude = 37.7749,
   longitude = -122.4194
  )
  val favoritesList = mutableListOf(favorite)
  coEvery { repository.getFavorites() } returns favoritesList
  coEvery { repository.deleteFavorite(any()) } answers {
   val fav = firstArg<FavoriteLocation>()
   favoritesList.removeAll { it.id == fav.id }
  }

  // When: Removing the favorite
  viewModel.removeFavorite(favorite)
  testScheduler.advanceUntilIdle() // Process all queued coroutines

  // Then: Verify the favorites list is empty and message is updated
  val favorites = viewModel.favorites.getOrAwaitValue()
  assertThat(favorites, equalTo(emptyList()))

  val message = viewModel.message.getOrAwaitValue()
  assertThat(message, equalTo("Home removed from favorites"))
 }
}