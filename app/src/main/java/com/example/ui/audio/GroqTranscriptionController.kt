package com.example.ui.audio

import android.content.Context
import com.example.data.local.AudioTrackEntity
import com.example.data.repository.AudioRepository
import com.example.data.security.GroqApiKeyStore
import com.example.groq.GroqTranscriptionManager
import com.example.groq.GroqTranscriptionResult
import com.example.lyrics.LyricsData
import com.example.lyrics.LyricsRepository
import com.example.lyrics.LyricsSaveResult
import com.example.playback.MusicPlaybackManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class GroqTranscriptionController(
    private val context: Context,
    private val scope: CoroutineScope,
    private val audioRepository: AudioRepository,
    private val lyricsRepository: LyricsRepository,
    private val playbackManager: MusicPlaybackManager,
    private val onLyricsData: (LyricsData) -> Unit,
    private val onSaveFeedbackMessage: (String?) -> Unit
) {
    // Transcription IA Groq Whisper large-v3
    private val groqApiKeyStore = GroqApiKeyStore.getInstance(context)
    private val _isGroqTranscribing = MutableStateFlow(false)
    val isGroqTranscribing: StateFlow<Boolean> = _isGroqTranscribing.asStateFlow()

    private val _groqProgressMessage = MutableStateFlow("")
    val groqProgressMessage: StateFlow<String> = _groqProgressMessage.asStateFlow()

    private val _groqTranscriptionResult = MutableStateFlow<GroqTranscriptionResult?>(null)
    val groqTranscriptionResult: StateFlow<GroqTranscriptionResult?> = _groqTranscriptionResult.asStateFlow()

    private val _groqErrorMessage = MutableStateFlow<String?>(null)
    val groqErrorMessage: StateFlow<String?> = _groqErrorMessage.asStateFlow()

    fun hasGroqApiKey(): Boolean = groqApiKeyStore.hasApiKey()


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
                context = context,
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
            onLyricsData(appliedData)
            _groqTranscriptionResult.value = null // Ferme le dialogue d'aperçu

            when (saveResult) {
                is LyricsSaveResult.TagWriteSuccess -> {
                    onSaveFeedbackMessage("✓ Paroles IA intégrées dans le fichier audio (${saveResult.tagType})")
                    audioRepository.updateLyricsStatus(track.id, true)
                }
                is LyricsSaveResult.LrcFileSuccess -> {
                    val fName = File(saveResult.lrcPath).name
                    onSaveFeedbackMessage("✓ Paroles IA enregistrées dans $fName")
                    audioRepository.updateLyricsStatus(track.id, true)
                }
                is LyricsSaveResult.AppCacheSuccess -> {
                    onSaveFeedbackMessage("✓ Paroles IA sauvegardées dans le cache de l'application")
                    audioRepository.updateLyricsStatus(track.id, true)
                }
                is LyricsSaveResult.Error -> {
                    onSaveFeedbackMessage("Paroles IA appliquées (${saveResult.message})")
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


}
