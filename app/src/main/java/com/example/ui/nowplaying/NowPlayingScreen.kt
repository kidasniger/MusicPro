package com.example.ui.nowplaying

import android.content.Context
import android.media.AudioManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import com.example.lyrics.LyricsData
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import coil.compose.AsyncImage
import com.example.data.local.AudioTrackEntity
import com.example.ui.theme.MusicProBackground
import com.example.ui.theme.MusicProCardBackground
import com.example.ui.theme.MusicProCyanGlow
import com.example.ui.theme.MusicProCyanLight
import com.example.ui.theme.MusicProCyanNeon
import com.example.ui.theme.MusicProCyanVibrant
import com.example.ui.theme.MusicProFavorite
import com.example.ui.theme.MusicProGreenEmerald
import com.example.ui.theme.MusicProPrimaryGradient
import com.example.ui.theme.MusicProSuccess
import com.example.ui.theme.MusicProSurface
import com.example.ui.theme.MusicProSurfaceElevated
import com.example.ui.theme.MusicProSurfaceVariant
import com.example.ui.theme.MusicProTextMuted
import com.example.ui.theme.MusicProTextPrimary
import com.example.ui.theme.MusicProTextSecondary
import com.example.ui.theme.MusicProVioletGlow
import com.example.ui.theme.MusicProVioletLight
import com.example.ui.theme.MusicProVioletPrimary
import java.util.Locale

