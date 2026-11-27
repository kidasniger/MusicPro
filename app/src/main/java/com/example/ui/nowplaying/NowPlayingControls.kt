package com.example.ui.nowplaying

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.SpeakerGroup
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.AudioTrackEntity
import com.example.ui.components.glass.GlassMaterials
import com.example.ui.components.glass.GlassSurface
import com.example.ui.theme.MusicProCyanLight
import com.example.ui.theme.MusicProCyanNeon
import com.example.ui.theme.MusicProFavorite
import com.example.ui.theme.MusicProPrimaryGradient
import com.example.ui.theme.MusicProVioletPrimary
import com.example.util.TrackTitleFormatter
import java.util.Locale

/** Contrôles de transport du lecteur (pilule titre, progression, boutons) — extraits de NowPlayingScreen.kt (mise à jour 101). */
/** Pilule de verre : titre (2 lignes max) + artiste, boutons ronds 40 dp Radio/File/J'aime (042). */
@Composable
internal fun TitleArtistPill(
    track: AudioTrackEntity?,
    isFavorite: Boolean,
    heartScale: Float,
    onToggleFavorite: () -> Unit,
    onOpenRadio: () -> Unit,
    onOpenQueue: () -> Unit
) {
    GlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(32.dp),
        material = GlassMaterials.regular()
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track?.title?.takeIf { it.isNotBlank() }
                        ?.let(TrackTitleFormatter::displayTitle)
                        ?: stringResource(R.string.np_no_track),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 17.sp
                )
                Text(
                    text = track?.artist ?: stringResource(R.string.np_player_name),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Round40Button(
                icon = Icons.Default.Radio,
                contentDescription = stringResource(R.string.np_radio),
                onClick = onOpenRadio
            )
            Round40Button(
                icon = Icons.AutoMirrored.Filled.QueueMusic,
                contentDescription = stringResource(R.string.np_queue),
                onClick = onOpenQueue
            )
            Round40Button(
                icon = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = stringResource(R.string.np_favorite),
                tint = if (isFavorite) MusicProFavorite else MaterialTheme.colorScheme.onSurfaceVariant,
                onClick = onToggleFavorite,
                testTag = "now_playing_favorite_button",
                scale = heartScale
            )
        }
    }
}

/** Bouton rond de 40 dp des pilules (042). */
@Composable
private fun Round40Button(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    testTag: String? = null,
    scale: Float = 1f
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier
                .size(20.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
        )
    }
}

/** Pilule « Passer à la vidéo » (046). */
@Composable
internal fun VideoPill(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
        modifier = Modifier.testTag("now_playing_video_pill")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.SpeakerGroup,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = stringResource(R.string.np_switch_to_video),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 6.dp)
            )
        }
    }
}

/** Pastille de compte à rebours de la minuterie (050). */
@Composable
internal fun SleepCountdownPill(
    remainingMs: Long,
    endOfTrack: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)),
        modifier = Modifier.testTag("now_playing_sleep_pill")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = if (endOfTrack) {
                    stringResource(R.string.np_sleep_end_of_track)
                } else {
                    formatTime(remainingMs)
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 5.dp)
            )
        }
    }
}

/**
 * Barre de progression fine et tactile (043) : 4 dp (10 dp au toucher), zone tactile de 40 dp,
 * écoulié à gauche, restant/total à droite, aperçu flottant du temps pendant le glissement.
 */
