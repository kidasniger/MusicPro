package com.example.ui.audio

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AudioTrackEntity
import com.example.data.repository.AudioRepository
import com.example.lyrics.LyricsData
import com.example.lyrics.LyricsRepository
import com.example.lyrics.LyricsSaveResult
import com.example.lyrics.remote.LrclibSearchResult
import com.example.playback.MusicPlaybackManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

sealed class LrclibSearchUiState {
    data object Idle : LrclibSearchUiState()
    data object Loading : LrclibSearchUiState()
    data class Success(val results: List<LrclibSearchResult>) : LrclibSearchUiState()
    data class Empty(val queryTitle: String, val queryArtist: String) : LrclibSearchUiState()
    data class Error(val message: String) : LrclibSearchUiState()
}

enum class LibraryTab(val label: String) {
    TRACKS("Morceaux"),
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

    // Tracks observed from Room Database
    val tracks: StateFlow<List<AudioTrackEntity>> = repository.allTracks
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

    // Gestionnaire de paroles synchronisées (LRC et ID3 SYLT via jaudiotagger)
    private val lyricsRepository = LyricsRepository.getInstance(application)
    private val _lyricsData = MutableStateFlow(LyricsData())
    val lyricsData: StateFlow<LyricsData> = _lyricsData.asStateFlow()

    private val _isLyricsLoading = MutableStateFlow(false)
    val isLyricsLoading: StateFlow<Boolean> = _isLyricsLoading.asStateFlow()

    // Recherche en ligne lrclib.net
    private val _lrclibSearchState = MutableStateFlow<LrclibSearchUiState>(LrclibSearchUiState.Idle)
    val lrclibSearchState: StateFlow<LrclibSearchUiState> = _lrclibSearchState.asStateFlow()

    private val _saveFeedbackMessage = MutableStateFlow<String?>(null)
    val saveFeedbackMessage: StateFlow<String?> = _saveFeedbackMessage.asStateFlow()

