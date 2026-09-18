package com.example.ui.playlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.data.local.AudioTrackEntity
import com.example.data.local.PlaylistSummary
import com.example.ui.theme.MusicProBackground
import com.example.ui.theme.MusicProCardBackground
import com.example.ui.theme.MusicProCyanLight
import com.example.ui.theme.MusicProCyanNeon
import com.example.ui.theme.MusicProCyanVibrant
import com.example.ui.theme.MusicProError
import com.example.ui.theme.MusicProGreenEmerald
import com.example.ui.theme.MusicProPrimaryGradient
import com.example.ui.theme.MusicProSurface
import com.example.ui.theme.MusicProSurfaceElevated
import com.example.ui.theme.MusicProTextMuted
import com.example.ui.theme.MusicProTextPrimary
import com.example.ui.theme.MusicProTextSecondary
import com.example.ui.theme.MusicProVioletGlow
import com.example.ui.theme.MusicProVioletLight
import com.example.ui.theme.MusicProVioletPrimary
import kotlin.math.roundToInt

@Composable
fun PlaylistDetailScreen(
    playlist: PlaylistSummary,
    tracks: List<AudioTrackEntity>,
    currentPlayingTrack: AudioTrackEntity?,
    isPlaying: Boolean,
    onBack: () -> Unit,
    onPlayAll: (startIndex: Int, shuffle: Boolean) -> Unit,
    onTrackClick: (track: AudioTrackEntity, queue: List<AudioTrackEntity>) -> Unit,
    onRemoveTrack: (trackId: Long) -> Unit,
    onReorderTracks: (orderedTrackIds: List<Long>) -> Unit,
    onDeletePlaylist: () -> Unit,
    onRenamePlaylist: (newName: String, newDescription: String) -> Unit,
    onOpenAddTracks: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Local reorderable track list synced with incoming Room data
    val localTracks = remember { mutableStateListOf<AudioTrackEntity>() }

    LaunchedEffect(tracks) {
        localTracks.clear()
        localTracks.addAll(tracks)
    }

    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var trackPendingRemoval by remember { mutableStateOf<AudioTrackEntity?>(null) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("playlist_detail_screen"),
        containerColor = MusicProBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("playlist_detail_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retour",
                        tint = MusicProTextPrimary
                    )
                }

                Text(
                    text = playlist.name,
                    color = MusicProTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                )

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.testTag("playlist_detail_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = MusicProTextPrimary
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(MusicProCardBackground)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Ajouter des morceaux", color = MusicProCyanNeon) },
                            leadingIcon = {
                                Icon(Icons.Default.Add, contentDescription = null, tint = MusicProCyanNeon)
                            },
                            onClick = {
                                showMenu = false
                                onOpenAddTracks()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Renommer la playlist", color = MusicProTextPrimary) },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = MusicProTextSecondary)
                            },
                            onClick = {
                                showMenu = false
                                showRenameDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Supprimer la playlist", color = MusicProError) },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = MusicProError)
                            },
                            onClick = {
                                showMenu = false
                                showDeleteConfirmDialog = true
                            }
                        )
                    }
                }
            }

            // Playlist Header Hero Card
            PlaylistHeaderHero(
                playlist = playlist,
                trackCount = localTracks.size,
                onPlayAll = { onPlayAll(0, false) },
                onShuffle = { onPlayAll(0, true) },
                onAddTracks = onOpenAddTracks
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Subheader: List count & Drag & drop hint
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "MORCEAUX (${localTracks.size})",
                    color = MusicProTextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                if (localTracks.size > 1) {
                    Text(
                        text = "Glisser pour réordonner",
                        color = MusicProCyanLight.copy(alpha = 0.8f),
                        fontSize = 11.sp
                    )
                }
            }

            // Track list or Empty state
            if (localTracks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MusicProSurfaceElevated,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PlaylistPlay,
                                    contentDescription = null,
                                    tint = MusicProVioletLight,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Text(
                            text = "Cette playlist est vide",
                            color = MusicProTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = "Ajoutez vos morceaux favoris depuis votre bibliothèque pour écouter votre sélection personnalisée.",
                            color = MusicProTextMuted,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )

                        Button(
                            onClick = onOpenAddTracks,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MusicProVioletPrimary),
                            modifier = Modifier.testTag("empty_playlist_add_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ajouter des morceaux")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(top = 6.dp, bottom = 80.dp)
                ) {
                    itemsIndexed(
                        items = localTracks,
                        key = { _, item -> item.id }
                    ) { index, track ->
                        val isCurrent = currentPlayingTrack?.id == track.id

                        ReorderableTrackRow(
                            index = index,
                            totalCount = localTracks.size,
                            track = track,
                            isCurrent = isCurrent,
                            isPlaying = isPlaying && isCurrent,
                            onClick = {
                                onTrackClick(track, localTracks.toList())
                            },
                            onRemove = {
                                trackPendingRemoval = track
                            },
                            onMoveUp = {
                                if (index > 0) {
                                    val item = localTracks.removeAt(index)
                                    localTracks.add(index - 1, item)
                                    onReorderTracks(localTracks.map { it.id })
                                }
                            },
                            onMoveDown = {
                                if (index < localTracks.size - 1) {
                                    val item = localTracks.removeAt(index)
                                    localTracks.add(index + 1, item)
                                    onReorderTracks(localTracks.map { it.id })
                                }
                            },
                            onDragMoved = { from, to ->
                                if (from in localTracks.indices && to in localTracks.indices && from != to) {
                                    val item = localTracks.removeAt(from)
                                    localTracks.add(to, item)
                                    onReorderTracks(localTracks.map { it.id })
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Dialogue de confirmation de suppression de la playlist
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Supprimer la playlist", color = MusicProTextPrimary) },
            text = {
                Text(
                    "Voulez-vous vraiment supprimer la playlist \"${playlist.name}\" ? Les fichiers audio d'origine ne seront pas supprimés.",
                    color = MusicProTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeletePlaylist()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MusicProError)
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Annuler", color = MusicProTextMuted)
                }
            },
            containerColor = MusicProCardBackground
        )
    }

    // Dialogue de suppression d'un morceau de la playlist
    trackPendingRemoval?.let { track ->
        AlertDialog(
            onDismissRequest = { trackPendingRemoval = null },
            title = { Text("Retirer le morceau", color = MusicProTextPrimary) },
            text = {
                Text(
                    "Retirer \"${track.title}\" de la playlist \"${playlist.name}\" ?",
                    color = MusicProTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRemoveTrack(track.id)
                        localTracks.removeAll { it.id == track.id }
                        trackPendingRemoval = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MusicProError)
                ) {
                    Text("Retirer")
                }
            },
            dismissButton = {
                TextButton(onClick = { trackPendingRemoval = null }) {
                    Text("Annuler", color = MusicProTextMuted)
                }
            },
            containerColor = MusicProCardBackground
        )
    }

    // Dialogue de renommage de la playlist
    if (showRenameDialog) {
        CreatePlaylistDialog(
            initialName = playlist.name,
            initialDescription = playlist.description,
            isEditing = true,
            onDismiss = { showRenameDialog = false },
            onConfirm = { newName, newDesc ->
                onRenamePlaylist(newName, newDesc)
                showRenameDialog = false
            }
        )
    }
}