@Composable
internal fun FineProgressBar(
    positionMs: Long,
    durationMs: Long,
    isSeeking: Boolean,
    onSeekStart: (Float) -> Unit,
    onSeekDrag: (Float) -> Unit,
    onSeekEnd: () -> Unit,
    onSeekTap: (Float) -> Unit
) {
    val fraction = (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    val barHeight by animateFloatAsState(
        targetValue = if (isSeeking) 10f else 4f,
        animationSpec = tween(durationMillis = 120),
        label = "np_bar_height"
    )
    val remainingMs = (durationMs - positionMs).coerceAtLeast(0L)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                formatTime(positionMs),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "−" + formatTime(remainingMs),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    " / " + formatTime(durationMs),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .semantics {
                    progressBarRangeInfo = ProgressBarRangeInfo(fraction, 0f..1f)
                    contentDescription = "Position de lecture"
                    stateDescription = "${formatTime(positionMs)} sur ${formatTime(durationMs)}"
                }
                .pointerInput(durationMs) {
                    detectTapGestures { offset ->
                        onSeekTap((offset.x / size.width.toFloat()).coerceIn(0f, 1f))
                    }
                }
                .pointerInput(durationMs) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            onSeekStart((offset.x / size.width.toFloat()).coerceIn(0f, 1f))
                        },
                        onHorizontalDrag = { change, _ ->
                            change.consume()
                            onSeekDrag((change.position.x / size.width.toFloat()).coerceIn(0f, 1f))
                        },
                        onDragEnd = { onSeekEnd() },
                        onDragCancel = { onSeekEnd() }
                    )
                },
            contentAlignment = Alignment.CenterStart
        ) {
            // Aperçu flottant du temps au-dessus du doigt (043).
            if (isSeeking) {
                Row(modifier = Modifier.matchParentSize()) {
                    Box(modifier = Modifier.weight(fraction, fill = true))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.75f),
                        modifier = Modifier.offsetFromTop()
                    ) {
                        Text(
                            text = formatTime(positionMs),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Box(modifier = Modifier.weight(1f - fraction, fill = true))
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(barHeight.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f))
                    .align(Alignment.CenterStart)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(barHeight.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .align(Alignment.CenterStart)
            )
        }
    }
}

/** Décalage vertical de l'aperçu flottant au-dessus de la barre. */
private fun Modifier.offsetFromTop(): Modifier = this.then(
    Modifier.graphicsLayer { translationY = -18.dp.toPx() }
)

/** 044 — Transport : aléatoire 28 dp, précédent 64 dp, lecture 88 dp, suivant 64 dp, répéter 28 dp. */
@Composable
internal fun TransportRow(
    isPlaying: Boolean,
    isShuffleEnabled: Boolean,
    repeatMode: Int,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .testTag("now_playing_transport_row"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TransportStateButton(
            icon = Icons.Default.Shuffle,
            contentDescription = stringResource(R.string.np_shuffle),
            active = isShuffleEnabled,
            onClick = onToggleShuffle
        )

        TransportCircle(size = 64.dp, onClick = onPrevious, testTag = "now_playing_previous_button") {
            Icon(
                Icons.Default.SkipPrevious,
                contentDescription = stringResource(R.string.np_previous),
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(32.dp)
            )
        }

        TransportCircle(
            size = 88.dp,
            onClick = onPlayPause,
            testTag = "now_playing_play_pause_button",
            background = SolidColor(MaterialTheme.colorScheme.primary)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = stringResource(
                    if (isPlaying) R.string.np_pause else R.string.np_play
                ),
                tint = Color.White,
                modifier = Modifier.size(42.dp)
            )
        }

        TransportCircle(size = 64.dp, onClick = onNext, testTag = "now_playing_next_button") {
            Icon(
                Icons.Default.SkipNext,
                contentDescription = stringResource(R.string.np_next),
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(32.dp)
            )
        }

        TransportStateButton(
            icon = if (repeatMode == androidx.media3.common.Player.REPEAT_MODE_ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
            contentDescription = stringResource(R.string.np_repeat),
            active = repeatMode != androidx.media3.common.Player.REPEAT_MODE_OFF,
            onClick = onToggleRepeat
        )
    }
}

/** Bouton circulaire de transport (verre simple) avec taille paramétrable (044). */
@Composable
private fun TransportCircle(
    size: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
    testTag: String? = null,
    background: Brush? = null,
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = background?.let { Color.Transparent }
            ?: MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
        border = if (background == null) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)) else null,
        modifier = Modifier
            .size(size)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (background != null) {
                        Modifier.background(background, CircleShape)
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center,
            content = { Row(verticalAlignment = Alignment.CenterVertically, content = content) }
        )
    }
}

/**
 * Pastille ronde d'état (aléatoire/répéter) — mise à jour 101 : même langage visuel que les
 * autres boutons de transport (cercle de verre de 44 dp) au lieu d'une icône nue de 28 dp,
 * avec point d'état sous la pastille quand le réglage est actif (044, accessibilité).
 */
@Composable
private fun TransportStateButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    active: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = if (active) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
            },
            border = BorderStroke(
                1.dp,
                if (active) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                } else {
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)
                }
            ),
            modifier = Modifier.size(44.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        // Point d'état : visible uniquement quand le réglage est actif.
        Box(
            modifier = Modifier
                .padding(top = 3.dp)
                .size(4.dp)
                .background(
                    if (active) MaterialTheme.colorScheme.primary else Color.Transparent,
                    CircleShape
                )
        )
    }
}


internal fun formatTime(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}
