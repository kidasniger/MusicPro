package com.example.ui.radio

import android.provider.Settings
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.example.R
import com.example.data.local.AudioTrackEntity
import com.example.ui.theme.MusicProTokens

/**
 * Écran « Radio / Mixes » (mise à jour 022) : cinq mixes générés localement, un bouton Lecture par mix
 * et un indicateur « en direct » à trois barres animées pendant la lecture d'un morceau du mix.
 */
@Composable
fun RadioScreen(
    tracks: List<AudioTrackEntity>,
    favorites: Set<Long>,
    currentPlayingTrack: AudioTrackEntity?,
    isPlaying: Boolean,
    onPlayMix: (List<AudioTrackEntity>) -> Unit,
    modifier: Modifier = Modifier
) {
    // Le jour fixe le tirage aléatoire : on évite de régénérer à chaque recomposition.
    val dayBucket = remember { System.currentTimeMillis() / MixEngine.DayMs }
    val mixes = remember(tracks, favorites, dayBucket) {
        MixEngine.generate(tracks, favorites, dayBucket * MixEngine.DayMs)
    }

    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    com.example.ui.navigation.ScrollToTopEffect(
        com.example.ui.home.NavigationSection.RADIO.name,
        listState
    )
    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .testTag("radio_screen"),
        contentPadding = PaddingValues(bottom = MusicProTokens.Spacing.Xl),
        verticalArrangement = Arrangement.spacedBy(MusicProTokens.Spacing.Md)
    ) {
        item { RadioHeader() }

        if (tracks.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.mixes_empty_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = MusicProTokens.Spacing.Xl,
                            vertical = MusicProTokens.Spacing.Xxl
                        )
                )
            }
        } else {
            items(mixes, key = { it.kind.name }) { mix ->
                val isLive = isPlaying && currentPlayingTrack != null &&
                    mix.tracks.any { it.id == currentPlayingTrack.id }
                MixCard(
                    mix = mix,
                    isLive = isLive,
                    onPlay = { onPlayMix(mix.tracks) }
                )
            }
        }
    }
}

@Composable
private fun RadioHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = MusicProTokens.Spacing.Lg,
                end = MusicProTokens.Spacing.Lg,
                top = MusicProTokens.Spacing.Xl
            ),
        verticalArrangement = Arrangement.spacedBy(MusicProTokens.Spacing.Xs)
    ) {
        Text(
            text = stringResource(R.string.mixes_screen_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.semantics { heading() }
        )
        Text(
            text = stringResource(R.string.mixes_screen_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MixCard(
    mix: Mix,
    isLive: Boolean,
    onPlay: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val title = stringResource(mix.kind.titleRes())
    val count = mix.tracks.size
    val countText = context.resources.getQuantityString(R.plurals.mixes_track_count, count, count)
    val hasTracks = count > 0

    Surface(
        shape = RoundedCornerShape(MusicProTokens.Radius.Lg),
        color = scheme.surfaceVariant,
        border = BorderStroke(MusicProTokens.Size.Stroke, scheme.outline),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MusicProTokens.Spacing.Lg)
            .testTag("radio_mix_" + mix.kind.name.lowercase())
    ) {
        Row(
            modifier = Modifier.padding(MusicProTokens.Spacing.Lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MusicProTokens.Spacing.Md)
        ) {
            Box(
                modifier = Modifier
                    .size(MusicProTokens.Size.Touch)
                    .clip(RoundedCornerShape(MusicProTokens.Radius.Md))
                    .background(scheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = mix.kind.icon(),
                    contentDescription = null,
                    tint = scheme.primary,
                    modifier = Modifier.size(MusicProTokens.Size.Icon)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(MusicProTokens.Spacing.Xs)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = scheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(mix.kind.descriptionRes()),
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MusicProTokens.Spacing.Sm)
                ) {
                    Text(
                        text = countText,
                        style = MaterialTheme.typography.labelMedium,
                        color = scheme.onSurfaceVariant
                    )
                    if (isLive) {
                        LiveBars()
                        Text(
                            text = stringResource(R.string.mixes_live),
                            style = MaterialTheme.typography.labelMedium,
                            color = scheme.primary
                        )
                    }
                }
            }

            IconButton(
                onClick = onPlay,
                enabled = hasTracks,
                modifier = Modifier
                    .size(MusicProTokens.Size.Touch)
                    .clip(CircleShape)
                    .background(if (hasTracks) scheme.primary else scheme.onSurface.copy(alpha = 0.12f))
                    .testTag("radio_play_" + mix.kind.name.lowercase())
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = stringResource(R.string.mixes_play_cd, title),
                    tint = if (hasTracks) scheme.onPrimary else scheme.onSurface.copy(alpha = 0.38f)
                )
            }
        }
    }
}

/**
 * Indicateur « en direct » : trois barres qui montent et descendent en décalé.
 * Avec « Réduire les animations » (échelle de durée à 0), les barres restent fixes.
 */
@Composable
private fun LiveBars(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.primary
    val description = stringResource(R.string.mixes_live)
    val context = LocalContext.current
    val reduceMotion = remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
    val transition = rememberInfiniteTransition(label = "mix_live_bars")
    val bar1 by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(420, easing = LinearEasing), RepeatMode.Reverse),
        label = "bar1"
    )
    val bar2 by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(560, easing = LinearEasing), RepeatMode.Reverse),
        label = "bar2"
    )
    val bar3 by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(480, easing = LinearEasing), RepeatMode.Reverse),
        label = "bar3"
    )
    val fractions = if (reduceMotion) listOf(0.6f, 1f, 0.8f) else listOf(bar1, bar2, bar3)

    Canvas(
        modifier = modifier
            .size(width = MusicProTokens.Spacing.Xl, height = MusicProTokens.Spacing.Lg)
            .semantics { contentDescription = description }
    ) {
        val slot = size.width / 5f
        fractions.forEachIndexed { index, fraction ->
            val barHeight = size.height * fraction
            drawRoundRect(
                color = color,
                topLeft = Offset(x = slot * 2f * index, y = size.height - barHeight),
                size = Size(width = slot, height = barHeight),
                cornerRadius = CornerRadius(slot / 2f, slot / 2f)
            )
        }
    }
}

private fun MixKind.icon(): ImageVector = when (this) {
    MixKind.DISCOVERY -> Icons.Default.Explore
    MixKind.ENERGY -> Icons.Default.Bolt
    MixKind.CALM -> Icons.Default.Spa
    MixKind.PARTY -> Icons.Default.Celebration
    MixKind.MEMORIES -> Icons.Default.History
}

private fun MixKind.titleRes(): Int = when (this) {
    MixKind.DISCOVERY -> R.string.mixes_discovery
    MixKind.ENERGY -> R.string.mixes_energy
    MixKind.CALM -> R.string.mixes_calm
    MixKind.PARTY -> R.string.mixes_party
    MixKind.MEMORIES -> R.string.mixes_memories
}

private fun MixKind.descriptionRes(): Int = when (this) {
    MixKind.DISCOVERY -> R.string.mixes_discovery_desc
    MixKind.ENERGY -> R.string.mixes_energy_desc
    MixKind.CALM -> R.string.mixes_calm_desc
    MixKind.PARTY -> R.string.mixes_party_desc
    MixKind.MEMORIES -> R.string.mixes_memories_desc
}
