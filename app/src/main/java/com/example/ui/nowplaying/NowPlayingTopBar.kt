package com.example.ui.nowplaying

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SpeakerGroup
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.AudioTrackEntity
import com.example.ui.theme.MusicProCyanNeon

/** Barre supérieure et menu du morceau du lecteur — extraits de NowPlayingScreen.kt (mise à jour 101). */
/** Barre supérieure : réduire (↓), menu ⋯ (minuterie, vitesse, karaoké, playlist, partager), appareils. */
@Composable
internal fun PlayerTopBar(
    onBack: () -> Unit,
    onOpenDevices: () -> Unit,
    onOpenOverflow: () -> Unit,
    showOverflowMenu: Boolean,
    onDismissOverflow: () -> Unit,
    onOpenSleepSheet: () -> Unit,
    onOpenSpeedSheet: () -> Unit,
    onOpenKaraoke: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onShare: () -> Unit,
    sleepActive: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.30f))
                .testTag("now_playing_back_button")
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = stringResource(R.string.np_minimize),
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Box {
            IconButton(
                onClick = onOpenOverflow,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.30f))
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(R.string.np_more_options),
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            DropdownMenu(
                expanded = showOverflowMenu,
                onDismissRequest = onDismissOverflow
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.np_menu_sleep_timer)) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Timer,
                            null,
                            tint = if (sleepActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    onClick = {
                        onDismissOverflow()
                        onOpenSleepSheet()
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.np_menu_speed)) },
                    leadingIcon = {
                        Icon(Icons.Default.Speed, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    },
                    onClick = {
                        onDismissOverflow()
                        onOpenSpeedSheet()
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.np_menu_karaoke)) },
                    leadingIcon = {
                        Icon(Icons.Default.Subtitles, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    },
                    onClick = {
                        onDismissOverflow()
                        onOpenKaraoke()
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.np_menu_add_to_playlist)) },
                    leadingIcon = {
                        Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    },
                    onClick = {
                        onDismissOverflow()
                        onAddToPlaylist()
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.np_menu_share)) },
                    leadingIcon = {
                        Icon(Icons.Default.Share, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    },
                    onClick = {
                        onDismissOverflow()
                        onShare()
                    }
                )
            }
        }

        Spacer(modifier = Modifier.width(4.dp))

        // 049 — Icône « appareils » en haut à droite.
        IconButton(
            onClick = onOpenDevices,
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.30f))
                .testTag("now_playing_devices_button")
        ) {
            Icon(
                imageVector = Icons.Default.SpeakerGroup,
                contentDescription = stringResource(R.string.np_output_title),
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/** 047 — Menu d'appui long sur la pochette : playlist, album, artiste, partager. */
@Composable
internal fun TrackMenuSheetContent(
    track: AudioTrackEntity?,
    onAddToPlaylist: () -> Unit,
    onOpenAlbum: () -> Unit,
    onOpenArtist: () -> Unit,
    onShare: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Text(
            text = track?.title ?: stringResource(R.string.np_no_track),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = track?.artist ?: stringResource(R.string.np_player_name),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        TrackMenuRow(Icons.Default.Add, stringResource(R.string.np_menu_add_to_playlist), onAddToPlaylist)
        TrackMenuRow(Icons.Default.Album, stringResource(R.string.np_menu_go_to_album), onOpenAlbum)
        TrackMenuRow(Icons.Default.Person, stringResource(R.string.np_menu_go_to_artist), onOpenArtist)
        TrackMenuRow(Icons.Default.Share, stringResource(R.string.np_menu_share), onShare)

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun TrackMenuRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}
