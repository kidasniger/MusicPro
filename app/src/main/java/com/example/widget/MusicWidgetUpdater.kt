package com.example.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Size
import androidx.glance.appwidget.updateAll
import com.example.data.local.AudioTrackEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Utilitaire pour synchroniser l'état de lecture avec le Widget Glance.
 */
object MusicWidgetUpdater {

    private val scope = CoroutineScope(Dispatchers.IO)

    fun update(
        context: Context,
        track: AudioTrackEntity?,
        isPlaying: Boolean
    ) {
        update(
            context = context,
            title = track?.title ?: "Aucune lecture",
            artist = track?.artist ?: "MusicPro",
            album = track?.album ?: "",
            albumArtUri = track?.albumArtUri,
            trackId = track?.id ?: -1L,
            isPlaying = isPlaying
        )
    }

    fun update(
        context: Context,
        title: String,
        artist: String,
        album: String,
        albumArtUri: String?,
        trackId: Long,
        isPlaying: Boolean
    ) {
        scope.launch {
            val safeTitle = title.ifBlank { "Aucune lecture" }
            val safeArtist = artist.ifBlank { "MusicPro" }
            val artBitmap = albumArtUri?.let { loadAlbumArtBitmap(context, it) }

            MusicWidgetState.save(
                context = context,
                title = safeTitle,
                artist = safeArtist,
                album = album,
                isPlaying = isPlaying,
                albumArtUri = albumArtUri,
                trackId = trackId,
                artBitmap = artBitmap
            )

            try {
                MusicGlanceWidget().updateAll(context)
            } catch (e: Exception) {
                // Ignore if widget is not placed on home screen yet
            }
        }
    }

    private fun loadAlbumArtBitmap(context: Context, uriString: String): Bitmap? {
        return try {
            val uri = Uri.parse(uriString)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                context.contentResolver.loadThumbnail(uri, Size(160, 160), null)
            } else {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val options = BitmapFactory.Options().apply {
                        inSampleSize = 2 // downsample for widget efficiency
                    }
                    BitmapFactory.decodeStream(stream, null, options)
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}
