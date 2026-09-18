package com.example.ui.lyrics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.SubtitlesOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.AudioTrackEntity
import com.example.lyrics.LyricLine
import com.example.lyrics.LyricsData
import com.example.lyrics.LyricsSource
import com.example.ui.theme.MusicProBackground
import com.example.ui.theme.MusicProCardBackground
import com.example.ui.theme.MusicProCyanLight
import com.example.ui.theme.MusicProCyanNeon
import com.example.ui.theme.MusicProPrimaryGradient
import com.example.ui.theme.MusicProSurfaceElevated
import com.example.ui.theme.MusicProSurfaceVariant
import com.example.ui.theme.MusicProTextMuted
import com.example.ui.theme.MusicProTextPrimary
import com.example.ui.theme.MusicProTextSecondary
import com.example.ui.theme.MusicProVioletGlow
import com.example.ui.theme.MusicProVioletLight
import com.example.ui.theme.MusicProVioletPastel
import com.example.ui.theme.MusicProVioletPrimary
import kotlinx.coroutines.launch

@Composable
fun LyricsScreen(
    track: AudioTrackEntity?,
    lyricsData: LyricsData,
    currentPositionMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    isLoading: Boolean,
    onBack: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onGenerateDemoLyrics: () -> Unit,
    onImportLrcText: (String) -> Unit,
    onOpenLrclibSearch: () -> Unit = {},
    onStartGroqTranscription: () -> Unit = {},
    isGroqTranscribing: Boolean = false,
    groqProgressMessage: String = "",
    groqErrorMessage: String? = null,
    onClearGroqError: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var isUserScrollingManually by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var manualLrcInput by remember { mutableStateOf("") }

    // Calcul réactif de l'index de la ligne active selon ExoPlayer.currentPosition
    val activeLineIndex by remember(lyricsData, currentPositionMs) {
        derivedStateOf { lyricsData.findActiveLineIndex(currentPositionMs) }
    }

    // Scroll automatique fluide centré sur la ligne active
    LaunchedEffect(activeLineIndex) {
        if (activeLineIndex in lyricsData.lines.indices && !listState.isScrollInProgress) {
            // Défilement centré pour laisser les lignes précédentes et suivantes visibles
            val scrollOffset = -220
            listState.animateScrollToItem(
                index = activeLineIndex.coerceAtLeast(0),
                scrollOffset = scrollOffset
            )
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MusicProBackground
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 1. Fond immersif avec pochette floutée et dégradé sombre
            if (!track?.albumArtUri.isNullOrBlank()) {
                AsyncImage(
                    model = track.albumArtUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(50.dp)
                        .alpha(0.18f)
                )
            }

            // Dégradé sombre pour préserver la lisibilité néon
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MusicProBackground.copy(alpha = 0.85f),
                                MusicProBackground.copy(alpha = 0.95f),
                                MusicProBackground
                            )
                        )
                    )
            )

            // 2. Contenu principal
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Top Bar
                LyricsTopBar(
                    track = track,
                    lyricsSource = lyricsData.source,
                    onBack = onBack,
                    onOpenLrclibSearch = onOpenLrclibSearch,
                    onStartGroqTranscription = onStartGroqTranscription,
                    onOpenImportDialog = { showImportDialog = true }
                )

                // Zone centrale : Liste des paroles ou état vide
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (isLoading) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = MusicProCyanNeon,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Recherche des balises ID3 SYLT & LRC...",
                                color = MusicProTextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    } else if (lyricsData.lines.isEmpty()) {
                        EmptyLyricsView(
                            track = track,
                            onOpenLrclibSearch = onOpenLrclibSearch,
                            onStartGroqTranscription = onStartGroqTranscription,
                            onGenerateDemoLyrics = onGenerateDemoLyrics,
                            onOpenImportDialog = { showImportDialog = true }
                        )
                    } else {
                        // Liste défilante des paroles synchronisées
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(
                                top = 120.dp,
                                bottom = 160.dp,
                                start = 24.dp,
                                end = 24.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(20.dp),
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("lyrics_lazy_column")
                        ) {
                            itemsIndexed(lyricsData.lines) { index, line ->
                                val isActive = index == activeLineIndex
                                val isPast = index < activeLineIndex

                                LyricLineItem(
                                    line = line,
                                    isActive = isActive,
                                    isPast = isPast,
                                    onClick = { onSeekTo(line.timeMs) }
                                )
                            }
                        }

                        // Gradient fading en haut et en bas pour un effet de défilement infini élégant
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .align(Alignment.TopCenter)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(MusicProBackground, Color.Transparent)
                                    )
                                )
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, MusicProBackground)
                                    )
                                )
                        )
                    }
                }

                // Mini barre de contrôle en bas pour piloter la lecture sans quitter les paroles
                LyricsBottomControlBar(
                    track = track,
                    currentPositionMs = currentPositionMs,
                    durationMs = if (durationMs > 0) durationMs else (track?.duration ?: 0L),
                    isPlaying = isPlaying,
                    onPlayPause = onPlayPause,
                    onNext = onNext,
                    onPrevious = onPrevious,
                    onSeekTo = onSeekTo
                )
            }
        }
    }

    // Dialogue d'importation / collage de paroles LRC
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            containerColor = MusicProSurfaceElevated,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Subtitles,
                        contentDescription = null,
                        tint = MusicProCyanNeon
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Importer des paroles LRC",
                        color = MusicProTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Collez votre texte au format LRC standard [mm:ss.xx]paroles :",
                        color = MusicProTextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = manualLrcInput,
                        onValueChange = { manualLrcInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .testTag("lrc_input_field"),
                        placeholder = {
                            Text(
                                "[00:12.50]Première ligne des paroles\n[00:24.00]Deuxième ligne synchronisée",
                                color = MusicProTextMuted,
                                fontSize = 12.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MusicProCyanNeon,
                            unfocusedBorderColor = MusicProVioletPrimary.copy(alpha = 0.5f),
                            focusedTextColor = MusicProTextPrimary,
                            unfocusedTextColor = MusicProTextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (manualLrcInput.isNotBlank()) {
                            onImportLrcText(manualLrcInput)
                            showImportDialog = false
                            manualLrcInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MusicProCyanNeon),
                    modifier = Modifier.testTag("confirm_import_lrc_button")
                ) {
                    Text("Appliquer", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Annuler", color = MusicProTextSecondary)
                }
            }
        )
    }

    // Dialogue d'attente pendant la transcription IA Groq Whisper
    if (isGroqTranscribing) {
        AlertDialog(
            onDismissRequest = { /* Empêcher la fermeture accidentelle pendant la transcription */ },
            containerColor = MusicProBackground,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MusicProVioletPrimary.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MusicProCyanNeon,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Groq Whisper large-v3",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MusicProTextPrimary
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = MusicProCyanNeon,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(46.dp)
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = groqProgressMessage.ifBlank { "Transcription audio en cours..." },
                        fontSize = 13.sp,
                        color = MusicProCyanLight,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Découpage automatique si fichier > 25 Mo",
                        fontSize = 11.sp,
                        color = MusicProTextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {}
        )
    }

    // Dialogue d'erreur Groq Whisper
    if (groqErrorMessage != null) {
        val isApiKeyIssue = groqErrorMessage.contains("clé", ignoreCase = true) ||
                groqErrorMessage.contains("api", ignoreCase = true) ||
                groqErrorMessage.contains("paramètres", ignoreCase = true)

        AlertDialog(
            onDismissRequest = onClearGroqError,
            containerColor = MusicProSurfaceElevated,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SubtitlesOff,
                        contentDescription = null,
                        tint = MusicProVioletLight,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Transcription Whisper",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MusicProTextPrimary
                    )
                }
            },
            text = {
                Text(
                    text = groqErrorMessage,
                    fontSize = 13.sp,
                    color = MusicProTextSecondary,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                if (isApiKeyIssue) {
                    Button(
                        onClick = {
                            onClearGroqError()
                            onOpenSettings()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MusicProCyanNeon),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Ouvrir Paramètres", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                } else {
                    Button(
                        onClick = onClearGroqError,
                        colors = ButtonDefaults.buttonColors(containerColor = MusicProCyanNeon),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("D'accord", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            },
            dismissButton = {
                if (isApiKeyIssue) {
                    TextButton(onClick = onClearGroqError) {
                        Text("Fermer", color = MusicProTextSecondary)
                    }
                }
            }
        )
    }
}

@Composable
private fun LyricsTopBar(
    track: AudioTrackEntity?,
    lyricsSource: LyricsSource,
    onBack: () -> Unit,
    onOpenLrclibSearch: () -> Unit,
    onStartGroqTranscription: () -> Unit,
    onOpenImportDialog: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(MusicProSurfaceElevated)
                .testTag("lyrics_back_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Retour",
                tint = MusicProCyanNeon,
                modifier = Modifier.size(22.dp)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = track?.title ?: "Paroles",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MusicProTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (lyricsSource != LyricsSource.NONE) MusicProCyanNeon else MusicProTextMuted)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = lyricsSource.label,
                    fontSize = 11.sp,
                    color = MusicProCyanNeon,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Bouton Transcription Groq Whisper IA
            IconButton(
                onClick = onStartGroqTranscription,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MusicProVioletPrimary.copy(alpha = 0.25f))
                    .testTag("lyrics_whisper_button")
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Transcrire avec Groq Whisper (IA)",
                    tint = MusicProCyanNeon,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Bouton recherche en ligne lrclib.net
            IconButton(
                onClick = onOpenLrclibSearch,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MusicProSurfaceElevated)
                    .testTag("lyrics_lrclib_search_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Rechercher sur lrclib.net",
                    tint = MusicProCyanNeon,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Bouton import manuel LRC
            IconButton(
                onClick = onOpenImportDialog,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MusicProSurfaceElevated)
                    .testTag("lyrics_import_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ContentPaste,
                    contentDescription = "Importer LRC",
                    tint = MusicProVioletLight,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun LyricLineItem(
    line: LyricLine,
    isActive: Boolean,
    isPast: Boolean,
    onClick: () -> Unit
) {
    // Animation douce de transition de taille et couleur
    val textColor by animateColorAsState(
        targetValue = when {
            isActive -> MusicProCyanNeon
            isPast -> MusicProTextSecondary.copy(alpha = 0.7f)
            else -> MusicProTextMuted.copy(alpha = 0.38f)
        },
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "lyric_color"
    )

    val fontSize by animateFloatAsState(
        targetValue = if (isActive) 23f else 17f,
        animationSpec = tween(durationMillis = 250),
        label = "lyric_size"
    )

    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.03f else 1.0f,
        animationSpec = tween(durationMillis = 250),
        label = "lyric_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .then(
                if (isActive) {
                    Modifier
                        .background(MusicProVioletPrimary.copy(alpha = 0.15f))
                        .border(
                            1.dp,
                            MusicProCyanNeon.copy(alpha = 0.45f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                } else {
                    Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                }
            )
            .scale(scale),
        contentAlignment = Alignment.CenterStart
    ) {
        Column {
            Text(
                text = line.text.ifBlank { "♪ ♪ ♪" },
                fontSize = fontSize.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                color = textColor,
                lineHeight = (fontSize * 1.35f).sp
            )

            // Affichage discret du timestamp si la ligne est active
            if (isActive && line.timeMs > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatTimestamp(line.timeMs),
                    fontSize = 11.sp,
                    color = MusicProVioletPastel.copy(alpha = 0.8f),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun EmptyLyricsView(
    track: AudioTrackEntity?,
    onOpenLrclibSearch: () -> Unit,
    onStartGroqTranscription: () -> Unit,
    onGenerateDemoLyrics: () -> Unit,
    onOpenImportDialog: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icône centrale stylisée
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(MusicProSurfaceElevated)
                .border(2.dp, MusicProVioletPrimary.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SubtitlesOff,
                contentDescription = null,
                tint = MusicProCyanNeon,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Aucune parole synchronisée",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MusicProTextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Générez les paroles avec Whisper IA ou recherchez instantanément sur lrclib.net pour les sauvegarder dans vos fichiers.",
            fontSize = 13.sp,
            color = MusicProTextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Bouton 1 : Transcrire avec Groq Whisper large-v3 (IA)
        Button(
            onClick = onStartGroqTranscription,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .shadow(12.dp, spotColor = MusicProCyanNeon)
                .testTag("transcribe_whisper_button"),
            colors = ButtonDefaults.buttonColors(containerColor = MusicProCyanNeon),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Transcrire avec Groq Whisper (IA)",
                color = Color.Black,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Bouton 2 : Rechercher en ligne sur lrclib.net
        Button(
            onClick = onOpenLrclibSearch,
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .shadow(6.dp, spotColor = MusicProVioletGlow)
                .testTag("search_lrclib_online_button"),
            colors = ButtonDefaults.buttonColors(containerColor = MusicProVioletPrimary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Rechercher sur lrclib.net (En ligne)",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Bouton 3 : Générer les paroles synchronisées de démonstration
        Button(
            onClick = onGenerateDemoLyrics,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .border(1.dp, MusicProVioletPrimary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .testTag("generate_demo_lyrics_button"),
            colors = ButtonDefaults.buttonColors(containerColor = MusicProSurfaceElevated),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MusicProVioletLight,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Générer les paroles (Démo hors-ligne)",
                color = MusicProVioletLight,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Bouton 4 : Importer manuellement un texte LRC
        Button(
            onClick = onOpenImportDialog,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .border(1.dp, MusicProSurfaceElevated, RoundedCornerShape(12.dp))
                .testTag("import_lrc_button"),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ContentPaste,
                contentDescription = null,
                tint = MusicProTextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Coller un texte / fichier .LRC manuel",
                color = MusicProTextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun LyricsBottomControlBar(
    track: AudioTrackEntity?,
    currentPositionMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeekTo: (Long) -> Unit
) {
    var isDraggingSlider by remember { mutableStateOf(false) }
    var sliderValueMs by remember { mutableFloatStateOf(0f) }

    val effectivePos = if (isDraggingSlider) sliderValueMs.toLong() else currentPositionMs
    val progressFraction = if (durationMs > 0) {
        (effectivePos.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Surface(
        color = MusicProSurfaceElevated.copy(alpha = 0.95f),
        border = BorderStroke(1.dp, MusicProVioletPrimary.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, spotColor = MusicProVioletGlow)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            // Slider de progression
            Slider(
                value = effectivePos.toFloat().coerceIn(0f, durationMs.toFloat().coerceAtLeast(1f)),
                onValueChange = {
                    isDraggingSlider = true
                    sliderValueMs = it
                },
                onValueChangeFinished = {
                    isDraggingSlider = false
                    onSeekTo(sliderValueMs.toLong())
                },
                valueRange = 0f..durationMs.toFloat().coerceAtLeast(1f),
                colors = SliderDefaults.colors(
                    thumbColor = MusicProCyanNeon,
                    activeTrackColor = MusicProCyanNeon,
                    inactiveTrackColor = Color(0x33FFFFFF)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .testTag("lyrics_scrubber")
            )

            // Durées écoulée et restante
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatTimestamp(effectivePos),
                    fontSize = 11.sp,
                    color = MusicProCyanNeon,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = formatTimestamp(durationMs),
                    fontSize = 11.sp,
                    color = MusicProTextMuted,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Boutons de contrôle média
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(
                    onClick = onPrevious,
                    modifier = Modifier
                        .size(42.dp)
                        .testTag("lyrics_prev_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Précédent",
                        tint = MusicProTextPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                IconButton(
                    onClick = onPlayPause,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(MusicProPrimaryGradient)
                        .testTag("lyrics_play_pause_button")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Lecture",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                IconButton(
                    onClick = onNext,
                    modifier = Modifier
                        .size(42.dp)
                        .testTag("lyrics_next_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Suivant",
                        tint = MusicProTextPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}

private fun formatTimestamp(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
