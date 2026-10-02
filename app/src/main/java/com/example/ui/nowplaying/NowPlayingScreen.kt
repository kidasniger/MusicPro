package com.example.ui.nowplaying

import android.content.Context
import android.media.AudioManager
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import coil.compose.AsyncImage
import com.example.data.local.AudioTrackEntity
import com.example.lyrics.LyricsData
import com.example.ui.theme.MusicProBackground
import com.example.ui.theme.MusicProCardBackground
import com.example.ui.theme.MusicProCyanGlow
import com.example.ui.theme.MusicProCyanLight
import com.example.ui.theme.MusicProCyanNeon
import com.example.ui.theme.MusicProFavorite
import com.example.ui.theme.MusicProGreenEmerald
import com.example.ui.theme.MusicProPrimaryGradient
import com.example.ui.theme.MusicProSurfaceElevated
import com.example.ui.theme.MusicProSurfaceVariant
import com.example.ui.theme.MusicProTextMuted
import com.example.ui.theme.MusicProTextPrimary
import com.example.ui.theme.MusicProTextSecondary
import com.example.ui.theme.MusicProVioletGlow
import com.example.ui.theme.MusicProVioletLight
import com.example.ui.theme.MusicProVioletPrimary
import kotlinx.coroutines.delay
import java.util.Locale

