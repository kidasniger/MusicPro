package com.example.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream

/**
 * Données d'état synchronisées pour le Widget Glance MusicPro.
 */
data class MusicWidgetState(
    val title: String = "Aucune lecture",
    val artist: String = "MusicPro",
    val album: String = "",
    val isPlaying: Boolean = false,
    val albumArtUri: String? = null,
    val trackId: Long = -1L
) {
    companion object {
        private const val PREFS_NAME = "musicpro_widget_prefs"
        private const val KEY_TITLE = "widget_title"
        private const val KEY_ARTIST = "widget_artist"
        private const val KEY_ALBUM = "widget_album"
        private const val KEY_IS_PLAYING = "widget_is_playing"
        private const val KEY_ALBUM_ART_URI = "widget_album_art_uri"
        private const val KEY_TRACK_ID = "widget_track_id"
        private const val KEY_ART_BASE64 = "widget_art_base64"

        fun load(context: Context): MusicWidgetState {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return MusicWidgetState(
                title = prefs.getString(KEY_TITLE, "Aucune lecture") ?: "Aucune lecture",
                artist = prefs.getString(KEY_ARTIST, "MusicPro") ?: "MusicPro",
                album = prefs.getString(KEY_ALBUM, "") ?: "",
                isPlaying = prefs.getBoolean(KEY_IS_PLAYING, false),
                albumArtUri = prefs.getString(KEY_ALBUM_ART_URI, null),
                trackId = prefs.getLong(KEY_TRACK_ID, -1L)
            )
        }

        fun save(
            context: Context,
            title: String,
            artist: String,
            album: String,
            isPlaying: Boolean,
            albumArtUri: String?,
            trackId: Long,
            artBitmap: Bitmap? = null
        ) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val editor = prefs.edit()
                .putString(KEY_TITLE, title.ifBlank { "Aucune lecture" })
                .putString(KEY_ARTIST, artist.ifBlank { "MusicPro" })
                .putString(KEY_ALBUM, album)
                .putBoolean(KEY_IS_PLAYING, isPlaying)
                .putString(KEY_ALBUM_ART_URI, albumArtUri)
                .putLong(KEY_TRACK_ID, trackId)

            if (artBitmap != null) {
                try {
                    val stream = ByteArrayOutputStream()
                    artBitmap.compress(Bitmap.CompressFormat.PNG, 85, stream)
                    val base64 = Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT)
                    editor.putString(KEY_ART_BASE64, base64)
                } catch (e: Exception) {
                    editor.remove(KEY_ART_BASE64)
                }
            } else if (albumArtUri == null) {
                editor.remove(KEY_ART_BASE64)
            }

            editor.apply()
        }

        fun loadBitmap(context: Context): Bitmap? {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val base64 = prefs.getString(KEY_ART_BASE64, null) ?: return null
            return try {
                val bytes = Base64.decode(base64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } catch (e: Exception) {
                null
            }
        }
    }
}
