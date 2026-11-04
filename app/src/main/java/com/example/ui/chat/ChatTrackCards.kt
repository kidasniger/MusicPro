package com.example.ui.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.AudioTrackEntity
import com.example.ui.artwork.artworkGridRequest
import com.example.ui.theme.MusicProTerracotta

/** Normalise pour le rapprochement : minuscules, ponctuation et espaces réduits. */
internal fun normalizeForMatch(value: String): String = value
    .lowercase()
    .replace(Regex("[\\p{Punct}\\p{IsPunctuation}]"), " ")
    .replace(Regex("\\s+"), " ")
    .trim()

/**
 * Retrouve les morceaux de [tracks] dont le TITRE apparaît dans le texte de la
 * réponse. Les titres les plus longs sont essayés d'abord pour que
 * « Love Song (Live) » gagne sur « Love Song » ; au plus [max] cartes.
 */
fun findMentionedTracks(text: String, tracks: List<AudioTrackEntity>, max: Int = 6): List<AudioTrackEntity> {
    if (text.isBlank() || tracks.isEmpty()) return emptyList()
    val normalizedText = normalizeForMatch(text)
    if (normalizedText.isBlank()) return emptyList()
    val found = ArrayList<AudioTrackEntity>(max)
    val seen = HashSet<Long>()
    tracks
        .sortedByDescending { it.title.length }
        .forEach { track ->
            val title = normalizeForMatch(track.title)
            if (title.length >= 3 && normalizedText.contains(title) && seen.add(track.id)) {
                found += track
                if (found.size >= max) return found
            }
        }
    return found
}

@Composable
fun ChatTrackCards(
    tracks: List<AudioTrackEntity>,
    onPlayTrack: (AudioTrackEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    if (tracks.isEmpty()) return
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(tracks, key = { it.id }) { track ->
            ChatTrackCard(track = track, onPlayTrack = onPlayTrack)
        }
    }
}

@Composable
private fun ChatTrackCard(
    track: AudioTrackEntity,
    onPlayTrack: (AudioTrackEntity) -> Unit
) {
    val context = LocalContext.current
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .width(250.dp)
            .clickable { onPlayTrack(track) }
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = context.artworkGridRequest(track.albumArtUri ?: track.contentUri),
                contentDescription = null, // décoratif : titre et artiste sont lus (082)
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(46.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp)
            ) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = track.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            FilledIconButton(
                onClick = { onPlayTrack(track) },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MusicProTerracotta,
                    contentColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = stringResource(R.string.chat_track_play, track.title)
                )
            }
        }
    }
}

