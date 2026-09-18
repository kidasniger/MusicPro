package com.example.groq

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.Locale

@JsonClass(generateAdapter = true)
data class GroqTranscriptionResponse(
    @Json(name = "task") val task: String? = null,
    @Json(name = "language") val language: String? = null,
    @Json(name = "duration") val duration: Double? = null,
    @Json(name = "text") val text: String? = null,
    @Json(name = "segments") val segments: List<GroqSegment>? = null
)

@JsonClass(generateAdapter = true)
data class GroqSegment(
    @Json(name = "id") val id: Long? = null,
    @Json(name = "seek") val seek: Long? = null,
    @Json(name = "start") val start: Double = 0.0,
    @Json(name = "end") val end: Double = 0.0,
    @Json(name = "text") val text: String = ""
) {
    /**
     * Timestamp au format LRC [mm:ss.xx]
     */
    val timestampLrc: String
        get() {
            val totalHundredths = (start * 100).toLong().coerceAtLeast(0L)
            val totalSeconds = totalHundredths / 100
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            val hundredths = totalHundredths % 100
            return String.format(Locale.US, "[%02d:%02d.%02d]", minutes, seconds, hundredths)
        }

    /**
     * Texte nettoyé du segment
     */
    val cleanText: String
        get() = text.trim()
}

/**
 * Résultat complet d'une transcription Whisper pour prévisualisation et intégration.
 */
data class GroqTranscriptionResult(
    val trackTitle: String,
    val artistName: String,
    val durationSeconds: Double,
    val detectedLanguage: String,
    val segments: List<GroqSegment>,
    val fullLrcContent: String,
    val chunkCount: Int = 1
) {
    val linesCount: Int get() = segments.size
}
