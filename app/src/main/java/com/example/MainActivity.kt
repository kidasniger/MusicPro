package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.permissions.PermissionViewModel
import com.example.ui.navigation.MusicProNavGraph
import com.example.ui.onboarding.OnboardingViewModel
import com.example.ui.theme.MusicProBackground
import com.example.ui.theme.MusicProTheme
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

  private val permissionViewModel: PermissionViewModel by viewModels()
  private val onboardingViewModel: OnboardingViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      MusicProTheme(darkTheme = true) {
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
          color = MusicProBackground
        ) {
          MusicProNavGraph(
            uiState = uiState,
            isOnboardingCompleted = isOnboardingCompleted,
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