/**
 * Écran de lecture Now Playing inspiré directement du design cyberpunk/synthwave de MusicPro.html :
 * - Disque vinyle avec aura néon violette & cyane et rotation fluide
 * - Badge de format audio haute fidélité (FLAC 24-bit, MP3 320kbps)
 * - Scrubber interactif avec timeline précise
 * - Bouton central géant de lecture avec dégradé et halo néon
 * - Commandes de lecture : Aléatoire, Répétition (Tout / 1 / Désactivé), Vitesse de lecture
 * - Gestion visuelle de l'Audio Focus, notification système et déconnexion casque
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    track: AudioTrackEntity?,
    isPlaying: Boolean,
    progressMs: Long,
    durationMs: Long,
    repeatMode: Int,
    isShuffleEnabled: Boolean,
    playbackSpeed: Float,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleFavorite: () -> Unit,
    onSetSpeed: (Float) -> Unit,
    lyricsData: LyricsData = LyricsData(),
    onOpenLyrics: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager }
    val maxVolume = remember { audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15 }
    var currentVolume by remember {
        mutableFloatStateOf(audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC)?.toFloat() ?: 10f)
    }

    var isUserDraggingSlider by remember { mutableStateOf(false) }
    var sliderDragPositionMs by remember { mutableFloatStateOf(0f) }

    val effectiveDuration = if (durationMs > 0) durationMs else (track?.duration ?: 1L).coerceAtLeast(1L)
    val displayPositionMs = if (isUserDraggingSlider) sliderDragPositionMs.toLong() else progressMs

    // Animation infinie de rotation pour la platine vinyle lors de la lecture
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl_rotation")
    val vinylRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vinyl_angle"
    )

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MusicProBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Bar de navigation et statut
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MusicProSurfaceElevated)
                        .testTag("now_playing_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Réduire le lecteur",
                        tint = MusicProCyanNeon,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "LECTURE EN COURS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = MusicProCyanNeon
                    )
                    Text(
                        text = track?.album ?: "MusicPro Player",
                        fontSize = 12.sp,
                        color = MusicProTextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onOpenLyrics,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MusicProSurfaceElevated)
                            .testTag("now_playing_lyrics_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Subtitles,
                            contentDescription = "Paroles synchronisées",
                            tint = MusicProCyanNeon,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MusicProSurfaceElevated)
                            .testTag("now_playing_favorite_button")
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favori",
                            tint = if (isFavorite) MusicProFavorite else MusicProTextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Vinyle central avec pochettes et halo néon
            Box(
                modifier = Modifier
                    .size(270.dp)
                    .shadow(
                        elevation = 24.dp,
                        shape = CircleShape,
                        spotColor = MusicProVioletGlow,
                        ambientColor = MusicProCyanGlow
                    )
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFF221A3A), Color(0xFF0D0B18))
                        )
                    )
                    .border(2.5.dp, MusicProPrimaryGradient, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Sillons concentriques du vinyle
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color(0x1FFFFFFF), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color(0x14FFFFFF), CircleShape)
                )

                // Pochette d'album au centre avec rotation fluide
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .clip(CircleShape)
                        .rotate(if (isPlaying) vinylRotation else 0f)
                        .border(2.dp, MusicProCyanNeon, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    var imageLoadError by remember(track?.id) { mutableStateOf(false) }

                    if (!track?.albumArtUri.isNullOrBlank() && !imageLoadError) {
                        AsyncImage(
                            model = track?.albumArtUri,
                            contentDescription = track?.title,
                            contentScale = ContentScale.Crop,
                            onError = { imageLoadError = true },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MusicProPrimaryGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Album,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(70.dp)
                            )
                        }
                    }

                    // Trou central du vinyle
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MusicProBackground)
                            .border(2.dp, Color(0x66FFFFFF), CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Métadonnées du morceau et badge Hi-Res Audio
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Format audio badge (FLAC / MP3)
                val format = track?.getAudioFormat() ?: "AUDIO"
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (format == "FLAC") MusicProGreenEmerald.copy(alpha = 0.2f) else MusicProVioletPrimary.copy(alpha = 0.25f),
                    border = BorderStroke(1.dp, if (format == "FLAC") MusicProGreenEmerald else MusicProCyanNeon)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = if (format == "FLAC") MusicProGreenEmerald else MusicProCyanNeon,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (format == "FLAC") "FLAC 24-bit • Lossless" else "$format • 320 kbps",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (format == "FLAC") MusicProGreenEmerald else MusicProCyanLight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = track?.title ?: "Aucun morceau sélectionné",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MusicProTextPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = track?.artist ?: "MusicPro Offline Player",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MusicProCyanLight,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Aperçu interactif des paroles synchronisées (LRC / ID3 SYLT)
            val activeLineIndex = remember(lyricsData, displayPositionMs) {
                lyricsData.findActiveLineIndex(displayPositionMs)
            }
            val activeLineText = if (activeLineIndex in lyricsData.lines.indices) {
                lyricsData.lines[activeLineIndex].text
            } else null

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                onClick = onOpenLyrics,
                shape = RoundedCornerShape(14.dp),
                color = MusicProSurfaceElevated.copy(alpha = 0.85f),
                border = BorderStroke(
                    1.dp,
                    if (activeLineText != null) MusicProCyanNeon.copy(alpha = 0.5f) else Color(0x26FFFFFF)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("now_playing_lyrics_preview_card")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Subtitles,
                            contentDescription = "Paroles",
                            tint = if (activeLineText != null) MusicProCyanNeon else MusicProVioletLight,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = activeLineText?.ifBlank { "♪ ♪ ♪" } ?: "Voir les paroles synchronisées",
                            fontSize = 13.sp,
                            fontWeight = if (activeLineText != null) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (activeLineText != null) MusicProCyanNeon else MusicProTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (lyricsData.lines.isNotEmpty()) "LRC / SYLT" else "Ouvrir",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MusicProVioletLight
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Scrubber / Slider de timeline avec design néon
            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = displayPositionMs.coerceIn(0L, effectiveDuration).toFloat(),
                    onValueChange = { newPos ->
                        isUserDraggingSlider = true
                        sliderDragPositionMs = newPos
                    },
                    onValueChangeFinished = {
                        isUserDraggingSlider = false
                        onSeekTo(sliderDragPositionMs.toLong())
                    },
                    valueRange = 0f..effectiveDuration.toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = MusicProCyanNeon,
                        activeTrackColor = MusicProCyanNeon,
                        inactiveTrackColor = MusicProSurfaceVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("now_playing_scrubber_slider")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTime(displayPositionMs),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MusicProCyanLight
                    )
                    Text(
                        text = formatTime(effectiveDuration),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MusicProTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Commandes de lecture principales
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Aléatoire (Shuffle)
                IconButton(
                    onClick = onToggleShuffle,
                    modifier = Modifier.size(46.dp).testTag("now_playing_shuffle_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Lecture aléatoire",
                        tint = if (isShuffleEnabled) MusicProCyanNeon else MusicProTextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Précédent
                IconButton(
                    onClick = onPrevious,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(MusicProSurfaceElevated)
                        .testTag("now_playing_prev_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Morceau précédent",
                        tint = MusicProTextPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Bouton central géant Play / Pause avec gradient et halo néon
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .shadow(16.dp, CircleShape, spotColor = MusicProVioletGlow)
                        .clip(CircleShape)
                        .background(MusicProPrimaryGradient)
                        .border(1.5.dp, MusicProCyanNeon, CircleShape)
                        .clickable(onClick = onPlayPause)
                        .testTag("now_playing_play_pause_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Lecture",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Suivant
                IconButton(
                    onClick = onNext,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(MusicProSurfaceElevated)
                        .testTag("now_playing_next_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Morceau suivant",
                        tint = MusicProTextPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Répétition (Repeat : OFF / ALL / ONE)
                IconButton(
                    onClick = onToggleRepeat,
                    modifier = Modifier.size(46.dp).testTag("now_playing_repeat_button")
                ) {
                    val isRepeatActive = repeatMode != Player.REPEAT_MODE_OFF
                    Icon(
                        imageVector = if (repeatMode == Player.REPEAT_MODE_ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                        contentDescription = "Mode de répétition",
                        tint = if (isRepeatActive) MusicProCyanNeon else MusicProTextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 6. Sélecteur de vitesse de lecture et contrôle du volume
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MusicProSurfaceElevated)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Vitesse",
                        tint = MusicProCyanNeon,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Vitesse :",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MusicProTextSecondary
                    )
                }

                val speedOptions = listOf(0.8f, 1.0f, 1.25f, 1.5f)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    speedOptions.forEach { speed ->
                        val isCurrentSpeed = playbackSpeed == speed
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isCurrentSpeed) MusicProVioletPrimary else Color(0x1FFFFFFF))
                                .border(
                                    1.dp,
                                    if (isCurrentSpeed) MusicProCyanNeon else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onSetSpeed(speed) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${speed}x",
                                fontSize = 11.sp,
                                fontWeight = if (isCurrentSpeed) FontWeight.Bold else FontWeight.Normal,
                                color = if (isCurrentSpeed) Color.White else MusicProTextMuted
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Contrôle volume système
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MusicProSurfaceElevated)
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeDown,
                    contentDescription = "Volume bas",
                    tint = MusicProTextMuted,
                    modifier = Modifier.size(18.dp)
                )

                Slider(
                    value = currentVolume,
                    onValueChange = { newVol ->
                        currentVolume = newVol
                        audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, newVol.toInt(), 0)
                    },
                    valueRange = 0f..maxVolume.toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = MusicProVioletLight,
                        activeTrackColor = MusicProVioletPrimary,
                        inactiveTrackColor = MusicProSurfaceVariant
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                )

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Volume haut",
                    tint = MusicProCyanNeon,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 7. Bannière technique : Gestion des cas limites (Audio Focus, Casque, Doze Mode)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MusicProCardBackground),
                border = BorderStroke(1.dp, Color(0x26FFFFFF)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MusicProGreenEmerald)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Lecteur Haute Résolution Actif",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MusicProTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatusFeatureChip(
                            icon = Icons.Default.NotificationsActive,
                            text = "Foreground Service Media3",
                            modifier = Modifier.weight(1f)
                        )
                        StatusFeatureChip(
                            icon = Icons.Default.Headphones,
                            text = "Pause si déconnexion",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatusFeatureChip(
                            icon = Icons.Default.Equalizer,
                            text = "Focus audio auto (appels)",
                            modifier = Modifier.weight(1f)
                        )
                        StatusFeatureChip(
                            icon = Icons.Default.CheckCircle,
                            text = "Doze Mode CPU WakeLock",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatusFeatureChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Color(0x14FFFFFF),
        border = BorderStroke(1.dp, Color(0x1AFFFFFF))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MusicProCyanNeon,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = MusicProTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}
