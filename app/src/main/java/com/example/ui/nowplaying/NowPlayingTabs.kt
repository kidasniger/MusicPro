package com.example.ui.nowplaying

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.AudioTrackEntity
import com.example.lyrics.LyricsData
import com.example.ui.components.glass.GlassMaterials
import com.example.ui.components.glass.GlassSurface
import com.example.ui.components.EqualizerIndicator
import com.example.ui.theme.MusicProCyanNeon
import com.example.ui.theme.MusicProVioletLight
import com.example.ui.theme.MusicProVioletPrimary
import java.text.DateFormat
import java.util.Date
import java.util.Locale

/** Onglets du lecteur (aperçu karaoké, paroles, infos, pilule d'onglets, feuilles vitesse/minuterie) — extraits de NowPlayingScreen.kt (mise à jour 101). */
/**
 * 045 / mise à jour 101 — Onglet Lecture : fenêtre karaoké synchronisée avec lueur douce
 * autour de la ligne active, ou espace d'invitation animé quand le morceau n'a aucune
 * parole. Toute la carte est cliquable et ouvre l'onglet Paroles.
 */
@Composable
internal fun LectureTabContent(
    lyricsData: LyricsData,
    positionMs: Long,
    animationsEnabled: Boolean,
    isPlaying: Boolean,
    vinylRotation: Float,
    onOpenLyrics: () -> Unit
) {
    Surface(
        onClick = onOpenLyrics,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
        modifier = Modifier
            .fillMaxSize()
            .testTag("now_playing_lyrics_preview_card")
    ) {
        if (lyricsData.lines.isEmpty()) {
            LyricsInvitation(
                animationsEnabled = animationsEnabled,
                isPlaying = isPlaying,
                vinylRotation = vinylRotation
            )
        } else {
            KaraokePreview(
                lyricsData = lyricsData,
                positionMs = positionMs
            )
        }
    }
}

/** Fenêtre karaoké de 5 lignes centrée sur la ligne active, lueur douce cyan (mise à jour 101). */
@Composable
private fun KaraokePreview(
    lyricsData: LyricsData,
    positionMs: Long
) {
    val activeIndex = remember(lyricsData, positionMs) {
        lyricsData.findActiveLineIndex(positionMs)
    }
    val windowStart = (activeIndex - 2).coerceAtLeast(0)
    val windowEnd = (windowStart + 5).coerceAtMost(lyricsData.lines.size)

    Box(modifier = Modifier.fillMaxSize()) {
        // Étiquette discrète en haut à droite (045) : confirme que les paroles sont synchronisées.
        Text(
            text = stringResource(R.string.np_lyrics_synced),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 10.dp, end = 14.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            repeat(windowEnd - windowStart) { offset ->
                val index = windowStart + offset
                val isActive = index == activeIndex
                val distance = kotlin.math.abs(index - activeIndex)
                val scale by animateFloatAsState(
                    targetValue = if (isActive) 1.15f else 1f,
                    animationSpec = tween(durationMillis = 180),
                    label = "np_karaoke_scale"
                )
                Text(
                    text = lyricsData.lines[index].text.ifBlank { "♪" },
                    fontSize = 14.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                    color = if (isActive) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    style = if (isActive) {
                        // Lueur douce : halo cyan derrière le texte de la ligne chantée.
                        TextStyle(
                            shadow = Shadow(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
                                offset = Offset.Zero,
                                blurRadius = 20f
                            )
                        )
                    } else {
                        TextStyle.Default
                    },
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            alpha = when (distance) {
                                0 -> 1f
                                1 -> 0.55f
                                else -> 0.30f
                            }
                        }
                        .padding(vertical = 2.dp)
                )
            }
        }
    }
}

/**
 * Espace d'invitation animé quand le morceau n'a aucune parole (mise à jour 101) :
 * mini-disque qui tourne pendant la lecture, barres d'égaliseur vivantes et invitation
 * à rechercher les paroles en ligne — à la place du grand rectangle quasi vide.
 */
