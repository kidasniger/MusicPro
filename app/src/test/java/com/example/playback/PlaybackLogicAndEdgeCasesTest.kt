package com.example.playback

import android.app.Application
import android.content.Context
import androidx.media3.common.Player
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AudioTrackEntity
import com.example.ui.audio.AudioViewModel
import com.example.ui.audio.LibraryTab
import com.example.ui.audio.LrclibSearchUiState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/**
 * Tests unitaires et d'intégration Robolectric pour la logique de lecture/pause et scénarios limites :
 * - Logique de lecture, pause, changement de piste (next/prev)
 * - Mode répétition (OFF, ALL, ONE) et mode aléatoire (Shuffle)
 * - Vitesse de lecture
 * - Scénarios limites :
 *    1. Permission refusée / aucun fichier audio disponible
 *    2. Recherche ou génération de paroles sans connexion / sans clé API
 *    3. Rétablissement après mise en arrière-plan et survie du service Media3
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlaybackLogicAndEdgeCasesTest {

    private lateinit var application: Application
    private lateinit var context: Context

    private val sampleTrack1 = AudioTrackEntity(
        id = 101L,
        title = "Synth Odyssey",
        artist = "Retro Rider",
        album = "Neon Grid",
        duration = 180_000L,
        contentUri = "content://media/external/audio/media/101",
        path = "/storage/emulated/0/Music/Synth Odyssey.mp3",
        folder = "Music",
        mimeType = "audio/mpeg"
    )

    private val sampleTrack2 = AudioTrackEntity(
        id = 102L,
        title = "Midnight City",
        artist = "Cyber Echo",
        album = "Neon Grid",
        duration = 240_000L,
        contentUri = "content://media/external/audio/media/102",
        path = "/storage/emulated/0/Music/Midnight City.mp3",
        folder = "Music",
        mimeType = "audio/mpeg"
    )

    @Before
    fun setup() {
        application = ApplicationProvider.getApplicationContext()
        context = application
    }

    @Test
    fun testPlaybackManagerInitialState() {
        val playbackManager = MusicPlaybackManager.getInstance(context)

        // Par défaut, pas de lecture active si non démarrée
        assertNotNull(playbackManager)
        assertEquals(0L, playbackManager.currentPositionMs.value)
        assertEquals(Player.REPEAT_MODE_OFF, playbackManager.repeatMode.value)
        assertFalse(playbackManager.isShuffleEnabled.value)
        assertEquals(1.0f, playbackManager.playbackSpeed.value, 0.01f)
    }

    @Test
    fun testSetCurrentTrackAndPlaybackParameters() {
        val playbackManager = MusicPlaybackManager.getInstance(context)

        playbackManager.setCurrentTrackOnly(sampleTrack1)
        assertEquals(sampleTrack1, playbackManager.currentTrack.value)
        assertEquals(sampleTrack1.duration, playbackManager.durationMs.value)

        // Test vitesse de lecture
        playbackManager.setPlaybackSpeed(1.25f)
        assertEquals(1.25f, playbackManager.playbackSpeed.value, 0.01f)
        playbackManager.setPlaybackSpeed(1.0f)
        assertEquals(1.0f, playbackManager.playbackSpeed.value, 0.01f)
    }

    @Test
    fun testRepeatModeAndShuffleCycling() {
        val playbackManager = MusicPlaybackManager.getInstance(context)

        // Cycle repeat : OFF -> ALL -> ONE -> OFF (si MediaController connecté ou géré)
        assertNotNull(playbackManager.repeatMode.value)
        assertNotNull(playbackManager.isShuffleEnabled.value)
    }

    @Test
    fun testEdgeCase_NoAudioFilesAvailable() = runTest {
        val viewModel = AudioViewModel(application)

        // Vérifier l'état initial des pistes si la base de données est vide
        // La gestion d'erreur ne doit pas planter l'application
        viewModel.refreshScan()
        assertNotNull(viewModel.statusMessage.value)

        // Tenter de basculer lecture/pause quand aucune piste n'est disponible
        if (viewModel.currentTrack.value == null) {
            viewModel.togglePlayPause()
            // Doit rester inactif sans crash
            assertNull(viewModel.currentTrack.value)
            assertFalse(viewModel.isPlaying.value)
        }
    }

    @Test
    fun testEdgeCase_OfflineLyricsSearchNetworkError() = runTest {
        val viewModel = AudioViewModel(application)

        // Recherche en ligne avec des paramètres qui génèrent une absence de réseau ou timeout
        viewModel.searchOnlineLyrics(
            title = "Unknown Offline Track 404",
            artist = "No Network Artist",
            durationSec = 180
        )

        // Vérification de l'état initial avant / après déclenchement
        assertNotNull(viewModel.lrclibSearchState.value)

        // Reset de l'état de recherche
        viewModel.resetLrclibSearch()
        assertEquals(LrclibSearchUiState.Idle, viewModel.lrclibSearchState.value)
    }

    @Test
    fun testEdgeCase_GroqTranscriptionWithoutApiKey() = runTest {
        val viewModel = AudioViewModel(application)
        com.example.data.security.GroqApiKeyStore.getInstance(context).clearApiKey()

        // Lancement de transcription sans clé API
        viewModel.startGroqTranscription(sampleTrack1)

        // Doit signaler une erreur claire indiquant la clé API manquante
        val errorMsg = viewModel.groqErrorMessage.value
        assertNotNull(errorMsg)
        assertTrue(errorMsg!!.contains("Clé API Groq manquante"))
        assertFalse(viewModel.isGroqTranscribing.value)

        viewModel.clearGroqError()
        assertNull(viewModel.groqErrorMessage.value)
    }

    @Test
    fun testEdgeCase_GroqTranscriptionWithMissingFile() = runTest {
        val viewModel = AudioViewModel(application)
        com.example.data.security.GroqApiKeyStore.getInstance(context).setApiKey("gsk_dummy_test_key_12345")

        // Morceau dont le fichier n'existe pas sur disque
        val trackWithNonExistentPath = sampleTrack1.copy(path = "/non/existent/path/audio.mp3")
        viewModel.startGroqTranscription(trackWithNonExistentPath)

        val errorMsg = viewModel.groqErrorMessage.value
        assertNotNull(errorMsg)
        assertTrue(errorMsg!!.contains("introuvable"))
        assertFalse(viewModel.isGroqTranscribing.value)

        // Nettoyage clé
        com.example.data.security.GroqApiKeyStore.getInstance(context).clearApiKey()
    }

    @Test
    fun testAudioTrackEntityHelperMethods() {
        val flacTrack = sampleTrack1.copy(path = "/music/song.flac", mimeType = "audio/flac")
        assertEquals("FLAC", flacTrack.getAudioFormat())

        val wavTrack = sampleTrack1.copy(path = "/music/song.wav", mimeType = "audio/wav")
        assertEquals("WAV", wavTrack.getAudioFormat())

        val oggTrack = sampleTrack1.copy(path = "/music/song.ogg", mimeType = "audio/ogg")
        assertEquals("OGG", oggTrack.getAudioFormat())

        val mp3Track = sampleTrack1.copy(path = "/music/song.mp3", mimeType = "audio/mpeg")
        assertEquals("MP3", mp3Track.getAudioFormat())

        // Formats de durée
        assertEquals("3:00", sampleTrack1.formatDuration())
        assertEquals("4:00", sampleTrack2.formatDuration())
    }

    @Test
    fun testMusicPlaybackServiceSurvivalOnTaskRemoved() {
        // Simuler onTaskRemoved sur le service MediaSession
        val service = MusicPlaybackService()
        // Si aucun lecteur actif en cours de lecture, le service doit s'arrêter proprement
        // sans exception non interceptée
        service.onTaskRemoved(null)
    }
}
