package com.example.ui.album

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.AudioTrackEntity
import com.example.ui.theme.MusicProTokens
import com.example.ui.theme.dominantAccentArgb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Morceaux d'un album, dans l'ordre des fichiers (les noms commencent souvent par le numéro de piste). */
internal fun albumTracksOf(tracks: List<AudioTrackEntity>, albumName: String): List<AudioTrackEntity> =
    tracks.filter { it.album == albumName }.sortedBy { it.path.ifBlank { it.title } }

/** Durée totale arrondie à la minute, 1 minute au minimum quand l'album n'est pas vide. */
internal fun albumDurationMinutes(tracks: List<AudioTrackEntity>): Int {
    if (tracks.isEmpty()) return 0
    return (tracks.sumOf { it.duration } / 60_000.0).let { Math.round(it).toInt() }.coerceAtLeast(1)
}

/**
 * Écran Album (mise à jour 023) : grande pochette centrée avec fondu vers sa couleur dominante, titre,
 * artiste, type, boutons Aléatoire / Lecture (pilule blanche) / Télécharger, description repliable
 * (« PLUS ») et liste numérotée avec badge « E » pour les titres explicites.
 *
 * [explicitTrackIds] : la base ne stocke pas encore l'information « explicite » ; tant qu'elle est
 * absente, le badge n'est affiché pour aucun titre.
 */
@Composable
fun AlbumScreen(
    albumName: String,
    tracks: List<AudioTrackEntity>,
    currentPlayingTrack: AudioTrackEntity?,
    isPlaying: Boolean,
    onBack: () -> Unit,
    onPlay: (shuffle: Boolean) -> Unit,
    onTrackClick: (AudioTrackEntity) -> Unit,
    onDownload: () -> Unit,
    onAddAlbumToPlaylist: () -> Unit,
    onSearch: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
    explicitTrackIds: Set<Long> = emptySet()
) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val coverUri = remember(tracks) { tracks.firstNotNullOfOrNull { it.albumArtUri?.takeIf(String::isNotBlank) } }
    val artist = remember(tracks) { tracks.firstOrNull()?.artist.orEmpty() }
    val coverColor = rememberCoverColor(coverUri)
    val count = tracks.size
    val countText = context.resources.getQuantityString(R.plurals.album_track_count, count, count)
    val minutesText = stringResource(R.string.album_minutes, albumDurationMinutes(tracks))
    val description = stringResource(R.string.album_description, artist, countText, minutesText) +
        " " + stringResource(R.string.album_description_extra)
    var expanded by remember { mutableStateOf(false) }
    var canExpand by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }
    val coverSize = MusicProTokens.Size.Touch * 5

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background)
            .testTag("album_screen")
    ) {
        // Fondu de la couleur dominante de la pochette vers le fond.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .size(width = MusicProTokens.Size.Touch, height = MusicProTokens.Size.Touch * 8)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(coverColor.copy(alpha = 0.40f), scheme.background)
                    )
                )
        )

        Box(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = MusicProTokens.Size.Touch + MusicProTokens.Spacing.Sm,
                    bottom = MusicProTokens.Spacing.Xxl
                ),
                verticalArrangement = Arrangement.spacedBy(MusicProTokens.Spacing.Sm)
            ) {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        val coverShape = RoundedCornerShape(MusicProTokens.Radius.Lg)
                        Box(
                            modifier = Modifier
                                .size(coverSize)
                                .shadow(MusicProTokens.Elevation.Raised, coverShape)
                                .clip(coverShape)
                                .background(scheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            if (coverUri != null) {
                                AsyncImage(
                                    model = coverUri,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = scheme.onSurfaceVariant,
                                    modifier = Modifier.size(MusicProTokens.Size.IllustrationIcon)
                                )
                            }
                        }
                    }
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = MusicProTokens.Spacing.Lg, vertical = MusicProTokens.Spacing.Sm),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(MusicProTokens.Spacing.Xs)
                    ) {
                        Text(
                            text = albumName,
                            style = MaterialTheme.typography.headlineMedium,
                            color = scheme.onBackground,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.semantics { heading() }
                        )
                        Text(
                            text = artist,
                            style = MaterialTheme.typography.titleMedium,
                            color = scheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = stringResource(R.string.album_type),
                            style = MaterialTheme.typography.labelLarge,
                            color = scheme.onSurfaceVariant
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(MusicProTokens.Spacing.Md, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RoundActionButton(
                            icon = Icons.Default.Shuffle,
                            description = stringResource(R.string.album_shuffle),
                            enabled = count > 0,
                            onClick = { onPlay(true) },
                            tag = "album_shuffle"
                        )
                        Button(
                            onClick = { onPlay(false) },
                            enabled = count > 0,
                            shape = MusicProTokens.Radius.PillShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color.Black,
                                disabledContainerColor = scheme.onSurface.copy(alpha = 0.12f),
                                disabledContentColor = scheme.onSurface.copy(alpha = 0.38f)
                            ),
                            modifier = Modifier
                                .heightIn(min = MusicProTokens.Size.Touch)
                                .testTag("album_play")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(Modifier.width(MusicProTokens.Spacing.Xs))
                            Text(
                                text = stringResource(R.string.album_play),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        RoundActionButton(
                            icon = Icons.Default.Download,
                            description = stringResource(R.string.album_download),
                            enabled = count > 0,
                            onClick = onDownload,
                            tag = "album_download"
                        )
                    }
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = MusicProTokens.Spacing.Lg)
                    ) {
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = scheme.onSurfaceVariant,
                            maxLines = if (expanded) Int.MAX_VALUE else 2,
                            overflow = TextOverflow.Ellipsis,
                            onTextLayout = { if (!expanded) canExpand = it.hasVisualOverflow }
                        )
                        if (canExpand || expanded) {
                            TextButton(
                                onClick = { expanded = !expanded },
                                modifier = Modifier
                                    .heightIn(min = MusicProTokens.Size.Touch)
                                    .testTag("album_more")
                            ) {
                                Text(
                                    text = stringResource(if (expanded) R.string.album_less else R.string.album_more),
                                    fontWeight = FontWeight.Bold,
                                    color = scheme.onBackground
                                )
                            }
                        }
                    }
                }

                itemsIndexed(tracks, key = { _, track -> track.id }) { index, track ->
                    AlbumTrackRow(
                        number = index + 1,
                        track = track,
                        isCurrent = currentPlayingTrack?.id == track.id,
                        isPlaying = isPlaying && currentPlayingTrack?.id == track.id,
                        isExplicit = track.id in explicitTrackIds,
                        onClick = { onTrackClick(track) }
                    )
                }
            }

            // Barre d'actions : retour à gauche, +, recherche et ⋯ à droite.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MusicProTokens.Spacing.Sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(MusicProTokens.Size.Touch).testTag("album_back")
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.album_back),
                        tint = scheme.onBackground
                    )
                }
                Spacer(Modifier.weight(1f))
                IconButton(
                    onClick = onAddAlbumToPlaylist,
                    enabled = count > 0,
                    modifier = Modifier.size(MusicProTokens.Size.Touch).testTag("album_add")
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.album_add),
                        tint = scheme.onBackground
                    )
                }
                IconButton(
                    onClick = onSearch,
                    modifier = Modifier.size(MusicProTokens.Size.Touch).testTag("album_search")
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = stringResource(R.string.album_search),
                        tint = scheme.onBackground
                    )
                }
                Box {
                    IconButton(
                        onClick = { menuOpen = true },
                        modifier = Modifier.size(MusicProTokens.Size.Touch).testTag("album_menu")
                    ) {
                        Icon(
                            Icons.Default.MoreHoriz,
                            contentDescription = stringResource(R.string.album_menu),
                            tint = scheme.onBackground
                        )
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.album_share)) },
                            onClick = {
                                menuOpen = false
                                onShare()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RoundActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    val scheme = MaterialTheme.colorScheme
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(MusicProTokens.Size.Touch)
            .clip(CircleShape)
            .border(MusicProTokens.Size.Stroke, scheme.outline, CircleShape)
            .testTag(tag)
    ) {
        Icon(icon, contentDescription = description, tint = scheme.onBackground)
    }
}

