package com.example.ui.artist

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
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
import androidx.compose.ui.text.style.TextGeometricTransform
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.AudioTrackEntity
import com.example.ui.theme.MusicProTokens

/** Taille du nom selon sa longueur, pour que les noms longs tiennent sur deux lignes au plus. */
internal fun artistNameFontSizeSp(name: String): Int = when {
    name.length <= 8 -> 80
    name.length <= 14 -> 64
    else -> 48
}

/**
 * Écran Artiste (mise à jour 024) : photo en niveaux de gris pleine largeur avec dégradé vers le fond,
 * nom en très gros caractères condensés, trois boutons (infos, Lecture, étoile), « Top morceaux »
 * (pochette 48 dp, titre, album et menu ⋯), puis Discographie, Singles et Artistes similaires.
 *
 * La base ne stocke ni photo d'artiste ni année de sortie : la photo est la première pochette de ses
 * morceaux, et la ligne secondaire d'un titre n'indique que l'album.
 */
@Composable
fun ArtistScreen(
    artistName: String,
    artistTracks: List<AudioTrackEntity>,
    allTracks: List<AudioTrackEntity>,
    currentPlayingTrack: AudioTrackEntity?,
    isPlaying: Boolean,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onPlay: () -> Unit,
    onToggleFavorite: () -> Unit,
    onTrackClick: (AudioTrackEntity) -> Unit,
    onAddToQueue: (AudioTrackEntity, Boolean) -> Unit,
    onOpenAlbum: (String) -> Unit,
    onOpenArtist: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val coverUri = remember(artistTracks) { artistCoverOf(artistTracks) }
    val releases = remember(artistTracks) { artistReleasesOf(artistTracks) }
    val discography = remember(releases) { discographyOf(releases) }
    val singles = remember(releases) { singlesOf(releases) }
    val topTracks = remember(artistTracks) { topTracksOf(artistTracks, Int.MAX_VALUE) }
    val similar = remember(allTracks, artistName) { similarArtistsOf(allTracks, artistName) }
    var showAll by remember { mutableStateOf(false) }
    var infoOpen by remember { mutableStateOf(false) }
    val visibleTop = if (showAll) topTracks else topTracks.take(TopTracksLimit)
    val hasTracks = artistTracks.isNotEmpty()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background)
            .testTag("artist_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = MusicProTokens.Spacing.Xxl),
            verticalArrangement = Arrangement.spacedBy(MusicProTokens.Spacing.Md)
        ) {
            item {
                ArtistHero(name = artistName, coverUri = coverUri)
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MusicProTokens.Spacing.Lg),
                    horizontalArrangement = Arrangement.spacedBy(MusicProTokens.Spacing.Md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RoundOutlineButton(
                        onClick = { infoOpen = true },
                        description = stringResource(R.string.artist_info),
                        tag = "artist_info"
                    ) {
                        Icon(Icons.Outlined.Info, contentDescription = null, tint = scheme.onBackground)
                    }
                    Button(
                        onClick = onPlay,
                        enabled = hasTracks,
                        shape = MusicProTokens.Radius.PillShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = scheme.onBackground,
                            contentColor = scheme.background,
                            disabledContainerColor = scheme.onSurface.copy(alpha = 0.12f),
                            disabledContentColor = scheme.onSurface.copy(alpha = 0.38f)
                        ),
                        modifier = Modifier
                            .heightIn(min = MusicProTokens.Size.Touch)
                            .testTag("artist_play")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(MusicProTokens.Spacing.Xs))
                        Text(
                            text = stringResource(R.string.artist_play),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    RoundOutlineButton(
                        onClick = onToggleFavorite,
                        description = stringResource(
                            if (isFavorite) R.string.artist_favorite_remove else R.string.artist_favorite_add
                        ),
                        tag = "artist_favorite"
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = null,
                            tint = if (isFavorite) scheme.primary else scheme.onBackground
                        )
                    }
                }
            }

            if (topTracks.isNotEmpty()) {
                item {
                    SectionTitle(stringResource(R.string.artist_top_tracks))
                }
                items(visibleTop, key = { it.id }) { track ->
                    TopTrackRow(
                        track = track,
                        isCurrent = currentPlayingTrack?.id == track.id,
                        isPlaying = isPlaying && currentPlayingTrack?.id == track.id,
                        onClick = { onTrackClick(track) },
                        onPlayNext = { onAddToQueue(track, true) },
                        onAddToQueue = { onAddToQueue(track, false) }
                    )
                }
                if (topTracks.size > TopTracksLimit) {
                    item {
                        TextButton(
                            onClick = { showAll = !showAll },
                            modifier = Modifier
                                .padding(horizontal = MusicProTokens.Spacing.Sm)
                                .heightIn(min = MusicProTokens.Size.Touch)
                                .testTag("artist_show_all")
                        ) {
                            Text(
                                text = stringResource(
                                    if (showAll) R.string.artist_show_less else R.string.artist_show_all
                                ),
                                fontWeight = FontWeight.Bold,
                                color = scheme.onBackground
                            )
                        }
                    }
                }
            }

            if (discography.isNotEmpty()) {
                item { SectionTitle(stringResource(R.string.artist_discography)) }
                item {
                    ReleaseCarousel(
                        releases = discography,
                        metaRes = R.string.artist_release_album,
                        tagPrefix = "artist_album_",
                        onClick = onOpenAlbum
                    )
                }
            }

            if (singles.isNotEmpty()) {
                item { SectionTitle(stringResource(R.string.artist_singles)) }
                item {
                    ReleaseCarousel(
                        releases = singles,
                        metaRes = R.string.artist_release_single,
                        tagPrefix = "artist_single_",
                        onClick = onOpenAlbum
                    )
                }
            }

            if (similar.isNotEmpty()) {
                item { SectionTitle(stringResource(R.string.artist_similar)) }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = MusicProTokens.Spacing.Lg),
                        horizontalArrangement = Arrangement.spacedBy(MusicProTokens.Spacing.Md)
                    ) {
                        items(similar, key = { it.name }) { artist ->
                            SimilarArtistItem(
                                artist = artist,
                                onClick = { onOpenArtist(artist.name) }
                            )
                        }
                    }
                }
            }
        }

        // Retour : pastille translucide au-dessus de la photo (contraste garanti sur tout fond).
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .statusBarsPadding()
                .padding(MusicProTokens.Spacing.Sm)
                .size(MusicProTokens.Size.Touch)
                .clip(CircleShape)
                .background(scheme.background.copy(alpha = 0.6f))
                .testTag("artist_back")
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.artist_back),
                tint = scheme.onBackground
            )
        }
    }

    if (infoOpen) {
        val countText = context.resources.getQuantityString(
            R.plurals.album_track_count, artistTracks.size, artistTracks.size
        )
        val albumsText = context.resources.getQuantityString(
            R.plurals.artist_album_count, releases.size, releases.size
        )
        val minutesText = stringResource(R.string.artist_minutes, artistDurationMinutes(artistTracks))
        AlertDialog(
            onDismissRequest = { infoOpen = false },
            title = { Text(artistName) },
            text = {
                Text(stringResource(R.string.artist_info_body, countText, albumsText, minutesText))
            },
            confirmButton = {
                TextButton(onClick = { infoOpen = false }) {
                    Text(stringResource(R.string.artist_info_close))
                }
            }
        )
    }
}

