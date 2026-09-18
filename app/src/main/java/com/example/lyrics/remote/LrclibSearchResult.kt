package com.example.lyrics.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LrclibSearchResult(
    @Json(name = "id") val id: Long? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "trackName") val trackName: String? = null,
    @Json(name = "artistName") val artistName: String? = null,
    @Json(name = "albumName") val albumName: String? = null,
    @Json(name = "duration") val duration: Double? = null,
    @Json(name = "instrumental") val instrumental: Boolean? = false,
    @Json(name = "plainLyrics") val plainLyrics: String? = null,
    @Json(name = "syncedLyrics") val syncedLyrics: String? = null
) {
    val displayTitle: String
        get() = trackName?.takeIf { it.isNotBlank() }
            ?: name?.takeIf { it.isNotBlank() }
            ?: "Sans titre"

    val displayArtist: String
        get() = artistName?.takeIf { it.isNotBlank() } ?: "Artiste inconnu"

    val displayAlbum: String?
        get() = albumName?.takeIf { it.isNotBlank() }

    val hasSyncedLyrics: Boolean
        get() = !syncedLyrics.isNullOrBlank()

    val hasPlainLyrics: Boolean
        get() = !plainLyrics.isNullOrBlank()

    val durationFormatted: String
        get() {
            val sec = duration?.toLong() ?: return "--:--"
            val m = sec / 60
            val s = sec % 60
            return String.format("%02d:%02d", m, s)
        }
}
