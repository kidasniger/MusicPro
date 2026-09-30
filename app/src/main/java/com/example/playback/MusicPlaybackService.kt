package com.example.playback

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.media.audiofx.Equalizer
import android.os.Bundle
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import android.os.SystemClock
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.MediaSession.ConnectionResult.AcceptedResultBuilder
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.Futures
import com.example.MainActivity
import com.example.R
import com.example.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Service Media3 de lecture audio en arrière-plan avec MediaSession.
 *
 * Gère :
 * - Lecture continue en arrière-plan via Foreground Service (type mediaPlayback)
 * - Notification MediaStyle avec métadonnées, pochette et contrôles (play/pause/prev/next/scrubber)
 * - Gestion automatique de l'Audio Focus (appels entrants, notifications, perte temporaire)
 * - Déconnexion casque/Bluetooth (Audio Becoming Noisy -> pause automatique)
 * - Maintien actif pendant le Doze mode via WakeLock (C.WAKE_MODE_LOCAL)
 */
@OptIn(UnstableApi::class)
class MusicPlaybackService : MediaSessionService() {

    private var player: ExoPlayer? = null
    private var mediaSession: MediaSession? = null
    private var equalizer: Equalizer? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val preferencesRepository by lazy { UserPreferencesRepository(applicationContext) }
    private var favoriteTrackIds: Set<Long> = emptySet()
    private var playbackPersistenceJob: Job? = null

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "musicpro_playback_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_SHOW_NOW_PLAYING = "com.example.musicpro.ACTION_SHOW_NOW_PLAYING"
        const val ACTION_SHOW_QUEUE = "com.example.musicpro.ACTION_SHOW_QUEUE"
        const val ACTION_SHOW_LYRICS = "com.example.musicpro.ACTION_SHOW_LYRICS"
        const val EXTRA_REQUEST_ID = "musicpro_request_id"

        const val ACTION_TOGGLE_FAVORITE = "com.example.musicpro.ACTION_TOGGLE_FAVORITE"
        const val ACTION_OPEN_QUEUE = "com.example.musicpro.ACTION_OPEN_QUEUE"
        const val ACTION_OPEN_LYRICS = "com.example.musicpro.ACTION_OPEN_LYRICS"

        private const val TAG = "MusicPlaybackService"
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()

        // 1. Configuration des attributs audio pour la musique et l'audio focus automatique
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        // 2. Initialisation d'ExoPlayer avec gestion d'Audio Focus, Audio Becoming Noisy et WakeLock
        val exoPlayer = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, /* handleAudioFocus = */ true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()

        try {
            val sessionId = exoPlayer.audioSessionId
            if (sessionId != androidx.media3.common.C.AUDIO_SESSION_ID_UNSET) {
                equalizer = Equalizer(0, sessionId)
            }
        } catch (error: Exception) {
            android.util.Log.w("MusicPlaybackService", "Égaliseur matériel indisponible: ${error.message}")
        }

        player = exoPlayer

        // Synchronisation des favoris avec le bouton cœur de la notification.
        serviceScope.launch {
            preferencesRepository.favoriteTrackIds.collectLatest { ids ->
                favoriteTrackIds = ids
                updateNotificationButtons()
            }
        }

