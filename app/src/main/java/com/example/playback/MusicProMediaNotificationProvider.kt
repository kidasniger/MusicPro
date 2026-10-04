package com.example.playback

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.DataSourceBitmapLoader
import androidx.media3.session.CacheBitmapLoader
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaStyleNotificationHelper
import com.example.R
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.MoreExecutors

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
class MusicProMediaNotificationProvider(
    private val context: Context
) : MediaNotification.Provider {

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "musicpro_playback_channel"
    }

    private val bitmapLoader = CacheBitmapLoader(
        DataSourceBitmapLoader.Builder(context)
            .setMaximumOutputDimension(256)
            .setMakeShared(true)
            .build()
    )

    @Volatile
    private var artworkBitmap: Bitmap? = null

    @Volatile
    private var artworkKey: String? = null

    @Volatile
    private var artworkLoadingKey: String? = null

    override fun getNotificationChannelInfo(): MediaNotification.Provider.NotificationChannelInfo {
        return MediaNotification.Provider.NotificationChannelInfo(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name)
        )
    }

    override fun handleCustomCommand(
        session: MediaSession,
        action: String,
        extras: Bundle
    ): Boolean {
        return false
    }

    override fun createNotification(
        mediaSession: MediaSession,
        mediaButtonPreferences: ImmutableList<CommandButton>,
        actionFactory: MediaNotification.ActionFactory,
        onNotificationChangedCallback: MediaNotification.Provider.Callback
    ): MediaNotification {
        return buildNotification(
            mediaSession,
            mediaButtonPreferences,
            actionFactory,
            onNotificationChangedCallback,
            requestArtwork = true
        )
    }

    private fun buildNotification(
        mediaSession: MediaSession,
        mediaButtonPreferences: ImmutableList<CommandButton>,
        actionFactory: MediaNotification.ActionFactory,
        onNotificationChangedCallback: MediaNotification.Provider.Callback,
        requestArtwork: Boolean
    ): MediaNotification {
        val player = mediaSession.player
        val currentItem = player.currentMediaItem
        val metadata = currentItem?.mediaMetadata

        val mediaId = currentItem?.mediaId.orEmpty()
        val currentArtworkKey = if (mediaId.isNotBlank()) {
            mediaId
        } else {
            (metadata?.title?.toString().orEmpty()) + "|" +
                (metadata?.artist?.toString().orEmpty()) + "|" +
                (metadata?.albumTitle?.toString().orEmpty())
        }

        if (artworkKey != currentArtworkKey) {
            artworkKey = currentArtworkKey
            artworkBitmap = null
            artworkLoadingKey = null
        }

        if (requestArtwork && artworkBitmap == null && artworkLoadingKey != currentArtworkKey) {
            val future = metadata?.let { bitmapLoader.loadBitmapFromMetadata(it) }
            if (future != null) {
                artworkLoadingKey = currentArtworkKey
                Futures.addCallback(
                    future,
                    object : FutureCallback<Bitmap> {
                        override fun onSuccess(result: Bitmap) {
                            if (artworkKey == currentArtworkKey) {
                                artworkBitmap = result
                                artworkLoadingKey = null
                                onNotificationChangedCallback.onNotificationChanged(
                                    buildNotification(
                                        mediaSession,
                                        mediaButtonPreferences,
                                        actionFactory,
                                        onNotificationChangedCallback,
                                        requestArtwork = false
                                    )
                                )
                            }
                        }

                        override fun onFailure(t: Throwable) {
                            if (artworkKey == currentArtworkKey) {
                                artworkLoadingKey = null
                            }
                        }
                    },
                    MoreExecutors.directExecutor()
                )
            }
        }

        val title = metadata?.title?.toString()?.ifBlank { null } ?: "MusicPro"
        val artist = metadata?.artist?.toString()?.ifBlank { null } ?: "Lecture en cours"

        val compact = RemoteViews(
            context.packageName,
            R.layout.notification_musicpro_compact
        )
        val expanded = RemoteViews(
            context.packageName,
            R.layout.notification_musicpro_expanded
        )

        val artwork = artworkBitmap
        if (artwork != null) {
            compact.setImageViewBitmap(R.id.notification_art_compact, artwork)
            expanded.setImageViewBitmap(R.id.notification_art_expanded, artwork)
        } else {
            compact.setImageViewResource(
                R.id.notification_art_compact,
                R.drawable.musicpro_logo_square
            )
            expanded.setImageViewResource(
                R.id.notification_art_expanded,
                R.drawable.musicpro_logo_square
            )
        }

        compact.setTextViewText(R.id.notification_title_compact, title)
        compact.setTextViewText(R.id.notification_artist_compact, artist)
        expanded.setTextViewText(R.id.notification_title_expanded, title)
        expanded.setTextViewText(R.id.notification_artist_expanded, artist)

        val isPlaying = player.isPlaying || (
            player.playWhenReady &&
                player.playbackState != Player.STATE_ENDED &&
                player.playbackState != Player.STATE_IDLE
        )
        val playIcon = if (isPlaying) {
            R.drawable.ic_widget_pause
        } else {
            R.drawable.ic_widget_play
        }

        compact.setImageViewResource(R.id.notification_play_compact, playIcon)
        expanded.setImageViewResource(R.id.notification_play, playIcon)

        fun bindMediaAction(
            viewId: Int,
            command: @Player.Command Int,
            vararg expandedVisibilityIds: Int
        ) {
            val available = player.isCommandAvailable(command)
            val pendingIntent =
                actionFactory.createMediaActionPendingIntent(mediaSession, command)

            compact.setOnClickPendingIntent(viewId, pendingIntent)
            expanded.setOnClickPendingIntent(viewId, pendingIntent)

            expandedVisibilityIds.forEach { visibilityId ->
                expanded.setViewVisibility(
                    visibilityId,
                    if (available) View.VISIBLE else View.GONE
                )
            }

            if (viewId == R.id.notification_prev_compact) {
                compact.setViewVisibility(
                    viewId,
                    if (available) View.VISIBLE else View.GONE
                )
            }
            if (viewId == R.id.notification_next_compact) {
                compact.setViewVisibility(
                    viewId,
                    if (available) View.VISIBLE else View.GONE
                )
            }
        }

        bindMediaAction(
            R.id.notification_prev_compact,
            Player.COMMAND_SEEK_TO_PREVIOUS,
            R.id.notification_prev
        )
        bindMediaAction(
            R.id.notification_play_compact,
            Player.COMMAND_PLAY_PAUSE,
            R.id.notification_play
        )
        bindMediaAction(
            R.id.notification_next_compact,
            Player.COMMAND_SEEK_TO_NEXT,
            R.id.notification_next
        )
        bindMediaAction(
            R.id.notification_prev,
            Player.COMMAND_SEEK_TO_PREVIOUS
        )
        bindMediaAction(
            R.id.notification_rewind,
            Player.COMMAND_SEEK_BACK
        )
        bindMediaAction(
            R.id.notification_play,
            Player.COMMAND_PLAY_PAUSE
        )
        bindMediaAction(
            R.id.notification_forward,
            Player.COMMAND_SEEK_FORWARD
        )
        bindMediaAction(
            R.id.notification_next,
            Player.COMMAND_SEEK_TO_NEXT
        )
        bindMediaAction(
            R.id.notification_close,
            Player.COMMAND_STOP
        )

        val favoriteButton = mediaButtonPreferences.firstOrNull {
            it.sessionCommand?.customAction == MusicPlaybackService.ACTION_TOGGLE_FAVORITE
        }
        if (favoriteButton?.isEnabled == true) {
            val favoriteAction = actionFactory.createCustomActionFromCustomCommandButton(
                mediaSession,
                favoriteButton
            )
            expanded.setOnClickPendingIntent(
                R.id.notification_favorite,
                favoriteAction.actionIntent
            )
            expanded.setViewVisibility(
                R.id.notification_favorite,
                View.VISIBLE
            )
        } else {
            expanded.setViewVisibility(
                R.id.notification_favorite,
                View.GONE
            )
        }

        val currentIndex = player.currentMediaItemIndex
        val nextItems = (1..2).mapNotNull { offset ->
            val nextIndex = currentIndex + offset
            if (nextIndex in 0 until player.mediaItemCount) {
                player.getMediaItemAt(nextIndex)
            } else {
                null
            }
        }

        if (nextItems.isEmpty()) {
            expanded.setViewVisibility(
                R.id.notification_next_section,
                View.GONE
            )
        } else {
            expanded.setViewVisibility(
                R.id.notification_next_section,
                View.VISIBLE
            )
            setNextItem(expanded, 0, nextItems.getOrNull(0))
            setNextItem(expanded, 1, nextItems.getOrNull(1))
        }

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_music_notification)
            .setContentTitle(title)
            .setContentText(artist)
            .setLargeIcon(artwork)
            .setContentIntent(mediaSession.sessionActivity)
            .setDeleteIntent(
                actionFactory.createNotificationDismissalIntent(mediaSession)
            )
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setColor(0xFF8A2BE2.toInt())
            .setColorized(true)
            .setCustomContentView(compact)
            .setCustomBigContentView(expanded)
            .setCustomHeadsUpContentView(compact)
            .setStyle(
                MediaStyleNotificationHelper.DecoratedMediaCustomViewStyle(
                    mediaSession
                )
            )

        return MediaNotification(
            NOTIFICATION_ID,
            notificationBuilder.build()
        )
    }

    private fun setNextItem(
        views: RemoteViews,
        position: Int,
        item: MediaItem?
    ) {
        val titleId = if (position == 0) {
            R.id.notification_next_1_title
        } else {
            R.id.notification_next_2_title
        }
        val artistId = if (position == 0) {
            R.id.notification_next_1_artist
        } else {
            R.id.notification_next_2_artist
        }

        if (item == null) {
            views.setViewVisibility(titleId, View.GONE)
            views.setViewVisibility(artistId, View.GONE)
            return
        }

        val metadata = item.mediaMetadata
        val title = metadata.title?.toString()?.ifBlank { null } ?: "Titre inconnu"
        val artist = metadata.artist?.toString()?.ifBlank { null } ?: "Artiste inconnu"

        views.setViewVisibility(titleId, View.VISIBLE)
        views.setViewVisibility(artistId, View.VISIBLE)
        views.setTextViewText(titleId, title)
        views.setTextViewText(artistId, artist)
    }
}