@Composable
private fun LyricsInvitation(
    animationsEnabled: Boolean,
    isPlaying: Boolean,
    vinylRotation: Float
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Mini-disque décoratif qui tourne avec la lecture.
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .rotate(if (isPlaying && animationsEnabled) vinylRotation else 0f)
                .background(
                    Brush.radialGradient(
                        0f to MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f),
                        0.55f to MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f),
                        1f to MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                    )
                )
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Album,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                modifier = Modifier.size(20.dp)
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Barres d'égaliseur animées : l'espace reste vivant même sans paroles.
        EqualizerIndicator(
            color = MaterialTheme.colorScheme.primary,
            barWidth = 5.dp,
            maxHeight = 18.dp,
            isPlaying = isPlaying,
            animate = animationsEnabled
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.np_lyrics_empty),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/** 045 — Onglet Paroles : liste synchronisée défilante, ligne active surlignée. */
@Composable
internal fun LyricsTabContent(
    lyricsData: LyricsData,
    positionMs: Long,
    animationsEnabled: Boolean,
    onOpenLyrics: () -> Unit
) {
    if (lyricsData.lines.isEmpty()) {
        Surface(
            onClick = onOpenLyrics,
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Subtitles,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = stringResource(R.string.np_lyrics_empty),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
        return
    }

    val activeIndex = remember(lyricsData, positionMs) {
        lyricsData.findActiveLineIndex(positionMs)
    }
    val listState = rememberLazyListState()
    LaunchedEffect(activeIndex, lyricsData) {
        if (activeIndex < 0) return@LaunchedEffect
        if (animationsEnabled) {
            listState.animateScrollToItem(activeIndex)
        } else {
            listState.scrollToItem(activeIndex)
        }
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            itemsIndexed(lyricsData.lines) { index, line ->
                val isActive = index == activeIndex
                Text(
                    text = line.text.ifBlank { "…" },
                    fontSize = if (isActive) 14.sp else 12.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/** 045 — Onglet Infos : fiche technique du morceau. */
@Composable
internal fun InfoTabContent(track: AudioTrackEntity?, durationMs: Long) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            val format = track?.getAudioFormat() ?: "MP3"
            val addedDate = track?.dateAdded?.takeIf { it > 0 }?.let {
                DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(it * 1000L))
            } ?: "—"
            val sizeLabel = track?.size?.takeIf { it > 0 }?.let {
                String.format(Locale.getDefault(), "%.1f MB", it / 1024f / 1024f)
            } ?: "—"

            InfoRow(stringResource(R.string.np_info_title_label), track?.title ?: "—")
            InfoRow(stringResource(R.string.np_info_artist_label), track?.artist ?: "—")
            InfoRow(stringResource(R.string.np_info_album_label), track?.album ?: "—")
            InfoRow(stringResource(R.string.np_info_format_label), format)
            InfoRow(stringResource(R.string.np_info_duration_label), formatTime(durationMs))
            InfoRow(stringResource(R.string.np_info_size_label), sizeLabel)
            InfoRow(stringResource(R.string.np_info_added_label), addedDate)
            if (!track?.path.isNullOrBlank()) {
                InfoRow(stringResource(R.string.np_info_folder_label), track.folder.ifBlank { track.path })
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(84.dp)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

/** 045 — Pilule d'onglets en verre : Lecture (vinyle), Paroles, Réglages audio, Infos. */
@Composable
internal fun PlayerTabsPill(
    selected: PlayerTab,
    onSelect: (PlayerTab) -> Unit
) {
    GlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(26.dp),
        material = GlassMaterials.regular()
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TabItem(PlayerTab.LECTURE, Icons.Default.Album, R.string.np_tab_playback, selected, onSelect)
            TabItem(PlayerTab.PAROLES, Icons.Default.Subtitles, R.string.np_tab_lyrics, selected, onSelect)
            TabItem(PlayerTab.EGALISEUR, Icons.Default.Equalizer, R.string.np_tab_equalizer, selected, onSelect)
            TabItem(PlayerTab.INFOS, Icons.Default.Info, R.string.np_tab_info, selected, onSelect)
        }
    }
}

@Composable
private fun RowScope.TabItem(
    tab: PlayerTab,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    labelRes: Int,
    selected: PlayerTab,
    onSelect: (PlayerTab) -> Unit
) {
    val isSelected = tab == selected
    Surface(
        onClick = { onSelect(tab) },
        shape = RoundedCornerShape(22.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.22f) else Color.Transparent,
        modifier = Modifier
            .weight(1f)
            .fillMaxSize()
            .padding(vertical = 2.dp)
            .testTag("np_tab_${tab.name.lowercase()}")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = stringResource(labelRes),
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(17.dp)
            )
            Text(
                text = stringResource(labelRes),
                fontSize = 9.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

/** Contenu de la feuille de vitesse de lecture. */
@Composable
internal fun SpeedSheetContent(
    playbackSpeed: Float,
    onSetSpeed: (Float) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.np_speed_title),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            TextButton(onClick = onClose, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(stringResource(R.string.np_close), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        val allSpeeds = listOf(0.5f, 0.8f, 1.0f, 1.25f, 1.5f, 2.0f)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            allSpeeds.forEach { speed ->
                val isSelected = playbackSpeed == speed
                Surface(
                    onClick = { onSetSpeed(speed) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "${speed}x",
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

/** 050 — Feuille de la minuterie de sommeil : 5, 15, 30, 45, 60 min ou fin du morceau. */
@Composable
internal fun SleepTimerSheetContent(
    active: Boolean,
    endOfTrack: Boolean,
    remainingMs: Long,
    selectedMinutes: Int,
    onSetMinutes: (Int) -> Unit,
    onSetEndOfTrack: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Text(
                text = stringResource(R.string.np_menu_sleep_timer),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 10.dp)
            )
            if (active) {
                Text(
                    text = formatTime(remainingMs) + stringResource(R.string.np_sleep_remaining_suffix),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val minuteOptions = listOf(5, 15, 30, 45, 60)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ChipOption(
                label = stringResource(R.string.np_sleep_off),
                selected = !active && !endOfTrack,
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            )
            minuteOptions.forEach { minutes ->
                ChipOption(
                    label = stringResource(R.string.np_sleep_minutes, minutes),
                    selected = active && !endOfTrack && selectedMinutes == minutes,
                    onClick = { onSetMinutes(minutes) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        ChipOption(
            label = stringResource(R.string.np_sleep_end_of_track),
            selected = endOfTrack,
            onClick = onSetEndOfTrack,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ChipOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
        border = BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)
        ),
        modifier = modifier
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 9.dp)
        )
    }
}
