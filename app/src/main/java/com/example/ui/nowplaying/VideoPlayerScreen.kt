package com.example.ui.nowplaying

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.R
import kotlinx.coroutines.delay

/**
 * Mise à jour 046 — Lecteur vidéo plein écran « Passer à la vidéo ».
 *
 * Un ExoPlayer dédié lit le fichier vidéo ; la position de départ est celle du morceau audio
 * (±100 ms), et à la fermeture la position courante est renvoyée pour recaler la lecture audio.
 * [onClose] reçoit (positionFinaleMs, vidéoEnCoursDeLecture).
 */
@Composable
fun VideoPlayerScreen(
    videoUri: Uri,
    startPositionMs: Long,
    onClose: (currentPositionMs: Long, wasPlaying: Boolean) -> Unit
) {
    val context = LocalContext.current
    var closed by remember { mutableStateOf(false) }
    var currentMs by remember { mutableLongStateOf(startPositionMs) }
    var wasPlaying by remember { mutableStateOf(false) }

    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUri))
            seekTo(startPositionMs.coerceAtLeast(0L))
            prepare()
            playWhenReady = true
        }
    }

    LaunchedEffect(Unit) {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                wasPlaying = isPlaying
            }

            override fun onPlayerError(error: PlaybackException) {
                Toast.makeText(context, R.string.np_video_error, Toast.LENGTH_SHORT).show()
                if (!closed) {
                    closed = true
                    onClose(startPositionMs, false)
                }
            }
        })
        // Suivi de la position pour recaler la lecture audio à la fermeture.
        while (!closed) {
            currentMs = player.currentPosition.coerceAtLeast(0L)
            delay(200L)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (!closed) {
                closed = true
                onClose(currentMs, wasPlaying)
            }
            player.release()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { viewContext ->
                PlayerView(viewContext).apply {
                    this.player = player
                    useController = true
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        IconButton(
            onClick = {
                if (!closed) {
                    closed = true
                    onClose(currentMs, wasPlaying)
                }
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(12.dp)
                .size(44.dp)
                .background(Color.Black.copy(alpha = 0.45f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.np_video_close),
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
