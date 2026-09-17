package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.preferences.UserPreferencesRepository
import com.example.permissions.PermissionUtils
import com.example.permissions.PermissionViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("MusicPro", appName)
  }

  @Test
  fun `verify audio permission is required on Android 36`() {
    val perm = PermissionUtils.getAudioPermission()
    assertEquals(android.Manifest.permission.READ_MEDIA_AUDIO, perm)
  }

  @Test
  fun `verify app permissions list includes audio and notifications`() {
    val permissions = PermissionUtils.getAppPermissions()
    assertTrue(permissions.contains(android.Manifest.permission.READ_MEDIA_AUDIO))
    assertTrue(permissions.contains(android.Manifest.permission.POST_NOTIFICATIONS))
  }

  @Test
  fun `verify PermissionViewModel updates correctly on permission refusal`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val viewModel = PermissionViewModel()

    val fakeResults = mapOf(
      android.Manifest.permission.READ_MEDIA_AUDIO to false,
      android.Manifest.permission.POST_NOTIFICATIONS to true
    )

    viewModel.onPermissionsResult(context, fakeResults)
    val state = viewModel.uiState.value

    assertFalse(state.isAudioGranted)
    assertTrue(state.isDeniedExplanationNeeded)
    assertFalse(state.canProceedToApp)
  }

  @Test
  fun `verify PermissionViewModel allows app progression when audio is granted`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val viewModel = PermissionViewModel()

    val fakeResults = mapOf(
      android.Manifest.permission.READ_MEDIA_AUDIO to true,
      android.Manifest.permission.POST_NOTIFICATIONS to true
    )

    viewModel.onPermissionsResult(context, fakeResults)
    val state = viewModel.uiState.value

    assertTrue(state.isAudioGranted)
    assertFalse(state.isDeniedExplanationNeeded)
    assertTrue(state.canProceedToApp)
  }

  @Test
  fun `verify onboarding completion flag in DataStore preferences`() = runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repository = UserPreferencesRepository(context)

    // Initially false
    val initial = repository.isOnboardingCompleted.first()
    assertFalse(initial)

    // Mark completed
    repository.setOnboardingCompleted(true)
    val afterCompletion = repository.isOnboardingCompleted.first()
    assertTrue(afterCompletion)
  }

  @Test
  fun `verify Room database caching, search and filtering`() = runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = androidx.room.Room.inMemoryDatabaseBuilder(
      context,
      com.example.data.local.MusicProDatabase::class.java
    ).allowMainThreadQueries().build()

    val dao = db.audioTrackDao()

    val track1 = com.example.data.local.AudioTrackEntity(
      id = 1L,
      title = "Neon Horizons",
      artist = "Cyber Pulse",
      album = "Neon Drift",
      duration = 210000L,
      contentUri = "content://media/external/audio/media/1",
      path = "/storage/emulated/0/Music/Pop/Neon_Horizons.flac",
      folder = "Pop",
      mimeType = "audio/flac",
      hasSyncedLyrics = true
    )

    val track2 = com.example.data.local.AudioTrackEntity(
      id = 2L,
      title = "Electric Aurora",
      artist = "Luna Star",
      album = "Starlight",
      duration = 185000L,
      contentUri = "content://media/external/audio/media/2",
      path = "/storage/emulated/0/Music/Dance/Electric_Aurora.mp3",
      folder = "Dance",
      mimeType = "audio/mpeg",
      hasSyncedLyrics = false
    )

    // Verify empty state initially
    assertEquals(0, dao.getTrackCount())

    // Insert tracks
    dao.insertTracks(listOf(track1, track2))
    assertEquals(2, dao.getTrackCount())

    // Verify search
    val searchResults = dao.searchTracks("Cyber").first()
    assertEquals(1, searchResults.size)
    assertEquals("Neon Horizons", searchResults.first().title)

    // Verify filter by album
    val albumTracks = dao.getTracksByAlbum("Starlight").first()
    assertEquals(1, albumTracks.size)
    assertEquals("Electric Aurora", albumTracks.first().title)

    // Verify filter by folder
    val folderTracks = dao.getTracksByFolder("Pop").first()
    assertEquals(1, folderTracks.size)
    assertEquals("Neon Horizons", folderTracks.first().title)

    // Verify clear cache
    dao.clearAllTracks()
    assertEquals(0, dao.getTrackCount())

    db.close()
  }
}

