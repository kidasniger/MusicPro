package com.example.ui.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.AudioTrackEntity
import com.example.data.local.PlaylistSummary
import com.example.ui.audio.AlbumSummary
import com.example.ui.audio.ArtistSummary
import com.example.ui.audio.FolderSummary
import com.example.ui.audio.LibraryTab
import com.example.ui.components.EmptyAudioStateView
import com.example.ui.playlist.PlaylistsScreen
import com.example.ui.theme.MusicProBackground
import com.example.ui.theme.MusicProCardBackground
import com.example.ui.theme.MusicProCyanLight
import com.example.ui.theme.MusicProCyanNeon
import com.example.ui.theme.MusicProCyanVibrant
import com.example.ui.theme.MusicProGreenEmerald
import com.example.ui.theme.MusicProPrimaryGradient
import com.example.ui.theme.MusicProSurface
import com.example.ui.theme.MusicProSurfaceElevated
import com.example.ui.theme.MusicProSurfaceVariant
import com.example.ui.theme.MusicProTextMuted
import com.example.ui.theme.MusicProTextPrimary
import com.example.ui.theme.MusicProTextSecondary
import com.example.ui.theme.MusicProTextTertiary
import com.example.ui.theme.MusicProVioletGlow
import com.example.ui.theme.MusicProVioletLight
import com.example.ui.theme.MusicProVioletPrimary
import com.example.ui.theme.MusicProVioletVibrant

