package com.example.widget

import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.updateAll
import com.example.playback.MusicPlaybackManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver pour le Widget Glance MusicPro.
 * Gère les intentions de mise à jour et les actions utilisateur (play/pause, suivant, précédent).
 */
class MusicWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = MusicGlanceWidget()

    companion object {
        const val ACTION_PLAY_PAUSE = "com.example.musicpro.ACTION_WIDGET_PLAY_PAUSE"
        const val ACTION_NEXT = "com.example.musicpro.ACTION_WIDGET_NEXT"
        const val ACTION_PREV = "com.example.musicpro.ACTION_WIDGET_PREV"
        const val ACTION_UPDATE_STATE = "com.example.musicpro.ACTION_WIDGET_UPDATE_STATE"

        fun createPlayPauseIntent(context: Context): Intent {
            return Intent(context, MusicWidgetReceiver::class.java).apply {
                action = ACTION_PLAY_PAUSE
            }
        }

        fun createNextIntent(context: Context): Intent {
            return Intent(context, MusicWidgetReceiver::class.java).apply {
                action = ACTION_NEXT
            }
        }

        fun createPrevIntent(context: Context): Intent {
            return Intent(context, MusicWidgetReceiver::class.java).apply {
                action = ACTION_PREV
            }
        }

        fun createUpdateIntent(context: Context): Intent {
            return Intent(context, MusicWidgetReceiver::class.java).apply {
                action = ACTION_UPDATE_STATE
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        val playbackManager = MusicPlaybackManager.getInstance(context)

        when (intent.action) {
            ACTION_PLAY_PAUSE -> {
                playbackManager.togglePlayPause()
                // Mise à jour immédiate optimiste de l'état
                val currentState = MusicWidgetState.load(context)
                val newIsPlaying = !currentState.isPlaying
                MusicWidgetState.save(
                    context = context,
                    title = currentState.title,
                    artist = currentState.artist,
                    album = currentState.album,
                    isPlaying = newIsPlaying,
                    albumArtUri = currentState.albumArtUri,
                    trackId = currentState.trackId,
                    artBitmap = MusicWidgetState.loadBitmap(context)
                )
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        MusicGlanceWidget().updateAll(context)
                    } catch (_: Exception) {}
                }
            }

            ACTION_NEXT -> {
                playbackManager.playNext()
            }

            ACTION_PREV -> {
                playbackManager.playPrevious()
            }

            ACTION_UPDATE_STATE -> {
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        MusicGlanceWidget().updateAll(context)
                    } catch (_: Exception) {}
                }
            }
        }
    }
}
