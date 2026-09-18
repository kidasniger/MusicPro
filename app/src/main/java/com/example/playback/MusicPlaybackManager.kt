package com.example.playback

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.data.local.AudioTrackEntity
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

/**
 * Gestionnaire de lecture audio Media3 connectant l'interface Compose au MusicPlaybackService
 * via un MediaController asynchrone.
 */
class MusicPlaybackManager private constructor(private val appContext: Context) {

    private val scope = CoroutineScope(Dispatchers.Main)
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    private var currentPlaylist: List<AudioTrackEntity> = emptyList()
    private var positionTickerJob: Job? = null

    // StateFlows exposés pour l'UI Compose
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _currentTrack = MutableStateFlow<AudioTrackEntity?>(null)
    val currentTrack: StateFlow<AudioTrackEntity?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    init {
        connectToService()
    }

    companion object {
        private const val TAG = "MusicPlaybackManager"

        @Volatile
        private var INSTANCE: MusicPlaybackManager? = null

        fun getInstance(context: Context): MusicPlaybackManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MusicPlaybackManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }

    private fun connectToService() {
        val sessionToken = SessionToken(
            appContext,
            ComponentName(appContext, MusicPlaybackService::class.java)
        )

        controllerFuture = MediaController.Builder(appContext, sessionToken).buildAsync()
        controllerFuture?.addListener({
            try {
                val controller = controllerFuture?.get() ?: return@addListener
                mediaController = controller
                _isConnected.value = true
                setupControllerListener(controller)
                updateStateFromController(controller)
            } catch (e: Exception) {
                _errorMessage.value = "Impossible de se connecter au service audio: ${e.message}"
            }
        }, MoreExecutors.directExecutor())
    }