/**
 * Écran de lecture Now Playing raffiné et ergonomique :
 * - Conception 100% plein écran sans défilement nécessaire
 * - Disque vinyle interactif avec rotation fluide et tap tactile (Play/Pause)
 * - Scrubber personnalisé avec curseur néon circulaire à halo lumineux
 * - Ligne karaoké interactive synchronisée en temps réel
 * - Barre de contrôles audio secondaires unifiée (Vitesse, Volume compact, Options/Minuterie)
 * - Fiche détaillée d'informations audio et minuterie de veille dans une feuille modale
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
    onOpenQueue: () -> Unit = {},
    onOpenEqualizer: () -> Unit = {},
    onOpenKaraokeSettings: () -> Unit = {},
    onAddToPlaylist: () -> Unit = {},
    onShare: () -> Unit = {},
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


    var showOptionsSheet by remember { mutableStateOf(false) }
    var sleepTimerMinutes by remember { mutableStateOf<Int?>(null) }
    var sleepTimerSecondsRemaining by remember { mutableStateOf<Int?>(null) }

    // Minuterie de veille automatique (Sleep Timer)
    LaunchedEffect(sleepTimerMinutes) {
        val mins = sleepTimerMinutes
        if (mins != null && mins > 0) {
            var remaining = mins * 60
            while (remaining > 0) {
                sleepTimerSecondsRemaining = remaining
                delay(1000L)
                remaining--
            }
            sleepTimerSecondsRemaining = 0
            if (isPlaying) {
                onPlayPause()
            }
            sleepTimerMinutes = null
            sleepTimerSecondsRemaining = null
        } else {
            sleepTimerSecondsRemaining = null
        }
    }

    val effectiveDuration = if (durationMs > 0) durationMs else (track?.duration ?: 1L).coerceAtLeast(1L)
    val displayPositionMs = progressMs.coerceIn(0L, effectiveDuration)

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

    // Interception de la touche retour pour fermer l'écran Now Playing
    BackHandler {
        onBack()
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MusicProBackground
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val screenHeight = maxHeight
            val isCompactScreen = screenHeight < 620.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .then(
                        if (isCompactScreen) Modifier.verticalScroll(rememberScrollState())
                        else Modifier
                    )
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = if (isCompactScreen) Arrangement.Top else Arrangement.SpaceBetween
            ) {
                // 1. Barre supérieure : Réduire, Titre d'album, Paroles & Favori
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MusicProSurfaceElevated)
                            .testTag("now_playing_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Réduire le lecteur",
                            tint = MusicProCyanNeon,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = "LECTURE EN COURS",
                            fontSize = 12.sp,
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
                            onClick = onOpenQueue,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MusicProSurfaceElevated)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.QueueMusic,
                                contentDescription = "File d'attente",
                                tint = MusicProCyanNeon,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(
                            onClick = onOpenLyrics,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(
                                    if (lyricsData.lines.isNotEmpty()) MusicProVioletPrimary.copy(alpha = 0.35f)
                                    else MusicProSurfaceElevated
                                )
                                .testTag("now_playing_lyrics_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Subtitles,
                                contentDescription = "Paroles synchronisées",
                                tint = if (lyricsData.lines.isNotEmpty()) MusicProCyanNeon else MusicProTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(
                            onClick = onToggleFavorite,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MusicProSurfaceElevated)
                                .testTag("now_playing_favorite_button")
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favori",
                                tint = if (isFavorite) MusicProFavorite else MusicProTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 2. Disque vinyle central interactif (taille adaptative)
                val vinylSize = when {
                    screenHeight < 600.dp -> 160.dp
                    screenHeight < 720.dp -> 190.dp
                    screenHeight < 840.dp -> 220.dp
                    else -> 245.dp
                }

                var gestureDx by remember { mutableFloatStateOf(0f) }
                var gestureDy by remember { mutableFloatStateOf(0f) }

                Box(
                    modifier = Modifier
                        .size(vinylSize)
                        .pointerInput(track?.id) {
                            detectDragGestures(
                                onDragStart = {
                                    gestureDx = 0f
                                    gestureDy = 0f
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    gestureDx += dragAmount.x
                                    gestureDy += dragAmount.y
                                },
                                onDragEnd = {
                                    val horizontal = kotlin.math.abs(gestureDx) >= kotlin.math.abs(gestureDy)
                                    when {
                                        horizontal && gestureDx < -90f -> onNext()
                                        horizontal && gestureDx > 90f -> onPrevious()
                                        else -> onPlayPause()
                                    }
                                    gestureDx = 0f
                                    gestureDy = 0f
                                },
                                onDragCancel = {
                                    gestureDx = 0f
                                    gestureDy = 0f
                                }
                            )
                        }
                        .shadow(
                            elevation = 20.dp,
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
                        .border(2.dp, MusicProPrimaryGradient, CircleShape)
                        .clickable { onPlayPause() },
                    contentAlignment = Alignment.Center
                ) {
                    // Sillons concentriques du disque
                    Box(
                        modifier = Modifier
                            .size(vinylSize * 0.88f)
                            .clip(CircleShape)
                            .border(1.dp, Color(0x1AFFFFFF), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(vinylSize * 0.74f)
                            .clip(CircleShape)
                            .border(1.dp, Color(0x12FFFFFF), CircleShape)
                    )

                    // Pochette centrale rotative
                    val centerArtSize = vinylSize * 0.62f
                    Box(
                        modifier = Modifier
                            .size(centerArtSize)
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
                                    modifier = Modifier.size(centerArtSize * 0.45f)
                                )
                            }
                        }

                        // Trou central du vinyle
                        Box(
                            modifier = Modifier
                                .size(vinylSize * 0.10f)
                                .clip(CircleShape)
                                .background(MusicProBackground)
                                .border(1.5.dp, Color(0x80FFFFFF), CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3. Badge format Hi-Res Audio, Titre et Artiste
                val format = track?.getAudioFormat() ?: "AUDIO"
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (format == "FLAC") MusicProGreenEmerald.copy(alpha = 0.2f) else MusicProVioletPrimary.copy(alpha = 0.25f),
                    border = BorderStroke(1.dp, if (format == "FLAC") MusicProGreenEmerald else MusicProCyanNeon)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = if (format == "FLAC") MusicProGreenEmerald else MusicProCyanNeon,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (format == "FLAC") "FLAC 24-bit • Lossless" else "$format • 320 kbps",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (format == "FLAC") MusicProGreenEmerald else MusicProCyanLight
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = track?.title ?: "Aucun morceau sélectionné",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MusicProTextPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = track?.artist ?: "MusicPro Offline Player",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MusicProCyanLight,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Ligne karaoké interactive synchronisée
                val activeLineIndex = remember(lyricsData, displayPositionMs) {
                    lyricsData.findActiveLineIndex(displayPositionMs)
                }
                val activeLineText = if (activeLineIndex in lyricsData.lines.indices) {
                    lyricsData.lines[activeLineIndex].text
                } else null

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    onClick = onOpenLyrics,
                    shape = RoundedCornerShape(12.dp),
                    color = MusicProSurfaceElevated.copy(alpha = 0.8f),
                    border = BorderStroke(
                        1.dp,
                        if (activeLineText != null) MusicProCyanNeon.copy(alpha = 0.6f) else Color(0x22FFFFFF)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("now_playing_lyrics_preview_card")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
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
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = activeLineText?.ifBlank { "♪ ♪ ♪" } ?: "Voir les paroles synchronisées",
                                fontSize = 12.sp,
                                fontWeight = if (activeLineText != null) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (activeLineText != null) MusicProCyanNeon else MusicProTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (lyricsData.lines.isNotEmpty()) "LRC / SYLT" else "Ouvrir",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MusicProVioletLight
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 4. Barre de progression tactile, sans curseur musical
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        TextButton(
                            onClick = { onSeekTo((displayPositionMs - 10_000L).coerceAtLeast(0L)) },
                            modifier = Modifier.size(width = 58.dp, height = 40.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) { Text("−10 s", color = MusicProTextSecondary, fontSize = 11.sp) }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x28FFFFFF))
                                .pointerInput(effectiveDuration) {
                                    detectTapGestures { offset ->
                                        val fraction = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                                        onSeekTo((effectiveDuration * fraction).toLong())
                                    }
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(
                                        (displayPositionMs.toFloat() / effectiveDuration.toFloat()).coerceIn(0f, 1f)
                                    )
                                    .fillMaxSize()
                                    .background(MusicProCyanNeon)
                            )
                        }
                        TextButton(
                            onClick = { onSeekTo((displayPositionMs + 10_000L).coerceAtMost(effectiveDuration)) },
                            modifier = Modifier.size(width = 58.dp, height = 40.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) { Text("+10 s", color = MusicProTextSecondary, fontSize = 11.sp) }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(formatTime(displayPositionMs), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MusicProCyanLight)
                        Text(formatTime(effectiveDuration), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MusicProTextMuted)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Volume : commandes + / −, sans curseur
                    Row(
                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                val newVol = (currentVolume - 1f).coerceAtLeast(0f)
                                currentVolume = newVol
                                audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, newVol.toInt(), 0)
                            },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.VolumeDown, "Baisser le volume", tint = MusicProTextSecondary, modifier = Modifier.size(18.dp))
                        }
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(((currentVolume / maxVolume.toFloat()) * 100f).roundToInt().toString() + "%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MusicProCyanNeon)
                            Text(if (currentVolume == 0f) "Muet" else "Volume", fontSize = 10.sp, color = MusicProTextMuted)
                        }
                        IconButton(
                            onClick = {
                                val newVol = (currentVolume + 1f).coerceAtMost(maxVolume.toFloat())
                                currentVolume = newVol
                                audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, newVol.toInt(), 0)
                            },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, "Augmenter le volume", tint = MusicProCyanNeon, modifier = Modifier.size(18.dp))
                        }
                    }

                    // Bouton Options audio & Minuterie de mise en veille
                    IconButton(
                        onClick = { showOptionsSheet = true },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (sleepTimerSecondsRemaining != null) MusicProVioletPrimary.copy(alpha = 0.6f)
                                else Color(0x1AFFFFFF)
                            )
                    ) {
                        Icon(
                            imageVector = if (sleepTimerSecondsRemaining != null) Icons.Default.Timer else Icons.Default.Tune,
                            contentDescription = "Options audio et minuterie",
                            tint = if (sleepTimerSecondsRemaining != null) MusicProCyanNeon else MusicProTextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }

    // Feuille modale des options audio, minuterie de veille et caractéristiques techniques
    if (showOptionsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showOptionsSheet = false },
            containerColor = MusicProCardBackground,
            scrimColor = Color.Black.copy(alpha = 0.65f),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            NowPlayingOptionsSheetContent(
                track = track,
                playbackSpeed = playbackSpeed,
                onSetSpeed = onSetSpeed,
                sleepTimerMinutes = sleepTimerMinutes,
                sleepTimerSecondsRemaining = sleepTimerSecondsRemaining,
                onSetSleepTimer = { mins -> sleepTimerMinutes = mins },
                onClose = { showOptionsSheet = false },
                onOpenQueue = onOpenQueue,
                onOpenEqualizer = onOpenEqualizer,
                onOpenKaraokeSettings = onOpenKaraokeSettings,
                onAddToPlaylist = onAddToPlaylist,
                onShare = onShare
            )
        }
    }
}

/**
 * Contenu de la feuille modale pour les paramètres de lecture et les détails techniques audio
 */