@Composable
private fun AlbumTrackRow(
    number: Int,
    track: AudioTrackEntity,
    isCurrent: Boolean,
    isPlaying: Boolean,
    isExplicit: Boolean,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val titleColor = if (isCurrent) scheme.primary else scheme.onBackground
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = MusicProTokens.Size.Touch)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = MusicProTokens.Spacing.Lg, vertical = MusicProTokens.Spacing.Sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MusicProTokens.Spacing.Md)
    ) {
        Box(modifier = Modifier.width(MusicProTokens.Spacing.Xl), contentAlignment = Alignment.Center) {
            if (isPlaying) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = scheme.primary,
                    modifier = Modifier.size(MusicProTokens.Size.Icon)
                )
            } else {
                Text(
                    text = number.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isCurrent) scheme.primary else scheme.onSurfaceVariant
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.bodyLarge,
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MusicProTokens.Spacing.Xs)
            ) {
                if (isExplicit) ExplicitBadge()
                Text(
                    text = track.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Text(
            text = track.formatDuration(),
            style = MaterialTheme.typography.labelMedium,
            color = scheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ExplicitBadge() {
    val scheme = MaterialTheme.colorScheme
    val description = stringResource(R.string.album_explicit_cd)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(MusicProTokens.Spacing.Xs))
            .background(scheme.onSurfaceVariant.copy(alpha = 0.24f))
            .padding(horizontal = MusicProTokens.Spacing.Xs)
            .semantics { contentDescription = description }
    ) {
        Text(
            text = stringResource(R.string.album_explicit_badge),
            style = MaterialTheme.typography.labelSmall,
            color = scheme.onSurface,
            fontWeight = FontWeight.Bold
        )
    }
}

/** Couleur dominante de la pochette (calcul hors du thread principal), avec fondu de 320 ms. */
@Composable
private fun rememberCoverColor(uri: String?): Color {
    val context = LocalContext.current.applicationContext
    val scheme = MaterialTheme.colorScheme
    val isDark = scheme.background.luminance() < 0.5f
    val extracted by produceState<Color?>(initialValue = null, uri, isDark) {
        value = if (uri.isNullOrBlank()) {
            null
        } else {
            withContext(Dispatchers.IO) { extractCoverColor(context, uri, isDark) }?.let { Color(it) }
        }
    }
    val reduceMotion = remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
    val animated by animateColorAsState(
        targetValue = extracted ?: scheme.primary,
        animationSpec = if (reduceMotion) snap() else tween(MusicProTokens.Duration.Slow),
        label = "album_cover_color"
    )
    return animated
}

private fun extractCoverColor(context: android.content.Context, uriString: String, isDark: Boolean): Int? {
    val bitmap = decodeSmallBitmap(context, Uri.parse(uriString)) ?: return null
    return try {
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        dominantAccentArgb(pixels, isDark)
    } finally {
        bitmap.recycle()
    }
}

private fun decodeSmallBitmap(context: android.content.Context, uri: Uri): Bitmap? = runCatching {
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
