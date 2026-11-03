package com.example

import android.os.Bundle
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.preferences.AccentChoice
import com.example.permissions.PermissionViewModel
import com.example.playback.MusicPlaybackService
import com.example.ui.components.AppUpdateDialog
import com.example.ui.components.glass.GlassStyle
import com.example.ui.components.glass.LocalGlassColorStrength
import com.example.ui.components.glass.LocalGlassFrostDp
import com.example.ui.components.glass.LocalGlassMaterial
import com.example.ui.components.glass.LocalGlassStyle
import com.example.ui.components.glass.LocalGlassTintHue
import com.example.ui.navigation.MusicProNavGraph
import com.example.ui.onboarding.OnboardingViewModel
import com.example.ui.settings.SettingsViewModel
import com.example.ui.theme.MusicProTheme
import com.example.ui.theme.rememberArtworkAccent
import com.example.ui.theme.resolveIsDark
import com.example.updater.DownloadState
import com.example.updater.UpdateCheckState
import com.example.widget.MusicWidgetUpdater
import com.example.ui.theme.MyApplicationTheme
import com.example.util.ShortcutActions
import androidx.compose.material3.MaterialTheme

class MainActivity : AppCompatActivity() {

  private val mediaUiRequestState = mutableStateOf<Pair<String, Long>?>(null)

  // Mise à jour 080 — raccourcis d'application (Lecture aléatoire, Rechercher).
  // « Dernière lecture » est couvert par ACTION_SHOW_NOW_PLAYING (mediaUiRequestState).
  private val shortcutRequestState = mutableStateOf<ShortcutActions.ShortcutRequest?>(null)

  private val permissionViewModel: PermissionViewModel by viewModels()
  private val onboardingViewModel: OnboardingViewModel by viewModels()
  private val settingsViewModel: SettingsViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    // Mise à jour 077 — API SplashScreen d'Android 12 : un seul écran de démarrage,
    // aucun double écran blanc, couleurs du thème appliquées dès la fenêtre froide.
    installSplashScreen()
    super.onCreate(savedInstanceState)
    mediaUiRequestState.value = readMediaUiRequest(intent)
    shortcutRequestState.value = readShortcutRequest(intent)
    enableEdgeToEdge()

