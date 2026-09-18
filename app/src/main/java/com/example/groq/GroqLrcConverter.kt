package com.example.groq

import java.util.Locale

object GroqLrcConverter {

    /**
     * Convertit une liste de segments Groq (avec timestamps) en fichier texte au format .LRC standard.
     */
    fun convertToLrc(
        segments: List<GroqSegment>,
        title: String? = null,
        artist: String? = null,
        album: String? = null
    ): String {
        val sb = StringBuilder()

        // Métadonnées d'en-tête LRC standard
        if (!title.isNullOrBlank()) {
            sb.append("[ti:").append(title.trim()).append("]\n")
        }
        if (!artist.isNullOrBlank()) {
            sb.append("[ar:").append(artist.trim()).append("]\n")
        }
        if (!album.isNullOrBlank()) {
            sb.append("[al:").append(album.trim()).append("]\n")
        }
        sb.append("[by:MusicPro Whisper large-v3]\n")

        // Trier les segments chronologiquement par start time
        val sortedSegments = segments.sortedBy { it.start }

        for (seg in sortedSegments) {
            val text = seg.cleanText
            if (text.isNotBlank()) {
                val formattedTime = formatTimestamp(seg.start)
                sb.append(formattedTime).append(" ").append(text).append("\n")
            }
        }

        return sb.toString().trim()
    }

    /**
     * Formate un temps en secondes (ex: 124.56) en horodatage LRC standard [mm:ss.xx].
     */
    fun formatTimestamp(secondsValue: Double): String {
        val totalHundredths = (secondsValue * 100).toLong().coerceAtLeast(0L)
        val totalSeconds = totalHundredths / 100
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val hundredths = totalHundredths % 100
        return String.format(Locale.US, "[%02d:%02d.%02d]", minutes, seconds, hundredths)
    }
}
