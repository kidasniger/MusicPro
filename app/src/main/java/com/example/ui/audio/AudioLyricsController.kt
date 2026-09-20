package com.example.ui.audio

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.IntentSenderRequest
import androidx.core.content.ContextCompat
import com.example.data.local.AudioTrackEntity
import com.example.data.repository.AudioRepository
import com.example.data.security.GroqApiKeyStore
import com.example.groq.GroqTranscriptionManager
import com.example.groq.GroqTranscriptionResult
import com.example.lyrics.LrcParser
import com.example.lyrics.LyricsData
import com.example.lyrics.LyricsRepository
import com.example.lyrics.LyricsSaveResult
import com.example.lyrics.remote.LrclibSearchResult
import com.example.playback.MusicPlaybackManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class AudioLyricsController(
    private val application: Context,
    private val scope: CoroutineScope,
    private val audioRepository: AudioRepository,
    private val playbackManager: MusicPlaybackManager
) {
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

    // Gestion des permissions d'écriture Scoped Storage / MediaStore (style Musicolet)
    private val _pendingLyricsWrite = MutableStateFlow<PendingLyricsWrite?>(null)
    val pendingLyricsWrite: StateFlow<PendingLyricsWrite?> = _pendingLyricsWrite.asStateFlow()

    private val _intentSenderRequest = MutableSharedFlow<IntentSenderRequest>(extraBufferCapacity = 1)
    val intentSenderRequest: SharedFlow<IntentSenderRequest> = _intentSenderRequest.asSharedFlow()

    private val _legacyWritePermissionRequest = MutableSharedFlow<Boolean>(extraBufferCapacity = 1)
    val legacyWritePermissionRequest: SharedFlow<Boolean> = _legacyWritePermissionRequest.asSharedFlow()

    // Transcription IA Groq Whisper large-v3
    private val groqApiKeyStore = GroqApiKeyStore.getInstance(application)
    private val _isGroqTranscribing = MutableStateFlow(false)
    val isGroqTranscribing: StateFlow<Boolean> = _isGroqTranscribing.asStateFlow()

    private val _groqProgressMessage = MutableStateFlow("")
    val groqProgressMessage: StateFlow<String> = _groqProgressMessage.asStateFlow()

    private val _groqTranscriptionResult = MutableStateFlow<GroqTranscriptionResult?>(null)
    val groqTranscriptionResult: StateFlow<GroqTranscriptionResult?> = _groqTranscriptionResult.asStateFlow()

    private val _groqErrorMessage = MutableStateFlow<String?>(null)
    val groqErrorMessage: StateFlow<String?> = _groqErrorMessage.asStateFlow()

    fun hasGroqApiKey(): Boolean = groqApiKeyStore.hasApiKey()

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
        scope = scope,
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
        scope = scope,
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
        scope = scope,
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
        scope = scope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        // Au démarrage, on lit d'abord Room. Un scan MediaStore n'est effectué
        // automatiquement que si la bibliothèque locale est réellement vide.
        // Aucun nettoyage destructif n'est lancé à chaque ouverture de l'application.
        scope.launch {
            val count = audioRepository.getTrackCount()
            if (count > 0) {
                _statusMessage.value = "$count morceaux chargés depuis la bibliothèque locale"
                if (currentTrack.value == null) {
                    tracks.value.firstOrNull()?.let { playbackManager.setCurrentTrackOnly(it) }
                }
            } else {
                // Scan initial discret si la base locale est vide
                val scanned = audioRepository.refreshMediaStoreScan()
                if (scanned > 0) {
                    _statusMessage.value = "$scanned morceaux trouvés"
                    if (currentTrack.value == null) {
                        tracks.value.firstOrNull()?.let { playbackManager.setCurrentTrackOnly(it) }
                    }
                }
            }
        }

        // Chargement automatique des paroles et mise à jour de la date d'écoute à chaque changement de piste
        scope.launch {
            currentTrack.collect { track ->
                if (track != null) {
                    loadLyricsForTrack(track)
                    if (playbackManager.isPlaying.value) {
                        audioRepository.updateLastPlayed(track.id)
                    }
                } else {
                    _lyricsData.value = LyricsData()
                }
            }
        }
    }

    fun refreshScan() {
        scope.launch {
            _isScanning.value = true
            _statusMessage.value = "Scan MediaStore en cours..."
            try {
                val count = audioRepository.refreshMediaStoreScan()
                if (count > 0) {
                    _statusMessage.value = "$count morceau(x) trouvé(s) et synchronisé(s)"
                    if (currentTrack.value == null) {
                        tracks.value.firstOrNull()?.let { playbackManager.setCurrentTrackOnly(it) }
                    }
                } else {
                    _statusMessage.value = "Aucun nouveau fichier audio détecté"
                }
            } catch (e: Exception) {
                _statusMessage.value = "Scan terminé avec avertissement : ${e.message}"
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
        scope.launch {
            audioRepository.updateLastPlayed(track.id)
        }
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
        scope.launch {
            _isLyricsLoading.value = true
            try {
                val data = lyricsRepository.getLyricsForTrack(track)
                _lyricsData.value = data
                if (data.lines.isNotEmpty() && data.isSynchronized && !track.hasSyncedLyrics) {
                    audioRepository.updateLyricsStatus(track.id, true)
                }
            } finally {
                _isLyricsLoading.value = false
            }
        }
    }

    fun importLrcText(track: AudioTrackEntity, lrcText: String) {
        val imported = lyricsRepository.importLrcText(track.id, lrcText)
        _lyricsData.value = imported
        if (imported.lines.isNotEmpty() && imported.isSynchronized) {
            scope.launch {
                audioRepository.updateLyricsStatus(track.id, true)
            }
        }
    }

    /**
     * Lance la recherche de paroles sur lrclib.net avec titre, artiste et durée.
     * Gère les états d'erreur réseau, timeout et aucun résultat.
     */
    fun searchOnlineLyrics(title: String, artist: String, durationSec: Int?) {
        scope.launch {
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
     * Enregistre le résultat de paroles sélectionné dans les tags du fichier ou en .lrc,
     * et applique immédiatement les paroles à la lecture en cours.
     */
    fun applyLrclibResult(
        track: AudioTrackEntity,
        result: LrclibSearchResult,
        onComplete: ((LyricsSaveResult) -> Unit)? = null
    ) {
        val isCurrentPlaying = playbackManager.currentTrack.value?.id == track.id && playbackManager.isPlaying.value
        val savedPos = if (isCurrentPlaying) playbackManager.currentPositionMs.value else 0L

        scope.launch {
            if (isCurrentPlaying) {
                playbackManager.pause()
            }
            val (saveResult, appliedData) = lyricsRepository.applyAndSaveLyrics(track, result)
            _lyricsData.value = appliedData
            when (saveResult) {
                is LyricsSaveResult.TagWriteSuccess -> {
                    _saveFeedbackMessage.value = "✓ Paroles intégrées dans le fichier audio (${saveResult.tagType})"
                    audioRepository.updateLyricsStatus(track.id, true)
                }
                is LyricsSaveResult.LrcFileSuccess -> {
                    val fName = File(saveResult.lrcPath).name
                    _saveFeedbackMessage.value = "✓ Fichier $fName sauvegardé à côté du morceau"
                    audioRepository.updateLyricsStatus(track.id, true)
                }
                is LyricsSaveResult.AppCacheSuccess -> {
                    _saveFeedbackMessage.value = "✓ Paroles enregistrées dans le cache de l'application"
                    audioRepository.updateLyricsStatus(track.id, true)
                }
                is LyricsSaveResult.Error -> {
                    _saveFeedbackMessage.value = "Paroles appliquées (${saveResult.message})"
                }
            }
            if (isCurrentPlaying) {
                playbackManager.reloadCurrentTrack(positionMs = savedPos, autoResume = true)
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

    /**
     * Lance la transcription audio par IA via Groq Whisper large-v3.
     */
    fun startGroqTranscription(track: AudioTrackEntity) {
        val apiKey = groqApiKeyStore.getApiKey()
        if (apiKey.isBlank()) {
            _groqErrorMessage.value = "Clé API Groq manquante. Rendez-vous dans les Paramètres pour renseigner votre clé."
            return
        }

        val audioPath = track.path
        if (audioPath.isNullOrBlank()) {
            _groqErrorMessage.value = "Chemin d'accès au fichier audio invalide."
            return
        }

        val file = File(audioPath)
        if (!file.exists()) {
            _groqErrorMessage.value = "Fichier audio introuvable sur l'appareil."
            return
        }

        scope.launch {
            _isGroqTranscribing.value = true
            _groqErrorMessage.value = null
            _groqTranscriptionResult.value = null
            _groqProgressMessage.value = "Démarrage de la transcription Whisper..."

            val result = GroqTranscriptionManager.transcribeAudioFile(
                context = application,
                audioFile = file,
                apiKey = apiKey,
                trackTitle = track.title,
                artistName = track.artist,
                albumName = track.album,
                durationMs = track.duration,
                onProgress = { progress ->
                    _groqProgressMessage.value = progress
                }
            )

            _isGroqTranscribing.value = false
            result.fold(
                onSuccess = { transcriptionResult ->
                    _groqTranscriptionResult.value = transcriptionResult
                },
                onFailure = { error ->
                    _groqErrorMessage.value = error.message ?: "Échec de la transcription Whisper."
                }
            )
        }
    }

    /**
     * Intègre le résultat de transcription Groq en ID3 SYLT ou .lrc compagnon,
     * et l'applique immédiatement au lecteur en cours.
     */
    fun applyGroqResult(
        track: AudioTrackEntity,
        result: GroqTranscriptionResult,
        onComplete: ((LyricsSaveResult) -> Unit)? = null
    ) {
        val isCurrentPlaying = playbackManager.currentTrack.value?.id == track.id && playbackManager.isPlaying.value
        val savedPos = if (isCurrentPlaying) playbackManager.currentPositionMs.value else 0L

        scope.launch {
            if (isCurrentPlaying) {
                playbackManager.pause()
            }
            val (saveResult, appliedData) = lyricsRepository.applyAndSaveLrcText(track, result.fullLrcContent)
            _lyricsData.value = appliedData
            _groqTranscriptionResult.value = null // Ferme le dialogue d'aperçu

            when (saveResult) {
                is LyricsSaveResult.TagWriteSuccess -> {
                    _saveFeedbackMessage.value = "✓ Paroles IA intégrées dans le fichier audio (${saveResult.tagType})"
                    audioRepository.updateLyricsStatus(track.id, true)
                }
                is LyricsSaveResult.LrcFileSuccess -> {
                    val fName = File(saveResult.lrcPath).name
                    _saveFeedbackMessage.value = "✓ Paroles IA enregistrées dans $fName"
                    audioRepository.updateLyricsStatus(track.id, true)
                }
                is LyricsSaveResult.AppCacheSuccess -> {
                    _saveFeedbackMessage.value = "✓ Paroles IA sauvegardées dans le cache de l'application"
                    audioRepository.updateLyricsStatus(track.id, true)
                }
                is LyricsSaveResult.Error -> {
                    _saveFeedbackMessage.value = "Paroles IA appliquées (${saveResult.message})"
                }
            }
            if (isCurrentPlaying) {
                playbackManager.reloadCurrentTrack(positionMs = savedPos, autoResume = true)
            }
            onComplete?.invoke(saveResult)
        }
    }

    fun dismissGroqPreview() {
        _groqTranscriptionResult.value = null
    }

    fun clearGroqError() {
        _groqErrorMessage.value = null
    }

    /**
     * Vérifie si l'écriture directe dans le fichier audio nécessite une autorisation explicite du système.
     * Sur Android 11+ (API 30+), MediaStore.createWriteRequest ouvre la boîte de dialogue système (comme Musicolet).
     * Sur Android <= 10 (API 29), la permission WRITE_EXTERNAL_STORAGE est demandée si absente.
     */
    fun needsWritePermission(track: AudioTrackEntity): Boolean {
        if (track.path.isBlank()) return false
        val file = File(track.path)
        if (file.exists() && file.canWrite()) {
            return false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return true
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            return ContextCompat.checkSelfPermission(
                application,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        }
        return false
    }

    /**
     * Déclenche la demande d'autorisation d'écriture système pour la piste audio donnée.
     */
    fun triggerWritePermissionRequest(track: AudioTrackEntity) {
        val context = application
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val uri = if (track.contentUri.isNotBlank()) {
                    Uri.parse(track.contentUri)
                } else {
                    ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, track.id)
                }
                val pendingIntent = MediaStore.createWriteRequest(context.contentResolver, listOf(uri))
                val request = IntentSenderRequest.Builder(pendingIntent.intentSender).build()
                _intentSenderRequest.tryEmit(request)
            } catch (e: Exception) {
                Log.e("AudioLyricsController", "Erreur createWriteRequest: ${e.message}", e)
                onWritePermissionResult(granted = false)
            }
        } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            _legacyWritePermissionRequest.tryEmit(true)
        } else {
            onWritePermissionResult(granted = true)
        }
    }

    /**
     * Callback invoqué lorsque l'utilisateur répond à la boîte de dialogue système d'autorisation.
     */
    fun onWritePermissionResult(granted: Boolean) {
        val pending = _pendingLyricsWrite.value ?: return
        _pendingLyricsWrite.value = null

        if (!granted) {
            _saveFeedbackMessage.value = "Autorisation refusée : paroles enregistrées dans le cache privé de l'app."
        }

        when (pending) {
            is PendingLyricsWrite.Lrclib -> {
                applyLrclibResult(pending.track, pending.result)
            }
            is PendingLyricsWrite.Groq -> {
                applyGroqResult(pending.track, pending.result)
            }
            is PendingLyricsWrite.ManualLrc -> {
                applyManualLrcResult(pending.track, pending.lrcText)
            }
            is PendingLyricsWrite.EmbedCurrent -> {
                embedLyricsInTrack(pending.track, pending.lyricsData)
            }
        }
    }

    /**
     * Demande d'application et sauvegarde des paroles lrclib.net avec demande d'autorisation système.
     */
    fun requestApplyLrclib(track: AudioTrackEntity, result: LrclibSearchResult) {
        if (needsWritePermission(track)) {
            _pendingLyricsWrite.value = PendingLyricsWrite.Lrclib(track, result)
            triggerWritePermissionRequest(track)
        } else {
            applyLrclibResult(track, result)
        }
    }

    /**
     * Demande d'intégration des paroles Groq Whisper avec demande d'autorisation système.
     */
    fun requestApplyGroq(track: AudioTrackEntity, result: GroqTranscriptionResult) {
        if (needsWritePermission(track)) {
            _pendingLyricsWrite.value = PendingLyricsWrite.Groq(track, result)
            triggerWritePermissionRequest(track)
        } else {
            applyGroqResult(track, result)
        }
    }

    /**
     * Demande d'intégration des paroles actuellement affichées dans les tags du fichier audio physique.
     */
    fun requestEmbedCurrentLyrics(track: AudioTrackEntity, lyricsData: LyricsData) {
        if (needsWritePermission(track)) {
            _pendingLyricsWrite.value = PendingLyricsWrite.EmbedCurrent(track, lyricsData)
            triggerWritePermissionRequest(track)
        } else {
            embedLyricsInTrack(track, lyricsData)
        }
    }

    /**
     * Écrit les paroles affichées dans les balises ID3 SYLT/USLT du fichier audio physique.
     */
    fun embedLyricsInTrack(track: AudioTrackEntity, lyricsData: LyricsData) {
        val isCurrentPlaying = playbackManager.currentTrack.value?.id == track.id && playbackManager.isPlaying.value
        val savedPos = if (isCurrentPlaying) playbackManager.currentPositionMs.value else 0L

        scope.launch {
            if (isCurrentPlaying) {
                playbackManager.pause()
            }
            val lrcText = LrcParser.toLrcString(lyricsData)
            val (saveResult, appliedData) = lyricsRepository.applyAndSaveLrcText(track, lrcText)
            _lyricsData.value = appliedData
            when (saveResult) {
                is LyricsSaveResult.TagWriteSuccess -> {
                    _saveFeedbackMessage.value = "✓ Paroles intégrées avec succès dans le fichier audio (${saveResult.tagType})"
                    audioRepository.updateLyricsStatus(track.id, true)
                }
                is LyricsSaveResult.LrcFileSuccess -> {
                    _saveFeedbackMessage.value = "✓ Paroles enregistrées dans le fichier .lrc compagnon"
                    audioRepository.updateLyricsStatus(track.id, true)
                }
                is LyricsSaveResult.AppCacheSuccess -> {
                    _saveFeedbackMessage.value = "✓ Paroles enregistrées dans le cache de l'application"
                    audioRepository.updateLyricsStatus(track.id, true)
                }
                is LyricsSaveResult.Error -> {
                    _saveFeedbackMessage.value = "Erreur d'intégration : ${saveResult.message}"
                }
            }
            if (isCurrentPlaying) {
                playbackManager.reloadCurrentTrack(positionMs = savedPos, autoResume = true)
            }
        }
    }

    fun applyManualLrcResult(track: AudioTrackEntity, lrcText: String) {
        val isCurrentPlaying = playbackManager.currentTrack.value?.id == track.id && playbackManager.isPlaying.value
        val savedPos = if (isCurrentPlaying) playbackManager.currentPositionMs.value else 0L

        scope.launch {
            if (isCurrentPlaying) {
                playbackManager.pause()
            }
            val (saveResult, appliedData) = lyricsRepository.applyAndSaveLrcText(track, lrcText)
            _lyricsData.value = appliedData
            when (saveResult) {
                is LyricsSaveResult.TagWriteSuccess -> {
                    _saveFeedbackMessage.value = "✓ Paroles intégrées dans le fichier audio (${saveResult.tagType})"
                    audioRepository.updateLyricsStatus(track.id, true)
                }
                is LyricsSaveResult.LrcFileSuccess -> {
                    _saveFeedbackMessage.value = "✓ Paroles enregistrées en fichier .lrc"
                    audioRepository.updateLyricsStatus(track.id, true)
                }
                is LyricsSaveResult.AppCacheSuccess -> {
                    _saveFeedbackMessage.value = "✓ Paroles sauvegardées dans le cache de l'application"
                    audioRepository.updateLyricsStatus(track.id, true)
                }
                is LyricsSaveResult.Error -> {
                    _saveFeedbackMessage.value = "Paroles appliquées (${saveResult.message})"
                }
            }
            if (isCurrentPlaying) {
                playbackManager.reloadCurrentTrack(positionMs = savedPos, autoResume = true)
            }
        }
    }


}
