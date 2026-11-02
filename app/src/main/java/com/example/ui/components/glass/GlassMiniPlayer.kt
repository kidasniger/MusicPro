package com.example.ui.components.glass

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.AudioTrackEntity
import kotlin.math.abs

/** Portée de transition partagée fournie par [SharedCoverHost]. */
@OptIn(ExperimentalSharedTransitionApi::class)
internal val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

/** Portée d'AnimatedVisibility courante, fournie par [SharedCoverVisibility]. */
internal val LocalCoverVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

private const val COVER_SHARED_KEY = "now_playing_cover"

/**
 * Conteneur racine de la transition partagée de la pochette (mini-lecteur <-> lecteur plein écran).
 * Remplace un simple Box plein écran.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedCoverHost(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    SharedTransitionLayout(modifier = modifier) {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
            Box(modifier = Modifier.fillMaxSize(), content = content)
        }
    }
}

/**
 * AnimatedVisibility (fondu) qui expose sa portée à [sharedCover].
 * Durée 0 si l'utilisateur a désactivé les animations.
 */
@Composable
fun SharedCoverVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val animationsEnabled = rememberAnimationsEnabled()
    val systemAnimationsEnabled = rememberSystemAnimationsEnabled()
    val duration = when {
        !systemAnimationsEnabled -> 0
        LocalReduceAnimations.current -> 120
        animationsEnabled -> 250
        else -> 0
    }
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(animationSpec = tween(durationMillis = duration)),
        exit = fadeOut(animationSpec = tween(durationMillis = duration))
    ) {
        CompositionLocalProvider(LocalCoverVisibilityScope provides this) {
            content()
        }
    }
}

/**
 * Marque une pochette comme élément partagé. Sans [SharedCoverHost] / [SharedCoverVisibility]
 * parents, ne fait rien. Sans animations système : bascule instantanée.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedCover(): Modifier {
    val transitionScope = LocalSharedTransitionScope.current ?: return this
    val visibilityScope = LocalCoverVisibilityScope.current ?: return this
    val animationsEnabled = rememberAnimationsEnabled()
    val boundsTransform = remember(animationsEnabled) {
        BoundsTransform { _, _ ->
            if (animationsEnabled) {
                tween(durationMillis = 350, easing = FastOutSlowInEasing)
            } else {
                snap()
            }
        }
    }
    return with(transitionScope) {
        this@sharedCover.sharedElement(
            rememberSharedContentState(key = COVER_SHARED_KEY),
            animatedVisibilityScope = visibilityScope,
            boundsTransform = boundsTransform
        )
    }
}

/**
 * Mini-lecteur : pilule de verre de 56 dp, posée 8 dp au-dessus de la barre de navigation.
 * - tap ou glissement vers le haut : ouvre le lecteur plein écran ;
 * - glissement horizontal : morceau précédent / suivant.
 */
@Composable
fun GlassMiniPlayer(
    visible: Boolean,
    track: AudioTrackEntity,
    isPlaying: Boolean,
    onPlayPauseToggle: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    SharedCoverVisibility(visible = visible) {
        MiniPlayerPill(
            track = track,
            isPlaying = isPlaying,
            onPlayPauseToggle = onPlayPauseToggle,
            onNext = onNext,
            onPrevious = onPrevious,
            onOpen = onOpen,
            modifier = modifier
        )
    }
}

@Composable
private fun MiniPlayerPill(
    track: AudioTrackEntity,
    isPlaying: Boolean,
    onPlayPauseToggle: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animationsEnabled = rememberAnimationsEnabled()
    val openLabel = stringResource(R.string.glass_mini_player_open)

    val currentOnNext by rememberUpdatedState(onNext)
    val currentOnPrevious by rememberUpdatedState(onPrevious)
    val currentOnOpen by rememberUpdatedState(onOpen)

    var dragX by remember(track.id) { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    val shownDragX by animateFloatAsState(
        targetValue = dragX,
        animationSpec = if (isDragging || !animationsEnabled) snap() else tween(durationMillis = 180),
        label = "mini_player_drag"
    )

    GlassSurface(
        modifier = modifier
            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        material = GlassMaterials.thick()
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(track.id) {
                    val horizontalThreshold = 56.dp.toPx()
                    val verticalThreshold = 40.dp.toPx()
                    val maxShift = 24.dp.toPx()
                    var totalX = 0f
                    var totalY = 0f
                    detectDragGestures(
                        onDragStart = {
                            totalX = 0f
                            totalY = 0f
                            isDragging = true
                        },
                        onDrag = { change, amount ->
                            change.consume()
                            totalX += amount.x
                            totalY += amount.y
                            dragX = if (abs(totalX) > abs(totalY)) {
                                (totalX * 0.4f).coerceIn(-maxShift, maxShift)
                            } else {
                                0f
                            }
                        },
                        onDragEnd = {
                            val absX = abs(totalX)
                            val absY = abs(totalY)
                            isDragging = false
                            dragX = 0f
                            when {
                                absX > absY && absX >= horizontalThreshold -> {
                                    if (totalX > 0f) currentOnPrevious() else currentOnNext()
                                }
                                absY > absX && totalY <= -verticalThreshold -> currentOnOpen()
                            }
                            totalX = 0f
                            totalY = 0f
                        },
                        onDragCancel = {
                            isDragging = false
                            dragX = 0f
                            totalX = 0f
                            totalY = 0f
                        }
                    )
                }
                .clickable(onClickLabel = openLabel, role = Role.Button, onClick = onOpen)
                .graphicsLayer { translationX = shownDragX }
                .padding(start = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pochette arrondie 40 dp (élément partagé avec le lecteur plein écran)
            Box(
                modifier = Modifier
                    .sharedCover()
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (!track.albumArtUri.isNullOrBlank()) {
                    AsyncImage(
                        model = track.albumArtUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                Text(
                    text = track.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = track.artist,
                    fontSize = 12.sp,
                    color = LocalContentColor.current.copy(alpha = 0.75f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onNext) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = stringResource(R.string.glass_mini_player_next),
                    modifier = Modifier.size(24.dp)
                )
            }

            FilledIconButton(
                onClick = onPlayPauseToggle,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.testTag("mini_player_play_pause_button")
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = stringResource(
                        if (isPlaying) R.string.glass_mini_player_pause else R.string.glass_mini_player_play
                    ),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
