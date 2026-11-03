package com.example.ui.screenshots

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AudioTrackEntity
import com.example.data.preferences.AccentChoice
import com.example.data.preferences.AppearanceMode
import com.example.data.preferences.AppStyle
import com.example.ui.components.AlphabetIndexer
import com.example.ui.components.EmptyAudioStateView
import com.example.ui.components.EqualizerIndicator
import com.example.ui.components.OfflineBanner
import com.example.ui.components.TrackActionsSheet
import com.example.ui.components.glass.GlassMiniPlayer
import com.example.ui.library.TrackRowItem
import com.example.ui.settings.GlassFilmCard
import com.example.ui.settings.GlassPreviewCard
import com.example.ui.settings.GlassPreviewState
import com.example.ui.settings.SettingsScreen
import com.example.ui.settings.SettingsViewModel
import com.example.ui.theme.MusicProCyanNeon
import com.example.ui.theme.MusicProTheme
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Mise à jour 090 — tests visuels automatisés : 10 écrans clés × 6 combinaisons de thème
 * (clair/sombre × Minimal/Néon/Verre) × 3 tailles d'appareil = 180 captures de référence
 * commises dans `src/test/screenshots/090/`.
 *
 * Les captures ne s'exécutent qu'en mode explicite pour ne pas ralentir les tests standards :
 *
 *  - enregistrement des références : `./gradlew :app:testDebugUnitTest -Pmusicpro.screenshots=record`
 *  - vérification (échec si différence > 0,5 %) : `./gradlew verifyRoborazziDebug -Pmusicpro.screenshots=verify`
 *
 * Le seuil de 0,5 % est appliqué via `RoborazziOptions.CompareOptions(changeThreshold = 0.005f)`.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class KeyScreensScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mode: String = System.getProperty("musicpro.screenshots") ?: ""

    private fun captureScreen(
        screen: String,
        sizeTag: String,
        themeTag: String,
        appearance: AppearanceMode,
        style: AppStyle,
        content: @Composable () -> Unit
    ) {
        if (mode.isEmpty()) return
        composeTestRule.setContent {
            MusicProTheme(
                appearance = appearance,
                style = style,
                accent = AccentChoice.TERRACOTTA,
                reduceAnimations = true
            ) {
                Box(
                    Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .padding(12.dp)
                ) { content() }
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().captureRoboImage(
            filePath = "src/test/screenshots/090/$screen-$themeTag-$sizeTag.png",
            roborazziOptions = RoborazziOptions(
                compareOptions = RoborazziOptions.CompareOptions(changeThreshold = 0.005f)
            )
        )
    }

    private fun sampleTrack(id: Long = 1L) = AudioTrackEntity(
        id = id,
        title = "Nuit électrique",
        artist = "Étoile Froide",
        album = "Aurore",
        duration = 215_000L,
        contentUri = "content://media/audio/$id"
    )

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen01_track_row_small_minimal_clair() =
        captureScreen("01_track_row", "small", "minimal-clair", AppearanceMode.LIGHT, AppStyle.MINIMAL) {
            TrackRowItem(
            index = 1,
            track = sampleTrack(),
            isCurrent = true,
            isPlaying = true,
            isFavorite = false,
            onClick = {},
            onToggleFavorite = {},
            onOpenActions = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen01_track_row_small_minimal_sombre() =
        captureScreen("01_track_row", "small", "minimal-sombre", AppearanceMode.DARK, AppStyle.MINIMAL) {
            TrackRowItem(
            index = 1,
            track = sampleTrack(),
            isCurrent = true,
            isPlaying = true,
            isFavorite = false,
            onClick = {},
            onToggleFavorite = {},
            onOpenActions = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen01_track_row_small_neon_clair() =
        captureScreen("01_track_row", "small", "neon-clair", AppearanceMode.LIGHT, AppStyle.NEON) {
            TrackRowItem(
            index = 1,
            track = sampleTrack(),
            isCurrent = true,
            isPlaying = true,
            isFavorite = false,
            onClick = {},
            onToggleFavorite = {},
            onOpenActions = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen01_track_row_small_neon_sombre() =
        captureScreen("01_track_row", "small", "neon-sombre", AppearanceMode.DARK, AppStyle.NEON) {
            TrackRowItem(
            index = 1,
            track = sampleTrack(),
            isCurrent = true,
            isPlaying = true,
            isFavorite = false,
            onClick = {},
            onToggleFavorite = {},
            onOpenActions = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen01_track_row_small_verre_clair() =
        captureScreen("01_track_row", "small", "verre-clair", AppearanceMode.LIGHT, AppStyle.GLASS) {
            TrackRowItem(
            index = 1,
            track = sampleTrack(),
            isCurrent = true,
            isPlaying = true,
            isFavorite = false,
            onClick = {},
            onToggleFavorite = {},
            onOpenActions = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen01_track_row_small_verre_sombre() =
        captureScreen("01_track_row", "small", "verre-sombre", AppearanceMode.DARK, AppStyle.GLASS) {
            TrackRowItem(
            index = 1,
            track = sampleTrack(),
            isCurrent = true,
            isPlaying = true,
            isFavorite = false,
            onClick = {},
            onToggleFavorite = {},
            onOpenActions = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen01_track_row_standard_minimal_clair() =
        captureScreen("01_track_row", "standard", "minimal-clair", AppearanceMode.LIGHT, AppStyle.MINIMAL) {
            TrackRowItem(
            index = 1,
            track = sampleTrack(),
            isCurrent = true,
            isPlaying = true,
            isFavorite = false,
            onClick = {},
            onToggleFavorite = {},
            onOpenActions = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen01_track_row_standard_minimal_sombre() =
        captureScreen("01_track_row", "standard", "minimal-sombre", AppearanceMode.DARK, AppStyle.MINIMAL) {
            TrackRowItem(
            index = 1,
            track = sampleTrack(),
            isCurrent = true,
            isPlaying = true,
            isFavorite = false,
            onClick = {},
            onToggleFavorite = {},
            onOpenActions = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen01_track_row_standard_neon_clair() =
        captureScreen("01_track_row", "standard", "neon-clair", AppearanceMode.LIGHT, AppStyle.NEON) {
            TrackRowItem(
            index = 1,
            track = sampleTrack(),
            isCurrent = true,
            isPlaying = true,
            isFavorite = false,
            onClick = {},
            onToggleFavorite = {},
            onOpenActions = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen01_track_row_standard_neon_sombre() =
        captureScreen("01_track_row", "standard", "neon-sombre", AppearanceMode.DARK, AppStyle.NEON) {
            TrackRowItem(
            index = 1,
            track = sampleTrack(),
            isCurrent = true,
            isPlaying = true,
            isFavorite = false,
            onClick = {},
            onToggleFavorite = {},
            onOpenActions = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen01_track_row_standard_verre_clair() =
        captureScreen("01_track_row", "standard", "verre-clair", AppearanceMode.LIGHT, AppStyle.GLASS) {
            TrackRowItem(
            index = 1,
            track = sampleTrack(),
            isCurrent = true,
            isPlaying = true,
            isFavorite = false,
            onClick = {},
            onToggleFavorite = {},
            onOpenActions = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen01_track_row_standard_verre_sombre() =
        captureScreen("01_track_row", "standard", "verre-sombre", AppearanceMode.DARK, AppStyle.GLASS) {
            TrackRowItem(
            index = 1,
            track = sampleTrack(),
            isCurrent = true,
            isPlaying = true,
            isFavorite = false,
            onClick = {},
            onToggleFavorite = {},
            onOpenActions = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen01_track_row_tablet_minimal_clair() =
        captureScreen("01_track_row", "tablet", "minimal-clair", AppearanceMode.LIGHT, AppStyle.MINIMAL) {
            TrackRowItem(
            index = 1,
            track = sampleTrack(),
            isCurrent = true,
            isPlaying = true,
            isFavorite = false,
            onClick = {},
            onToggleFavorite = {},
            onOpenActions = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen01_track_row_tablet_minimal_sombre() =
        captureScreen("01_track_row", "tablet", "minimal-sombre", AppearanceMode.DARK, AppStyle.MINIMAL) {
            TrackRowItem(
            index = 1,
            track = sampleTrack(),
            isCurrent = true,
            isPlaying = true,
            isFavorite = false,
            onClick = {},
            onToggleFavorite = {},
            onOpenActions = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen01_track_row_tablet_neon_clair() =
        captureScreen("01_track_row", "tablet", "neon-clair", AppearanceMode.LIGHT, AppStyle.NEON) {
            TrackRowItem(
            index = 1,
            track = sampleTrack(),
            isCurrent = true,
            isPlaying = true,
            isFavorite = false,
            onClick = {},
            onToggleFavorite = {},
            onOpenActions = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen01_track_row_tablet_neon_sombre() =
        captureScreen("01_track_row", "tablet", "neon-sombre", AppearanceMode.DARK, AppStyle.NEON) {
            TrackRowItem(
            index = 1,
            track = sampleTrack(),
            isCurrent = true,
            isPlaying = true,
            isFavorite = false,
            onClick = {},
            onToggleFavorite = {},
            onOpenActions = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen01_track_row_tablet_verre_clair() =
        captureScreen("01_track_row", "tablet", "verre-clair", AppearanceMode.LIGHT, AppStyle.GLASS) {
            TrackRowItem(
            index = 1,
            track = sampleTrack(),
            isCurrent = true,
            isPlaying = true,
            isFavorite = false,
            onClick = {},
            onToggleFavorite = {},
            onOpenActions = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen01_track_row_tablet_verre_sombre() =
        captureScreen("01_track_row", "tablet", "verre-sombre", AppearanceMode.DARK, AppStyle.GLASS) {
            TrackRowItem(
            index = 1,
            track = sampleTrack(),
            isCurrent = true,
            isPlaying = true,
            isFavorite = false,
            onClick = {},
            onToggleFavorite = {},
            onOpenActions = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen02_mini_player_small_minimal_clair() =
        captureScreen("02_mini_player", "small", "minimal-clair", AppearanceMode.LIGHT, AppStyle.MINIMAL) {
            GlassMiniPlayer(
            visible = true,
            track = sampleTrack(2),
            isPlaying = true,
            onPlayPauseToggle = {},
            onNext = {},
            onPrevious = {},
            onOpen = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen02_mini_player_small_minimal_sombre() =
        captureScreen("02_mini_player", "small", "minimal-sombre", AppearanceMode.DARK, AppStyle.MINIMAL) {
            GlassMiniPlayer(
            visible = true,
            track = sampleTrack(2),
            isPlaying = true,
            onPlayPauseToggle = {},
            onNext = {},
            onPrevious = {},
            onOpen = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen02_mini_player_small_neon_clair() =
        captureScreen("02_mini_player", "small", "neon-clair", AppearanceMode.LIGHT, AppStyle.NEON) {
            GlassMiniPlayer(
            visible = true,
            track = sampleTrack(2),
            isPlaying = true,
            onPlayPauseToggle = {},
            onNext = {},
            onPrevious = {},
            onOpen = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen02_mini_player_small_neon_sombre() =
        captureScreen("02_mini_player", "small", "neon-sombre", AppearanceMode.DARK, AppStyle.NEON) {
            GlassMiniPlayer(
            visible = true,
            track = sampleTrack(2),
            isPlaying = true,
            onPlayPauseToggle = {},
            onNext = {},
            onPrevious = {},
            onOpen = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen02_mini_player_small_verre_clair() =
        captureScreen("02_mini_player", "small", "verre-clair", AppearanceMode.LIGHT, AppStyle.GLASS) {
            GlassMiniPlayer(
            visible = true,
            track = sampleTrack(2),
            isPlaying = true,
            onPlayPauseToggle = {},
            onNext = {},
            onPrevious = {},
            onOpen = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen02_mini_player_small_verre_sombre() =
        captureScreen("02_mini_player", "small", "verre-sombre", AppearanceMode.DARK, AppStyle.GLASS) {
            GlassMiniPlayer(
            visible = true,
            track = sampleTrack(2),
            isPlaying = true,
            onPlayPauseToggle = {},
            onNext = {},
            onPrevious = {},
            onOpen = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen02_mini_player_standard_minimal_clair() =
        captureScreen("02_mini_player", "standard", "minimal-clair", AppearanceMode.LIGHT, AppStyle.MINIMAL) {
            GlassMiniPlayer(
            visible = true,
            track = sampleTrack(2),
            isPlaying = true,
            onPlayPauseToggle = {},
            onNext = {},
            onPrevious = {},
            onOpen = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen02_mini_player_standard_minimal_sombre() =
        captureScreen("02_mini_player", "standard", "minimal-sombre", AppearanceMode.DARK, AppStyle.MINIMAL) {
            GlassMiniPlayer(
            visible = true,
            track = sampleTrack(2),
            isPlaying = true,
            onPlayPauseToggle = {},
            onNext = {},
            onPrevious = {},
            onOpen = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen02_mini_player_standard_neon_clair() =
        captureScreen("02_mini_player", "standard", "neon-clair", AppearanceMode.LIGHT, AppStyle.NEON) {
            GlassMiniPlayer(
            visible = true,
            track = sampleTrack(2),
            isPlaying = true,
            onPlayPauseToggle = {},
            onNext = {},
            onPrevious = {},
            onOpen = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen02_mini_player_standard_neon_sombre() =
        captureScreen("02_mini_player", "standard", "neon-sombre", AppearanceMode.DARK, AppStyle.NEON) {
            GlassMiniPlayer(
            visible = true,
            track = sampleTrack(2),
            isPlaying = true,
            onPlayPauseToggle = {},
            onNext = {},
            onPrevious = {},
            onOpen = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen02_mini_player_standard_verre_clair() =
        captureScreen("02_mini_player", "standard", "verre-clair", AppearanceMode.LIGHT, AppStyle.GLASS) {
            GlassMiniPlayer(
            visible = true,
            track = sampleTrack(2),
            isPlaying = true,
            onPlayPauseToggle = {},
            onNext = {},
            onPrevious = {},
            onOpen = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen02_mini_player_standard_verre_sombre() =
        captureScreen("02_mini_player", "standard", "verre-sombre", AppearanceMode.DARK, AppStyle.GLASS) {
            GlassMiniPlayer(
            visible = true,
            track = sampleTrack(2),
            isPlaying = true,
            onPlayPauseToggle = {},
            onNext = {},
            onPrevious = {},
            onOpen = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen02_mini_player_tablet_minimal_clair() =
        captureScreen("02_mini_player", "tablet", "minimal-clair", AppearanceMode.LIGHT, AppStyle.MINIMAL) {
            GlassMiniPlayer(
            visible = true,
            track = sampleTrack(2),
            isPlaying = true,
            onPlayPauseToggle = {},
            onNext = {},
            onPrevious = {},
            onOpen = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen02_mini_player_tablet_minimal_sombre() =
        captureScreen("02_mini_player", "tablet", "minimal-sombre", AppearanceMode.DARK, AppStyle.MINIMAL) {
            GlassMiniPlayer(
            visible = true,
            track = sampleTrack(2),
            isPlaying = true,
            onPlayPauseToggle = {},
            onNext = {},
            onPrevious = {},
            onOpen = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen02_mini_player_tablet_neon_clair() =
        captureScreen("02_mini_player", "tablet", "neon-clair", AppearanceMode.LIGHT, AppStyle.NEON) {
            GlassMiniPlayer(
            visible = true,
            track = sampleTrack(2),
            isPlaying = true,
            onPlayPauseToggle = {},
            onNext = {},
            onPrevious = {},
            onOpen = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen02_mini_player_tablet_neon_sombre() =
        captureScreen("02_mini_player", "tablet", "neon-sombre", AppearanceMode.DARK, AppStyle.NEON) {
            GlassMiniPlayer(
            visible = true,
            track = sampleTrack(2),
            isPlaying = true,
            onPlayPauseToggle = {},
            onNext = {},
            onPrevious = {},
            onOpen = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen02_mini_player_tablet_verre_clair() =
        captureScreen("02_mini_player", "tablet", "verre-clair", AppearanceMode.LIGHT, AppStyle.GLASS) {
            GlassMiniPlayer(
            visible = true,
            track = sampleTrack(2),
            isPlaying = true,
            onPlayPauseToggle = {},
            onNext = {},
            onPrevious = {},
            onOpen = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen02_mini_player_tablet_verre_sombre() =
        captureScreen("02_mini_player", "tablet", "verre-sombre", AppearanceMode.DARK, AppStyle.GLASS) {
            GlassMiniPlayer(
            visible = true,
            track = sampleTrack(2),
            isPlaying = true,
            onPlayPauseToggle = {},
            onNext = {},
            onPrevious = {},
            onOpen = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen03_actions_sheet_small_minimal_clair() =
        captureScreen("03_actions_sheet", "small", "minimal-clair", AppearanceMode.LIGHT, AppStyle.MINIMAL) {
            TrackActionsSheet(
            track = sampleTrack(3),
            isFavorite = false,
            onDismiss = {},
            onPlayNext = {},
            onAddToQueue = {},
            onAddToPlaylist = {},
            onToggleFavorite = {},
            onGoToAlbum = {},
            onGoToArtist = {},
            onOpenLyrics = {},
            onShare = {},
            onDelete = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen03_actions_sheet_small_minimal_sombre() =
        captureScreen("03_actions_sheet", "small", "minimal-sombre", AppearanceMode.DARK, AppStyle.MINIMAL) {
            TrackActionsSheet(
            track = sampleTrack(3),
            isFavorite = false,
            onDismiss = {},
            onPlayNext = {},
            onAddToQueue = {},
            onAddToPlaylist = {},
            onToggleFavorite = {},
            onGoToAlbum = {},
            onGoToArtist = {},
            onOpenLyrics = {},
            onShare = {},
            onDelete = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen03_actions_sheet_small_neon_clair() =
        captureScreen("03_actions_sheet", "small", "neon-clair", AppearanceMode.LIGHT, AppStyle.NEON) {
            TrackActionsSheet(
            track = sampleTrack(3),
            isFavorite = false,
            onDismiss = {},
            onPlayNext = {},
            onAddToQueue = {},
            onAddToPlaylist = {},
            onToggleFavorite = {},
            onGoToAlbum = {},
            onGoToArtist = {},
            onOpenLyrics = {},
            onShare = {},
            onDelete = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen03_actions_sheet_small_neon_sombre() =
        captureScreen("03_actions_sheet", "small", "neon-sombre", AppearanceMode.DARK, AppStyle.NEON) {
            TrackActionsSheet(
            track = sampleTrack(3),
            isFavorite = false,
            onDismiss = {},
            onPlayNext = {},
            onAddToQueue = {},
            onAddToPlaylist = {},
            onToggleFavorite = {},
            onGoToAlbum = {},
            onGoToArtist = {},
            onOpenLyrics = {},
            onShare = {},
            onDelete = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen03_actions_sheet_small_verre_clair() =
        captureScreen("03_actions_sheet", "small", "verre-clair", AppearanceMode.LIGHT, AppStyle.GLASS) {
            TrackActionsSheet(
            track = sampleTrack(3),
            isFavorite = false,
            onDismiss = {},
            onPlayNext = {},
            onAddToQueue = {},
            onAddToPlaylist = {},
            onToggleFavorite = {},
            onGoToAlbum = {},
            onGoToArtist = {},
            onOpenLyrics = {},
            onShare = {},
            onDelete = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen03_actions_sheet_small_verre_sombre() =
        captureScreen("03_actions_sheet", "small", "verre-sombre", AppearanceMode.DARK, AppStyle.GLASS) {
            TrackActionsSheet(
            track = sampleTrack(3),
            isFavorite = false,
            onDismiss = {},
            onPlayNext = {},
            onAddToQueue = {},
            onAddToPlaylist = {},
            onToggleFavorite = {},
            onGoToAlbum = {},
            onGoToArtist = {},
            onOpenLyrics = {},
            onShare = {},
            onDelete = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen03_actions_sheet_standard_minimal_clair() =
        captureScreen("03_actions_sheet", "standard", "minimal-clair", AppearanceMode.LIGHT, AppStyle.MINIMAL) {
            TrackActionsSheet(
            track = sampleTrack(3),
            isFavorite = false,
            onDismiss = {},
            onPlayNext = {},
            onAddToQueue = {},
            onAddToPlaylist = {},
            onToggleFavorite = {},
            onGoToAlbum = {},
            onGoToArtist = {},
            onOpenLyrics = {},
            onShare = {},
            onDelete = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen03_actions_sheet_standard_minimal_sombre() =
        captureScreen("03_actions_sheet", "standard", "minimal-sombre", AppearanceMode.DARK, AppStyle.MINIMAL) {
            TrackActionsSheet(
            track = sampleTrack(3),
            isFavorite = false,
            onDismiss = {},
            onPlayNext = {},
            onAddToQueue = {},
            onAddToPlaylist = {},
            onToggleFavorite = {},
            onGoToAlbum = {},
            onGoToArtist = {},
            onOpenLyrics = {},
            onShare = {},
            onDelete = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen03_actions_sheet_standard_neon_clair() =
        captureScreen("03_actions_sheet", "standard", "neon-clair", AppearanceMode.LIGHT, AppStyle.NEON) {
            TrackActionsSheet(
            track = sampleTrack(3),
            isFavorite = false,
            onDismiss = {},
            onPlayNext = {},
            onAddToQueue = {},
            onAddToPlaylist = {},
            onToggleFavorite = {},
            onGoToAlbum = {},
            onGoToArtist = {},
            onOpenLyrics = {},
            onShare = {},
            onDelete = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen03_actions_sheet_standard_neon_sombre() =
        captureScreen("03_actions_sheet", "standard", "neon-sombre", AppearanceMode.DARK, AppStyle.NEON) {
            TrackActionsSheet(
            track = sampleTrack(3),
            isFavorite = false,
            onDismiss = {},
            onPlayNext = {},
            onAddToQueue = {},
            onAddToPlaylist = {},
            onToggleFavorite = {},
            onGoToAlbum = {},
            onGoToArtist = {},
            onOpenLyrics = {},
            onShare = {},
            onDelete = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen03_actions_sheet_standard_verre_clair() =
        captureScreen("03_actions_sheet", "standard", "verre-clair", AppearanceMode.LIGHT, AppStyle.GLASS) {
            TrackActionsSheet(
            track = sampleTrack(3),
            isFavorite = false,
            onDismiss = {},
            onPlayNext = {},
            onAddToQueue = {},
            onAddToPlaylist = {},
            onToggleFavorite = {},
            onGoToAlbum = {},
            onGoToArtist = {},
            onOpenLyrics = {},
            onShare = {},
            onDelete = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen03_actions_sheet_standard_verre_sombre() =
        captureScreen("03_actions_sheet", "standard", "verre-sombre", AppearanceMode.DARK, AppStyle.GLASS) {
            TrackActionsSheet(
            track = sampleTrack(3),
            isFavorite = false,
            onDismiss = {},
            onPlayNext = {},
            onAddToQueue = {},
            onAddToPlaylist = {},
            onToggleFavorite = {},
            onGoToAlbum = {},
            onGoToArtist = {},
            onOpenLyrics = {},
            onShare = {},
            onDelete = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen03_actions_sheet_tablet_minimal_clair() =
        captureScreen("03_actions_sheet", "tablet", "minimal-clair", AppearanceMode.LIGHT, AppStyle.MINIMAL) {
            TrackActionsSheet(
            track = sampleTrack(3),
            isFavorite = false,
            onDismiss = {},
            onPlayNext = {},
            onAddToQueue = {},
            onAddToPlaylist = {},
            onToggleFavorite = {},
            onGoToAlbum = {},
            onGoToArtist = {},
            onOpenLyrics = {},
            onShare = {},
            onDelete = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen03_actions_sheet_tablet_minimal_sombre() =
        captureScreen("03_actions_sheet", "tablet", "minimal-sombre", AppearanceMode.DARK, AppStyle.MINIMAL) {
            TrackActionsSheet(
            track = sampleTrack(3),
            isFavorite = false,
            onDismiss = {},
            onPlayNext = {},
            onAddToQueue = {},
            onAddToPlaylist = {},
            onToggleFavorite = {},
            onGoToAlbum = {},
            onGoToArtist = {},
            onOpenLyrics = {},
            onShare = {},
            onDelete = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen03_actions_sheet_tablet_neon_clair() =
        captureScreen("03_actions_sheet", "tablet", "neon-clair", AppearanceMode.LIGHT, AppStyle.NEON) {
            TrackActionsSheet(
            track = sampleTrack(3),
            isFavorite = false,
            onDismiss = {},
            onPlayNext = {},
            onAddToQueue = {},
            onAddToPlaylist = {},
            onToggleFavorite = {},
            onGoToAlbum = {},
            onGoToArtist = {},
            onOpenLyrics = {},
            onShare = {},
            onDelete = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen03_actions_sheet_tablet_neon_sombre() =
        captureScreen("03_actions_sheet", "tablet", "neon-sombre", AppearanceMode.DARK, AppStyle.NEON) {
            TrackActionsSheet(
            track = sampleTrack(3),
            isFavorite = false,
            onDismiss = {},
            onPlayNext = {},
            onAddToQueue = {},
            onAddToPlaylist = {},
            onToggleFavorite = {},
            onGoToAlbum = {},
            onGoToArtist = {},
            onOpenLyrics = {},
            onShare = {},
            onDelete = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen03_actions_sheet_tablet_verre_clair() =
        captureScreen("03_actions_sheet", "tablet", "verre-clair", AppearanceMode.LIGHT, AppStyle.GLASS) {
            TrackActionsSheet(
            track = sampleTrack(3),
            isFavorite = false,
            onDismiss = {},
            onPlayNext = {},
            onAddToQueue = {},
            onAddToPlaylist = {},
            onToggleFavorite = {},
            onGoToAlbum = {},
            onGoToArtist = {},
            onOpenLyrics = {},
            onShare = {},
            onDelete = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen03_actions_sheet_tablet_verre_sombre() =
        captureScreen("03_actions_sheet", "tablet", "verre-sombre", AppearanceMode.DARK, AppStyle.GLASS) {
            TrackActionsSheet(
            track = sampleTrack(3),
            isFavorite = false,
            onDismiss = {},
            onPlayNext = {},
            onAddToQueue = {},
            onAddToPlaylist = {},
            onToggleFavorite = {},
            onGoToAlbum = {},
            onGoToArtist = {},
            onOpenLyrics = {},
            onShare = {},
            onDelete = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen04_empty_state_small_minimal_clair() =
        captureScreen("04_empty_state", "small", "minimal-clair", AppearanceMode.LIGHT, AppStyle.MINIMAL) {
            EmptyAudioStateView()
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen04_empty_state_small_minimal_sombre() =
        captureScreen("04_empty_state", "small", "minimal-sombre", AppearanceMode.DARK, AppStyle.MINIMAL) {
            EmptyAudioStateView()
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen04_empty_state_small_neon_clair() =
        captureScreen("04_empty_state", "small", "neon-clair", AppearanceMode.LIGHT, AppStyle.NEON) {
            EmptyAudioStateView()
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen04_empty_state_small_neon_sombre() =
        captureScreen("04_empty_state", "small", "neon-sombre", AppearanceMode.DARK, AppStyle.NEON) {
            EmptyAudioStateView()
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen04_empty_state_small_verre_clair() =
        captureScreen("04_empty_state", "small", "verre-clair", AppearanceMode.LIGHT, AppStyle.GLASS) {
            EmptyAudioStateView()
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen04_empty_state_small_verre_sombre() =
        captureScreen("04_empty_state", "small", "verre-sombre", AppearanceMode.DARK, AppStyle.GLASS) {
            EmptyAudioStateView()
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen04_empty_state_standard_minimal_clair() =
        captureScreen("04_empty_state", "standard", "minimal-clair", AppearanceMode.LIGHT, AppStyle.MINIMAL) {
            EmptyAudioStateView()
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen04_empty_state_standard_minimal_sombre() =
        captureScreen("04_empty_state", "standard", "minimal-sombre", AppearanceMode.DARK, AppStyle.MINIMAL) {
            EmptyAudioStateView()
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen04_empty_state_standard_neon_clair() =
        captureScreen("04_empty_state", "standard", "neon-clair", AppearanceMode.LIGHT, AppStyle.NEON) {
            EmptyAudioStateView()
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen04_empty_state_standard_neon_sombre() =
        captureScreen("04_empty_state", "standard", "neon-sombre", AppearanceMode.DARK, AppStyle.NEON) {
            EmptyAudioStateView()
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen04_empty_state_standard_verre_clair() =
        captureScreen("04_empty_state", "standard", "verre-clair", AppearanceMode.LIGHT, AppStyle.GLASS) {
            EmptyAudioStateView()
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen04_empty_state_standard_verre_sombre() =
        captureScreen("04_empty_state", "standard", "verre-sombre", AppearanceMode.DARK, AppStyle.GLASS) {
            EmptyAudioStateView()
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen04_empty_state_tablet_minimal_clair() =
        captureScreen("04_empty_state", "tablet", "minimal-clair", AppearanceMode.LIGHT, AppStyle.MINIMAL) {
            EmptyAudioStateView()
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen04_empty_state_tablet_minimal_sombre() =
        captureScreen("04_empty_state", "tablet", "minimal-sombre", AppearanceMode.DARK, AppStyle.MINIMAL) {
            EmptyAudioStateView()
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen04_empty_state_tablet_neon_clair() =
        captureScreen("04_empty_state", "tablet", "neon-clair", AppearanceMode.LIGHT, AppStyle.NEON) {
            EmptyAudioStateView()
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen04_empty_state_tablet_neon_sombre() =
        captureScreen("04_empty_state", "tablet", "neon-sombre", AppearanceMode.DARK, AppStyle.NEON) {
            EmptyAudioStateView()
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen04_empty_state_tablet_verre_clair() =
        captureScreen("04_empty_state", "tablet", "verre-clair", AppearanceMode.LIGHT, AppStyle.GLASS) {
            EmptyAudioStateView()
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen04_empty_state_tablet_verre_sombre() =
        captureScreen("04_empty_state", "tablet", "verre-sombre", AppearanceMode.DARK, AppStyle.GLASS) {
            EmptyAudioStateView()
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen05_offline_banner_small_minimal_clair() =
        captureScreen("05_offline_banner", "small", "minimal-clair", AppearanceMode.LIGHT, AppStyle.MINIMAL) {
            OfflineBanner(isOnline = false, onRetry = {})
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen05_offline_banner_small_minimal_sombre() =
        captureScreen("05_offline_banner", "small", "minimal-sombre", AppearanceMode.DARK, AppStyle.MINIMAL) {
            OfflineBanner(isOnline = false, onRetry = {})
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen05_offline_banner_small_neon_clair() =
        captureScreen("05_offline_banner", "small", "neon-clair", AppearanceMode.LIGHT, AppStyle.NEON) {
            OfflineBanner(isOnline = false, onRetry = {})
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen05_offline_banner_small_neon_sombre() =
        captureScreen("05_offline_banner", "small", "neon-sombre", AppearanceMode.DARK, AppStyle.NEON) {
            OfflineBanner(isOnline = false, onRetry = {})
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen05_offline_banner_small_verre_clair() =
        captureScreen("05_offline_banner", "small", "verre-clair", AppearanceMode.LIGHT, AppStyle.GLASS) {
            OfflineBanner(isOnline = false, onRetry = {})
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen05_offline_banner_small_verre_sombre() =
        captureScreen("05_offline_banner", "small", "verre-sombre", AppearanceMode.DARK, AppStyle.GLASS) {
            OfflineBanner(isOnline = false, onRetry = {})
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen05_offline_banner_standard_minimal_clair() =
        captureScreen("05_offline_banner", "standard", "minimal-clair", AppearanceMode.LIGHT, AppStyle.MINIMAL) {
            OfflineBanner(isOnline = false, onRetry = {})
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen05_offline_banner_standard_minimal_sombre() =
        captureScreen("05_offline_banner", "standard", "minimal-sombre", AppearanceMode.DARK, AppStyle.MINIMAL) {
            OfflineBanner(isOnline = false, onRetry = {})
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen05_offline_banner_standard_neon_clair() =
        captureScreen("05_offline_banner", "standard", "neon-clair", AppearanceMode.LIGHT, AppStyle.NEON) {
            OfflineBanner(isOnline = false, onRetry = {})
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen05_offline_banner_standard_neon_sombre() =
        captureScreen("05_offline_banner", "standard", "neon-sombre", AppearanceMode.DARK, AppStyle.NEON) {
            OfflineBanner(isOnline = false, onRetry = {})
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen05_offline_banner_standard_verre_clair() =
        captureScreen("05_offline_banner", "standard", "verre-clair", AppearanceMode.LIGHT, AppStyle.GLASS) {
            OfflineBanner(isOnline = false, onRetry = {})
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen05_offline_banner_standard_verre_sombre() =
        captureScreen("05_offline_banner", "standard", "verre-sombre", AppearanceMode.DARK, AppStyle.GLASS) {
            OfflineBanner(isOnline = false, onRetry = {})
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen05_offline_banner_tablet_minimal_clair() =
        captureScreen("05_offline_banner", "tablet", "minimal-clair", AppearanceMode.LIGHT, AppStyle.MINIMAL) {
            OfflineBanner(isOnline = false, onRetry = {})
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen05_offline_banner_tablet_minimal_sombre() =
        captureScreen("05_offline_banner", "tablet", "minimal-sombre", AppearanceMode.DARK, AppStyle.MINIMAL) {
            OfflineBanner(isOnline = false, onRetry = {})
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen05_offline_banner_tablet_neon_clair() =
        captureScreen("05_offline_banner", "tablet", "neon-clair", AppearanceMode.LIGHT, AppStyle.NEON) {
            OfflineBanner(isOnline = false, onRetry = {})
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen05_offline_banner_tablet_neon_sombre() =
        captureScreen("05_offline_banner", "tablet", "neon-sombre", AppearanceMode.DARK, AppStyle.NEON) {
            OfflineBanner(isOnline = false, onRetry = {})
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen05_offline_banner_tablet_verre_clair() =
        captureScreen("05_offline_banner", "tablet", "verre-clair", AppearanceMode.LIGHT, AppStyle.GLASS) {
            OfflineBanner(isOnline = false, onRetry = {})
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen05_offline_banner_tablet_verre_sombre() =
        captureScreen("05_offline_banner", "tablet", "verre-sombre", AppearanceMode.DARK, AppStyle.GLASS) {
            OfflineBanner(isOnline = false, onRetry = {})
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen06_glass_preview_small_minimal_clair() =
        captureScreen("06_glass_preview", "small", "minimal-clair", AppearanceMode.LIGHT, AppStyle.MINIMAL) {
            GlassPreviewCard(state = GlassPreviewState())
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen06_glass_preview_small_minimal_sombre() =
        captureScreen("06_glass_preview", "small", "minimal-sombre", AppearanceMode.DARK, AppStyle.MINIMAL) {
            GlassPreviewCard(state = GlassPreviewState())
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen06_glass_preview_small_neon_clair() =
        captureScreen("06_glass_preview", "small", "neon-clair", AppearanceMode.LIGHT, AppStyle.NEON) {
            GlassPreviewCard(state = GlassPreviewState())
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen06_glass_preview_small_neon_sombre() =
        captureScreen("06_glass_preview", "small", "neon-sombre", AppearanceMode.DARK, AppStyle.NEON) {
            GlassPreviewCard(state = GlassPreviewState())
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen06_glass_preview_small_verre_clair() =
        captureScreen("06_glass_preview", "small", "verre-clair", AppearanceMode.LIGHT, AppStyle.GLASS) {
            GlassPreviewCard(state = GlassPreviewState())
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen06_glass_preview_small_verre_sombre() =
        captureScreen("06_glass_preview", "small", "verre-sombre", AppearanceMode.DARK, AppStyle.GLASS) {
            GlassPreviewCard(state = GlassPreviewState())
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen06_glass_preview_standard_minimal_clair() =
        captureScreen("06_glass_preview", "standard", "minimal-clair", AppearanceMode.LIGHT, AppStyle.MINIMAL) {
            GlassPreviewCard(state = GlassPreviewState())
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen06_glass_preview_standard_minimal_sombre() =
        captureScreen("06_glass_preview", "standard", "minimal-sombre", AppearanceMode.DARK, AppStyle.MINIMAL) {
            GlassPreviewCard(state = GlassPreviewState())
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen06_glass_preview_standard_neon_clair() =
        captureScreen("06_glass_preview", "standard", "neon-clair", AppearanceMode.LIGHT, AppStyle.NEON) {
            GlassPreviewCard(state = GlassPreviewState())
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen06_glass_preview_standard_neon_sombre() =
        captureScreen("06_glass_preview", "standard", "neon-sombre", AppearanceMode.DARK, AppStyle.NEON) {
            GlassPreviewCard(state = GlassPreviewState())
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen06_glass_preview_standard_verre_clair() =
        captureScreen("06_glass_preview", "standard", "verre-clair", AppearanceMode.LIGHT, AppStyle.GLASS) {
            GlassPreviewCard(state = GlassPreviewState())
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen06_glass_preview_standard_verre_sombre() =
        captureScreen("06_glass_preview", "standard", "verre-sombre", AppearanceMode.DARK, AppStyle.GLASS) {
            GlassPreviewCard(state = GlassPreviewState())
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen06_glass_preview_tablet_minimal_clair() =
        captureScreen("06_glass_preview", "tablet", "minimal-clair", AppearanceMode.LIGHT, AppStyle.MINIMAL) {
            GlassPreviewCard(state = GlassPreviewState())
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen06_glass_preview_tablet_minimal_sombre() =
        captureScreen("06_glass_preview", "tablet", "minimal-sombre", AppearanceMode.DARK, AppStyle.MINIMAL) {
            GlassPreviewCard(state = GlassPreviewState())
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen06_glass_preview_tablet_neon_clair() =
        captureScreen("06_glass_preview", "tablet", "neon-clair", AppearanceMode.LIGHT, AppStyle.NEON) {
            GlassPreviewCard(state = GlassPreviewState())
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen06_glass_preview_tablet_neon_sombre() =
        captureScreen("06_glass_preview", "tablet", "neon-sombre", AppearanceMode.DARK, AppStyle.NEON) {
            GlassPreviewCard(state = GlassPreviewState())
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen06_glass_preview_tablet_verre_clair() =
        captureScreen("06_glass_preview", "tablet", "verre-clair", AppearanceMode.LIGHT, AppStyle.GLASS) {
            GlassPreviewCard(state = GlassPreviewState())
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen06_glass_preview_tablet_verre_sombre() =
        captureScreen("06_glass_preview", "tablet", "verre-sombre", AppearanceMode.DARK, AppStyle.GLASS) {
            GlassPreviewCard(state = GlassPreviewState())
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen07_glass_film_small_minimal_clair() =
        captureScreen("07_glass_film", "small", "minimal-clair", AppearanceMode.LIGHT, AppStyle.MINIMAL) {
            GlassFilmCard(
            filmOpacity = 50,
            onFilmOpacityChange = {},
            activeTabOpacity = 26,
            onActiveTabOpacityChange = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen07_glass_film_small_minimal_sombre() =
        captureScreen("07_glass_film", "small", "minimal-sombre", AppearanceMode.DARK, AppStyle.MINIMAL) {
            GlassFilmCard(
            filmOpacity = 50,
            onFilmOpacityChange = {},
            activeTabOpacity = 26,
            onActiveTabOpacityChange = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen07_glass_film_small_neon_clair() =
        captureScreen("07_glass_film", "small", "neon-clair", AppearanceMode.LIGHT, AppStyle.NEON) {
            GlassFilmCard(
            filmOpacity = 50,
            onFilmOpacityChange = {},
            activeTabOpacity = 26,
            onActiveTabOpacityChange = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen07_glass_film_small_neon_sombre() =
        captureScreen("07_glass_film", "small", "neon-sombre", AppearanceMode.DARK, AppStyle.NEON) {
            GlassFilmCard(
            filmOpacity = 50,
            onFilmOpacityChange = {},
            activeTabOpacity = 26,
            onActiveTabOpacityChange = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen07_glass_film_small_verre_clair() =
        captureScreen("07_glass_film", "small", "verre-clair", AppearanceMode.LIGHT, AppStyle.GLASS) {
            GlassFilmCard(
            filmOpacity = 50,
            onFilmOpacityChange = {},
            activeTabOpacity = 26,
            onActiveTabOpacityChange = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen07_glass_film_small_verre_sombre() =
        captureScreen("07_glass_film", "small", "verre-sombre", AppearanceMode.DARK, AppStyle.GLASS) {
            GlassFilmCard(
            filmOpacity = 50,
            onFilmOpacityChange = {},
            activeTabOpacity = 26,
            onActiveTabOpacityChange = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen07_glass_film_standard_minimal_clair() =
        captureScreen("07_glass_film", "standard", "minimal-clair", AppearanceMode.LIGHT, AppStyle.MINIMAL) {
            GlassFilmCard(
            filmOpacity = 50,
            onFilmOpacityChange = {},
            activeTabOpacity = 26,
            onActiveTabOpacityChange = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen07_glass_film_standard_minimal_sombre() =
        captureScreen("07_glass_film", "standard", "minimal-sombre", AppearanceMode.DARK, AppStyle.MINIMAL) {
            GlassFilmCard(
            filmOpacity = 50,
            onFilmOpacityChange = {},
            activeTabOpacity = 26,
            onActiveTabOpacityChange = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen07_glass_film_standard_neon_clair() =
        captureScreen("07_glass_film", "standard", "neon-clair", AppearanceMode.LIGHT, AppStyle.NEON) {
            GlassFilmCard(
            filmOpacity = 50,
            onFilmOpacityChange = {},
            activeTabOpacity = 26,
            onActiveTabOpacityChange = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen07_glass_film_standard_neon_sombre() =
        captureScreen("07_glass_film", "standard", "neon-sombre", AppearanceMode.DARK, AppStyle.NEON) {
            GlassFilmCard(
            filmOpacity = 50,
            onFilmOpacityChange = {},
            activeTabOpacity = 26,
            onActiveTabOpacityChange = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen07_glass_film_standard_verre_clair() =
        captureScreen("07_glass_film", "standard", "verre-clair", AppearanceMode.LIGHT, AppStyle.GLASS) {
            GlassFilmCard(
            filmOpacity = 50,
            onFilmOpacityChange = {},
            activeTabOpacity = 26,
            onActiveTabOpacityChange = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen07_glass_film_standard_verre_sombre() =
        captureScreen("07_glass_film", "standard", "verre-sombre", AppearanceMode.DARK, AppStyle.GLASS) {
            GlassFilmCard(
            filmOpacity = 50,
            onFilmOpacityChange = {},
            activeTabOpacity = 26,
            onActiveTabOpacityChange = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen07_glass_film_tablet_minimal_clair() =
        captureScreen("07_glass_film", "tablet", "minimal-clair", AppearanceMode.LIGHT, AppStyle.MINIMAL) {
            GlassFilmCard(
            filmOpacity = 50,
            onFilmOpacityChange = {},
            activeTabOpacity = 26,
            onActiveTabOpacityChange = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen07_glass_film_tablet_minimal_sombre() =
        captureScreen("07_glass_film", "tablet", "minimal-sombre", AppearanceMode.DARK, AppStyle.MINIMAL) {
            GlassFilmCard(
            filmOpacity = 50,
            onFilmOpacityChange = {},
            activeTabOpacity = 26,
            onActiveTabOpacityChange = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen07_glass_film_tablet_neon_clair() =
        captureScreen("07_glass_film", "tablet", "neon-clair", AppearanceMode.LIGHT, AppStyle.NEON) {
            GlassFilmCard(
            filmOpacity = 50,
            onFilmOpacityChange = {},
            activeTabOpacity = 26,
            onActiveTabOpacityChange = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen07_glass_film_tablet_neon_sombre() =
        captureScreen("07_glass_film", "tablet", "neon-sombre", AppearanceMode.DARK, AppStyle.NEON) {
            GlassFilmCard(
            filmOpacity = 50,
            onFilmOpacityChange = {},
            activeTabOpacity = 26,
            onActiveTabOpacityChange = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen07_glass_film_tablet_verre_clair() =
        captureScreen("07_glass_film", "tablet", "verre-clair", AppearanceMode.LIGHT, AppStyle.GLASS) {
            GlassFilmCard(
            filmOpacity = 50,
            onFilmOpacityChange = {},
            activeTabOpacity = 26,
            onActiveTabOpacityChange = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen07_glass_film_tablet_verre_sombre() =
        captureScreen("07_glass_film", "tablet", "verre-sombre", AppearanceMode.DARK, AppStyle.GLASS) {
            GlassFilmCard(
            filmOpacity = 50,
            onFilmOpacityChange = {},
            activeTabOpacity = 26,
            onActiveTabOpacityChange = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen08_equalizer_small_minimal_clair() =
        captureScreen("08_equalizer", "small", "minimal-clair", AppearanceMode.LIGHT, AppStyle.MINIMAL) {
            EqualizerIndicator(color = MusicProCyanNeon)
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen08_equalizer_small_minimal_sombre() =
        captureScreen("08_equalizer", "small", "minimal-sombre", AppearanceMode.DARK, AppStyle.MINIMAL) {
            EqualizerIndicator(color = MusicProCyanNeon)
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen08_equalizer_small_neon_clair() =
        captureScreen("08_equalizer", "small", "neon-clair", AppearanceMode.LIGHT, AppStyle.NEON) {
            EqualizerIndicator(color = MusicProCyanNeon)
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen08_equalizer_small_neon_sombre() =
        captureScreen("08_equalizer", "small", "neon-sombre", AppearanceMode.DARK, AppStyle.NEON) {
            EqualizerIndicator(color = MusicProCyanNeon)
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen08_equalizer_small_verre_clair() =
        captureScreen("08_equalizer", "small", "verre-clair", AppearanceMode.LIGHT, AppStyle.GLASS) {
            EqualizerIndicator(color = MusicProCyanNeon)
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen08_equalizer_small_verre_sombre() =
        captureScreen("08_equalizer", "small", "verre-sombre", AppearanceMode.DARK, AppStyle.GLASS) {
            EqualizerIndicator(color = MusicProCyanNeon)
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen08_equalizer_standard_minimal_clair() =
        captureScreen("08_equalizer", "standard", "minimal-clair", AppearanceMode.LIGHT, AppStyle.MINIMAL) {
            EqualizerIndicator(color = MusicProCyanNeon)
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen08_equalizer_standard_minimal_sombre() =
        captureScreen("08_equalizer", "standard", "minimal-sombre", AppearanceMode.DARK, AppStyle.MINIMAL) {
            EqualizerIndicator(color = MusicProCyanNeon)
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen08_equalizer_standard_neon_clair() =
        captureScreen("08_equalizer", "standard", "neon-clair", AppearanceMode.LIGHT, AppStyle.NEON) {
            EqualizerIndicator(color = MusicProCyanNeon)
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen08_equalizer_standard_neon_sombre() =
        captureScreen("08_equalizer", "standard", "neon-sombre", AppearanceMode.DARK, AppStyle.NEON) {
            EqualizerIndicator(color = MusicProCyanNeon)
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen08_equalizer_standard_verre_clair() =
        captureScreen("08_equalizer", "standard", "verre-clair", AppearanceMode.LIGHT, AppStyle.GLASS) {
            EqualizerIndicator(color = MusicProCyanNeon)
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen08_equalizer_standard_verre_sombre() =
        captureScreen("08_equalizer", "standard", "verre-sombre", AppearanceMode.DARK, AppStyle.GLASS) {
            EqualizerIndicator(color = MusicProCyanNeon)
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen08_equalizer_tablet_minimal_clair() =
        captureScreen("08_equalizer", "tablet", "minimal-clair", AppearanceMode.LIGHT, AppStyle.MINIMAL) {
            EqualizerIndicator(color = MusicProCyanNeon)
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen08_equalizer_tablet_minimal_sombre() =
        captureScreen("08_equalizer", "tablet", "minimal-sombre", AppearanceMode.DARK, AppStyle.MINIMAL) {
            EqualizerIndicator(color = MusicProCyanNeon)
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen08_equalizer_tablet_neon_clair() =
        captureScreen("08_equalizer", "tablet", "neon-clair", AppearanceMode.LIGHT, AppStyle.NEON) {
            EqualizerIndicator(color = MusicProCyanNeon)
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen08_equalizer_tablet_neon_sombre() =
        captureScreen("08_equalizer", "tablet", "neon-sombre", AppearanceMode.DARK, AppStyle.NEON) {
            EqualizerIndicator(color = MusicProCyanNeon)
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen08_equalizer_tablet_verre_clair() =
        captureScreen("08_equalizer", "tablet", "verre-clair", AppearanceMode.LIGHT, AppStyle.GLASS) {
            EqualizerIndicator(color = MusicProCyanNeon)
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen08_equalizer_tablet_verre_sombre() =
        captureScreen("08_equalizer", "tablet", "verre-sombre", AppearanceMode.DARK, AppStyle.GLASS) {
            EqualizerIndicator(color = MusicProCyanNeon)
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen09_alphabet_indexer_small_minimal_clair() =
        captureScreen("09_alphabet_indexer", "small", "minimal-clair", AppearanceMode.LIGHT, AppStyle.MINIMAL) {
            AlphabetIndexer(
            letters = ('A'..'Z').toList(),
            currentLetter = { null },
            onLetterSelected = {},
            onDragEnded = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen09_alphabet_indexer_small_minimal_sombre() =
        captureScreen("09_alphabet_indexer", "small", "minimal-sombre", AppearanceMode.DARK, AppStyle.MINIMAL) {
            AlphabetIndexer(
            letters = ('A'..'Z').toList(),
            currentLetter = { null },
            onLetterSelected = {},
            onDragEnded = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen09_alphabet_indexer_small_neon_clair() =
        captureScreen("09_alphabet_indexer", "small", "neon-clair", AppearanceMode.LIGHT, AppStyle.NEON) {
            AlphabetIndexer(
            letters = ('A'..'Z').toList(),
            currentLetter = { null },
            onLetterSelected = {},
            onDragEnded = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen09_alphabet_indexer_small_neon_sombre() =
        captureScreen("09_alphabet_indexer", "small", "neon-sombre", AppearanceMode.DARK, AppStyle.NEON) {
            AlphabetIndexer(
            letters = ('A'..'Z').toList(),
            currentLetter = { null },
            onLetterSelected = {},
            onDragEnded = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen09_alphabet_indexer_small_verre_clair() =
        captureScreen("09_alphabet_indexer", "small", "verre-clair", AppearanceMode.LIGHT, AppStyle.GLASS) {
            AlphabetIndexer(
            letters = ('A'..'Z').toList(),
            currentLetter = { null },
            onLetterSelected = {},
            onDragEnded = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen09_alphabet_indexer_small_verre_sombre() =
        captureScreen("09_alphabet_indexer", "small", "verre-sombre", AppearanceMode.DARK, AppStyle.GLASS) {
            AlphabetIndexer(
            letters = ('A'..'Z').toList(),
            currentLetter = { null },
            onLetterSelected = {},
            onDragEnded = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen09_alphabet_indexer_standard_minimal_clair() =
        captureScreen("09_alphabet_indexer", "standard", "minimal-clair", AppearanceMode.LIGHT, AppStyle.MINIMAL) {
            AlphabetIndexer(
            letters = ('A'..'Z').toList(),
            currentLetter = { null },
            onLetterSelected = {},
            onDragEnded = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen09_alphabet_indexer_standard_minimal_sombre() =
        captureScreen("09_alphabet_indexer", "standard", "minimal-sombre", AppearanceMode.DARK, AppStyle.MINIMAL) {
            AlphabetIndexer(
            letters = ('A'..'Z').toList(),
            currentLetter = { null },
            onLetterSelected = {},
            onDragEnded = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen09_alphabet_indexer_standard_neon_clair() =
        captureScreen("09_alphabet_indexer", "standard", "neon-clair", AppearanceMode.LIGHT, AppStyle.NEON) {
            AlphabetIndexer(
            letters = ('A'..'Z').toList(),
            currentLetter = { null },
            onLetterSelected = {},
            onDragEnded = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen09_alphabet_indexer_standard_neon_sombre() =
        captureScreen("09_alphabet_indexer", "standard", "neon-sombre", AppearanceMode.DARK, AppStyle.NEON) {
            AlphabetIndexer(
            letters = ('A'..'Z').toList(),
            currentLetter = { null },
            onLetterSelected = {},
            onDragEnded = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen09_alphabet_indexer_standard_verre_clair() =
        captureScreen("09_alphabet_indexer", "standard", "verre-clair", AppearanceMode.LIGHT, AppStyle.GLASS) {
            AlphabetIndexer(
            letters = ('A'..'Z').toList(),
            currentLetter = { null },
            onLetterSelected = {},
            onDragEnded = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen09_alphabet_indexer_standard_verre_sombre() =
        captureScreen("09_alphabet_indexer", "standard", "verre-sombre", AppearanceMode.DARK, AppStyle.GLASS) {
            AlphabetIndexer(
            letters = ('A'..'Z').toList(),
            currentLetter = { null },
            onLetterSelected = {},
            onDragEnded = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen09_alphabet_indexer_tablet_minimal_clair() =
        captureScreen("09_alphabet_indexer", "tablet", "minimal-clair", AppearanceMode.LIGHT, AppStyle.MINIMAL) {
            AlphabetIndexer(
            letters = ('A'..'Z').toList(),
            currentLetter = { null },
            onLetterSelected = {},
            onDragEnded = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen09_alphabet_indexer_tablet_minimal_sombre() =
        captureScreen("09_alphabet_indexer", "tablet", "minimal-sombre", AppearanceMode.DARK, AppStyle.MINIMAL) {
            AlphabetIndexer(
            letters = ('A'..'Z').toList(),
            currentLetter = { null },
            onLetterSelected = {},
            onDragEnded = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen09_alphabet_indexer_tablet_neon_clair() =
        captureScreen("09_alphabet_indexer", "tablet", "neon-clair", AppearanceMode.LIGHT, AppStyle.NEON) {
            AlphabetIndexer(
            letters = ('A'..'Z').toList(),
            currentLetter = { null },
            onLetterSelected = {},
            onDragEnded = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen09_alphabet_indexer_tablet_neon_sombre() =
        captureScreen("09_alphabet_indexer", "tablet", "neon-sombre", AppearanceMode.DARK, AppStyle.NEON) {
            AlphabetIndexer(
            letters = ('A'..'Z').toList(),
            currentLetter = { null },
            onLetterSelected = {},
            onDragEnded = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen09_alphabet_indexer_tablet_verre_clair() =
        captureScreen("09_alphabet_indexer", "tablet", "verre-clair", AppearanceMode.LIGHT, AppStyle.GLASS) {
            AlphabetIndexer(
            letters = ('A'..'Z').toList(),
            currentLetter = { null },
            onLetterSelected = {},
            onDragEnded = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen09_alphabet_indexer_tablet_verre_sombre() =
        captureScreen("09_alphabet_indexer", "tablet", "verre-sombre", AppearanceMode.DARK, AppStyle.GLASS) {
            AlphabetIndexer(
            letters = ('A'..'Z').toList(),
            currentLetter = { null },
            onLetterSelected = {},
            onDragEnded = {}
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen10_settings_small_minimal_clair() =
        captureScreen("10_settings", "small", "minimal-clair", AppearanceMode.LIGHT, AppStyle.MINIMAL) {
            SettingsScreen(
            onBack = {},
            onNavigateToPermissions = {},
            settingsViewModel = SettingsViewModel(ApplicationProvider.getApplicationContext())
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen10_settings_small_minimal_sombre() =
        captureScreen("10_settings", "small", "minimal-sombre", AppearanceMode.DARK, AppStyle.MINIMAL) {
            SettingsScreen(
            onBack = {},
            onNavigateToPermissions = {},
            settingsViewModel = SettingsViewModel(ApplicationProvider.getApplicationContext())
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen10_settings_small_neon_clair() =
        captureScreen("10_settings", "small", "neon-clair", AppearanceMode.LIGHT, AppStyle.NEON) {
            SettingsScreen(
            onBack = {},
            onNavigateToPermissions = {},
            settingsViewModel = SettingsViewModel(ApplicationProvider.getApplicationContext())
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen10_settings_small_neon_sombre() =
        captureScreen("10_settings", "small", "neon-sombre", AppearanceMode.DARK, AppStyle.NEON) {
            SettingsScreen(
            onBack = {},
            onNavigateToPermissions = {},
            settingsViewModel = SettingsViewModel(ApplicationProvider.getApplicationContext())
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen10_settings_small_verre_clair() =
        captureScreen("10_settings", "small", "verre-clair", AppearanceMode.LIGHT, AppStyle.GLASS) {
            SettingsScreen(
            onBack = {},
            onNavigateToPermissions = {},
            settingsViewModel = SettingsViewModel(ApplicationProvider.getApplicationContext())
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-normal-long-notround-port-xhdpi")
    fun screen10_settings_small_verre_sombre() =
        captureScreen("10_settings", "small", "verre-sombre", AppearanceMode.DARK, AppStyle.GLASS) {
            SettingsScreen(
            onBack = {},
            onNavigateToPermissions = {},
            settingsViewModel = SettingsViewModel(ApplicationProvider.getApplicationContext())
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen10_settings_standard_minimal_clair() =
        captureScreen("10_settings", "standard", "minimal-clair", AppearanceMode.LIGHT, AppStyle.MINIMAL) {
            SettingsScreen(
            onBack = {},
            onNavigateToPermissions = {},
            settingsViewModel = SettingsViewModel(ApplicationProvider.getApplicationContext())
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen10_settings_standard_minimal_sombre() =
        captureScreen("10_settings", "standard", "minimal-sombre", AppearanceMode.DARK, AppStyle.MINIMAL) {
            SettingsScreen(
            onBack = {},
            onNavigateToPermissions = {},
            settingsViewModel = SettingsViewModel(ApplicationProvider.getApplicationContext())
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen10_settings_standard_neon_clair() =
        captureScreen("10_settings", "standard", "neon-clair", AppearanceMode.LIGHT, AppStyle.NEON) {
            SettingsScreen(
            onBack = {},
            onNavigateToPermissions = {},
            settingsViewModel = SettingsViewModel(ApplicationProvider.getApplicationContext())
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen10_settings_standard_neon_sombre() =
        captureScreen("10_settings", "standard", "neon-sombre", AppearanceMode.DARK, AppStyle.NEON) {
            SettingsScreen(
            onBack = {},
            onNavigateToPermissions = {},
            settingsViewModel = SettingsViewModel(ApplicationProvider.getApplicationContext())
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen10_settings_standard_verre_clair() =
        captureScreen("10_settings", "standard", "verre-clair", AppearanceMode.LIGHT, AppStyle.GLASS) {
            SettingsScreen(
            onBack = {},
            onNavigateToPermissions = {},
            settingsViewModel = SettingsViewModel(ApplicationProvider.getApplicationContext())
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w412dp-h915dp-normal-long-notround-port-xxhdpi")
    fun screen10_settings_standard_verre_sombre() =
        captureScreen("10_settings", "standard", "verre-sombre", AppearanceMode.DARK, AppStyle.GLASS) {
            SettingsScreen(
            onBack = {},
            onNavigateToPermissions = {},
            settingsViewModel = SettingsViewModel(ApplicationProvider.getApplicationContext())
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen10_settings_tablet_minimal_clair() =
        captureScreen("10_settings", "tablet", "minimal-clair", AppearanceMode.LIGHT, AppStyle.MINIMAL) {
            SettingsScreen(
            onBack = {},
            onNavigateToPermissions = {},
            settingsViewModel = SettingsViewModel(ApplicationProvider.getApplicationContext())
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen10_settings_tablet_minimal_sombre() =
        captureScreen("10_settings", "tablet", "minimal-sombre", AppearanceMode.DARK, AppStyle.MINIMAL) {
            SettingsScreen(
            onBack = {},
            onNavigateToPermissions = {},
            settingsViewModel = SettingsViewModel(ApplicationProvider.getApplicationContext())
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen10_settings_tablet_neon_clair() =
        captureScreen("10_settings", "tablet", "neon-clair", AppearanceMode.LIGHT, AppStyle.NEON) {
            SettingsScreen(
            onBack = {},
            onNavigateToPermissions = {},
            settingsViewModel = SettingsViewModel(ApplicationProvider.getApplicationContext())
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen10_settings_tablet_neon_sombre() =
        captureScreen("10_settings", "tablet", "neon-sombre", AppearanceMode.DARK, AppStyle.NEON) {
            SettingsScreen(
            onBack = {},
            onNavigateToPermissions = {},
            settingsViewModel = SettingsViewModel(ApplicationProvider.getApplicationContext())
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen10_settings_tablet_verre_clair() =
        captureScreen("10_settings", "tablet", "verre-clair", AppearanceMode.LIGHT, AppStyle.GLASS) {
            SettingsScreen(
            onBack = {},
            onNavigateToPermissions = {},
            settingsViewModel = SettingsViewModel(ApplicationProvider.getApplicationContext())
        )
        }

    @Test
    @Config(sdk = [36], qualifiers = "w820dp-h1180dp-normal-long-notround-port-xhdpi")
    fun screen10_settings_tablet_verre_sombre() =
        captureScreen("10_settings", "tablet", "verre-sombre", AppearanceMode.DARK, AppStyle.GLASS) {
            SettingsScreen(
            onBack = {},
            onNavigateToPermissions = {},
            settingsViewModel = SettingsViewModel(ApplicationProvider.getApplicationContext())
        )
        }

}
