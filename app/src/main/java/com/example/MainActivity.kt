package com.example

import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.permissions.PermissionViewModel
import com.example.playback.MusicPlaybackService
import com.example.ui.navigation.MusicProNavGraph
import com.example.ui.onboarding.OnboardingViewModel
import com.example.ui.settings.SettingsViewModel
import com.example.ui.theme.MusicProTheme
import com.example.ui.theme.MyApplicationTheme
import androidx.compose.material3.MaterialTheme

class MainActivity : ComponentActivity() {

  private val mediaUiRequestState = mutableStateOf<Pair<String, Long>?>(null)

  private val permissionViewModel: PermissionViewModel by viewModels()
  private val onboardingViewModel: OnboardingViewModel by viewModels()
  private val settingsViewModel: SettingsViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    mediaUiRequestState.value = readMediaUiRequest(intent)
    enableEdgeToEdge()

    setContent {
      val currentThemeMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()
      val mediaUiRequest by mediaUiRequestState
      val isDynamicColor by settingsViewModel.isDynamicColor.collectAsStateWithLifecycle()

      MusicProTheme(themeMode = currentThemeMode, dynamicColor = isDynamicColor) {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        val uiState by permissionViewModel.uiState.collectAsStateWithLifecycle()
        val isOnboardingCompleted by onboardingViewModel.isOnboardingCompleted.collectAsStateWithLifecycle()

        // Re-vérifier automatiquement les permissions lorsque l'utilisateur revient des paramètres système
        DisposableEffect(lifecycleOwner) {
          val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
              permissionViewModel.checkPermissions(context)
            }
          }
          lifecycleOwner.lifecycle.addObserver(observer)
          onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
          }
        }

        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          val openNowPlaying = intent?.action == MusicPlaybackService.ACTION_SHOW_NOW_PLAYING
          MusicProNavGraph(
            uiState = uiState,
            isOnboardingCompleted = isOnboardingCompleted,
            initialOpenNowPlaying = openNowPlaying,
            initialOpenQueue = mediaUiRequest?.first == MusicPlaybackService.ACTION_SHOW_QUEUE,
            initialOpenLyrics = mediaUiRequest?.first == MusicPlaybackService.ACTION_SHOW_LYRICS,
            mediaUiRequestId = mediaUiRequest?.second ?: 0L,
            onCompleteOnboarding = {
              onboardingViewModel.completeOnboarding()
            },
            onRequestPermissions = { result ->
              permissionViewModel.onPermissionsResult(context, result)
            },
            onManualCheck = {
              permissionViewModel.checkPermissions(context)
            }
          )
        }
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    mediaUiRequestState.value = readMediaUiRequest(intent)
  }

  private fun readMediaUiRequest(intent: Intent?): Pair<String, Long>? {
    val action = intent?.action ?: return null
    if (action != MusicPlaybackService.ACTION_SHOW_NOW_PLAYING &&
        action != MusicPlaybackService.ACTION_SHOW_QUEUE &&
        action != MusicPlaybackService.ACTION_SHOW_LYRICS
    ) {
      return null
    }
    return action to intent.getLongExtra(MusicPlaybackService.EXTRA_REQUEST_ID, System.nanoTime())
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  MyApplicationTheme { Greeting("Android") }
}

