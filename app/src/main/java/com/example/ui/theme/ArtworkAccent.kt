package com.example.ui.theme

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.AudioTrackEntity
import com.example.playback.MusicPlaybackManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

/**
 * Accent « Selon la pochette » (mise à jour 017) : couleur dominante de la pochette du morceau en
 * cours, recalculée à chaque changement de morceau (hors du thread principal). `null` tant qu'il
 * n'y a pas de pochette exploitable ou quand l'option n'est pas choisie ; le fondu de 400 ms est
 * fait par [MusicProTheme].
 *
 * Le gestionnaire de lecture n'est consulté que si [enabled] est vrai.
 */
@Composable
fun rememberArtworkAccent(enabled: Boolean, isDark: Boolean): State<Color?> {
    val context = LocalContext.current.applicationContext
    val trackFlow: StateFlow<AudioTrackEntity?> = remember(enabled) {
        if (enabled) {
            MusicPlaybackManager.getInstance(context).currentTrack
        } else {
            MutableStateFlow<AudioTrackEntity?>(null)
        }
    }
    val track by trackFlow.collectAsStateWithLifecycle()
    val artworkUri = track?.albumArtUri
    return produceState<Color?>(initialValue = null, enabled, artworkUri, isDark) {
        value = if (!enabled || artworkUri.isNullOrBlank()) {
            null
        } else {
            withContext(Dispatchers.IO) { extractArtworkAccent(context, artworkUri, isDark) }
                ?.let { Color(it) }
        }
    }
}

private fun extractArtworkAccent(context: Context, uriString: String, isDark: Boolean): Int? {
    val bitmap = decodeSmallBitmap(context, Uri.parse(uriString)) ?: return null
    return try {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        dominantAccentArgb(pixels, isDark)
    } finally {
        bitmap.recycle()
    }
}

/** Décode la pochette en au plus ~128 px de côté : largement suffisant pour une couleur dominante. */
private fun decodeSmallBitmap(context: Context, uri: Uri): Bitmap? = runCatching {
    val resolver = context.contentResolver
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
    val longestSide = maxOf(bounds.outWidth, bounds.outHeight)
    if (longestSide <= 0) return@runCatching null
    var sampleSize = 1
    while (longestSide / sampleSize > 128) sampleSize *= 2
    val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
    resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
}.getOrNull()
