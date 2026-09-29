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
    queue: List<AudioTrackEntity>, currentIndex: Int, isPlaying: Boolean,
    onPlayTrack: (AudioTrackEntity) -> Unit, onRemove: (Int) -> Unit,
    onMove: (Int, Int) -> Unit, onClear: () -> Unit, onClose: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onClose, containerColor = MusicProCardBackground) {
        Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 18.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text("À suivre", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MusicProTextPrimary); Text(queue.size.toString() + " morceau(s)", fontSize = 12.sp, color = MusicProCyanNeon) }
                TextButton(onClick = onClear, enabled = queue.size > 1) { Text("Vider", color = MusicProTextSecondary) }
            }
            LazyColumn(contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)) {
                itemsIndexed(queue, key = { _, it -> it.id }) { index, track ->
                    val current = index == currentIndex
                    Row(Modifier.fillMaxWidth().background(if (current) MusicProSurfaceElevated else Color.Transparent, RoundedCornerShape(10.dp)).padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(Modifier.size(42.dp), shape = RoundedCornerShape(8.dp), color = MusicProSurface) {
                            if (!track.albumArtUri.isNullOrBlank()) AsyncImage(track.albumArtUri, null, contentScale = ContentScale.Crop) else Icon(Icons.Default.MusicNote, null, tint = MusicProVioletLight)
                        }
                        Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text(track.title, color = if (current) MusicProCyanNeon else MusicProTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1); Text(track.artist, color = MusicProTextSecondary, fontSize = 11.sp, maxLines = 1) }
                        IconButton(onClick = { onPlayTrack(track) }) { Icon(if (current && isPlaying) Icons.Default.MusicNote else Icons.Default.PlayArrow, "Lire", tint = MusicProCyanNeon) }
                        IconButton(onClick = { if (index > currentIndex) onMove(index, index - 1) else if (index < currentIndex) onMove(index, index + 1) }) { Icon(if (index > currentIndex) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, "Déplacer", tint = MusicProTextSecondary) }
                        IconButton(onClick = { onRemove(index) }, enabled = !current) { Icon(Icons.Default.DeleteOutline, "Retirer", tint = MusicProTextSecondary) }
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
            Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Equalizer, null, tint = MusicProCyanNeon); Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text("Égaliseur", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MusicProTextPrimary); Text("Effet appliqué au moteur audio", fontSize = 12.sp, color = MusicProTextSecondary) }; Switch(checked = enabled, onCheckedChange = onEnabledChange) }
            Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(5.dp)) { presets.forEach { item -> FilterChip(selected = preset == item, onClick = { onPresetChange(item) }, label = { Text(item, fontSize = 9.sp) }) } }
            labels.forEachIndexed { index, label -> Column(Modifier.fillMaxWidth().padding(top = 8.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(label, color = MusicProTextPrimary, fontSize = 11.sp); Text((levels.getOrElse(index) { 0 } / 100f).roundToInt().toString() + " dB", color = MusicProCyanNeon, fontSize = 11.sp) }; Slider(value = levels.getOrElse(index) { 0 }.toFloat(), onValueChange = { onBandChange(index, it.roundToInt()) }, valueRange = -1500f..1500f) } }
            OutlinedButton(onClick = onReset, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Icon(Icons.Default.Refresh, null); Spacer(Modifier.width(6.dp)); Text("Réinitialiser") }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun KaraokeSettingsDialog(
    fontSize: Float, activeColor: String, offsetMs: Long,
    onFontSizeChange: (Float) -> Unit, onColorChange: (String) -> Unit,
    onOffsetChange: (Long) -> Unit, onDismiss: () -> Unit
) {
    AlertDialog(onDismissRequest = onDismiss, containerColor = MusicProCardBackground, title = { Text("Mode karaoké", fontWeight = FontWeight.Bold, color = MusicProTextPrimary) }, text = { Column {
        Text("Taille du texte", color = MusicProTextSecondary, fontSize = 12.sp); Slider(value = fontSize, onValueChange = onFontSizeChange, valueRange = 12f..32f)
        Text(fontSize.roundToInt().toString() + " sp", color = MusicProCyanNeon, fontSize = 11.sp)
        Text("Ligne active", color = MusicProTextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { listOf("cyan" to "Cyan", "violet" to "Violet", "white" to "Blanc").forEach { pair -> FilterChip(selected = activeColor == pair.first, onClick = { onColorChange(pair.first) }, label = { Text(pair.second, fontSize = 10.sp) }) } }
        Text("Décalage", color = MusicProTextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp)); Slider(value = offsetMs.toFloat(), onValueChange = { onOffsetChange(it.roundToInt().toLong()) }, valueRange = -5000f..5000f); Text(offsetMs.toString() + " ms", color = MusicProCyanNeon, fontSize = 11.sp)
    } }, confirmButton = { TextButton(onClick = onDismiss) { Text("Fermer", color = MusicProCyanNeon) } })
}

private fun formatDuration(ms: Long): String {
    val total = (ms / 1000L).coerceAtLeast(0L); val h = total / 3600; val m = (total % 3600) / 60; val s = total % 60
    return if (h > 0) h.toString() + ":" + m.toString().padStart(2, '0') + ":" + s.toString().padStart(2, '0') else m.toString() + ":" + s.toString().padStart(2, '0')
}