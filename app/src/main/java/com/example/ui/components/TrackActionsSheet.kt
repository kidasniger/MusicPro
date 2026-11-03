package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.AudioTrackEntity
import com.example.ui.theme.MusicProCyanNeon

/**
 * Mise à jour 059 — feuille du bas ouverte par le menu ⋯ d'une ligne de morceau :
 * pochette + titre + artiste, puis neuf actions (zone tactile de 56 dp chacune) :
 * Lire ensuite, Ajouter à la file, Ajouter à une playlist, J'aime,
 * Aller à l'album, Aller à l'artiste, Paroles, Partager,
 * Supprimer de la bibliothèque.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackActionsSheet(
    track: AudioTrackEntity,
    isFavorite: Boolean,
    onDismiss: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onToggleFavorite: () -> Unit,
    onGoToAlbum: () -> Unit,
    onGoToArtist: () -> Unit,
    onOpenLyrics: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .padding(bottom = 16.dp)
        ) {
            // En-tête : pochette, titre, artiste.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (!track.albumArtUri.isNullOrBlank()) {
                        AsyncImage(
                            model = track.albumArtUri,
                            contentDescription = track.album,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(52.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = MusicProCyanNeon
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = track.artist,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            TrackActionRow(
                icon = Icons.AutoMirrored.Filled.PlaylistAdd,
                label = stringResource(R.string.track_action_play_next),
                onClick = { onDismiss(); onPlayNext() }
            )
            TrackActionRow(
                icon = Icons.AutoMirrored.Filled.QueueMusic,
                label = stringResource(R.string.track_action_add_queue),
                onClick = { onDismiss(); onAddToQueue() }
            )
            TrackActionRow(
                icon = Icons.Filled.LibraryAdd,
                label = stringResource(R.string.track_action_add_playlist),
                onClick = { onDismiss(); onAddToPlaylist() }
            )
            TrackActionRow(
                icon = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                label = stringResource(R.string.track_action_like),
                tint = if (isFavorite) Color(0xFFFF4081) else MaterialTheme.colorScheme.onBackground,
                onClick = { onDismiss(); onToggleFavorite() }
            )
            TrackActionRow(
                icon = Icons.Default.Album,
                label = stringResource(R.string.track_action_album),
                onClick = { onDismiss(); onGoToAlbum() }
            )
            TrackActionRow(
                icon = Icons.Default.Person,
                label = stringResource(R.string.track_action_artist),
                onClick = { onDismiss(); onGoToArtist() }
            )
            TrackActionRow(
                icon = Icons.Default.Info,
                label = stringResource(R.string.track_action_lyrics),
                onClick = { onDismiss(); onOpenLyrics() }
            )
            TrackActionRow(
                icon = Icons.Default.Share,
                label = stringResource(R.string.track_action_share),
                onClick = { onDismiss(); onShare() }
            )
            TrackActionRow(
                icon = Icons.Default.DeleteOutline,
                label = stringResource(R.string.track_action_delete),
                tint = Color(0xFFE57373),
                onClick = { onDismiss(); onDelete() }
            )
        }
    }
}

/** Une action : icône + libellé, zone tactile garantie à 56 dp de haut. */
@Composable
private fun TrackActionRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onBackground
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            color = tint,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