@Composable
fun LibraryScreen(
    tracks: List<AudioTrackEntity>,
    albums: List<AlbumSummary>,
    artists: List<ArtistSummary>,
    folders: List<FolderSummary>,
    selectedTab: LibraryTab,
    isScanning: Boolean,
    statusMessage: String?,
    currentPlayingTrack: AudioTrackEntity?,
    isPlaying: Boolean,
    favorites: Set<Long>,
    playlists: List<PlaylistSummary> = emptyList(),
    onPlaylistClick: (PlaylistSummary) -> Unit = {},
    onPlayPlaylistDirectly: (PlaylistSummary) -> Unit = {},
    onCreatePlaylist: (String, String) -> Unit = { _, _ -> },
    onRenamePlaylist: (Long, String, String) -> Unit = { _, _, _ -> },
    onDeletePlaylist: (Long) -> Unit = {},
    onSelectTab: (LibraryTab) -> Unit,
    onTrackClick: (AudioTrackEntity) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onRefreshScan: () -> Unit,
    onLoadDemoTracks: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
    )

    var selectedAlbum by remember { mutableStateOf<AlbumSummary?>(null) }
    var selectedArtist by remember { mutableStateOf<ArtistSummary?>(null) }
    var selectedFolder by remember { mutableStateOf<FolderSummary?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MusicProBackground)
            .testTag("library_screen")
    ) {
        // En-tête Bibliothèque avec statut et bouton de rafraîchissement
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Bibliothèque",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MusicProTextPrimary
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isScanning) MusicProCyanNeon else MusicProGreenEmerald)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isScanning) "Scan MediaStore en cours..." else "${tracks.size} titres • Cache Room actif",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MusicProTextSecondary
                    )
                }
            }

            // Bouton de rafraîchissement manuel du cache
            IconButton(
                onClick = onRefreshScan,
                enabled = !isScanning,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MusicProSurfaceElevated)
                    .testTag("library_refresh_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Rafraîchir le scan MediaStore",
                    tint = MusicProCyanNeon,
                    modifier = Modifier
                        .size(22.dp)
                        .then(if (isScanning) Modifier.rotate(rotationAngle) else Modifier)
                )
            }
        }

        // Indicateur d'activité de scan
        AnimatedVisibility(visible = isScanning) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = MusicProCyanNeon,
                trackColor = MusicProSurfaceElevated
            )
        }

        // Onglets de la bibliothèque : Morceaux / Playlists / Albums / Artistes / Dossiers
        androidx.compose.foundation.lazy.LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(LibraryTab.entries.size) { index ->
                val tab = LibraryTab.entries[index]
                val isSelected = tab == selectedTab
                val countLabel = when (tab) {
                    LibraryTab.TRACKS -> "(${tracks.size})"
                    LibraryTab.PLAYLISTS -> "(${playlists.size})"
                    LibraryTab.ALBUMS -> "(${albums.size})"
                    LibraryTab.ARTISTS -> "(${artists.size})"
                    LibraryTab.FOLDERS -> "(${folders.size})"
                }

                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) MusicProPrimaryGradient
                            else Brush.linearGradient(listOf(MusicProSurfaceElevated, MusicProSurfaceElevated))
                        )
                        .border(
                            1.dp,
                            if (isSelected) MusicProVioletGlow else Color(0x1AFFFFFF),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            selectedAlbum = null
                            selectedArtist = null
                            selectedFolder = null
                            onSelectTab(tab)
                        }
                        .padding(horizontal = 14.dp)
                        .testTag("library_tab_${tab.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${tab.label} $countLabel",
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else MusicProTextSecondary,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Contenu principal selon l'onglet
        if (tracks.isEmpty() && !isScanning && selectedTab != LibraryTab.PLAYLISTS) {
            EmptyAudioStateView(
                onRefreshScan = onRefreshScan,
                onLoadDemoTracks = onLoadDemoTracks,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            when (selectedTab) {
                LibraryTab.TRACKS -> {
                    TracksList(
                        tracks = tracks,
                        currentPlayingTrack = currentPlayingTrack,
                        isPlaying = isPlaying,
                        favorites = favorites,
                        onTrackClick = onTrackClick,
                        onToggleFavorite = onToggleFavorite
                    )
                }
                LibraryTab.PLAYLISTS -> {
                    PlaylistsScreen(
                        playlists = playlists,
                        onPlaylistClick = onPlaylistClick,
                        onPlayPlaylistDirectly = onPlayPlaylistDirectly,
                        onCreatePlaylist = onCreatePlaylist,
                        onRenamePlaylist = onRenamePlaylist,
                        onDeletePlaylist = onDeletePlaylist
                    )
                }
                LibraryTab.ALBUMS -> {
                    if (selectedAlbum != null) {
                        val albumTracks = tracks.filter { it.album == selectedAlbum!!.name }
                        AlbumDetailView(
                            album = selectedAlbum!!,
                            tracks = albumTracks,
                            currentPlayingTrack = currentPlayingTrack,
                            isPlaying = isPlaying,
                            favorites = favorites,
                            onBack = { selectedAlbum = null },
                            onPlayAll = { albumTracks.firstOrNull()?.let { onTrackClick(it) } },
                            onTrackClick = onTrackClick,
                            onToggleFavorite = onToggleFavorite
                        )
                    } else {
                        AlbumsGrid(
                            albums = albums,
                            onAlbumClick = { album ->
                                selectedAlbum = album
                            }
                        )
                    }
                }
                LibraryTab.ARTISTS -> {
                    if (selectedArtist != null) {
                        val artistTracks = tracks.filter { it.artist == selectedArtist!!.name }
                        ArtistDetailView(
                            artist = selectedArtist!!,
                            tracks = artistTracks,
                            currentPlayingTrack = currentPlayingTrack,
                            isPlaying = isPlaying,
                            favorites = favorites,
                            onBack = { selectedArtist = null },
                            onPlayAll = { artistTracks.firstOrNull()?.let { onTrackClick(it) } },
                            onTrackClick = onTrackClick,
                            onToggleFavorite = onToggleFavorite
                        )
                    } else {
                        ArtistsList(
                            artists = artists,
                            tracks = tracks,
                            onArtistClick = { artist ->
                                selectedArtist = artist
                            }
                        )
                    }
                }
                LibraryTab.FOLDERS -> {
                    if (selectedFolder != null) {
                        val folderTracks = tracks.filter { it.folder == selectedFolder!!.name }
                        FolderDetailView(
                            folder = selectedFolder!!,
                            tracks = folderTracks,
                            currentPlayingTrack = currentPlayingTrack,
                            isPlaying = isPlaying,
                            favorites = favorites,
                            onBack = { selectedFolder = null },
                            onPlayAll = { folderTracks.firstOrNull()?.let { onTrackClick(it) } },
                            onTrackClick = onTrackClick,
                            onToggleFavorite = onToggleFavorite
                        )
                    } else {
                        FoldersList(
                            folders = folders,
                            tracks = tracks,
                            onFolderClick = { folder ->
                                selectedFolder = folder
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TracksList(
    tracks: List<AudioTrackEntity>,
    currentPlayingTrack: AudioTrackEntity?,
    isPlaying: Boolean,
    favorites: Set<Long>,
    onTrackClick: (AudioTrackEntity) -> Unit,
    onToggleFavorite: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp, top = 4.dp)
    ) {
        itemsIndexed(tracks, key = { _, track -> track.id }) { index, track ->
            val isCurrent = currentPlayingTrack?.id == track.id
            val isFav = favorites.contains(track.id)

            TrackRowItem(
                index = index + 1,
                track = track,
                isCurrent = isCurrent,
                isPlaying = isPlaying,
                isFavorite = isFav,
                onClick = { onTrackClick(track) },
                onToggleFavorite = { onToggleFavorite(track.id) }
            )
        }
    }
}

@Composable
fun TrackRowItem(
    index: Int,
    track: AudioTrackEntity,
    isCurrent: Boolean,
    isPlaying: Boolean,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(
                if (isCurrent) MusicProSurfaceElevated.copy(alpha = 0.9f)
                else Color.Transparent
            )
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("track_item_${track.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Numéro ou icône de lecture animée
        Box(
            modifier = Modifier.width(28.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (isCurrent && isPlaying) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = "Lecture en cours",
                    tint = MusicProCyanNeon,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Text(
                    text = "%02d".format(index),
                    fontSize = 12.sp,
                    color = if (isCurrent) MusicProCyanNeon else MusicProTextMuted,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Pochette d'album ou icône avec gradient
        var imageLoadError by remember(track.id) { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MusicProSurfaceVariant)
                .border(
                    1.dp,
                    if (isCurrent) MusicProCyanNeon else Color(0x1FFFFFFF),
                    RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (!track.albumArtUri.isNullOrBlank() && !imageLoadError) {
                AsyncImage(
                    model = track.albumArtUri,
                    contentDescription = track.album,
                    contentScale = ContentScale.Crop,
                    onError = { imageLoadError = true },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = if (isCurrent) MusicProCyanNeon else MusicProVioletLight,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Titre et Artiste
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                fontSize = 14.sp,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isCurrent) MusicProCyanNeon else MusicProTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(3.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = track.artist,
                    fontSize = 12.sp,
                    color = MusicProTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Badge Format Audio (FLAC, MP3, etc.)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (track.getAudioFormat() == "FLAC") Color(0x3300E676)
                            else Color(0x268A2BE2)
                        )
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = track.getAudioFormat(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (track.getAudioFormat() == "FLAC") MusicProGreenEmerald else MusicProVioletLight
                    )
                }

                if (track.hasSyncedLyrics) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0x2600D4FF))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "LRC",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MusicProCyanNeon
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Durée
        Text(
            text = track.formatDuration(),
            fontSize = 12.sp,
            color = MusicProTextSecondary,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.width(6.dp))

        // Bouton favori
        IconButton(
            onClick = onToggleFavorite,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Favori",
                tint = if (isFavorite) Color(0xFFFF4081) else MusicProTextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun AlbumsGrid(
    albums: List<AlbumSummary>,
    onAlbumClick: (AlbumSummary) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 120.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(albums, key = { it.name }) { album ->
            AlbumGridItem(album = album, onClick = { onAlbumClick(album) })
        }
    }
}

@Composable
private fun AlbumGridItem(
    album: AlbumSummary,
    onClick: () -> Unit
) {
    var imageLoadError by remember(album.name) { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MusicProCardBackground)
            .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(10.dp)
            .testTag("album_item_${album.name}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(
                        listOf(MusicProVioletPrimary.copy(alpha = 0.5f), MusicProCyanVibrant.copy(alpha = 0.3f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (!album.coverUri.isNullOrBlank() && !imageLoadError) {
                AsyncImage(
                    model = album.coverUri,
                    contentDescription = album.name,
                    contentScale = ContentScale.Crop,
                    onError = { imageLoadError = true },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Album,
                    contentDescription = null,
                    tint = MusicProCyanNeon,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = album.name,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MusicProTextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = album.artist,
            fontSize = 11.sp,
            color = MusicProTextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "${album.trackCount} titres",
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = MusicProCyanLight
        )
    }
}

@Composable
private fun ArtistsList(
    artists: List<ArtistSummary>,
    tracks: List<AudioTrackEntity>,
    onArtistClick: (ArtistSummary) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp, top = 6.dp)
    ) {
        items(artists, key = { it.name }) { artist ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onArtistClick(artist) }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("artist_item_${artist.name}"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar rond artiste
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(MusicProVioletPrimary, MusicProCyanVibrant)
                            )
                        )
                        .border(1.5.dp, MusicProCyanNeon, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = artist.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MusicProTextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${artist.trackCount} morceaux • ${artist.albumsCount} album(s)",
                        fontSize = 12.sp,
                        color = MusicProTextSecondary
                    )
                }

                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = MusicProCyanNeon,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun FoldersList(
    folders: List<FolderSummary>,
    tracks: List<AudioTrackEntity>,
    onFolderClick: (FolderSummary) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp, top = 6.dp)
    ) {
        items(folders, key = { it.name }) { folder ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onFolderClick(folder) }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("folder_item_${folder.name}"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MusicProSurfaceElevated)
                        .border(1.dp, Color(0x3300D4FF), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = MusicProCyanNeon,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = folder.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MusicProTextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = folder.samplePath,
                        fontSize = 11.sp,
                        color = MusicProTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MusicProSurfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${folder.trackCount} titres",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MusicProCyanLight
                    )
                }
            }
        }
    }
}

@Composable
private fun AlbumDetailView(
    album: AlbumSummary,
    tracks: List<AudioTrackEntity>,
    currentPlayingTrack: AudioTrackEntity?,
    isPlaying: Boolean,
    favorites: Set<Long>,
    onBack: () -> Unit,
    onPlayAll: () -> Unit,
    onTrackClick: (AudioTrackEntity) -> Unit,
    onToggleFavorite: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retour aux albums",
                        tint = MusicProCyanNeon
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Albums",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MusicProTextSecondary
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                var imageLoadError by remember(album.name) { mutableStateOf(false) }
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MusicProSurfaceVariant)
                        .border(1.dp, MusicProVioletPrimary, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!album.coverUri.isNullOrBlank() && !imageLoadError) {
                        AsyncImage(
                            model = album.coverUri,
                            contentDescription = album.name,
                            contentScale = ContentScale.Crop,
                            onError = { imageLoadError = true },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Album,
                            contentDescription = null,
                            tint = MusicProCyanNeon,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = album.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MusicProTextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = album.artist,
                        fontSize = 13.sp,
                        color = MusicProVioletLight
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${album.trackCount} titre(s) disponible(s)",
                        fontSize = 11.sp,
                        color = MusicProTextMuted
                    )
                }
            }
        }

        item {
            Button(
                onClick = onPlayAll,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .height(44.dp)
                    .background(MusicProPrimaryGradient, RoundedCornerShape(12.dp))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Lire tout l'album",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        itemsIndexed(tracks, key = { _, track -> track.id }) { index, track ->
            val isCurrent = currentPlayingTrack?.id == track.id
            val isFav = favorites.contains(track.id)

            TrackRowItem(
                index = index + 1,
                track = track,
                isCurrent = isCurrent,
                isPlaying = isPlaying,
                isFavorite = isFav,
                onClick = { onTrackClick(track) },
                onToggleFavorite = { onToggleFavorite(track.id) }
            )
        }
    }
}

@Composable
private fun ArtistDetailView(
    artist: ArtistSummary,
    tracks: List<AudioTrackEntity>,
    currentPlayingTrack: AudioTrackEntity?,
    isPlaying: Boolean,
    favorites: Set<Long>,
    onBack: () -> Unit,
    onPlayAll: () -> Unit,
    onTrackClick: (AudioTrackEntity) -> Unit,
    onToggleFavorite: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retour aux artistes",
                        tint = MusicProCyanNeon
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Artistes",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MusicProTextSecondary
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(MusicProPrimaryGradient)
                        .border(2.dp, MusicProCyanNeon, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = artist.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MusicProTextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${artist.trackCount} morceau(x) • ${artist.albumsCount} album(s)",
                        fontSize = 12.sp,
                        color = MusicProCyanLight
                    )
                }
            }
        }

        item {
            Button(
                onClick = onPlayAll,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .height(44.dp)
                    .background(MusicProPrimaryGradient, RoundedCornerShape(12.dp))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Lire les morceaux de l'artiste",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        itemsIndexed(tracks, key = { _, track -> track.id }) { index, track ->
            val isCurrent = currentPlayingTrack?.id == track.id
            val isFav = favorites.contains(track.id)

            TrackRowItem(
                index = index + 1,
                track = track,
                isCurrent = isCurrent,
                isPlaying = isPlaying,
                isFavorite = isFav,
                onClick = { onTrackClick(track) },
                onToggleFavorite = { onToggleFavorite(track.id) }
            )
        }
    }
}

