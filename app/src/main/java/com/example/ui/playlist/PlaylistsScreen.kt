package com.example.ui.playlist

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PlaylistSummary
import com.example.ui.theme.MusicProCardBackground
import com.example.ui.theme.MusicProCyanNeon
import com.example.ui.theme.MusicProError
import com.example.ui.theme.MusicProPrimaryGradient
import com.example.ui.theme.MusicProSurface
import com.example.ui.theme.MusicProSurfaceElevated
import com.example.ui.theme.MusicProTextMuted
import com.example.ui.theme.MusicProTextPrimary
import com.example.ui.theme.MusicProTextSecondary
import com.example.ui.theme.MusicProVioletGlow
import com.example.ui.theme.MusicProVioletLight
import com.example.ui.theme.MusicProVioletPrimary

@Composable
fun PlaylistsScreen(
    playlists: List<PlaylistSummary>,
    onPlaylistClick: (PlaylistSummary) -> Unit,
    onPlayPlaylistDirectly: (PlaylistSummary) -> Unit,
    onCreatePlaylist: (name: String, description: String) -> Unit,
    onRenamePlaylist: (playlistId: Long, newName: String, newDesc: String) -> Unit,
    onDeletePlaylist: (playlistId: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var playlistToRename by remember { mutableStateOf<PlaylistSummary?>(null) }
    var playlistToDelete by remember { mutableStateOf<PlaylistSummary?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Playlists",
                        color = MusicProTextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${playlists.size} playlist(s) personnalisée(s)",
                        color = MusicProTextMuted,
                        fontSize = 13.sp
                    )
                }

                Button(
                    onClick = { showCreateDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MusicProVioletPrimary),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("create_playlist_top_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Nouvelle", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (playlists.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
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
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.QueueMusic,
                                    contentDescription = null,
                                    tint = MusicProCyanNeon,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }

                        Text(
                            text = "Aucune playlist créée",
                            color = MusicProTextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Créez vos propres listes de lecture thématiques et organisez vos morceaux préférés avec réordonnancement par glisser-déposer.",
                            color = MusicProTextMuted,
                            fontSize = 14.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { showCreateDialog = true },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MusicProVioletPrimary),
                            modifier = Modifier
                                .shadow(12.dp, RoundedCornerShape(14.dp), spotColor = MusicProVioletGlow)
                                .testTag("empty_playlists_create_button")
                        ) {
                            Icon(Icons.Default.PlaylistAdd, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Créer ma première playlist", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(playlists, key = { it.id }) { playlist ->
                        PlaylistCardItem(
                            playlist = playlist,
                            onClick = { onPlaylistClick(playlist) },
                            onPlayDirectly = { onPlayPlaylistDirectly(playlist) },
                            onRename = { playlistToRename = playlist },
                            onDelete = { playlistToDelete = playlist }
                        )
                    }

                    // Spacer for navigation bar and mini player
                    item {
                        Spacer(modifier = Modifier.height(90.dp))
                    }
                }
            }
        }

        // Floating Action Button
        if (playlists.isNotEmpty()) {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MusicProVioletPrimary,
                contentColor = MusicProTextPrimary,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 90.dp)
                    .shadow(16.dp, CircleShape, spotColor = MusicProVioletGlow)
                    .testTag("create_playlist_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Créer une playlist")
            }
        }
    }

    // Dialog Créer une playlist
    if (showCreateDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, desc ->
                onCreatePlaylist(name, desc)
                showCreateDialog = false
            }
        )
    }

    // Dialog Renommer une playlist
    playlistToRename?.let { playlist ->
        CreatePlaylistDialog(
            initialName = playlist.name,
            initialDescription = playlist.description,
            isEditing = true,
            onDismiss = { playlistToRename = null },
            onConfirm = { newName, newDesc ->
                onRenamePlaylist(playlist.id, newName, newDesc)
                playlistToRename = null
            }
        )
    }

    // Dialog Confirmation de suppression
    playlistToDelete?.let { playlist ->
        AlertDialog(
            onDismissRequest = { playlistToDelete = null },
            title = { Text("Supprimer \"${playlist.name}\" ?", color = MusicProTextPrimary) },
            text = {
                Text(
                    "Cette action supprimera définitivement la playlist. Vos fichiers audio d'origine restent inchangés.",
                    color = MusicProTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeletePlaylist(playlist.id)
                        playlistToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MusicProError)
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { playlistToDelete = null }) {
                    Text("Annuler", color = MusicProTextMuted)
                }
            },
            containerColor = MusicProCardBackground
        )
    }
}

@Composable
private fun PlaylistCardItem(
    playlist: PlaylistSummary,
    onClick: () -> Unit,
    onPlayDirectly: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MusicProCardBackground),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MusicProSurfaceElevated, RoundedCornerShape(16.dp))
            .testTag("playlist_card_${playlist.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Artwork Collage or Icon
            PlaylistCoverArt(
                artworkUris = playlist.sampleArtworkUris,
                modifier = Modifier.size(68.dp)
            )

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playlist.name,
                    color = MusicProTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (playlist.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = playlist.description,
                        color = MusicProTextSecondary,
                        fontSize = 12.sp,
                        maxLines = 1,
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
                            text = "${playlist.trackCount} morceau(x)",
                            color = MusicProCyanNeon,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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

            // Quick Play Button
            if (playlist.trackCount > 0) {
                IconButton(
                    onClick = onPlayDirectly,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MusicProSurfaceElevated)
                        .border(1.dp, MusicProVioletPrimary.copy(alpha = 0.5f), CircleShape)
                        .testTag("playlist_play_direct_${playlist.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Lire la playlist",
                        tint = MusicProCyanNeon,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Options Menu
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("playlist_menu_${playlist.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = MusicProTextMuted
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(MusicProCardBackground)
                ) {
                    DropdownMenuItem(
                        text = { Text("Renommer", color = MusicProTextPrimary) },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = MusicProTextSecondary)
                        },
                        onClick = {
                            showMenu = false
                            onRename()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Supprimer", color = MusicProError) },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = MusicProError)
                        },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}
