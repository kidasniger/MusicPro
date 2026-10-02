package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.AudioTrackEntity
import com.example.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun FavoritesScreen(
    tracks: List<AudioTrackEntity>,
    favorites: Set<Long>,
    currentTrack: AudioTrackEntity?,
    isPlaying: Boolean,
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit,
    onTrackClick: (AudioTrackEntity) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onAddToQueue: (AudioTrackEntity) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val favoriteTracks = tracks.filter { favorites.contains(it.id) }
    val totalDuration = favoriteTracks.sumOf { it.duration }
    Column(modifier.fillMaxWidth().background(MusicProBackground).navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour", tint = MusicProTextPrimary) }
            Column(Modifier.weight(1f)) {
                Text("❤️ Favoris", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MusicProTextPrimary)
                Text(favoriteTracks.size.toString() + " morceau(s) • " + formatDuration(totalDuration), fontSize = 12.sp, color = MusicProCyanNeon)
            }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onPlayAll, enabled = favoriteTracks.isNotEmpty(), modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Default.PlayArrow, null); Spacer(Modifier.width(6.dp)); Text("Lire tout")
            }
            OutlinedButton(onClick = onShuffle, enabled = favoriteTracks.isNotEmpty(), modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Default.Shuffle, null); Spacer(Modifier.width(6.dp)); Text("Aléatoire")
            }
        }
        if (favoriteTracks.isEmpty()) {
            Column(Modifier.fillMaxWidth().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.FavoriteBorder, null, tint = MusicProVioletLight, modifier = Modifier.size(56.dp))
                Text("Aucun favori", color = MusicProTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 10.dp))
                Text("Touchez le cœur sur un morceau pour le retrouver ici.", color = MusicProTextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp))
            }
        } else {
            LazyColumn(Modifier.fillMaxWidth(), contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)) {
                itemsIndexed(favoriteTracks, key = { _, it -> it.id }) { index, track ->
                    Surface(onClick = { onTrackClick(track) }, color = if (currentTrack?.id == track.id) MusicProSurfaceElevated else Color.Transparent, modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(if (isPlaying && currentTrack?.id == track.id) "▶" else (index + 1).toString(), color = MusicProCyanNeon, fontSize = 11.sp, modifier = Modifier.width(24.dp))
                            Surface(Modifier.size(46.dp), shape = RoundedCornerShape(9.dp), color = MusicProSurface) {
                                if (!track.albumArtUri.isNullOrBlank()) AsyncImage(track.albumArtUri, null, contentScale = ContentScale.Crop) else Icon(Icons.Default.MusicNote, null, tint = MusicProVioletLight)
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(track.title, color = if (currentTrack?.id == track.id) MusicProCyanNeon else MusicProTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                Text(track.artist + " • " + track.formatDuration(), color = MusicProTextSecondary, fontSize = 11.sp, maxLines = 1)
                            }
                            IconButton(onClick = { onAddToQueue(track) }) { Icon(Icons.AutoMirrored.Filled.QueueMusic, "File", tint = MusicProTextSecondary) }
                            IconButton(onClick = { onToggleFavorite(track.id) }) { Icon(Icons.Default.Favorite, "Retirer", tint = Color(0xFFFF4081)) }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueSheet(
    queue: List<AudioTrackEntity>,
    currentIndex: Int,
    isPlaying: Boolean,
    onPlayTrack: (AudioTrackEntity) -> Unit,
    onRemove: (Int) -> Unit,
    onMove: (Int, Int) -> Unit,
    onClear: () -> Unit,
    onClose: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onClose, containerColor = MusicProCardBackground) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 8.dp)
        ) {
            val safeCurrentIndex = currentIndex.coerceIn(0, (queue.size - 1).coerceAtLeast(0))
            val startIndex = (safeCurrentIndex - 4).coerceAtLeast(0)
            val endIndex = (safeCurrentIndex + 8).coerceAtMost(queue.lastIndex)
            val visibleQueue = if (queue.isEmpty()) emptyList() else queue.subList(startIndex, endIndex + 1)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("À suivre", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MusicProTextPrimary)
                    Text(
                        if (queue.isEmpty()) "Aucun morceau"
                        else "Position " + (safeCurrentIndex + 1) + "/" + queue.size,
                        fontSize = 12.sp,
                        color = MusicProCyanNeon
                    )
                }
                TextButton(onClick = onClear, enabled = queue.size > 1) {
                    Text("Vider", color = MusicProTextSecondary)
                }
            }

            if (queue.isEmpty()) {
                Column(
                    Modifier.fillMaxWidth().padding(vertical = 42.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.QueueMusic,
                        contentDescription = null,
                        tint = MusicProVioletLight,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        "Aucune file de lecture",
                        color = MusicProTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                    Text(
                        "Les morceaux suivants apparaîtront ici.",
                        color = MusicProTextSecondary,
                        fontSize = 12.sp
                    )
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)) {
                    itemsIndexed(
                        visibleQueue,
                        key = { localIndex, item -> (startIndex + localIndex).toString() + "_" + item.id.toString() }
                    ) { localIndex, track ->
                        val index = startIndex + localIndex
                        val isCurrent = index == safeCurrentIndex

                        Row(
                            Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isCurrent) MusicProSurfaceElevated else Color.Transparent,
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                Modifier.size(42.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = MusicProSurface
                            ) {
                                if (!track.albumArtUri.isNullOrBlank()) {
                                    AsyncImage(track.albumArtUri, null, contentScale = ContentScale.Crop)
                                } else {
                                    Icon(Icons.Default.MusicNote, null, tint = MusicProVioletLight)
                                }
                            }

                            Spacer(Modifier.width(10.dp))

                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = if (isCurrent && isPlaying) "▶ " + track.title else track.title,
                                    color = if (isCurrent) MusicProCyanNeon else MusicProTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                                    maxLines = 1
                                )
                                Text(
                                    track.artist,
                                    color = MusicProTextSecondary,
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                            }

                            if (!isCurrent) {
                                IconButton(onClick = { onPlayTrack(track) }) {
                                    Icon(Icons.Default.PlayArrow, "Lire maintenant", tint = MusicProCyanNeon)
                                }
                                IconButton(
                                    onClick = {
                                        if (index > safeCurrentIndex) onMove(index, index - 1)
                                        else onMove(index, index + 1)
                                    },
                                    enabled = queue.size > 1
                                ) {
                                    Icon(
                                        if (index > safeCurrentIndex) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        "Déplacer",
                                        tint = MusicProTextSecondary
                                    )
                                }
                                IconButton(onClick = { onRemove(index) }) {
                                    Icon(Icons.Default.DeleteOutline, "Retirer", tint = MusicProTextSecondary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerSheet(
    enabled: Boolean, preset: String, levels: List<Int>,
    onEnabledChange: (Boolean) -> Unit, onPresetChange: (String) -> Unit,
    onBandChange: (Int, Int) -> Unit, onReset: () -> Unit, onClose: () -> Unit
) {
    val presets = listOf("Flat", "Bass Boost", "Vocal", "Rock", "Classical", "Hip-Hop")
    val labels = listOf("60 Hz", "230 Hz", "910 Hz", "3.6 kHz", "14 kHz")
    ModalBottomSheet(onDismissRequest = onClose, containerColor = MusicProCardBackground) {
        Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Equalizer, null, tint = MusicProCyanNeon)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("Égaliseur", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MusicProTextPrimary)
                    Text(
                        if (enabled) "Effet activé" else "Effet désactivé",
                        fontSize = 12.sp,
                        color = if (enabled) MusicProCyanNeon else MusicProTextSecondary
                    )
                }
                OutlinedButton(onClick = { onEnabledChange(!enabled) }, shape = RoundedCornerShape(12.dp)) {
                    Text(if (enabled) "Désactiver" else "Activer", fontSize = 12.sp)
                }
            }
            Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(5.dp)) { presets.forEach { item -> FilterChip(selected = preset == item, onClick = { onPresetChange(item) }, label = { Text(item, fontSize = 9.sp) }) } }
            labels.forEachIndexed { index, label ->
                val level = levels.getOrElse(index) { 0 }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(label, color = MusicProTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text((level / 100f).roundToInt().toString() + " dB", color = MusicProCyanNeon, fontSize = 11.sp)
                    }
                    IconButton(
                        onClick = { onBandChange(index, (level - 100).coerceIn(-1500, 1500)) },
                        modifier = Modifier.size(44.dp)
                    ) { Text("−", color = MusicProTextPrimary, fontSize = 22.sp) }
                    IconButton(
                        onClick = { onBandChange(index, (level + 100).coerceIn(-1500, 1500)) },
                        modifier = Modifier.size(44.dp)
                    ) { Text("+", color = MusicProTextPrimary, fontSize = 20.sp) }
                }
            }
            OutlinedButton(onClick = onReset, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Icon(Icons.Default.Refresh, null); Spacer(Modifier.width(6.dp)); Text("Tout rétablir") }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun KaraokeSettingsDialog(
    fontSize: Float,
    activeColor: String,
    offsetMs: Long,
    onFontSizeChange: (Float) -> Unit,
    onColorChange: (String) -> Unit,
    onOffsetChange: (Long) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(onDismissRequest = onDismiss, containerColor = MusicProCardBackground, title = { Text("Mode karaoké", fontWeight = FontWeight.Bold, color = MusicProTextPrimary) }, text = { Column {
        Text("Taille du texte", color = MusicProTextSecondary, fontSize = 12.sp)
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = { onFontSizeChange((fontSize - 1f).coerceIn(12f, 32f)) },
                enabled = fontSize > 12f,
                modifier = Modifier.size(48.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("−", fontSize = 22.sp)
            }
            Text(
                fontSize.roundToInt().toString() + " sp",
                color = MusicProCyanNeon,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            OutlinedButton(
                onClick = { onFontSizeChange((fontSize + 1f).coerceIn(12f, 32f)) },
                enabled = fontSize < 32f,
                modifier = Modifier.size(48.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("+", fontSize = 20.sp)
            }
        }
        Text("Ligne active", color = MusicProTextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { listOf("cyan" to "Cyan", "violet" to "Violet", "white" to "Blanc").forEach { pair -> FilterChip(selected = activeColor == pair.first, onClick = { onColorChange(pair.first) }, label = { Text(pair.second, fontSize = 10.sp) }) } }
        Text("Décalage", color = MusicProTextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = { onOffsetChange((offsetMs - 50L).coerceIn(-5000L, 5000L)) },
                enabled = offsetMs > -5000L,
                modifier = Modifier.size(48.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("−", fontSize = 22.sp)
            }
            Text(
                offsetMs.toString() + " ms",
                color = MusicProCyanNeon,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            OutlinedButton(
                onClick = { onOffsetChange((offsetMs + 50L).coerceIn(-5000L, 5000L)) },
                enabled = offsetMs < 5000L,
                modifier = Modifier.size(48.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("+", fontSize = 20.sp)
            }
        }

        OutlinedButton(
            onClick = onReset,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Refresh, null)
            Spacer(Modifier.width(6.dp))
            Text("Tout rétablir")
        }
    } }, confirmButton = { TextButton(onClick = onDismiss) { Text("Fermer", color = MusicProCyanNeon) } })
}

private fun formatDuration(ms: Long): String {
    val total = (ms / 1000L).coerceAtLeast(0L); val h = total / 3600; val m = (total % 3600) / 60; val s = total % 60
    return if (h > 0) h.toString() + ":" + m.toString().padStart(2, '0') + ":" + s.toString().padStart(2, '0') else m.toString() + ":" + s.toString().padStart(2, '0')
}