    // Filtered search results
    val searchResults: StateFlow<List<AudioTrackEntity>> = combine(
        tracks,
        _searchQuery,
        _searchFilter
    ) { allTracks, query, filter ->
        if (query.isBlank()) {
            emptyList()
        } else {
            val q = query.trim().lowercase()
            allTracks.filter { track ->
                when (filter) {
                    SearchFilter.ALL -> track.title.lowercase().contains(q) ||
                            track.artist.lowercase().contains(q) ||
                            track.album.lowercase().contains(q)
                    SearchFilter.TITLES -> track.title.lowercase().contains(q)
                    SearchFilter.ARTISTS -> track.artist.lowercase().contains(q)
                    SearchFilter.ALBUMS -> track.album.lowercase().contains(q)
                }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Grouped Albums
    val albumSummaries: StateFlow<List<AlbumSummary>> = tracks.combine(_searchQuery) { allTracks, _ ->
        allTracks.groupBy { it.album }
            .map { (albumName, trackList) ->
                val first = trackList.first()
                AlbumSummary(
                    name = albumName.ifBlank { "Album Inconnu" },
                    artist = first.artist.ifBlank { "Artiste Inconnu" },
                    trackCount = trackList.size,
                    coverUri = trackList.firstOrNull { it.albumArtUri != null }?.albumArtUri,
                    sampleTrack = first
                )
            }.sortedBy { it.name }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Grouped Artists
    val artistSummaries: StateFlow<List<ArtistSummary>> = tracks.combine(_searchQuery) { allTracks, _ ->
        allTracks.groupBy { it.artist }
            .map { (artistName, trackList) ->
                ArtistSummary(
                    name = artistName.ifBlank { "Artiste Inconnu" },
                    trackCount = trackList.size,
                    albumsCount = trackList.map { it.album }.distinct().size
                )
            }.sortedBy { it.name }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Grouped Folders
    val folderSummaries: StateFlow<List<FolderSummary>> = tracks.combine(_searchQuery) { allTracks, _ ->
        allTracks.groupBy { it.folder.ifBlank { "Musique" } }
            .map { (folderName, trackList) ->
                FolderSummary(
                    name = folderName,
                    trackCount = trackList.size,
                    samplePath = trackList.first().path
                )
            }.sortedBy { it.name }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        // Au démarrage, si le cache Room est vide, tenter un scan MediaStore sans injecter de fausses pistes
        viewModelScope.launch {
            val count = repository.getTrackCount()
            if (count == 0) {
                refreshScan(autoFallbackDemoIfEmpty = false)
            } else {
                _statusMessage.value = "$count morceaux chargés depuis le cache local"
                if (currentTrack.value == null) {
                    tracks.value.firstOrNull()?.let { playbackManager.setCurrentTrackOnly(it) }
                }
            }
        }

        // Chargement automatique des paroles à chaque changement de piste
        viewModelScope.launch {
            currentTrack.collect { track ->
                if (track != null) {
                    loadLyricsForTrack(track)
                } else {
                    _lyricsData.value = LyricsData()
                }
            }
        }
    }

    fun refreshScan(autoFallbackDemoIfEmpty: Boolean = false) {
        viewModelScope.launch {
            _isScanning.value = true
            _statusMessage.value = "Scan MediaStore en cours..."
            try {
                val count = repository.refreshMediaStoreScan()
                if (count > 0) {
                    _statusMessage.value = "$count morceau(x) trouvé(s) et mis en cache Room"
                    if (currentTrack.value == null) {
                        tracks.value.firstOrNull()?.let { playbackManager.setCurrentTrackOnly(it) }
                    }
                } else {
                    if (autoFallbackDemoIfEmpty) {
                        val demoCount = repository.loadDemoTracks()
                        _statusMessage.value = "Mode Démo activé ($demoCount morceaux)"
                    } else {
                        _statusMessage.value = "Aucun fichier audio trouvé"
                    }
                }
            } catch (e: Exception) {
                _statusMessage.value = "Erreur lors du scan: ${e.message}"
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun loadDemoTracks() {
        viewModelScope.launch {
            _isScanning.value = true
            try {
                val count = repository.loadDemoTracks()
                _statusMessage.value = "Catalogue démo chargé ($count morceaux)"
                // Set first track as current
                tracks.value.firstOrNull()?.let {
                    playbackManager.setCurrentTrackOnly(it)
                }
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun selectTab(tab: LibraryTab) {
        _selectedTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSearchFilter(filter: SearchFilter) {
        _searchFilter.value = filter
    }

    fun toggleFavorite(trackId: Long) {
        val current = _favorites.value
        if (current.contains(trackId)) {
            _favorites.value = current - trackId
        } else {
            _favorites.value = current + trackId
        }
    }

    fun playTrack(track: AudioTrackEntity, playlist: List<AudioTrackEntity> = tracks.value) {
        val activePlaylist = if (playlist.isNotEmpty()) playlist else listOf(track)
        playbackManager.playTrack(track, activePlaylist)
    }

    fun togglePlayPause() {
        if (currentTrack.value == null) {
            tracks.value.firstOrNull()?.let { playTrack(it) }
            return
        }
        playbackManager.togglePlayPause()
    }

    fun playNext() {
        playbackManager.playNext()
    }

    fun playPrevious() {
        playbackManager.playPrevious()
    }

    fun seekTo(progressMs: Long) {
        playbackManager.seekTo(progressMs)
    }

    fun toggleRepeatMode() {
        playbackManager.toggleRepeatMode()
    }

    fun toggleShuffle() {
        playbackManager.toggleShuffle()
    }

    fun setPlaybackSpeed(speed: Float) {
        playbackManager.setPlaybackSpeed(speed)
    }

    fun loadLyricsForTrack(track: AudioTrackEntity) {
        viewModelScope.launch {
            _isLyricsLoading.value = true
            try {
                _lyricsData.value = lyricsRepository.getLyricsForTrack(track)
            } finally {
                _isLyricsLoading.value = false
            }
        }
    }

    fun generateDemoLyrics(track: AudioTrackEntity) {
        val generated = lyricsRepository.generateDemoLyrics(track)
        _lyricsData.value = generated
    }

    fun importLrcText(track: AudioTrackEntity, lrcText: String) {
        val imported = lyricsRepository.importLrcText(track.id, lrcText)
        _lyricsData.value = imported
    }

    /**
     * Lance la recherche de paroles sur lrclib.net avec titre, artiste et durée.
     * Gère les états d'erreur réseau, timeout et aucun résultat.
     */
    fun searchOnlineLyrics(title: String, artist: String, durationSec: Int?) {
        viewModelScope.launch {
            _lrclibSearchState.value = LrclibSearchUiState.Loading
            val result = lyricsRepository.searchLyricsOnline(title, artist, durationSec)
            result.fold(
                onSuccess = { list ->
                    if (list.isEmpty()) {
                        _lrclibSearchState.value = LrclibSearchUiState.Empty(title, artist)
                    } else {
                        _lrclibSearchState.value = LrclibSearchUiState.Success(list)
                    }
                },
                onFailure = { error ->
                    _lrclibSearchState.value = LrclibSearchUiState.Error(
                        error.message ?: "Erreur inconnue lors de la recherche."
                    )
                }
            )
        }
    }

    /**
     * Enregistre le résultat de paroles sélectionné en tag ID3 SYLT (si MP3 supporté)
     * ou en fichier .lrc compagnon, et applique immédiatement les paroles à la lecture en cours.
     */
    fun applyLrclibResult(
        track: AudioTrackEntity,
        result: LrclibSearchResult,
        onComplete: ((LyricsSaveResult) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val (saveResult, appliedData) = lyricsRepository.applyAndSaveLyrics(track, result)
            _lyricsData.value = appliedData
            when (saveResult) {
                is LyricsSaveResult.Id3SyltSuccess -> {
                    _saveFeedbackMessage.value = "✓ Paroles écrites en tag ID3 SYLT (${saveResult.linesCount} lignes)"
                }
                is LyricsSaveResult.LrcFileSuccess -> {
                    val fName = File(saveResult.lrcPath).name
                    _saveFeedbackMessage.value = "✓ Fichier $fName sauvegardé à côté du morceau"
                }
                is LyricsSaveResult.Error -> {
                    _saveFeedbackMessage.value = "Paroles appliquées en mémoire (${saveResult.message})"
                }
            }
            onComplete?.invoke(saveResult)
        }
    }

    fun clearSaveFeedback() {
        _saveFeedbackMessage.value = null
    }

    fun resetLrclibSearch() {
        _lrclibSearchState.value = LrclibSearchUiState.Idle
    }
}
