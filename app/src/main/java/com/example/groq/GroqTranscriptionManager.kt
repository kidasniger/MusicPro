package com.example.groq

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object GroqTranscriptionManager {

    private const val TAG = "GroqTranscriptionMgr"
    private const val MODEL_NAME = "whisper-large-v3"

    /**
     * Teste rapidement la validité d'une clé API Groq en effectuant une requête légère.
     */
    suspend fun testApiKey(apiKey: String): Result<Boolean> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(GroqException.InvalidApiKeyException("La clé API est vide."))
        }

        try {
            // Création d'un petit fichier audio factice de 1 seconde (silence WAV) pour valider l'accès
            val tempWav = File.createTempFile("test_groq_auth", ".wav")
            try {
                writeDummySilentWav(tempWav)
                val requestFile = tempWav.asRequestBody("audio/wav".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", tempWav.name, requestFile)

                val modelPart = MODEL_NAME.toRequestBody("text/plain".toMediaTypeOrNull())
                val formatPart = "verbose_json".toRequestBody("text/plain".toMediaTypeOrNull())

                val response = GroqClient.apiService.transcribeAudio(
                    authorization = "Bearer ${apiKey.trim()}",
                    file = body,
                    model = modelPart,
                    responseFormat = formatPart
                )

                if (response.isSuccessful) {
                    Result.success(true)
                } else {
                    val code = response.code()
                    val errorBody = response.errorBody()?.string().orEmpty()
                    Log.w(TAG, "Test clé API échoué code $code: $errorBody")
                    when (code) {
                        401 -> Result.failure(GroqException.InvalidApiKeyException("Clé API Groq invalide ou non autorisée."))
                        429 -> Result.failure(GroqException.RateLimitExceededException("Quota Groq temporairement atteint (Rate limit)."))
                        else -> Result.failure(GroqException.ServerException(code, errorBody.ifBlank { "Échec d'authentification" }))
                    }
                }
            } finally {
                tempWav.delete()
            }
        } catch (e: UnknownHostException) {
            Result.failure(GroqException.NetworkException("Impossible de contacter l'API Groq. Vérifiez votre connexion Internet.", e))
        } catch (e: SocketTimeoutException) {
            Result.failure(GroqException.NetworkException("Délai d'attente dépassé lors du test de la clé.", e))
        } catch (e: Exception) {
            Result.failure(GroqException.GeneralException(e.message ?: "Erreur inattendue", e))
        }
    }

    /**
     * Transcrit un fichier audio local en paroles synchronisées avec timestamps via Whisper large-v3.
     */
    suspend fun transcribeAudioFile(
        context: Context,
        audioFile: File,
        apiKey: String,
        trackTitle: String,
        artistName: String,
        albumName: String? = null,
        durationMs: Long = 0L,
        onProgress: (String) -> Unit = {}
    ): Result<GroqTranscriptionResult> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(GroqException.InvalidApiKeyException())
        }

        if (!audioFile.exists() || audioFile.length() == 0L) {
            return@withContext Result.failure(GroqException.GeneralException("Le fichier audio sélectionné est introuvable ou vide."))
        }

        onProgress("Préparation et vérification de la taille audio...")

        // Découpage automatique si le fichier dépasse la limite Groq (25 Mo)
        val chunks = AudioChunker.prepareChunks(context, audioFile, durationMs)
        if (chunks.isEmpty()) {
            return@withContext Result.failure(GroqException.FileTooLargeException("Impossible de préparer l'audio pour Groq."))
        }

        val allSegments = mutableListOf<GroqSegment>()
        var detectedLanguage = "inconnue"
        var totalDurationSec = 0.0

        try {
            for ((index, chunk) in chunks.withIndex()) {
                val chunkLabel = if (chunks.size > 1) " (morceau ${index + 1}/${chunks.size})" else ""
                onProgress("Transcription Whisper large-v3 en cours$chunkLabel...")

                val chunkResult = transcribeSingleChunk(chunk.file, apiKey)
                if (chunkResult.isFailure) {
                    return@withContext Result.failure(chunkResult.exceptionOrNull() ?: GroqException.GeneralException("Erreur de transcription"))
                }

                val response = chunkResult.getOrThrow()
                detectedLanguage = response.language ?: detectedLanguage
                totalDurationSec += (response.duration ?: 0.0)

                val segments = response.segments.orEmpty()
                for (seg in segments) {
                    val adjustedSeg = seg.copy(
                        start = seg.start + chunk.timeOffsetSeconds,
                        end = seg.end + chunk.timeOffsetSeconds
                    )
                    allSegments.add(adjustedSeg)
                }
            }

            onProgress("Génération et formatage des horodatages LRC...")

            // Trier par timestamp croissant
            val sortedSegments = allSegments.sortedBy { it.start }
            val lrcText = GroqLrcConverter.convertToLrc(
                segments = sortedSegments,
                title = trackTitle,
                artist = artistName,
                album = albumName
            )

            val finalResult = GroqTranscriptionResult(
                trackTitle = trackTitle,
                artistName = artistName,
                durationSeconds = if (totalDurationSec > 0.0) totalDurationSec else (durationMs / 1000.0),
                detectedLanguage = detectedLanguage,
                segments = sortedSegments,
                fullLrcContent = lrcText,
                chunkCount = chunks.size
            )

            Result.success(finalResult)
        } finally {
            AudioChunker.cleanupChunks(chunks)
        }
    }

    private suspend fun transcribeSingleChunk(
        file: File,
        apiKey: String
    ): Result<GroqTranscriptionResponse> {
        val mimeType = getAudioMimeType(file)
        val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

        val modelPart = MODEL_NAME.toRequestBody("text/plain".toMediaTypeOrNull())
        val formatPart = "verbose_json".toRequestBody("text/plain".toMediaTypeOrNull())
        val granPart = "segment".toRequestBody("text/plain".toMediaTypeOrNull())

        return try {
            val response = GroqClient.apiService.transcribeAudio(
                authorization = "Bearer ${apiKey.trim()}",
                file = body,
                model = modelPart,
                responseFormat = formatPart,
                timestampGranularities = granPart
            )

            if (response.isSuccessful) {
                val data = response.body()
                if (data != null) {
                    Result.success(data)
                } else {
                    Result.failure(GroqException.GeneralException("Réponse vide reçue des serveurs Groq."))
                }
            } else {
                val code = response.code()
                val errorStr = response.errorBody()?.string().orEmpty()
                Log.e(TAG, "Erreur transcription Groq ($code): $errorStr")
                when (code) {
                    401 -> Result.failure(GroqException.InvalidApiKeyException())
                    413 -> Result.failure(GroqException.FileTooLargeException("Fichier trop volumineux (> 25 Mo) pour l'API Groq."))
                    429 -> Result.failure(GroqException.RateLimitExceededException())
                    400 -> {
                        if (errorStr.contains("too long", ignoreCase = true) || errorStr.contains("maximum duration", ignoreCase = true)) {
                            Result.failure(GroqException.AudioTooLongException())
                        } else {
                            Result.failure(GroqException.GeneralException("Requête invalide ($errorStr)"))
                        }
                    }
                    in 500..599 -> Result.failure(GroqException.ServerException(code, "Le service Groq Whisper rencontre une anomalie momentanée."))
                    else -> Result.failure(GroqException.GeneralException("Erreur Groq HTTP $code : $errorStr"))
                }
            }
        } catch (e: UnknownHostException) {
            Result.failure(GroqException.NetworkException("Connexion Internet indisponible.", e))
        } catch (e: ConnectException) {
            Result.failure(GroqException.NetworkException("Impossible de se connecter aux serveurs Groq.", e))
        } catch (e: SocketTimeoutException) {
            Result.failure(GroqException.NetworkException("Délai d'attente dépassé pendant la transcription.", e))
        } catch (e: Exception) {
            Result.failure(GroqException.GeneralException(e.message ?: "Erreur inattendue", e))
        }
    }

    private fun getAudioMimeType(file: File): String {
        return when (file.extension.lowercase()) {
            "mp3" -> "audio/mpeg"
            "flac" -> "audio/flac"
            "wav" -> "audio/wav"
            "m4a", "mp4" -> "audio/mp4"
            "ogg", "oga" -> "audio/ogg"
            else -> "audio/mpeg"
        }
    }

    /**
     * Génère un fichier WAV de silence valide (0,5s mono 8000Hz 16-bit) pour valider l'authentification.
     */
    private fun writeDummySilentWav(file: File) {
        val sampleRate = 8000
        val numSamples = 4000 // 0.5 sec
        val dataSize = numSamples * 2
        val totalSize = 36 + dataSize

        val header = ByteArray(44)
        val buf = java.nio.ByteBuffer.wrap(header).order(java.nio.ByteOrder.LITTLE_ENDIAN)
        buf.put("RIFF".toByteArray())
        buf.putInt(totalSize)
        buf.put("WAVE".toByteArray())
        buf.put("fmt ".toByteArray())
        buf.putInt(16) // Subchunk1Size
        buf.putShort(1) // AudioFormat (1 = PCM)
        buf.putShort(1) // NumChannels (1 = mono)
        buf.putInt(sampleRate)
        buf.putInt(sampleRate * 2) // ByteRate
        buf.putShort(2) // BlockAlign
        buf.putShort(16) // BitsPerSample
        buf.put("data".toByteArray())
        buf.putInt(dataSize)

        file.outputStream().use { fos ->
            fos.write(header)
            fos.write(ByteArray(dataSize)) // Silent PCM bytes
        }
    }
}
