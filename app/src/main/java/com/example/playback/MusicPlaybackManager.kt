package com.example.playback

import android.content.ComponentName
import android.os.Bundle
import android.content.Context
import android.net.Uri
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
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.AudioRepository
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

/**
 * Gestionnaire de lecture audio Media3 connectant l'interface Compose au MusicPlaybackService
 * via un MediaController asynchrone.
 */
class MusicPlaybackManager private constructor(private val appContext: Context) {

    private val scope = CoroutineScope(Dispatchers.Main)
    private val preferencesRepository by lazy { UserPreferencesRepository(appContext) }
    private val audioRepository by lazy { AudioRepository.getInstance(appContext) }
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    private var currentPlaylist: List<AudioTrackEntity> = emptyList()

    private val _queue = MutableStateFlow<List<AudioTrackEntity>>(emptyList())
    val queue: StateFlow<List<AudioTrackEntity>> = _queue.asStateFlow()

    private val _queueIndex = MutableStateFlow(0)
    val queueIndex: StateFlow<Int> = _queueIndex.asStateFlow()
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

    private val _eqEnabled = MutableStateFlow(false)
    val eqEnabled: StateFlow<Boolean> = _eqEnabled.asStateFlow()

    private val _eqPreset = MutableStateFlow("Flat")
    val eqPreset: StateFlow<String> = _eqPreset.asStateFlow()