@Composable
private fun ArtistHero(name: String, coverUri: String?) {
    val scheme = MaterialTheme.colorScheme
    val grayscale = remember { ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }) }
    val fontSize = artistNameFontSizeSp(name)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(MusicProTokens.Size.HeroHeight)
            .background(scheme.surfaceVariant)
    ) {
        if (coverUri != null) {
            AsyncImage(
                model = coverUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                colorFilter = grayscale,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = scheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(MusicProTokens.Size.Illustration)
            )
        }
        // Dégradé vers le fond : le nom reste lisible en clair comme en sombre.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, scheme.background.copy(alpha = 0.55f), scheme.background),
                        startY = 0f
                    )
                )
        )
        Text(
            text = name.uppercase(),
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = fontSize.sp,
                lineHeight = (fontSize * 0.92f).sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-2).sp,
                textGeometricTransform = TextGeometricTransform(scaleX = 0.82f)
            ),
            color = scheme.onBackground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = MusicProTokens.Spacing.Lg, vertical = MusicProTokens.Spacing.Md)
                .semantics { heading() }
                .testTag("artist_name")
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier
            .padding(horizontal = MusicProTokens.Spacing.Lg)
            .semantics { heading() }
    )
}

@Composable
private fun RoundOutlineButton(
    onClick: () -> Unit,
    description: String,
    tag: String,
    content: @Composable () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(MusicProTokens.Size.Touch)
            .clip(CircleShape)
            .border(MusicProTokens.Size.Stroke, scheme.outline, CircleShape)
            .semantics { contentDescription = description }
            .testTag(tag)
    ) {
        content()
    }
}

