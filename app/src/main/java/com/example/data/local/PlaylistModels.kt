package com.example.data.local

/**
 * Modèle de synthèse d'une playlist avec son nombre de pistes et sa durée totale.
 */
data class PlaylistSummary(
    val id: Long,
    val name: String,
    val description: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val trackCount: Int = 0,
    val totalDurationMs: Long = 0L,
    val sampleArtworkUris: List<String> = emptyList()
) {
    fun formatDuration(): String {
        val totalSeconds = (totalDurationMs / 1000).coerceAtLeast(0)
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format("%dh %02dmin", hours, minutes)
        } else {
            String.format("%d:%02d", minutes, seconds)
        }
    }
}
