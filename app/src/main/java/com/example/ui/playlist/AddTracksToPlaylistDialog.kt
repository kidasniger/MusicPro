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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.local.AudioTrackEntity
import com.example.ui.theme.MusicProCardBackground
import com.example.ui.theme.MusicProCyanNeon
import com.example.ui.theme.MusicProGreenEmerald
import com.example.ui.theme.MusicProSurface
import com.example.ui.theme.MusicProSurfaceElevated
import com.example.ui.theme.MusicProTextMuted
import com.example.ui.theme.MusicProTextPrimary
import com.example.ui.theme.MusicProTextSecondary
import com.example.ui.theme.MusicProVioletGlow
import com.example.ui.theme.MusicProVioletLight
import com.example.ui.theme.MusicProVioletPrimary

@Composable
fun AddTracksToPlaylistDialog(
    allTracks: List<AudioTrackEntity>,
    existingTrackIds: Set<Long>,
    playlistName: String,
    onDismiss: () -> Unit,
    onAddTracks: (List<Long>) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val selectedTrackIds = remember { mutableStateListOf<Long>() }

    val filteredTracks = remember(allTracks, searchQuery) {
        if (searchQuery.isBlank()) {
            allTracks
        } else {
            val q = searchQuery.trim().lowercase()
            allTracks.filter {
                it.title.lowercase().contains(q) || it.artist.lowercase().contains(q) || it.album.lowercase().contains(q)
            }
        }
    }

    val availableTracks = remember(filteredTracks, existingTrackIds) {
        filteredTracks.filter { !existingTrackIds.contains(it.id) }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .shadow(24.dp, RoundedCornerShape(24.dp), spotColor = MusicProVioletGlow)
                .border(1.dp, MusicProCyanNeon.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                .testTag("add_tracks_to_playlist_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MusicProCardBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Ajouter à la playlist",
                            color = MusicProTextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = playlistName,
                            color = MusicProCyanNeon,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_add_tracks_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Fermer",
                            tint = MusicProTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Box
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Rechercher un morceau ou artiste...", color = MusicProTextMuted) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = MusicProCyanNeon)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Effacer", tint = MusicProTextMuted)
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MusicProTextPrimary,
                        unfocusedTextColor = MusicProTextPrimary,
                        focusedBorderColor = MusicProCyanNeon,
                        unfocusedBorderColor = MusicProVioletPrimary.copy(alpha = 0.4f),
                        cursorColor = MusicProCyanNeon,
                        focusedContainerColor = MusicProSurfaceElevated,
                        unfocusedContainerColor = MusicProSurfaceElevated
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_tracks_for_playlist")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Action Bar (Select All / Deselect All + Counter)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${selectedTrackIds.size} sélectionné(s) • ${availableTracks.size} disponible(s)",
                        color = MusicProTextMuted,
                        fontSize = 12.sp
                    )

                    Row {
                        TextButton(
                            onClick = {
                                selectedTrackIds.clear()
                                selectedTrackIds.addAll(availableTracks.map { it.id })
                            }
                        ) {
                            Text("Tout", color = MusicProVioletLight, fontSize = 12.sp)
                        }

                        if (selectedTrackIds.isNotEmpty()) {
                            TextButton(onClick = { selectedTrackIds.clear() }) {
                                Text("Aucun", color = MusicProTextMuted, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Track list
                if (filteredTracks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aucun morceau trouvé",
                            color = MusicProTextMuted,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredTracks, key = { it.id }) { track ->
                            val isAlreadyInPlaylist = existingTrackIds.contains(track.id)
                            val isSelected = selectedTrackIds.contains(track.id)

                            TrackSelectionRow(
                                track = track,
                                isAlreadyInPlaylist = isAlreadyInPlaylist,
                                isSelected = isSelected,
                                onToggle = {
                                    if (!isAlreadyInPlaylist) {
                                        if (isSelected) {
                                            selectedTrackIds.remove(track.id)
                                        } else {
                                            selectedTrackIds.add(track.id)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MusicProTextSecondary)
                    ) {
                        Text("Annuler")
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = {
                            if (selectedTrackIds.isNotEmpty()) {
                                onAddTracks(selectedTrackIds.toList())
                            }
                        },
                        enabled = selectedTrackIds.isNotEmpty(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MusicProVioletPrimary,
                            disabledContainerColor = MusicProSurfaceElevated
                        ),
                        modifier = Modifier.testTag("confirm_add_tracks_button")
                    ) {
                        Text(
                            text = if (selectedTrackIds.isEmpty()) "Sélectionnez des morceaux" else "Ajouter (${selectedTrackIds.size})"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackSelectionRow(
    track: AudioTrackEntity,
    isAlreadyInPlaylist: Boolean,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        onClick = onToggle,
        enabled = !isAlreadyInPlaylist,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MusicProSurfaceElevated else MusicProSurface,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isSelected) MusicProCyanNeon.copy(alpha = 0.6f) else MusicProSurfaceElevated,
                RoundedCornerShape(12.dp)
            )
            .testTag("track_select_row_${track.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Album art
            Box(
                modifier = Modifier
                    .size(44.dp)
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
                        tint = MusicProCyanNeon,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Title & Artist
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    color = if (isAlreadyInPlaylist) MusicProTextMuted else MusicProTextPrimary,
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

            // Status or Checkbox
            if (isAlreadyInPlaylist) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MusicProSurfaceElevated,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Text(
                        text = "Déjà ajouté",
                        color = MusicProGreenEmerald,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            } else {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MusicProCyanNeon,
                        checkmarkColor = MusicProCardBackground,
                        uncheckedColor = MusicProTextMuted
                    )
                )
            }
        }
    }
}
