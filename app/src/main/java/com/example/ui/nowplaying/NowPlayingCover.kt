package com.example.ui.nowplaying

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.AudioTrackEntity
import com.example.data.preferences.PlayerPresentation
import com.example.ui.components.glass.sharedCover
import com.example.ui.theme.MusicProCyanGlow
import com.example.ui.theme.MusicProCyanLight
import com.example.ui.theme.MusicProCyanNeon
import com.example.ui.theme.MusicProPrimaryGradient
import com.example.ui.theme.MusicProVioletGlow

/** Présentations de pochette (pochette, vinyle, minimal) du lecteur — extraits de NowPlayingScreen.kt (mise à jour 101). */
/**
 * Zone centrale : pochette carrée (défaut), vinyle ou minimal (048).
 * Gestes 047 : glisser vers le bas = réduire ; double tap gauche/droite = ±10 s avec onde ;
 * appui long = menu. En vinyle, le geste horizontal reste réservé au morceau précédent/suivant.
 */
@Composable
internal fun ImmersiveCover(
    track: AudioTrackEntity?,
    presentationMode: PlayerPresentation,
    isPlaying: Boolean,
    progressMs: Long,
    vinylRotation: Float,
    animationsEnabled: Boolean,
    backgroundColor: Color,
    onBack: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeekBy: (Long) -> Unit,
    onOpenTrackMenu: () -> Unit
) {
    when (presentationMode) {
        PlayerPresentation.VINYLE -> VinylPresentation(
            track = track,
            isPlaying = isPlaying,
            progressMs = progressMs,
            vinylRotation = vinylRotation,
            onNext = onNext,
            onPrevious = onPrevious,
            onPlayPause = onPlayPause
        )
        PlayerPresentation.MINIMAL -> MinimalPresentation(
            track = track,
            backgroundColor = backgroundColor
        )
        PlayerPresentation.POCHETTE -> SquareCoverPresentation(
            track = track,
            isPlaying = isPlaying,
            animationsEnabled = animationsEnabled,
            onBack = onBack,
            onPlayPause = onPlayPause,
            onSeekBy = onSeekBy,
            onOpenTrackMenu = onOpenTrackMenu
        )
    }
}