@Composable
private fun FolderDetailView(
    folder: FolderSummary,
    tracks: List<AudioTrackEntity>,
    currentPlayingTrack: AudioTrackEntity?,
    isPlaying: Boolean,
    favorites: Set<Long>,
    onBack: () -> Unit,
    onPlayAll: () -> Unit,
    onTrackClick: (AudioTrackEntity) -> Unit,
    onToggleFavorite: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retour aux dossiers",
                        tint = MusicProCyanNeon
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Dossiers",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MusicProTextSecondary
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MusicProSurfaceElevated)
                        .border(1.5.dp, MusicProCyanNeon, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = MusicProCyanNeon,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = folder.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MusicProTextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = folder.samplePath,
                        fontSize = 11.sp,
                        color = MusicProTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${folder.trackCount} fichier(s) audio",
                        fontSize = 11.sp,
                        color = MusicProVioletLight
                    )
                }
            }
        }

        item {
            Button(
                onClick = onPlayAll,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .height(44.dp)
                    .background(MusicProPrimaryGradient, RoundedCornerShape(12.dp))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Lire tout le dossier",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        itemsIndexed(tracks, key = { _, track -> track.id }) { index, track ->
            val isCurrent = currentPlayingTrack?.id == track.id
            val isFav = favorites.contains(track.id)

            TrackRowItem(
                index = index + 1,
                track = track,
                isCurrent = isCurrent,
                isPlaying = isPlaying,
                isFavorite = isFav,
                onClick = { onTrackClick(track) },
                onToggleFavorite = { onToggleFavorite(track.id) }
            )
        }
    }
}
