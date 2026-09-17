package com.example.ui.audio

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AudioTrackEntity
import com.example.data.repository.AudioRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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

    // Playback state
    private val _currentTrack = MutableStateFlow<AudioTrackEntity?>(null)
    val currentTrack: StateFlow<AudioTrackEntity?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _progressMs = MutableStateFlow(0L)
    val progressMs: StateFlow<Long> = _progressMs.asStateFlow()

    private var playbackTickerJob: Job? = null

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
        // Au démarrage, si le cache Room est vide, tenter un scan MediaStore
        viewModelScope.launch {
            val count = repository.getTrackCount()
            if (count == 0) {
                refreshScan(autoFallbackDemoIfEmpty = true)
            } else {
                _statusMessage.value = "$count morceaux chargés depuis le cache local"
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
                    _statusMessage.value = "$count morceaux trouvés et mis en cache"
                } else {
                    if (autoFallbackDemoIfEmpty) {
                        val demoCount = repository.loadDemoTracks()
                        _statusMessage.value = "Mode Démo activé ($demoCount morceaux de test)"
                    } else {
                        _statusMessage.value = "Aucun fichier audio trouvé dans le MediaStore"
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
                    _currentTrack.value = it
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

    fun playTrack(track: AudioTrackEntity) {
        _currentTrack.value = track
        _isPlaying.value = true
        _progressMs.value = 0L
        startPlaybackTicker(track.duration)
    }

    fun togglePlayPause() {
        if (_currentTrack.value == null) {
            tracks.value.firstOrNull()?.let { playTrack(it) }
            return
        }
        val newState = !_isPlaying.value
        _isPlaying.value = newState
        if (newState) {
            _currentTrack.value?.let { startPlaybackTicker(it.duration) }
        } else {
            playbackTickerJob?.cancel()
        }
    }

    fun playNext() {
        val all = tracks.value
        if (all.isEmpty()) return
        val current = _currentTrack.value
        val currentIndex = all.indexOfFirst { it.id == current?.id }
        val nextTrack = if (currentIndex != -1 && currentIndex < all.size - 1) {
            all[currentIndex + 1]
        } else {
            all.first()
        }
        playTrack(nextTrack)
    }

    fun playPrevious() {
        val all = tracks.value
        if (all.isEmpty()) return
        val current = _currentTrack.value
        val currentIndex = all.indexOfFirst { it.id == current?.id }
        val prevTrack = if (currentIndex > 0) {
            all[currentIndex - 1]
        } else {
            all.last()
        }
        playTrack(prevTrack)
    }

    fun seekTo(progressMs: Long) {
        _progressMs.value = progressMs
    }

    private fun startPlaybackTicker(totalDurationMs: Long) {
        playbackTickerJob?.cancel()
        playbackTickerJob = viewModelScope.launch {
            while (_isPlaying.value) {
                delay(500)
                if (_progressMs.value + 500 >= totalDurationMs && totalDurationMs > 0) {
                    playNext()
                    break
                } else {
                    _progressMs.value += 500
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        playbackTickerJob?.cancel()
    }
}