/** Présentation par défaut : pochette carrée pleine largeur, gestes complets (041/047/048). */
@Composable
private fun SquareCoverPresentation(
    track: AudioTrackEntity?,
    isPlaying: Boolean,
    animationsEnabled: Boolean,
    onBack: () -> Unit,
    onPlayPause: () -> Unit,
    onSeekBy: (Long) -> Unit,
    onOpenTrackMenu: () -> Unit
) {
    // Onde du double tap (047) : côté gauche/droite, s'estompe en 400 ms.
    var rippleTrigger by remember { mutableIntStateOf(0) }
    var rippleSideStart by remember { mutableStateOf(true) }
    val rippleAlpha = remember { Animatable(0f) }

    LaunchedEffect(rippleTrigger) {
        if (rippleTrigger > 0) {
            rippleAlpha.snapTo(0.55f)
            rippleAlpha.animateTo(0f, tween(durationMillis = 400))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(track?.id) {
                var dragY = 0f
                detectVerticalDragGestures(
                    onDragStart = { dragY = 0f },
                    onVerticalDrag = { change, amount ->
                        change.consume()
                        dragY += amount
                    },
                    onDragEnd = {
                        if (dragY > 90.dp.toPx()) onBack()
                        dragY = 0f
                    },
                    onDragCancel = { dragY = 0f }
                )
            }
            .pointerInput(track?.id) {
                detectTapGestures(
                    onTap = { onPlayPause() },
                    onDoubleTap = { offset ->
                        val isLeft = offset.x < size.width / 2f
                        rippleSideStart = isLeft
                        rippleTrigger++
                        onSeekBy(if (isLeft) -10_000L else 10_000L)
                    },
                    onLongPress = { onOpenTrackMenu() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        var imageLoadError by remember(track?.id) { mutableStateOf(false) }

        if (!track?.albumArtUri.isNullOrBlank() && !imageLoadError) {
            AsyncImage(
                model = track?.albumArtUri,
                contentDescription = track?.title,
                contentScale = ContentScale.Crop,
                onError = { imageLoadError = true },
                modifier = Modifier
                    .fillMaxSize()
                    .sharedCover()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .sharedCover()
                    .background(SolidColor(MaterialTheme.colorScheme.primary)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Album,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(84.dp)
                )
            }
        }

        // Onde visuelle du double tap.
        if (rippleAlpha.value > 0f) {
            Box(
                modifier = Modifier
                    .align(if (rippleSideStart) Alignment.CenterStart else Alignment.CenterEnd)
                    .fillMaxSize(0.5f)
                    .graphicsLayer { alpha = rippleAlpha.value }
                    .background(
                        Brush.radialGradient(
                            listOf(Color.White.copy(alpha = 0.5f), Color.White.copy(alpha = 0f))
                        ),
                        CircleShape
                    )
            )
        }
    }
}

/** Présentation vinyle (048) : disque 33 tours, geste horizontal = morceau précédent/suivant. */
@Composable
private fun VinylPresentation(
    track: AudioTrackEntity?,
    isPlaying: Boolean,
    progressMs: Long,
    vinylRotation: Float,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val vinylSize = minOf(maxWidth * 0.72f, maxHeight * 0.88f).coerceAtLeast(150.dp)
        Box(
            modifier = Modifier
                .size(vinylSize)
                .pointerInput(track?.id) {
                    var horizontalDrag = 0f
                    detectHorizontalDragGestures(
                        onDragStart = { horizontalDrag = 0f },
                        onHorizontalDrag = { change, amount ->
                            change.consume()
                            horizontalDrag += amount
                        },
                        onDragEnd = {
                            when {
                                horizontalDrag > 80f -> onPrevious()
                                horizontalDrag < -80f -> onNext()
                            }
                            horizontalDrag = 0f
                        },
                        onDragCancel = { horizontalDrag = 0f }
                    )
                }
                .shadow(
                    elevation = 20.dp,
                    shape = CircleShape,
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                    ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.40f)
                )
                .rotate(if (isPlaying) vinylRotation else 0f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFF221A3A), Color(0xFF0D0B18))
                    )
                )
                .border(2.dp, SolidColor(MaterialTheme.colorScheme.primary), CircleShape)
                .clickable { onPlayPause() },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(vinylSize * 0.88f)
                    .clip(CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(vinylSize * 0.74f)
                    .clip(CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f), CircleShape)
            )

            val centerArtSize = vinylSize * 0.62f
            Box(
                modifier = Modifier
                    .sharedCover()
                    .size(centerArtSize)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                var imageLoadError by remember(track?.id) { mutableStateOf(false) }

                if (!track?.albumArtUri.isNullOrBlank() && !imageLoadError) {
                    AsyncImage(
                        model = track?.albumArtUri,
                        contentDescription = track?.title,
                        contentScale = ContentScale.Crop,
                        onError = { imageLoadError = true },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(SolidColor(MaterialTheme.colorScheme.primary)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Album,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(centerArtSize * 0.45f)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(vinylSize * 0.10f)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.background)
                        .border(1.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.50f), CircleShape)
                )
            }

            // Mise à jour 101 — reflet lumineux solidaire du disque : il balaie la surface
            // pendant la rotation, comme la lumière d'une étagère sur un vrai 33 tours.
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.linearGradient(
                            0.00f to Color.White.copy(alpha = 0f),
                            0.30f to Color.White.copy(alpha = 0.12f),
                            0.50f to Color.White.copy(alpha = 0.03f),
                            0.68f to Color.White.copy(alpha = 0f)
                        ),
                        shape = CircleShape
                    )
            )
        }
    }
}

/** Présentation minimaliste (048) : texte seul sur fond uni, sans pochette. */
@Composable
private fun MinimalPresentation(
    track: AudioTrackEntity?,
    backgroundColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = track?.title ?: stringResource(R.string.np_no_track),
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
        )
        Text(
            text = track?.artist ?: stringResource(R.string.np_player_name),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
        )
    }
}
