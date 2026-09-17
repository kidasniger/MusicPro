package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audio_tracks")
data class AudioTrackEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long, // in milliseconds
    val contentUri: String,
    val albumArtUri: String? = null,
    val path: String = "",
    val folder: String = "",
    val mimeType: String? = null,
    val size: Long = 0L,
    val hasSyncedLyrics: Boolean = false,
    val dateAdded: Long = 0L
) {
    fun formatDuration(): String {
        val totalSeconds = (duration / 1000).coerceAtLeast(0)
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%d:%02d".format(minutes, seconds)
    }

    fun getAudioFormat(): String {
        return when {
            mimeType?.contains("flac", ignoreCase = true) == true || path.endsWith(".flac", ignoreCase = true) -> "FLAC"
            mimeType?.contains("wav", ignoreCase = true) == true || path.endsWith(".wav", ignoreCase = true) -> "WAV"
            mimeType?.contains("aac", ignoreCase = true) == true || path.endsWith(".aac", ignoreCase = true) -> "AAC"
            mimeType?.contains("m4a", ignoreCase = true) == true || path.endsWith(".m4a", ignoreCase = true) -> "M4A"
            else -> "MP3"
        }
    }
}
