package com.example.taqs360.favorite.model.repository
import com.example.taqs360.favorite.model.data.FavoriteLocation
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FavoriteRepositoryImplTest {

 private lateinit var fakeDataSource: FakeFavoriteLocalDataSource
 private lateinit var repository: FavoriteRepositoryImpl

 @Before
 fun setup() {
  fakeDataSource = FakeFavoriteLocalDataSource()
  repository = FavoriteRepositoryImpl(fakeDataSource)
 }

 @Test
 fun saveFavorite_addsFavorite() = runBlocking {
  // Given: A favorite location to save
  val favorite = FavoriteLocation(
   id = "1",
   locationName = "Home",
   latitude = 37.7749,
   longitude = -122.4194
  )

  // When: Saving the favorite
  repository.saveFavorite(favorite)

  // Then: The favorite should be in the list
  val favorites = repository.getFavorites()
  assertEquals(1, favorites.size)
  assertEquals(favorite, favorites[0])
 }

 @Test
 fun deleteFavorite_removesFavorite() = runBlocking {
  // Given: A favorite location pre-populated in the data source
  val favorite = FavoriteLocation(
   id = "1",
   locationName = "Home",
   latitude = 37.7749,
   longitude = -122.4194
  )
  fakeDataSource.saveFavorite(favorite)

  // When: Deleting the favorite
  repository.deleteFavorite(favorite)

  // Then: The list should be empty
  val favorites = repository.getFavorites()
  assertTrue(favorites.isEmpty())
 }
}