@Composable
private fun NowPlayingOptionsSheetContent(
    track: AudioTrackEntity?,
    playbackSpeed: Float,
    onSetSpeed: (Float) -> Unit,
    sleepTimerMinutes: Int?,
    sleepTimerSecondsRemaining: Int?,
    onSetSleepTimer: (Int?) -> Unit,
    onClose: () -> Unit,
    onOpenQueue: () -> Unit = {},
    onOpenEqualizer: () -> Unit = {},
    onOpenKaraokeSettings: () -> Unit = {},
    onAddToPlaylist: () -> Unit = {},
    onShare: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        // En-tête
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Options de lecture",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MusicProTextPrimary
            )
            TextButton(
                onClick = onClose,
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text(
                    text = "Fermer",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MusicProCyanNeon
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Section 1 : Minuterie de mise en veille (Sleep Timer)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = null,
                tint = MusicProCyanNeon,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Minuterie de veille :",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MusicProTextPrimary
            )
            if (sleepTimerSecondsRemaining != null) {
                Spacer(modifier = Modifier.width(8.dp))
                val remainingMins = sleepTimerSecondsRemaining / 60
                val remainingSecs = sleepTimerSecondsRemaining % 60
                Text(
                    text = String.format(Locale.getDefault(), "%02d:%02d restant", remainingMins, remainingSecs),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MusicProCyanNeon
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val timerOptions = listOf(null to "Off", 15 to "15 min", 30 to "30 min", 45 to "45 min", 60 to "60 min")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            timerOptions.forEach { (mins, label) ->
                val isSelected = sleepTimerMinutes == mins
                Surface(
                    onClick = { onSetSleepTimer(mins) },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) MusicProVioletPrimary else Color(0x1FFFFFFF),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) MusicProCyanNeon else Color(0x22FFFFFF)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else MusicProTextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SecondaryActionButton(Icons.AutoMirrored.Filled.QueueMusic, "File d'attente", onOpenQueue)
            SecondaryActionButton(Icons.Default.Equalizer, "Égaliseur", onOpenEqualizer)
            SecondaryActionButton(Icons.Default.Tune, "Karaoké", onOpenKaraokeSettings)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SecondaryActionButton(Icons.Default.Add, "Playlist", onAddToPlaylist)
            SecondaryActionButton(Icons.Default.Share, "Partager", onShare)
            Spacer(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Section 2 : Vitesse de lecture étendue
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Speed,
                contentDescription = null,
                tint = MusicProVioletLight,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Vitesse de lecture :",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MusicProTextPrimary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

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
                    color = if (isSelected) MusicProVioletPrimary else Color(0x1FFFFFFF),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) MusicProCyanNeon else Color(0x1AFFFFFF)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "${speed}x",
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else MusicProTextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun RowScope.SecondaryActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = Color(0x14FFFFFF),
        border = BorderStroke(1.dp, Color(0x1AFFFFFF)),
        modifier = Modifier.weight(1f)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = label, tint = MusicProCyanNeon, modifier = Modifier.size(17.dp))
            Text(label, fontSize = 10.sp, color = MusicProTextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}
