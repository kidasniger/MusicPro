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

    // File d'attente réellement choisie par l'utilisateur. La bibliothèque/playlist de
    // lecture reste indépendante et ne doit pas apparaître comme "À suivre".
    private val userQueue = mutableListOf<AudioTrackEntity>()

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

                val currentMediaIndex = mediaController?.currentMediaItemIndex ?: 0
                if (currentMediaIndex > 0 && userQueue.isNotEmpty()) {
                    val currentId = _currentTrack.value?.id
                    val queuedIndex = userQueue.indexOfFirst { it.id == currentId }
                    if (queuedIndex >= 0) {
                        userQueue.removeAt(queuedIndex)
                        _queue.value = userQueue.toList()
                    }
                }
                _queueIndex.value = currentMediaIndex

                // Toujours publier immédiatement la position du nouveau morceau.
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
            userQueue.clear()
            _queue.value = emptyList()
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

    /** File d'attente utilisateur, distincte de la bibliothèque/playlist de lecture. */
    fun addToQueue(track: AudioTrackEntity, playNext: Boolean = false) {
        if (userQueue.any { it.id == track.id }) return
        if (_currentTrack.value?.id == track.id) return

        if (playNext) {
            userQueue.add(0, track)
        } else {
            userQueue.add(track)
        }
        _queue.value = userQueue.toList()

        val controller = mediaController ?: return
        val current = _currentTrack.value ?: return
        val wasPlaying = controller.isPlaying
        val position = controller.currentPosition.coerceAtLeast(0L)

        currentPlaylist = listOf(current) + userQueue
        _queueIndex.value = 0

        controller.setMediaItems(currentPlaylist.map { it.toMediaItem() }, 0, position)
        controller.prepare()
        if (wasPlaying) controller.play()
    }

    /** Lit une entrée de la file puis conserve toutes les entrées suivantes. */
    fun playQueuedTrack(index: Int) {
        if (index !in userQueue.indices) return

        val selected = userQueue.removeAt(index)
        val remaining = userQueue.toList()
        _queue.value = remaining

        currentPlaylist = listOf(selected) + remaining
        _queueIndex.value = 0
        _currentTrack.value = selected

        val controller = mediaController ?: return
        controller.setMediaItems(currentPlaylist.map { it.toMediaItem() }, 0, 0L)
        controller.prepare()
        controller.play()

        _currentPositionMs.value = 0L
        _durationMs.value = selected.duration
        _isPlaying.value = true
        startPositionTicker()
    }

    fun removeFromQueue(index: Int) {
        if (index !in userQueue.indices) return

        userQueue.removeAt(index)
        _queue.value = userQueue.toList()

        val controller = mediaController ?: return
        val mediaIndex = index + 1
        if (mediaIndex in 1 until controller.mediaItemCount) {
            controller.removeMediaItem(mediaIndex)
            if (mediaIndex in currentPlaylist.indices) {
                currentPlaylist = currentPlaylist.toMutableList().also { it.removeAt(mediaIndex) }
            }
        }
    }

    fun moveQueueItem(from: Int, to: Int) {
        if (from !in userQueue.indices || to !in userQueue.indices || from == to) return

        val item = userQueue.removeAt(from)
        userQueue.add(to, item)
        _queue.value = userQueue.toList()

        val controller = mediaController
        val mediaFrom = from + 1
        val mediaTo = to + 1
        if (controller != null &&
            mediaFrom in 1 until controller.mediaItemCount &&
            mediaTo in 1 until controller.mediaItemCount
        ) {
            controller.moveMediaItem(mediaFrom, mediaTo)
        }

        if (mediaFrom in currentPlaylist.indices && mediaTo in currentPlaylist.indices) {
            val updated = currentPlaylist.toMutableList()
            val moved = updated.removeAt(mediaFrom)
            updated.add(mediaTo, moved)
            currentPlaylist = updated
        }
    }

    fun clearQueue() {
        userQueue.clear()
        _queue.value = emptyList()

        val current = _currentTrack.value ?: return
        currentPlaylist = listOf(current)
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
            "Flat" to listOf(0, 0, 0, 0, 0),
            "Bass Boost" to listOf(650, 450, 150, -100, 250),
            "Vocal" to listOf(-250, 50, 500, 350, -100),
            "Rock" to listOf(550, 350, -150, 400, 550),
            "Classical" to listOf(350, 150, -100, 250, 450),
            "Hip-Hop" to listOf(600, 350, 100, 350, 600)
        )
        val levels = presets[name] ?: presets.getValue("Flat")

        _eqPreset.value = name
        _eqLevels.value = levels

        sendAudioEffect(AudioEffectCommands.SET_ENABLED, Bundle().apply {
            putBoolean(AudioEffectCommands.KEY_ENABLED, name != "Flat")
        })

        levels.forEachIndexed { index, level ->
            sendAudioEffect(AudioEffectCommands.SET_BAND, Bundle().apply {
                putInt(AudioEffectCommands.KEY_BAND, index)
                putShort(
                    AudioEffectCommands.KEY_LEVEL,
                    level.coerceIn(-1500, 1500).toShort()
                )
            })
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
