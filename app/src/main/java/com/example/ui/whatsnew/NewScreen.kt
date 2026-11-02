package com.example.ui.whatsnew

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.AudioTrackEntity
import com.example.ui.theme.MusicProTokens

/** Nombre maximum de pochettes par carrousel. */
internal const val NewCarouselLimit = 20

/** Un album regroupé à partir des morceaux détectés. */
internal data class NewAlbum(
    val name: String,
    val artist: String,
    val coverUri: String?,
    val tracks: List<AudioTrackEntity>
)

/** Derniers fichiers détectés, du plus récent au plus ancien. */
internal fun recentTracks(tracks: List<AudioTrackEntity>): List<AudioTrackEntity> =
    tracks.sortedByDescending { it.dateAdded }.take(NewCarouselLimit)

/** Morceaux jamais écoutés (`lastPlayed` à 0), les plus récemment ajoutés d'abord. */
internal fun unplayedTracks(tracks: List<AudioTrackEntity>): List<AudioTrackEntity> =
    tracks.filter { it.lastPlayed == 0L }.sortedByDescending { it.dateAdded }.take(NewCarouselLimit)

/** Albums classés par date d'ajout de leur morceau le plus récent ; les albums sans nom sont ignorés. */
internal fun recentAlbums(tracks: List<AudioTrackEntity>): List<NewAlbum> =
    tracks
        .filter { it.album.isNotBlank() }
        .groupBy { it.album }
        .map { (name, list) ->
            val album = NewAlbum(
                name = name,
                artist = list.first().artist,
                coverUri = list.firstNotNullOfOrNull { it.albumArtUri?.takeIf(String::isNotBlank) },
                tracks = list
            )
            album to list.maxOf { it.dateAdded }
        }
        .sortedByDescending { it.second }
        .take(NewCarouselLimit)
        .map { it.first }

/**
 * Écran « Nouveau » (mise à jour 021) : ajouts récents, morceaux jamais écoutés et albums récemment
 * ajoutés, en carrousels horizontaux de pochettes 140 dp à coins 12 dp. Tout est calculé à partir de
 * la liste de morceaux déjà chargée. État vide : illustration simple et bouton « Analyser mon appareil ».
 */
@Composable
fun NewScreen(
    tracks: List<AudioTrackEntity>,
    currentPlayingTrack: AudioTrackEntity?,
    isScanning: Boolean,
    onPlayTrack: (AudioTrackEntity, List<AudioTrackEntity>) -> Unit,
    onScan: () -> Unit,
    modifier: Modifier = Modifier
) {
    val recent = remember(tracks) { recentTracks(tracks) }
    val unplayed = remember(tracks) { unplayedTracks(tracks) }
    val albums = remember(tracks) { recentAlbums(tracks) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("new_screen"),
        contentPadding = PaddingValues(bottom = MusicProTokens.Spacing.Xl),
        verticalArrangement = Arrangement.spacedBy(MusicProTokens.Spacing.Lg)
    ) {
        item { NewHeader() }

        if (tracks.isEmpty()) {
            item { NewEmptyState(isScanning = isScanning, onScan = onScan) }
        } else {
            if (recent.isNotEmpty()) {
                item { SectionTitle(stringResource(R.string.new_section_recent)) }
                item {
                    TrackCarousel(
                        list = recent,
                        currentPlayingTrack = currentPlayingTrack,
                        onPlayTrack = onPlayTrack
                    )
                }
            }
            if (unplayed.isNotEmpty()) {
                item { SectionTitle(stringResource(R.string.new_section_unplayed)) }
                item {
                    TrackCarousel(
                        list = unplayed,
                        currentPlayingTrack = currentPlayingTrack,
                        onPlayTrack = onPlayTrack
                    )
                }
            }
            if (albums.isNotEmpty()) {
                item { SectionTitle(stringResource(R.string.new_section_albums)) }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = MusicProTokens.Spacing.Lg),
                        horizontalArrangement = Arrangement.spacedBy(MusicProTokens.Spacing.Md)
                    ) {
                        items(albums, key = { it.name }) { album ->
                            val isCurrent = album.tracks.any { it.id == currentPlayingTrack?.id }
                            CoverCard(
                                title = album.name,
                                subtitle = album.artist,
                                coverUri = album.coverUri,
                                selected = isCurrent,
                                onClick = { onPlayTrack(album.tracks.first(), album.tracks) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NewHeader() {
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
            text = stringResource(R.string.new_screen_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.semantics { heading() }
        )
        Text(
            text = stringResource(R.string.new_screen_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier
            .padding(horizontal = MusicProTokens.Spacing.Lg)
            .semantics { heading() }
    )
}

@Composable
private fun TrackCarousel(
    list: List<AudioTrackEntity>,
    currentPlayingTrack: AudioTrackEntity?,
    onPlayTrack: (AudioTrackEntity, List<AudioTrackEntity>) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = MusicProTokens.Spacing.Lg),
        horizontalArrangement = Arrangement.spacedBy(MusicProTokens.Spacing.Md)
    ) {
        items(list, key = { it.id }) { track ->
            CoverCard(
                title = track.title,
                subtitle = track.artist,
                coverUri = track.albumArtUri,
                selected = currentPlayingTrack?.id == track.id,
                onClick = { onPlayTrack(track, list) }
            )
        }
    }
}

/** Pochette 140 dp à coins 12 dp, titre et sous-titre sur une ligne ; contour accent si en lecture. */
@Composable
private fun CoverCard(
    title: String,
    subtitle: String,
    coverUri: String?,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(MusicProTokens.Radius.Md)
    val scheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .width(MusicProTokens.Size.CoverCarousel)
            .clip(shape)
            .clickable(role = Role.Button, onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(MusicProTokens.Spacing.Xs)
    ) {
        Box(
            modifier = Modifier
                .size(MusicProTokens.Size.CoverCarousel)
                .clip(shape)
                .background(scheme.surfaceVariant)
                .border(
                    width = if (selected) MusicProTokens.Size.StrokeSelected else MusicProTokens.Size.Stroke,
                    color = if (selected) scheme.primary else scheme.outline,
                    shape = shape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (!coverUri.isNullOrBlank()) {
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
                    modifier = Modifier.size(MusicProTokens.Size.CoverIcon)
                )
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = scheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = scheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** État vide : pastille d'illustration, message et bouton « Analyser mon appareil ». */
@Composable
private fun NewEmptyState(isScanning: Boolean, onScan: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MusicProTokens.Spacing.Xl, vertical = MusicProTokens.Spacing.Xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MusicProTokens.Spacing.Lg)
    ) {
        Box(
            modifier = Modifier
                .size(MusicProTokens.Size.Illustration)
                .clip(CircleShape)
                .background(scheme.surfaceVariant)
                .border(MusicProTokens.Size.Stroke, scheme.outline, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.NewReleases,
                contentDescription = null,
                tint = scheme.primary,
                modifier = Modifier.size(MusicProTokens.Size.IllustrationIcon)
            )
        }
        Text(
            text = stringResource(R.string.new_empty_title),
            style = MaterialTheme.typography.headlineSmall,
            color = scheme.onBackground,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.new_empty_message),
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Button(
            onClick = onScan,
            enabled = !isScanning,
            modifier = Modifier
                .heightIn(min = MusicProTokens.Size.Touch)
                .testTag("new_empty_scan_button")
        ) {
            Text(
                text = stringResource(
                    if (isScanning) R.string.new_scanning else R.string.new_empty_scan
                )
            )
        }
    }
}
