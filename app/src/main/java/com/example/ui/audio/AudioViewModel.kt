package com.example.ui.audio

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AudioTrackEntity
import com.example.data.local.PlaylistSummary
import com.example.data.repository.AudioRepository
import com.example.data.repository.PlaylistRepository
import com.example.groq.GroqTranscriptionResult
import com.example.lyrics.LyricsData
import com.example.lyrics.remote.LrclibSearchResult
import com.example.playback.MusicPlaybackManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

sealed class PendingLyricsWrite {
    data class Lrclib(val track: AudioTrackEntity, val result: LrclibSearchResult) : PendingLyricsWrite()
    data class Groq(val track: AudioTrackEntity, val result: GroqTranscriptionResult) : PendingLyricsWrite()
    data class ManualLrc(val track: AudioTrackEntity, val lrcText: String) : PendingLyricsWrite()
    data class EmbedCurrent(val track: AudioTrackEntity, val lyricsData: LyricsData) : PendingLyricsWrite()
}

sealed class LrclibSearchUiState {
    data object Idle : LrclibSearchUiState()
    data object Loading : LrclibSearchUiState()
    data class Success(val results: List<LrclibSearchResult>) : LrclibSearchUiState()
    data class Empty(val queryTitle: String, val queryArtist: String) : LrclibSearchUiState()
    data class Error(val message: String) : LrclibSearchUiState()
}

enum class LibraryTab(val label: String) {
    TRACKS("Morceaux"),
    PLAYLISTS("Playlists"),
    ALBUMS("Albums"),
    ARTISTS("Artistes"),
    FOLDERS("Dossiers")
}

enum class SearchFilter(val label: String) {
    ALL("Tous"),
    TITLES("Titres"),
    ARTISTS("Artistes"),
    ALBUMS("Albums")
}

data class AlbumSummary(
    val name: String,
    val artist: String,
    val trackCount: Int,
    val coverUri: String?,
    val sampleTrack: AudioTrackEntity
)

data class ArtistSummary(
    val name: String,
    val trackCount: Int,
    val albumsCount: Int
)

data class FolderSummary(
    val name: String,
    val trackCount: Int,
    val samplePath: String
)

class AudioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AudioRepository.getInstance(application)
    private val playlistRepository = PlaylistRepository.getInstance(application)

    // Playlists gérées via Room Database
    val playlists: StateFlow<List<PlaylistSummary>> = playlistRepository.getPlaylistSummaries()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedPlaylistId = MutableStateFlow<Long?>(null)
    val selectedPlaylistId: StateFlow<Long?> = _selectedPlaylistId.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val selectedPlaylistTracks: StateFlow<List<AudioTrackEntity>> = _selectedPlaylistId
        .flatMapLatest { id ->
            if (id != null) {
                playlistRepository.getTracksForPlaylist(id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val selectedPlaylist: StateFlow<PlaylistSummary?> = combine(playlists, _selectedPlaylistId) { list, id ->
        list.firstOrNull { it.id == id }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Tracks observed from Room Database
    val tracks: StateFlow<List<AudioTrackEntity>> = repository.allTracks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Morceaux récemment écoutés (triés par date d'écoute décroissante)
    val recentTracks: StateFlow<List<AudioTrackEntity>> = repository.getRecentTracks(20)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>("Indexation locale automatique")
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _selectedTab = MutableStateFlow(LibraryTab.TRACKS)
    val selectedTab: StateFlow<LibraryTab> = _selectedTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchFilter = MutableStateFlow(SearchFilter.ALL)
    val searchFilter: StateFlow<SearchFilter> = _searchFilter.asStateFlow()

    private val _favorites = MutableStateFlow<Set<Long>>(emptySet())
    val favorites: StateFlow<Set<Long>> = _favorites.asStateFlow()

    // Gestionnaire de lecture Media3 (Foreground Service, Audio Focus, MediaStyle notification)
    private val playbackManager = MusicPlaybackManager.getInstance(application)

    val currentTrack: StateFlow<AudioTrackEntity?> = playbackManager.currentTrack
    val isPlaying: StateFlow<Boolean> = playbackManager.isPlaying
    val progressMs: StateFlow<Long> = playbackManager.currentPositionMs
    val durationMs: StateFlow<Long> = playbackManager.durationMs
    val repeatMode: StateFlow<Int> = playbackManager.repeatMode
    val isShuffleEnabled: StateFlow<Boolean> = playbackManager.isShuffleEnabled
    val playbackSpeed: StateFlow<Float> = playbackManager.playbackSpeed
    val playbackErrorMessage: StateFlow<String?> = playbackManager.errorMessage

    fun clearPlaybackErrorMessage() = playbackManager.clearErrorMessage()

    private val lyricsController = AudioLyricsController(
        application = application,
        scope = viewModelScope,
        audioRepository = repository,
        playbackManager = playbackManager
    )

    val lyricsData: StateFlow<LyricsData> = lyricsController.lyricsData
    val isLyricsLoading: StateFlow<Boolean> = lyricsController.isLyricsLoading
    val lrclibSearchState: StateFlow<LrclibSearchUiState> = lyricsController.lrclibSearchState
    val saveFeedbackMessage: StateFlow<String?> = lyricsController.saveFeedbackMessage
    val pendingLyricsWrite: StateFlow<PendingLyricsWrite?> = lyricsController.pendingLyricsWrite
    val intentSenderRequest: SharedFlow<IntentSenderRequest> = lyricsController.intentSenderRequest
    val legacyWritePermissionRequest: SharedFlow<Boolean> = lyricsController.legacyWritePermissionRequest
    val isGroqTranscribing: StateFlow<Boolean> = lyricsController.isGroqTranscribing
    val groqProgressMessage: StateFlow<String> = lyricsController.groqProgressMessage
    val groqTranscriptionResult: StateFlow<GroqTranscriptionResult?> = lyricsController.groqTranscriptionResult
    val groqErrorMessage: StateFlow<String?> = lyricsController.groqErrorMessage

    fun hasGroqApiKey(): Boolean = lyricsController.hasGroqApiKey()
    fun loadLyricsForTrack(track: AudioTrackEntity) = lyricsController.loadLyricsForTrack(track)
    fun importLrcText(track: AudioTrackEntity, lrcText: String) = lyricsController.importLrcText(track, lrcText)
    fun searchOnlineLyrics(title: String, artist: String, durationSec: Int?) =
        lyricsController.searchOnlineLyrics(title, artist, durationSec)
    fun applyLrclibResult(
        track: AudioTrackEntity,
        result: LrclibSearchResult,
        onComplete: ((LyricsSaveResult) -> Unit)? = null
    ) = lyricsController.applyLrclibResult(track, result, onComplete)
    fun clearSaveFeedback() = lyricsController.clearSaveFeedback()
    fun resetLrclibSearch() = lyricsController.resetLrclibSearch()
    fun startGroqTranscription(track: AudioTrackEntity) = lyricsController.startGroqTranscription(track)
    fun applyGroqResult(
        track: AudioTrackEntity,
        result: GroqTranscriptionResult,
        onComplete: ((LyricsSaveResult) -> Unit)? = null
    ) = lyricsController.applyGroqResult(track, result, onComplete)
    fun dismissGroqPreview() = lyricsController.dismissGroqPreview()
    fun clearGroqError() = lyricsController.clearGroqError()
    fun needsWritePermission(track: AudioTrackEntity): Boolean = lyricsController.needsWritePermission(track)
    fun triggerWritePermissionRequest(track: AudioTrackEntity) = lyricsController.triggerWritePermissionRequest(track)
    fun onWritePermissionResult(granted: Boolean) = lyricsController.onWritePermissionResult(granted)
    fun requestApplyLrclib(track: AudioTrackEntity, result: LrclibSearchResult) =
        lyricsController.requestApplyLrclib(track, result)
    fun requestApplyGroq(track: AudioTrackEntity, result: GroqTranscriptionResult) =
        lyricsController.requestApplyGroq(track, result)
    fun requestEmbedCurrentLyrics(track: AudioTrackEntity, lyricsData: LyricsData) =
        lyricsController.requestEmbedCurrentLyrics(track, lyricsData)
    fun embedLyricsInTrack(track: AudioTrackEntity, lyricsData: LyricsData) =
        lyricsController.embedLyricsInTrack(track, lyricsData)
    fun applyManualLrcResult(track: AudioTrackEntity, lrcText: String) =
        lyricsController.applyManualLrcResult(track, lrcText)

    // ==========================================
    // GESTION DES PLAYLISTS ROOM
    // ==========================================

    fun selectPlaylist(playlistId: Long?) {
        _selectedPlaylistId.value = playlistId
    }

    fun createPlaylist(name: String, description: String = "", initialTrackIds: List<Long> = emptyList()) {
        viewModelScope.launch {
            val newId = playlistRepository.createPlaylist(name, description)
            if (initialTrackIds.isNotEmpty()) {
                playlistRepository.addTracksToPlaylist(newId, initialTrackIds)
            }
            _statusMessage.value = "Playlist \"$name\" créée"
        }
    }

    fun updatePlaylistName(playlistId: Long, newName: String, newDesc: String? = null) {
        viewModelScope.launch {
            playlistRepository.updatePlaylistName(playlistId, newName, newDesc)
            _statusMessage.value = "Playlist renommée"
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            playlistRepository.deletePlaylist(playlistId)
            if (_selectedPlaylistId.value == playlistId) {
                _selectedPlaylistId.value = null
            }
            _statusMessage.value = "Playlist supprimée"
        }
    }

    fun addTracksToPlaylist(playlistId: Long, trackIds: List<Long>) {
        viewModelScope.launch {
            playlistRepository.addTracksToPlaylist(playlistId, trackIds)
            _statusMessage.value = "${trackIds.size} morceau(x) ajouté(s)"
        }
    }

    fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) {
        viewModelScope.launch {
            playlistRepository.removeTrackFromPlaylist(playlistId, trackId)
            _statusMessage.value = "Morceau retiré de la playlist"
        }
    }

    fun reorderPlaylistTracks(playlistId: Long, orderedTrackIds: List<Long>) {
        viewModelScope.launch {
            playlistRepository.reorderTracks(playlistId, orderedTrackIds)
        }
    }

    fun playPlaylist(playlistTracks: List<AudioTrackEntity>, startIndex: Int = 0, shuffle: Boolean = false) {
        if (playlistTracks.isEmpty()) return
        val queue = if (shuffle) playlistTracks.shuffled() else playlistTracks
        val targetIndex = if (shuffle) 0 else startIndex.coerceIn(0, queue.size - 1)
        playTrack(queue[targetIndex], queue)
    }

    fun playPlaylistDirectly(playlistId: Long, shuffle: Boolean = false) {
        viewModelScope.launch {
            val plTracks = playlistRepository.getTracksOnce(playlistId)
            if (plTracks.isNotEmpty()) {
                playPlaylist(plTracks, startIndex = 0, shuffle = shuffle)
            } else {
                _statusMessage.value = "La playlist est vide"
            }
        }
    }
}