        // 3. PendingIntent pour réouvrir l'application directement sur l'écran de lecture
        val intent = Intent(this, MainActivity::class.java).apply {
            action = ACTION_SHOW_NOW_PLAYING
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // 4. MediaSession Media3 avec token de session
        mediaSession = MediaSession.Builder(this, exoPlayer)
            .setSessionActivity(pendingIntent)
            .setCallback(object : MediaSession.Callback {
                override fun onConnect(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo
                ): MediaSession.ConnectionResult {
                    val isLegacyController =
                        controller.packageName == MediaSession.ControllerInfo.LEGACY_CONTROLLER_PACKAGE_NAME
                    val isOwnController =
                        controller.packageName == packageName && controller.uid == Process.myUid()
                    val isNotificationController = session.isMediaNotificationController(controller)

                    if (!controller.isTrusted &&
                        !isOwnController &&
                        !isLegacyController &&
                        !isNotificationController
                    ) {
                        android.util.Log.w(
                            "MusicPlaybackService",
                            "MediaSession: contrôleur refusé ${controller.packageName}"
                        )
                        return MediaSession.ConnectionResult.reject()
                    }

                    val available = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
                        .add(AudioEffectCommands.SET_ENABLED)
                        .add(AudioEffectCommands.SET_PRESET)
                        .add(AudioEffectCommands.SET_BAND)
                        .add(AudioEffectCommands.RESET)
                        .add(SessionCommand(ACTION_TOGGLE_FAVORITE, Bundle.EMPTY))
                        .add(SessionCommand(ACTION_OPEN_QUEUE, Bundle.EMPTY))
                        .add(SessionCommand(ACTION_OPEN_LYRICS, Bundle.EMPTY))
                        .build()

                    val acceptedBuilder = AcceptedResultBuilder(session)
                        .setAvailableSessionCommands(available)
                    if (isNotificationController) {
                        acceptedBuilder.setMediaButtonPreferences(buildNotificationButtons())
                    }
                    return acceptedBuilder.build()
                }

                @SuppressLint("WrongConstant")
                override fun onCustomCommand(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo,
                    customCommand: SessionCommand,
                    args: Bundle
                ): ListenableFuture<SessionResult> {
                    return try {
                        when (customCommand.customAction) {
                            AudioEffectCommands.ACTION_SET_ENABLED -> {
                                equalizer?.enabled = args.getBoolean(AudioEffectCommands.KEY_ENABLED, true)
                            }
                            AudioEffectCommands.ACTION_SET_PRESET -> {
                                equalizer?.enabled = true
                                equalizer?.usePreset(args.getShort(AudioEffectCommands.KEY_PRESET, 0.toShort()))
                            }
                            AudioEffectCommands.ACTION_SET_BAND -> {
                                val eq = equalizer
                                if (eq != null) {
                                    eq.enabled = true
                                    val count = eq.numberOfBands.toInt().coerceAtLeast(1)
                                    val requested = args.getInt(AudioEffectCommands.KEY_BAND, 0).coerceIn(0, 4)
                                    val actual = if (count == 1) 0 else
                                        (requested.toFloat() * (count - 1) / 4f).toInt()
                                    val range = eq.bandLevelRange
                                    val level = args.getShort(AudioEffectCommands.KEY_LEVEL, 0.toShort())
                                        .coerceIn(range[0], range[1])
                                    eq.setBandLevel(actual.toShort(), level)
                                }
                            }
                            AudioEffectCommands.ACTION_RESET -> {
                                equalizer?.enabled = false
                                equalizer?.let { eq ->
                                    val range = eq.bandLevelRange
                                    val neutral = 0.coerceIn(range[0].toInt(), range[1].toInt()).toShort()
                                    for (band in 0 until eq.numberOfBands) {
                                        eq.setBandLevel(band.toShort(), neutral)
                                    }
                                }
                            }
                            ACTION_TOGGLE_FAVORITE -> toggleCurrentFavorite()
                            ACTION_OPEN_QUEUE -> openMediaUi(ACTION_SHOW_QUEUE)
                            ACTION_OPEN_LYRICS -> openMediaUi(ACTION_SHOW_LYRICS)
                        }
                        Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                    } catch (error: Exception) {
                        android.util.Log.w("MusicPlaybackService", "Commande EQ refusée: ${error.message}")
                        Futures.immediateFuture(SessionResult(SessionResult.RESULT_ERROR_UNKNOWN))
                    }
                }
            })
            .build()

        // Synchronisation du Widget Glance sur les événements de lecture
        exoPlayer.addListener(object : androidx.media3.common.Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                notifyWidgetUpdate(exoPlayer, isPlaying)
                persistPlaybackState(exoPlayer)
                if (isPlaying) startPlaybackPersistence(exoPlayer) else stopPlaybackPersistence()
            }

            override fun onMediaItemTransition(mediaItem: androidx.media3.common.MediaItem?, reason: Int) {
                updateNotificationButtons()
                notifyWidgetUpdate(exoPlayer, exoPlayer.isPlaying)
                persistPlaybackState(exoPlayer)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                notifyWidgetUpdate(exoPlayer, exoPlayer.isPlaying)
                if (playbackState == androidx.media3.common.Player.STATE_ENDED) {
                    persistPlaybackState(exoPlayer, forcePositionMs = 0L, forcePlaying = false)
                }
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                android.util.Log.e("MusicPlaybackService", "ExoPlayer error: ${error.errorCodeName} - ${error.message}", error)
                notifyWidgetUpdate(exoPlayer, false)
            }
        })

        // 5. Notification Media système avec canal dédié et id
        val notificationProvider = DefaultMediaNotificationProvider.Builder(this)
            .setChannelId(NOTIFICATION_CHANNEL_ID)
            .setChannelName(R.string.notification_channel_name)
            .setNotificationId(NOTIFICATION_ID)
            .build()

        setMediaNotificationProvider(notificationProvider)
    }

    private fun persistPlaybackState(
        exoPlayer: ExoPlayer,
        forcePositionMs: Long? = null,
        forcePlaying: Boolean? = null
    ) {
        val trackId = exoPlayer.currentMediaItem?.mediaId?.toLongOrNull() ?: return
        if (trackId <= 0L) return
        val position = (forcePositionMs ?: exoPlayer.currentPosition).coerceAtLeast(0L)
        val isPlaying = forcePlaying ?: exoPlayer.isPlaying
        serviceScope.launch {
            runCatching {
                preferencesRepository.setSavedPlaybackState(trackId, position, isPlaying)
            }
        }
    }

    private fun startPlaybackPersistence(exoPlayer: ExoPlayer) {
        playbackPersistenceJob?.cancel()
        playbackPersistenceJob = serviceScope.launch {
            while (isActive) {
                delay(1500)
                persistPlaybackState(exoPlayer)
            }
        }
    }

    private fun stopPlaybackPersistence() {
        playbackPersistenceJob?.cancel()
        playbackPersistenceJob = null
    }

    private fun buildNotificationButtons(): List<CommandButton> {
        val isFavorite = player?.currentMediaItem?.mediaId?.toLongOrNull()?.let(favoriteTrackIds::contains) == true

        return listOf(
            CommandButton.Builder(
                if (isFavorite) CommandButton.ICON_HEART_FILLED else CommandButton.ICON_HEART_UNFILLED
            )
                .setDisplayName(if (isFavorite) "Retirer des favoris" else "Ajouter aux favoris")
                .setSessionCommand(SessionCommand(ACTION_TOGGLE_FAVORITE, Bundle.EMPTY))
                .build(),
            CommandButton.Builder(CommandButton.ICON_QUEUE_ADD)
                .setDisplayName("Ouvrir la file d'attente")
                .setSessionCommand(SessionCommand(ACTION_OPEN_QUEUE, Bundle.EMPTY))
                .build(),
            CommandButton.Builder(CommandButton.ICON_SUBTITLES)
                .setDisplayName("Ouvrir les paroles")
                .setSessionCommand(SessionCommand(ACTION_OPEN_LYRICS, Bundle.EMPTY))
                .build()
        )
    }

    private fun updateNotificationButtons() {
        mediaSession?.setMediaButtonPreferences(buildNotificationButtons())
    }

    private fun toggleCurrentFavorite() {
        val trackId = player?.currentMediaItem?.mediaId?.toLongOrNull() ?: return
        serviceScope.launch(Dispatchers.IO) {
            val next = favoriteTrackIds.toMutableSet().apply {
                if (!add(trackId)) remove(trackId)
            }
            preferencesRepository.setFavoriteTrackIds(next)
        }
    }

    private fun openMediaUi(action: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            this.action = action
            putExtra(EXTRA_REQUEST_ID, SystemClock.uptimeMillis())
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_SINGLE_TOP or
                Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(intent)
    }

    private fun notifyWidgetUpdate(player: ExoPlayer, isPlaying: Boolean) {
        val currentItem = player.currentMediaItem
        val metadata = currentItem?.mediaMetadata
        val title = metadata?.title?.toString() ?: "Aucune lecture"
        val artist = metadata?.artist?.toString() ?: "MusicPro"
        val album = metadata?.albumTitle?.toString() ?: ""
        val artUri = metadata?.artworkUri?.toString()
        val trackId = currentItem?.mediaId?.toLongOrNull() ?: -1L

        com.example.widget.MusicWidgetUpdater.update(
            context = applicationContext,
            title = title,
            artist = artist,
            album = album,
            albumArtUri = artUri,
            trackId = trackId,
            isPlaying = isPlaying
        )
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        val session = mediaSession ?: return null
        val isLegacyController =
            controllerInfo.packageName == MediaSession.ControllerInfo.LEGACY_CONTROLLER_PACKAGE_NAME
        val isOwnController =
            controllerInfo.packageName == packageName && controllerInfo.uid == Process.myUid()
        val isNotificationController = session.isMediaNotificationController(controllerInfo)

        return if (
            controllerInfo.isTrusted ||
            isOwnController ||
            isLegacyController ||
            isNotificationController
        ) {
            session
        } else {
            android.util.Log.w(
                "MusicPlaybackService",
                "MediaSession: requête de session refusée pour ${controllerInfo.packageName}"
            )
            null
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        player?.let { persistPlaybackState(it) }
        stopPlaybackPersistence()
        serviceScope.cancel()
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        equalizer?.release()
        equalizer = null
        player = null
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_desc)
                setShowBadge(false)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }
}