    private fun setupControllerListener(controller: MediaController) {
        controller.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                if (isPlaying) {
                    startPositionTicker()
                } else {
                    stopPositionTicker()
                }
                com.example.widget.MusicWidgetUpdater.update(appContext, _currentTrack.value, isPlaying)
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                updateCurrentTrackFromMediaItem(mediaItem)
                com.example.widget.MusicWidgetUpdater.update(appContext, _currentTrack.value, _isPlaying.value)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        _durationMs.value = mediaController?.duration?.coerceAtLeast(0L) ?: 0L
                    }
                    Player.STATE_ENDED -> {
                        _isPlaying.value = false
                        stopPositionTicker()
                    }
                    Player.STATE_IDLE -> {
                        _isPlaying.value = false
                        stopPositionTicker()
                    }
                    Player.STATE_BUFFERING -> {}
                }
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                _repeatMode.value = repeatMode
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                _isShuffleEnabled.value = shuffleModeEnabled
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e(TAG, "ExoPlayer playback error: ${error.errorCodeName} - ${error.message}", error)
                _errorMessage.value = "Erreur de lecture: fichier introuvable ou source audio inaccessible"
                _isPlaying.value = false
                stopPositionTicker()
            }
        })
    }

    private fun updateStateFromController(controller: MediaController) {
        _isPlaying.value = controller.isPlaying
        _repeatMode.value = controller.repeatMode
        _isShuffleEnabled.value = controller.shuffleModeEnabled
        _durationMs.value = controller.duration.coerceAtLeast(0L)
        _currentPositionMs.value = controller.currentPosition.coerceAtLeast(0L)
        updateCurrentTrackFromMediaItem(controller.currentMediaItem)

        if (controller.isPlaying) {
            startPositionTicker()
        }
    }

    private fun updateCurrentTrackFromMediaItem(mediaItem: MediaItem?) {
        if (mediaItem == null) {
            return
        }
        val mediaId = mediaItem.mediaId
        val track = currentPlaylist.firstOrNull { it.id.toString() == mediaId }
        if (track != null) {
            _currentTrack.value = track
            _durationMs.value = if (track.duration > 0) track.duration else (mediaController?.duration?.coerceAtLeast(0L) ?: 0L)
        } else {
            // Créer une entité temporaire depuis les métadonnées Media3
            val metadata = mediaItem.mediaMetadata
            val fallbackTrack = AudioTrackEntity(
                id = mediaId.toLongOrNull() ?: System.currentTimeMillis(),
                title = metadata.title?.toString() ?: "Titre Inconnu",
                artist = metadata.artist?.toString() ?: "Artiste Inconnu",
                album = metadata.albumTitle?.toString() ?: "Album Inconnu",
                duration = mediaController?.duration?.coerceAtLeast(0L) ?: 0L,
                path = mediaItem.localConfiguration?.uri?.toString() ?: "",
                contentUri = mediaItem.localConfiguration?.uri?.toString() ?: "",
                albumArtUri = metadata.artworkUri?.toString()
            )
            _currentTrack.value = fallbackTrack
        }
    }

    fun playTrack(track: AudioTrackEntity, playlist: List<AudioTrackEntity> = listOf(track)) {
        val controller = mediaController ?: run {
            _errorMessage.value = "Le contrôleur audio est en cours d'initialisation..."
            return
        }

        currentPlaylist = playlist
        _currentTrack.value = track

        val mediaItems = playlist.map { it.toMediaItem() }
        val targetIndex = playlist.indexOfFirst { it.id == track.id }.coerceAtLeast(0)

        controller.setMediaItems(mediaItems, targetIndex, 0L)
        controller.prepare()
        controller.play()

        _currentPositionMs.value = 0L
        _durationMs.value = track.duration
        _isPlaying.value = true
        startPositionTicker()
    }

    fun togglePlayPause() {
        val controller = mediaController ?: return
        if (controller.isPlaying) {
            controller.pause()
        } else {
            if (controller.mediaItemCount == 0 && _currentTrack.value != null) {
                _currentTrack.value?.let { playTrack(it) }
            } else {
                controller.play()
            }
        }
    }

    fun playNext() {
        val controller = mediaController ?: return
        if (controller.hasNextMediaItem()) {
            controller.seekToNextMediaItem()
        } else if (currentPlaylist.isNotEmpty()) {
            val currentIndex = currentPlaylist.indexOfFirst { it.id == _currentTrack.value?.id }
            val nextTrack = if (currentIndex != -1 && currentIndex < currentPlaylist.size - 1) {
                currentPlaylist[currentIndex + 1]
            } else {
                currentPlaylist.first()
            }
            playTrack(nextTrack, currentPlaylist)
        }
    }

    fun playPrevious() {
        val controller = mediaController ?: return
        if (controller.currentPosition > 3000L) {
            // Si plus de 3s de lecture, recommencer le morceau actuel
            controller.seekTo(0L)
            _currentPositionMs.value = 0L
        } else if (controller.hasPreviousMediaItem()) {
            controller.seekToPreviousMediaItem()
        } else if (currentPlaylist.isNotEmpty()) {
            val currentIndex = currentPlaylist.indexOfFirst { it.id == _currentTrack.value?.id }
            val prevTrack = if (currentIndex > 0) {
                currentPlaylist[currentIndex - 1]
            } else {
                currentPlaylist.last()
            }
            playTrack(prevTrack, currentPlaylist)
        }
    }

    fun seekTo(positionMs: Long) {
        val controller = mediaController ?: return
        controller.seekTo(positionMs.coerceAtLeast(0L))
        _currentPositionMs.value = positionMs
    }

    fun toggleRepeatMode() {
        val controller = mediaController ?: return
        val nextMode = when (controller.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        controller.repeatMode = nextMode
        _repeatMode.value = nextMode
    }

    fun toggleShuffle() {
        val controller = mediaController ?: return
        val newShuffle = !controller.shuffleModeEnabled
        controller.shuffleModeEnabled = newShuffle
        _isShuffleEnabled.value = newShuffle
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        val controller = mediaController ?: return
        controller.playbackParameters = PlaybackParameters(speed)
    }

    fun setCurrentTrackOnly(track: AudioTrackEntity) {
        _currentTrack.value = track
        _durationMs.value = track.duration
    }

    private fun startPositionTicker() {
        positionTickerJob?.cancel()
        positionTickerJob = scope.launch {
            while (isActive) {
                mediaController?.let { controller ->
                    if (controller.isPlaying) {
                        _currentPositionMs.value = controller.currentPosition.coerceAtLeast(0L)
                        val dur = controller.duration
                        if (dur > 0) {
                            _durationMs.value = dur
                        }
                    }
                }
                delay(180)
            }
        }
    }

    private fun stopPositionTicker() {
        positionTickerJob?.cancel()
        positionTickerJob = null
    }

    private fun AudioTrackEntity.toMediaItem(): MediaItem {
        val trackUri = resolvePlayableUri(this)

        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setAlbumTitle(album)
            .setArtworkUri(if (!albumArtUri.isNullOrBlank()) Uri.parse(albumArtUri) else null)
            .setExtras(Bundle().apply {
                putLong("track_id", id)
                putLong("duration_ms", duration)
                putString("format", getAudioFormat())
            })
            .build()

        return MediaItem.Builder()
            .setMediaId(id.toString())
            .setUri(trackUri)
            .setMediaMetadata(metadata)
            .build()
    }

    private fun resolvePlayableUri(track: AudioTrackEntity): Uri {
        // 1. Détection des pistes de démonstration (ID 1000..1020 ou URI synthétique MediaStore)
        val isDemoTrack = (track.id in 1000L..1020L) ||
                track.contentUri.startsWith("content://media/external/audio/media/100") ||
                track.path.contains("/Music/Pop/") ||
                track.path.contains("/Music/Dance/") ||
                track.path.contains("/Music/Electro/") ||
                track.path.contains("/Music/Rock/")

        if (isDemoTrack) {
            val demoFile = DemoAudioGenerator.getOrCreateDemoAudioFile(appContext, track.id)
            return Uri.fromFile(demoFile)
        }

        // 2. Vérification de la validité de l'URI content:// via ContentResolver
        if (track.contentUri.isNotBlank()) {
            val uri = Uri.parse(track.contentUri)
            if (track.contentUri.startsWith("content://")) {
                var isAccessible = false
                try {
                    appContext.contentResolver.openAssetFileDescriptor(uri, "r")?.use {
                        isAccessible = true
                    }
                } catch (e: Exception) {
                    isAccessible = false
                }
                if (isAccessible) {
                    return uri
                }
            } else if (track.contentUri.startsWith("file://")) {
                val f = File(uri.path ?: "")
                if (f.exists() && f.canRead()) {
                    return uri
                }
            } else if (track.contentUri.startsWith("http://") || track.contentUri.startsWith("https://")) {
                return uri
            }
        }

        // 3. Vérification du chemin direct dans le système de fichiers
        if (track.path.isNotBlank()) {
            if (track.path.startsWith("content://") || track.path.startsWith("http://") || track.path.startsWith("https://")) {
                return Uri.parse(track.path)
            }
            val file = File(track.path)
            if (file.exists() && file.canRead()) {
                return Uri.fromFile(file)
            }
        }

        // 4. File-safe fallback pour toute piste dont le fichier physique est manquant
        val fallbackFile = DemoAudioGenerator.getOrCreateDemoAudioFile(appContext, track.id)
        return Uri.fromFile(fallbackFile)
    }
}