    setContent {
      val appearance by settingsViewModel.appearanceMode.collectAsStateWithLifecycle()
      val appStyle by settingsViewModel.appStyle.collectAsStateWithLifecycle()
      val accentChoice by settingsViewModel.accentChoice.collectAsStateWithLifecycle()
      val textSizePreference by settingsViewModel.textSizePreference.collectAsStateWithLifecycle()
      val listDensityPreference by settingsViewModel.listDensityPreference.collectAsStateWithLifecycle()
      val reduceAnimations by settingsViewModel.reduceAnimations.collectAsStateWithLifecycle()
      val eveningDark by settingsViewModel.eveningDarkActive.collectAsStateWithLifecycle()
      val mediaUiRequest by mediaUiRequestState
      val shortcutRequest by shortcutRequestState
      val isDynamicColor by settingsViewModel.isDynamicColor.collectAsStateWithLifecycle()
      val glassMaterial by settingsViewModel.glassMaterial.collectAsStateWithLifecycle()
      val glassColorStrength by settingsViewModel.glassColorStrength.collectAsStateWithLifecycle()
      val glassFrostDp by settingsViewModel.glassFrostDp.collectAsStateWithLifecycle()
      val glassTintHue by settingsViewModel.glassTintHue.collectAsStateWithLifecycle()
      val glassFilmOpacity by settingsViewModel.glassFilmOpacity.collectAsStateWithLifecycle()
      val glassActiveTabOpacity by settingsViewModel.glassActiveTabOpacity.collectAsStateWithLifecycle()
      val glassStyle = remember(glassFilmOpacity, glassActiveTabOpacity) {
        GlassStyle(filmOpacity = glassFilmOpacity, activeTabOpacity = glassActiveTabOpacity)
      }

      // Apparence réellement affichée (Auto suit le système, « Sombre le soir » suit l'heure).
      val systemDark = isSystemInDarkTheme()
      val isDark = resolveIsDark(appearance, eveningDark, systemDark)

      // Mise à jour 020 : barres d'état et de gestes transparentes, icônes claires ou sombres
      // selon le thème réellement affiché, y compris quand il change en cours d'écran.
      DisposableEffect(isDark) {
        this@MainActivity.enableEdgeToEdge(
          statusBarStyle = SystemBarStyle.auto(
            android.graphics.Color.TRANSPARENT,
            android.graphics.Color.TRANSPARENT
          ) { isDark },
          navigationBarStyle = SystemBarStyle.auto(
            android.graphics.Color.TRANSPARENT,
            android.graphics.Color.TRANSPARENT
          ) { isDark }
        )
        onDispose { }
      }

      // Mise à jour 080 — le widget suit le style choisi (clair / sombre / verre) :
      // on le rafraîchit à chaque changement d'apparence.
      LaunchedEffect(appearance, appStyle) {
        MusicWidgetUpdater.refreshStyle(this@MainActivity)
      }

      // Accent « Selon la pochette » : couleur dominante du morceau en cours (null sinon).
      val artworkAccent by rememberArtworkAccent(
        enabled = accentChoice == AccentChoice.ARTWORK,
        isDark = isDark
      )

      MusicProTheme(
        appearance = appearance,
        style = appStyle,
        accent = accentChoice,
        eveningDark = eveningDark,
        artworkAccent = artworkAccent,
        dynamicColor = isDynamicColor,
        fontScale = textSizePreference.multiplier,
        listDensity = listDensityPreference,
        reduceAnimations = reduceAnimations
      ) {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        val uiState by permissionViewModel.uiState.collectAsStateWithLifecycle()
        val isOnboardingCompleted by onboardingViewModel.isOnboardingCompleted.collectAsStateWithLifecycle()
        val updateCheckState by settingsViewModel.updateCheckState.collectAsStateWithLifecycle()
        val downloadState by settingsViewModel.downloadState.collectAsStateWithLifecycle()
        var dismissedUpdateVersion by rememberSaveable { mutableStateOf<String?>(null) }

        // La vérification automatique est globale : elle ne dépend d'aucun écran
        // et l'état de mise à jour est affiché au niveau de l'Activity.
        if (isOnboardingCompleted == true) {
          LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(900L)
            settingsViewModel.checkForUpdates()
          }
        }

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

        // Réglages « Effet verre », « Givre », « Couleur du verre », « Teinte du verre »,
        // « Film » et « Onglet actif » : appliqués à toute l'application.
        CompositionLocalProvider(
          LocalGlassMaterial provides glassMaterial,
          LocalGlassColorStrength provides glassColorStrength,
          LocalGlassFrostDp provides glassFrostDp,
          LocalGlassTintHue provides glassTintHue,
          LocalGlassStyle provides glassStyle
        ) {
          Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
          ) {
            val openNowPlaying = intent?.action == MusicPlaybackService.ACTION_SHOW_NOW_PLAYING
            MusicProNavGraph(
              uiState = uiState,
              isOnboardingCompleted = isOnboardingCompleted,
              settingsViewModel = settingsViewModel,
              initialOpenNowPlaying = openNowPlaying,
              initialOpenQueue = mediaUiRequest?.first == MusicPlaybackService.ACTION_SHOW_QUEUE,
              initialOpenLyrics = mediaUiRequest?.first == MusicPlaybackService.ACTION_SHOW_LYRICS,
              initialShufflePlay = shortcutRequest == ShortcutActions.ShortcutRequest.SHUFFLE_PLAY,
              initialOpenSearch = shortcutRequest == ShortcutActions.ShortcutRequest.OPEN_SEARCH,
              isWarmStart = savedInstanceState != null,
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

        val currentUpdate = updateCheckState
        if (
          isOnboardingCompleted == true &&
          currentUpdate is UpdateCheckState.UpdateAvailable &&
          dismissedUpdateVersion != currentUpdate.latestVersion
        ) {
          AppUpdateDialog(
            updateInfo = currentUpdate,
            downloadState = downloadState,
            onDismiss = {
              dismissedUpdateVersion = currentUpdate.latestVersion
            },
            onDownloadAndInstall = { downloadUrl ->
              settingsViewModel.downloadAndInstallUpdate(downloadUrl)
            },
            onInstallExisting = {
              settingsViewModel.installExistingApk()
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
    shortcutRequestState.value = readShortcutRequest(intent)
  }

  private fun readShortcutRequest(intent: Intent?): ShortcutActions.ShortcutRequest? {
    val request = ShortcutActions.parse(intent?.action)
    // OPEN_LAST_PLAYED correspond à ACTION_SHOW_NOW_PLAYING, déjà traité par
    // [readMediaUiRequest] ; on évite un double déclenchement.
    return request?.takeIf { it != ShortcutActions.ShortcutRequest.OPEN_LAST_PLAYED }
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
