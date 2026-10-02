package com.example.ui.lyrics

import androidx.activity.compose.BackHandler
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
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.SubtitlesOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
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
import com.example.ui.theme.MusicProCyanLight
import com.example.ui.theme.MusicProCyanNeon
import com.example.ui.theme.MusicProPrimaryGradient
import com.example.ui.theme.MusicProVioletGlow
import com.example.ui.theme.MusicProVioletLight
import com.example.ui.theme.MusicProVioletPastel
import com.example.ui.theme.MusicProVioletPrimary
import kotlinx.coroutines.delay
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
    onImportLrcText: (String) -> Unit,
    onEmbedLyricsInAudioFile: () -> Unit = {},
    onOpenLrclibSearch: () -> Unit = {},
    onStartGroqTranscription: () -> Unit = {},
    isGroqTranscribing: Boolean = false,
    groqProgressMessage: String = "",
    groqErrorMessage: String? = null,
    onClearGroqError: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    karaokeFontSize: Float = 18f,
    karaokeActiveColor: String = "cyan",
    karaokeOffsetMs: Long = 0L,
    onOpenKaraokeSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var isUserScrollingManually by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var manualLrcInput by remember { mutableStateOf("") }

    // Mode plein écran immersif : masquage automatique des contrôles après 4s d'inactivité
    var areControlsVisible by remember { mutableStateOf(true) }
    var lastInteractionTime by remember { mutableStateOf(System.currentTimeMillis()) }

    val registerInteraction = {
        lastInteractionTime = System.currentTimeMillis()
        if (!areControlsVisible) {
            areControlsVisible = true
        }
    }

    // Masquage automatique après 4 secondes d'inactivité pendant la lecture
    LaunchedEffect(lastInteractionTime, isPlaying, areControlsVisible, lyricsData.lines.size) {
        if (isPlaying && areControlsVisible && lyricsData.lines.isNotEmpty()) {
            delay(4000)
            areControlsVisible = false
        }
    }

    // Détection de défilement manuel pour réveiller les contrôles
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            registerInteraction()
        }
    }

    // Interception de la touche retour pour fermer la boîte de dialogue ou l'écran des paroles
    BackHandler {
        if (showImportDialog) {
            showImportDialog = false
        } else {
            onBack()
        }
    }

    // À chaque nouveau morceau, repartir immédiatement au début de la liste.
    // Les anciennes paroles peuvent rester brièvement en mémoire pendant leur chargement :
    // le reset par identifiant évite de conserver la position visuelle du morceau précédent.
    LaunchedEffect(track?.id) {
        isUserScrollingManually = false
        if (lyricsData.lines.isNotEmpty()) {
            listState.scrollToItem(0)
        }
    }

    // Calcul de l'index actif à partir de la position fournie par ExoPlayer.
    // La recherche est dichotomique et reste légère même avec beaucoup de lignes.
    val effectiveKaraokePosition = (currentPositionMs - karaokeOffsetMs).coerceAtLeast(0L)
    val activeLineIndex = lyricsData.findActiveLineIndex(effectiveKaraokePosition)

    // Défilement immédiat : une animation précédente ne doit pas retarder la ligne active.
    LaunchedEffect(activeLineIndex) {
        if (activeLineIndex in lyricsData.lines.indices && !listState.isScrollInProgress) {
            listState.scrollToItem(
                index = activeLineIndex.coerceAtLeast(0),
                scrollOffset = -220
            )
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        areControlsVisible = !areControlsVisible
                        lastInteractionTime = System.currentTimeMillis()
                    }
                )
        ) {
            // 1. Fond immersif avec pochette floutée et dégradé sombre
            if (!track?.albumArtUri.isNullOrBlank()) {
                AsyncImage(
                    model = track.albumArtUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(60.dp)
                        .alpha(0.20f)
                )
            }

            // Dégradé sombre pour préserver la lisibilité néon et l'immersion
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.background.copy(alpha = 0.82f),
                                MaterialTheme.colorScheme.background.copy(alpha = 0.94f),
                                MaterialTheme.colorScheme.background
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
                // Top Bar animée (masquable en mode immersif)
                AnimatedVisibility(
                    visible = areControlsVisible,
                    enter = fadeIn(tween(260)) + expandVertically(tween(260)),
                    exit = fadeOut(tween(260)) + shrinkVertically(tween(260))
                ) {
                    LyricsTopBar(
                        track = track,
                        lyricsSource = lyricsData.source,
                        hasLyrics = lyricsData.lines.isNotEmpty(),
                        onBack = onBack,
                        onOpenLrclibSearch = {
                            registerInteraction()
                            onOpenLrclibSearch()
                        },
                        onStartGroqTranscription = {
                            registerInteraction()
                            onStartGroqTranscription()
                        },
                        onOpenImportDialog = {
                            registerInteraction()
                            showImportDialog = true
                        },
                        onEmbedInAudioFile = {
                            registerInteraction()
                            onEmbedLyricsInAudioFile()
                        }
                    )
                }

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
                                text = "Recherche de paroles intégrées…",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                        }
                    } else if (lyricsData.lines.isEmpty()) {
                        EmptyLyricsView(
                            track = track,
                            onOpenLrclibSearch = onOpenLrclibSearch,
                            onStartGroqTranscription = onStartGroqTranscription,
                            onOpenImportDialog = { showImportDialog = true }
                        )
                    } else {
                        // Liste défilante des paroles synchronisées
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(
                                top = if (areControlsVisible) 80.dp else 120.dp,
                                bottom = if (areControlsVisible) 140.dp else 100.dp,
                                start = 24.dp,
                                end = 24.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
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
                                    baseFontSize = karaokeFontSize,
                                    activeColorName = karaokeActiveColor,
                                    onClick = {
                                        registerInteraction()
                                        onSeekTo((line.timeMs + karaokeOffsetMs).coerceAtLeast(0L))
                                    }
                                )
                            }
                        }

                        // Gradient fading doux en haut et en bas pour un défilement infini et naturel
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .align(Alignment.TopCenter)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.background,
                                            MaterialTheme.colorScheme.background.copy(alpha = 0.75f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.Transparent,
                                            MaterialTheme.colorScheme.background.copy(alpha = 0.75f),
                                            MaterialTheme.colorScheme.background
                                        )
                                    )
                                )
                        )
                    }
                }

                // Mini barre de contrôle en bas (masquable en mode immersif)
                AnimatedVisibility(
                    visible = areControlsVisible,
                    enter = fadeIn(tween(260)) + expandVertically(tween(260)),
                    exit = fadeOut(tween(260)) + shrinkVertically(tween(260))
                ) {
                    LyricsBottomControlBar(
                        track = track,
                        currentPositionMs = currentPositionMs,
                        durationMs = if (durationMs > 0) durationMs else (track?.duration ?: 0L),
                        isPlaying = isPlaying,
                        onPlayPause = {
                            registerInteraction()
                            onPlayPause()
                        },
                        onNext = {
                            registerInteraction()
                            onNext()
                        },
                        onPrevious = {
                            registerInteraction()
                            onPrevious()
                        },
                        onSeekTo = {
                            registerInteraction()
                            onSeekTo(it)
                        }
                    )
                }
            }

            // Indicateur flottant discret lorsque les contrôles sont masqués
            AnimatedVisibility(
                visible = !areControlsVisible,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp)
            ) {
                Surface(
                    onClick = { registerInteraction() },
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, MusicProVioletPrimary.copy(alpha = 0.4f)),
                    modifier = Modifier.shadow(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = MusicProCyanNeon,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Afficher les contrôles",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }

    // Dialogue d'importation / collage de paroles LRC
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Subtitles,
                        contentDescription = null,
                        tint = MusicProCyanNeon
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Importer des paroles",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Collez votre texte de paroles synchronisées :",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MusicProCyanNeon,
                            unfocusedBorderColor = MusicProVioletPrimary.copy(alpha = 0.5f),
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground
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
                    Text("Annuler", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    // Dialogue d'attente pendant la transcription IA Groq Whisper
    if (isGroqTranscribing) {
        AlertDialog(
            onDismissRequest = { /* Empêcher la fermeture accidentelle pendant la transcription */ },
            containerColor = MaterialTheme.colorScheme.background,
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
                        text = "Génération assistée",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
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
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
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
                        text = "Transcription assistée",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            },
            text = {
                Text(
                    text = groqErrorMessage,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        Text("Fermer", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    hasLyrics: Boolean,
    onBack: () -> Unit,
    onOpenLrclibSearch: () -> Unit,
    onStartGroqTranscription: () -> Unit,
    onOpenImportDialog: () -> Unit,
    onEmbedInAudioFile: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Bouton retour épuré
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .testTag("lyrics_back_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Retour",
                tint = MusicProCyanNeon,
                modifier = Modifier.size(20.dp)
            )
        }

        // Centre : Titre et Artiste avec badge de source sans chevauchement
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = track?.title ?: "Paroles",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (!track?.artist.isNullOrBlank()) {
                    Text(
                        text = track.artist,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = " • ",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (lyricsSource != LyricsSource.NONE) MusicProCyanNeon else MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = lyricsSource.label,
                    fontSize = 12.sp,
                    color = MusicProCyanNeon,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }
        }

        // Boutons d'action regroupés et non superposés
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Bouton Transcription Groq Whisper IA
            IconButton(
                onClick = onStartGroqTranscription,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MusicProVioletPrimary.copy(alpha = 0.25f))
                    .testTag("lyrics_whisper_button")
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Générer les paroles",
                    tint = MusicProCyanNeon,
                    modifier = Modifier.size(19.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Bouton recherche en ligne lrclib.net
            IconButton(
                onClick = onOpenLrclibSearch,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .testTag("lyrics_lrclib_search_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Rechercher sur lrclib.net",
                    tint = MusicProCyanNeon,
                    modifier = Modifier.size(19.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Menu contextuel discret "Plus d'options" (Sauvegarde ID3 & Import manuel)
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("lyrics_more_options_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Plus d'options",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(19.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    if (hasLyrics && lyricsSource != LyricsSource.ID3_SYLT) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Intégrer au fichier audio (ID3)",
                                    color = MusicProCyanNeon,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = null,
                                    tint = MusicProCyanNeon,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            onClick = {
                                showMenu = false
                                onEmbedInAudioFile()
                            }
                        )
                    }

                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Coller / Importer des paroles",
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = null,
                                tint = MusicProVioletLight,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            showMenu = false
                            onOpenImportDialog()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LyricLineItem(
    line: LyricLine,
    isActive: Boolean,
    isPast: Boolean,
    baseFontSize: Float = 18f,
    activeColorName: String = "cyan",
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    // Transitions fluides de typographie et couleur (Style moderne & épuré)
    val textColor by animateColorAsState(
        targetValue = when {
            isActive -> when (activeColorName) {
                "violet" -> MusicProVioletLight
                "white" -> Color.White
                else -> MusicProCyanNeon
            }
            isPast -> Color.White.copy(alpha = 0.48f)
            else -> Color.White.copy(alpha = 0.24f)
        },
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "lyric_color"
    )

    val fontSize by animateFloatAsState(
        targetValue = if (isActive) baseFontSize + 5f else baseFontSize - 1f,
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "lyric_size"
    )

    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.02f else 1.0f,
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "lyric_scale"
    )

    val verticalPadding by animateFloatAsState(
        targetValue = if (isActive) 10f else 6f,
        animationSpec = tween(durationMillis = 250),
        label = "lyric_padding"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            )
            .padding(horizontal = 8.dp, vertical = verticalPadding.dp)
            .scale(scale),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = line.text.ifBlank { "♪ ♪ ♪" },
            fontSize = fontSize.sp,
            fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = textColor,
            lineHeight = (fontSize * 1.38f).sp
        )
    }
}

@Composable
private fun EmptyLyricsView(
    track: AudioTrackEntity?,
    onOpenLrclibSearch: () -> Unit,
    onStartGroqTranscription: () -> Unit,
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
                .background(MaterialTheme.colorScheme.surfaceVariant)
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
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Générez ou recherchez les paroles pour les associer à votre morceau.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                text = "Générer les paroles",
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

        // Bouton 3 : Importer manuellement un texte LRC
        Button(
            onClick = onOpenImportDialog,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                .testTag("import_lrc_button"),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ContentPaste,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Coller un texte de paroles",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
    val effectivePos = currentPositionMs.coerceAtLeast(0L)

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
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
            // Position audio affichée uniquement : le déplacement reste disponible
            // dans l'écran principal "Lecture en cours".
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatTimestamp(effectivePos),
                    fontSize = 12.sp,
                    color = MusicProCyanNeon,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = formatTimestamp(durationMs),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        .size(48.dp)
                        .testTag("lyrics_prev_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Précédent",
                        tint = MaterialTheme.colorScheme.onBackground,
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
                        .size(48.dp)
                        .testTag("lyrics_next_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Suivant",
                        tint = MaterialTheme.colorScheme.onBackground,
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