@Composable
private fun TopTrackRow(
    track: AudioTrackEntity,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    var menuOpen by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = MusicProTokens.Size.Touch)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = MusicProTokens.Spacing.Lg, vertical = MusicProTokens.Spacing.Xs)
            .testTag("artist_top_row_${track.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MusicProTokens.Spacing.Md)
    ) {
        Box(
            modifier = Modifier
                .size(MusicProTokens.Size.Touch)
                .clip(RoundedCornerShape(MusicProTokens.Radius.Sm))
                .background(scheme.surfaceVariant),
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
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = scheme.onSurfaceVariant,
                    modifier = Modifier.size(MusicProTokens.Size.Icon)
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isCurrent) scheme.primary else scheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = track.album.ifBlank { "Album Inconnu" },
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (isPlaying) {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = null,
                tint = scheme.primary,
                modifier = Modifier.size(MusicProTokens.Size.Icon)
            )
        }
        Box {
            IconButton(
                onClick = { menuOpen = true },
                modifier = Modifier.size(MusicProTokens.Size.Touch)
            ) {
                Icon(
                    Icons.Default.MoreHoriz,
                    contentDescription = stringResource(R.string.artist_row_menu, track.title),
                    tint = scheme.onSurfaceVariant
                )
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.artist_play_next)) },
                    onClick = {
                        menuOpen = false
                        onPlayNext()
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.artist_add_queue)) },
                    onClick = {
                        menuOpen = false
                        onAddToQueue()
                    }
                )
            }
        }
    }
}

@Composable
private fun ReleaseCarousel(
    releases: List<ArtistRelease>,
    metaRes: Int,
    tagPrefix: String,
    onClick: (String) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    LazyRow(
        contentPadding = PaddingValues(horizontal = MusicProTokens.Spacing.Lg),
        horizontalArrangement = Arrangement.spacedBy(MusicProTokens.Spacing.Md)
    ) {
        items(releases, key = { it.name }) { release ->
            val count = context.resources.getQuantityString(
                R.plurals.album_track_count, release.tracks.size, release.tracks.size
            )
            Column(
                modifier = Modifier
                    .width(MusicProTokens.Size.CoverCarousel)
                    .clickable(role = Role.Button) { onClick(release.name) }
                    .testTag(tagPrefix + release.name),
                verticalArrangement = Arrangement.spacedBy(MusicProTokens.Spacing.Xs)
            ) {
                Box(
                    modifier = Modifier
                        .size(MusicProTokens.Size.CoverCarousel)
                        .clip(RoundedCornerShape(MusicProTokens.Radius.Md))
                        .background(scheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (release.coverUri != null) {
                        AsyncImage(
                            model = release.coverUri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = scheme.onSurfaceVariant,
                            modifier = Modifier.size(MusicProTokens.Size.CoverIcon)
                        )
                    }
                }
                Text(
                    text = release.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(metaRes, count),
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SimilarArtistItem(artist: SimilarArtist, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .width(MusicProTokens.Size.Illustration)
            .clickable(role = Role.Button, onClick = onClick)
            .testTag("artist_similar_${artist.name}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MusicProTokens.Spacing.Xs)
    ) {
        Box(
            modifier = Modifier
                .size(MusicProTokens.Size.Illustration)
                .clip(CircleShape)
                .background(scheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (artist.coverUri != null) {
                AsyncImage(
                    model = artist.coverUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = scheme.onSurfaceVariant,
                    modifier = Modifier.size(MusicProTokens.Size.IllustrationIcon)
                )
            }
        }
        Text(
            text = artist.name,
            style = MaterialTheme.typography.bodySmall,
            color = scheme.onBackground,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