    private val _eqLevels = MutableStateFlow(listOf(0, 0, 0, 0, 0))
    val eqLevels: StateFlow<List<Int>> = _eqLevels.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var hasRetriedFallback = false

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
                } else {
                    restoreSavedPlayback(controller)
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
                val previousTrackId = _currentTrack.value?.id
                updateCurrentTrackFromMediaItem(mediaItem)
                _queue.value = currentPlaylist
                _queueIndex.value = currentPlaylist.indexOfFirst { it.id == _currentTrack.value?.id }.coerceAtLeast(0)

                // Une nouvelle piste commence toujours à sa position de lecture réelle.
                // Media3 remet normalement la nouvelle piste à 0 ms lors d'une transition
                // automatique/manuelle. Mettre immédiatement ce changement dans le StateFlow
                // évite que l'écran des paroles conserve quelques instants la position du
                // morceau précédent.
                if (_currentTrack.value?.id != previousTrackId) {
                    _currentPositionMs.value = mediaController?.currentPosition?.coerceAtLeast(0L) ?: 0L
                    _durationMs.value = _currentTrack.value?.duration?.coerceAtLeast(0L)
                        ?: mediaController?.duration?.coerceAtLeast(0L)
                        ?: 0L
                }

                com.example.widget.MusicWidgetUpdater.update(appContext, _currentTrack.value, _isPlaying.value)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        hasRetriedFallback = false
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
                val track = _currentTrack.value
                val position = _currentPositionMs.value

                // Tentative de récupération automatique si le fichier physique existe
                if (track != null && !hasRetriedFallback && track.path.isNotBlank()) {
                    val directFile = File(track.path)
                    if (directFile.exists() && directFile.canRead()) {
                        Log.i(TAG, "Tentative de récupération automatique sur fichier direct pour ${track.title}")
                        hasRetriedFallback = true
                        try {
                            val controller = mediaController
                            if (controller != null) {
                                val directMediaItem = track.toMediaItem(preferDirectFile = true)
                                controller.setMediaItem(directMediaItem, position.coerceAtLeast(0L))
                                controller.prepare()
                                controller.play()
                                _isPlaying.value = true
                                startPositionTicker()
                                return
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Échec récupération automatique: ${e.message}", e)
                        }
                    }
                }

                hasRetriedFallback = false
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

    private fun restoreSavedPlayback(controller: MediaController) {
        if (controller.mediaItemCount > 0) return

        scope.launch {
            val saved = preferencesRepository.savedPlaybackState.first() ?: return@launch
            if (controller.mediaItemCount > 0) return@launch

            val track = audioRepository.getTrackById(saved.trackId) ?: return@launch
            val library = audioRepository.getAllTracksSnapshot()
                .ifEmpty { listOf(track) }
            val restoredPlaylist = if (library.any { it.id == track.id }) library else listOf(track)
            val targetIndex = restoredPlaylist.indexOfFirst { it.id == track.id }.coerceAtLeast(0)

            controller.shuffleModeEnabled = saved.shuffleEnabled
            _isShuffleEnabled.value = saved.shuffleEnabled

            val maxPosition = (track.duration - 250L).coerceAtLeast(0L)
            val position = saved.positionMs.coerceIn(0L, maxPosition)

            currentPlaylist = restoredPlaylist
            _queue.value = restoredPlaylist
            _queueIndex.value = targetIndex
            _currentTrack.value = track
            _currentPositionMs.value = position
            _durationMs.value = track.duration

            val mediaItems = restoredPlaylist.map { it.toMediaItem() }
            controller.setMediaItems(mediaItems, targetIndex, position)
            controller.prepare()
            if (saved.wasPlaying) {
                controller.play()
                _isPlaying.value = true
                startPositionTicker()
            } else {
                controller.pause()
                _isPlaying.value = false
            }
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
        _queue.value = playlist
        _queueIndex.value = playlist.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
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

    fun pause() {
        val controller = mediaController ?: return
        controller.pause()
        _isPlaying.value = false
        stopPositionTicker()
    }

    fun play() {
        val controller = mediaController ?: return
        if (controller.mediaItemCount == 0 && _currentTrack.value != null) {
            _currentTrack.value?.let { playTrack(it) }
        } else {
            controller.play()
            _isPlaying.value = true
            startPositionTicker()
        }
    }

    fun reloadCurrentTrack(positionMs: Long = _currentPositionMs.value, autoResume: Boolean = true) {
        val track = _currentTrack.value ?: return
        val controller = mediaController ?: return
        val playlist = currentPlaylist.ifEmpty { listOf(track) }
        val targetIndex = playlist.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        val mediaItems = playlist.map { it.toMediaItem() }

        controller.setMediaItems(mediaItems, targetIndex, positionMs.coerceAtLeast(0L))
        controller.prepare()
        if (autoResume) {
            controller.play()
            _isPlaying.value = true
            startPositionTicker()
        } else {
            controller.pause()
            _isPlaying.value = false
        }
    }

    fun togglePlayPause() {
        val controller = mediaController ?: return
        if (controller.isPlaying) {
            pause()
        } else {
            play()
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
        scope.launch {
            preferencesRepository.setShuffleEnabled(newShuffle)
        }
    }

    fun addToQueue(track: AudioTrackEntity, playNext: Boolean = false) {
        val controller = mediaController
        val current = currentPlaylist.toMutableList()
        if (current.any { it.id == track.id }) return
        val insertAt = if (playNext) (_queueIndex.value + 1).coerceAtMost(current.size) else current.size
        current.add(insertAt, track)
        currentPlaylist = current
        _queue.value = current
        if (controller != null) controller.addMediaItem(insertAt, track.toMediaItem())
    }

    fun removeFromQueue(index: Int) {
        val current = currentPlaylist
        if (index !in current.indices) return
        if (index == _queueIndex.value) return
        mediaController?.removeMediaItem(index)
        currentPlaylist = current.toMutableList().also { it.removeAt(index) }
        _queue.value = currentPlaylist
        _queueIndex.value = currentPlaylist.indexOfFirst { it.id == _currentTrack.value?.id }.coerceAtLeast(0)
    }

    fun moveQueueItem(from: Int, to: Int) {
        val current = currentPlaylist.toMutableList()
        if (from !in current.indices || to !in current.indices || from == to) return
        mediaController?.moveMediaItem(from, to)
        val item = current.removeAt(from)
        current.add(to, item)
        currentPlaylist = current
        _queue.value = current
        _queueIndex.value = current.indexOfFirst { it.id == _currentTrack.value?.id }.coerceAtLeast(0)
    }

    fun clearQueue() {
        val current = _currentTrack.value ?: return
        currentPlaylist = listOf(current)
        _queue.value = listOf(current)
        _queueIndex.value = 0
        val controller = mediaController ?: return
        if (controller.mediaItemCount > 1) {
            controller.removeMediaItems(1, controller.mediaItemCount)
        }
    }

    fun setEqualizerEnabled(enabled: Boolean) {
        _eqEnabled.value = enabled
        sendAudioEffect(AudioEffectCommands.SET_ENABLED, Bundle().apply {
            putBoolean(AudioEffectCommands.KEY_ENABLED, enabled)
        })
    }

    fun setEqualizerPreset(name: String) {
        val presets = mapOf(
            "Flat" to -1,
            "Bass Boost" to 0,
            "Vocal" to 1,
            "Rock" to 2,
            "Classical" to 3,
            "Hip-Hop" to 4
        )
        val preset = presets[name] ?: -1
        _eqPreset.value = name
        if (preset >= 0) {
            sendAudioEffect(AudioEffectCommands.SET_PRESET, Bundle().apply {
                putShort(AudioEffectCommands.KEY_PRESET, preset.toShort())
            })
        } else {
            _eqLevels.value = listOf(0, 0, 0, 0, 0)
            sendAudioEffect(AudioEffectCommands.RESET, Bundle.EMPTY)
        }
    }

    fun setEqualizerBand(index: Int, levelMb: Int) {
        val normalizedIndex = index.coerceIn(0, 4)
        val next = _eqLevels.value.toMutableList()
        next[normalizedIndex] = levelMb.coerceIn(-1500, 1500)
        _eqLevels.value = next
        _eqPreset.value = "Personnalisé"
        sendAudioEffect(AudioEffectCommands.SET_BAND, Bundle().apply {
            putInt(AudioEffectCommands.KEY_BAND, normalizedIndex)
            putShort(AudioEffectCommands.KEY_LEVEL, next[normalizedIndex].toShort())
        })
    }

    fun resetEqualizer() {
        _eqEnabled.value = false
        _eqPreset.value = "Flat"
        _eqLevels.value = listOf(0, 0, 0, 0, 0)
        sendAudioEffect(AudioEffectCommands.RESET, Bundle.EMPTY)
    }

    private fun sendAudioEffect(command: androidx.media3.session.SessionCommand, args: Bundle) {
        mediaController?.sendCustomCommand(command, args)
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
                delay(80)
            }
        }
    }

    private fun stopPositionTicker() {
        positionTickerJob?.cancel()
        positionTickerJob = null
    }

    private fun AudioTrackEntity.toMediaItem(preferDirectFile: Boolean = false): MediaItem {
        val uri = when {
            preferDirectFile && path.isNotBlank() && !path.startsWith("content://") && !path.startsWith("http") -> {
                Uri.fromFile(File(path))
            }
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
