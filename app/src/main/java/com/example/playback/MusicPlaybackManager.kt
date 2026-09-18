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
import androidx.core.content.ContextCompat
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
    private var pendingTrackToPlay: AudioTrackEntity? = null
    private var pendingPlaylistToPlay: List<AudioTrackEntity> = emptyList()

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

                val pendingTrack = pendingTrackToPlay
                if (pendingTrack != null) {
                    val pendingList = if (pendingPlaylistToPlay.isNotEmpty()) pendingPlaylistToPlay else listOf(pendingTrack)
                    pendingTrackToPlay = null
                    pendingPlaylistToPlay = emptyList()
                    playTrack(pendingTrack, pendingList)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Impossible de se connecter au service audio: ${e.message}"
            }
        }, ContextCompat.getMainExecutor(appContext))
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
        val controller = mediaController
        if (controller == null) {
            pendingTrackToPlay = track
            pendingPlaylistToPlay = playlist
            _currentTrack.value = track
            _isPlaying.value = true
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
        val uri = when {
            contentUri.isNotBlank() -> Uri.parse(contentUri)
            path.isNotBlank() -> {
                if (path.startsWith("content://") || path.startsWith("http://") || path.startsWith("https://") || path.startsWith("file://")) {
                    Uri.parse(path)
                } else {
                    Uri.fromFile(File(path))
                }
            }
            else -> Uri.parse("content://media/external/audio/media/$id")
        }

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
            .setUri(uri)
            .setMediaMetadata(metadata)
            .build()
    }
}