@Composable
private fun PlaylistHeaderHero(
    playlist: PlaylistSummary,
    trackCount: Int,
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit,
    onAddTracks: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MusicProCardBackground,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(1.dp, MusicProVioletPrimary.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Artwork Collage or Icon
                PlaylistCoverArt(
                    artworkUris = playlist.sampleArtworkUris,
                    modifier = Modifier.size(90.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = playlist.name,
                        color = MusicProTextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (playlist.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = playlist.description,
                            color = MusicProTextSecondary,
                            fontSize = 13.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MusicProSurfaceElevated
                        ) {
                            Text(
                                text = "$trackCount morceau(x)",
                                color = MusicProCyanNeon,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        if (playlist.totalDurationMs > 0) {
                            Text(
                                text = playlist.formatDuration(),
                                color = MusicProTextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onPlayAll,
                    enabled = trackCount > 0,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MusicProVioletPrimary),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("playlist_play_all_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Lire", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onShuffle,
                    enabled = trackCount > 0,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MusicProCyanNeon),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("playlist_shuffle_button")
                ) {
                    Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Aléatoire", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                IconButton(
                    onClick = onAddTracks,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MusicProSurfaceElevated)
                        .border(1.dp, MusicProCyanNeon.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .testTag("playlist_add_tracks_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Ajouter des morceaux", tint = MusicProCyanNeon)
                }
            }
        }
    }
}

@Composable
fun PlaylistCoverArt(
    artworkUris: List<String>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MusicProSurfaceElevated)
            .border(1.dp, MusicProVioletPrimary.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
    ) {
        when {
            artworkUris.size >= 4 -> {
                // 2x2 collage
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(modifier = Modifier.weight(1f)) {
                        AsyncImage(
                            model = artworkUris[0],
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.weight(1f).fillMaxSize()
                        )
                        AsyncImage(
                            model = artworkUris[1],
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.weight(1f).fillMaxSize()
                        )
                    }
                    Row(modifier = Modifier.weight(1f)) {
                        AsyncImage(
                            model = artworkUris[2],
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.weight(1f).fillMaxSize()
                        )
                        AsyncImage(
                            model = artworkUris[3],
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.weight(1f).fillMaxSize()
                        )
                    }
                }
            }
            artworkUris.isNotEmpty() -> {
                AsyncImage(
                    model = artworkUris.first(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                Icon(
                    imageVector = Icons.Default.PlaylistPlay,
                    contentDescription = null,
                    tint = MusicProCyanNeon,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}

@Composable
private fun ReorderableTrackRow(
    index: Int,
    totalCount: Int,
    track: AudioTrackEntity,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDragMoved: (from: Int, to: Int) -> Unit
) {
    var offsetY by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isCurrent) MusicProSurfaceElevated else MusicProSurface,
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(if (isDragging) 10f else 1f)
            .offset { IntOffset(0, offsetY.roundToInt()) }
            .border(
                1.dp,
                if (isCurrent) MusicProCyanNeon.copy(alpha = 0.7f) else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .testTag("reorder_track_row_${track.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Drag Handle with pointer gesture
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .pointerInput(track.id, index) {
                        detectDragGestures(
                            onDragStart = { isDragging = true },
                            onDragEnd = {
                                isDragging = false
                                val rowHeightPx = 56.dp.toPx()
                                val steps = (offsetY / rowHeightPx).roundToInt()
                                val target = (index + steps).coerceIn(0, totalCount - 1)
                                offsetY = 0f
                                if (target != index) {
                                    onDragMoved(index, target)
                                }
                            },
                            onDragCancel = {
                                isDragging = false
                                offsetY = 0f
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                offsetY += dragAmount.y
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = "Glisser pour réordonner",
                    tint = if (isDragging) MusicProCyanNeon else MusicProTextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Index badge (#1, #2)
            Text(
                text = "${index + 1}",
                color = if (isCurrent) MusicProCyanNeon else MusicProTextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(22.dp)
            )

            // Artwork
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MusicProSurfaceElevated),
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
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = if (isCurrent) MusicProCyanNeon else MusicProTextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Track details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    color = if (isCurrent) MusicProCyanNeon else MusicProTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${track.artist} • ${track.formatDuration()}",
                    color = MusicProTextMuted,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Nudge Up / Down quick buttons for high accessibility
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy((-4).dp)
            ) {
                IconButton(
                    onClick = onMoveUp,
                    enabled = index > 0,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Monter",
                        tint = if (index > 0) MusicProTextSecondary else MusicProTextMuted.copy(alpha = 0.3f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onMoveDown,
                    enabled = index < totalCount - 1,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Descendre",
                        tint = if (index < totalCount - 1) MusicProTextSecondary else MusicProTextMuted.copy(alpha = 0.3f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Remove Button
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("remove_track_from_playlist_${track.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Retirer",
                    tint = MusicProTextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
