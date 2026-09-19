package com.example.lyrics

data class LyricLine(
    val timeMs: Long,
    val text: String
)

data class LyricsData(
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val offsetMs: Long = 0L,
    val lines: List<LyricLine> = emptyList(),
    val source: LyricsSource = LyricsSource.NONE
) {
    val isSynchronized: Boolean
        get() = lines.isNotEmpty() && lines.any { it.timeMs > 0 }

    /**
     * Recherche dichotomique rapide de l'index de la ligne active
     * selon la position de lecture actuelle.
     */
    fun findActiveLineIndex(currentPositionMs: Long): Int {
        if (lines.isEmpty()) return -1
        val adjustedPos = currentPositionMs + offsetMs
        var low = 0
        var high = lines.size - 1
        var result = -1

        while (low <= high) {
            val mid = (low + high) ushr 1
            if (lines[mid].timeMs <= adjustedPos) {
                result = mid
                low = mid + 1
            } else {
                high = mid - 1
            }
        }
        return result
    }
}

enum class LyricsSource(val label: String) {
    ID3_SYLT("Tag ID3 SYLT (Synchronisé)"),
    ID3_USLT("Tag ID3 USLT / Métadonnées"),
    LRC_FILE("Fichier .LRC"),
    LRCLIB_NET("En ligne (lrclib.net)"),
    GROQ_WHISPER("Transcription IA (Whisper)"),
    NONE("Aucune parole")
}
