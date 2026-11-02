package com.example.ui.lyrics

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.SubtitlesOff
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.AudioTrackEntity
import com.example.lyrics.LyricLine
import com.example.lyrics.LyricsData
import com.example.lyrics.LyricsSource
import com.example.ui.audio.AudioViewModel
import com.example.ui.components.glass.rememberAnimationsEnabled
import com.example.ui.theme.MusicProCyanLight
import com.example.ui.theme.MusicProCyanNeon
import com.example.ui.theme.MusicProPrimaryGradient
import com.example.ui.theme.MusicProVioletGlow
import com.example.ui.theme.MusicProVioletLight
import com.example.ui.theme.MusicProVioletPrimary
import kotlinx.coroutines.delay

/** Palette sombre propre à la vue immersive : le texte blanc reste lisible (WCAG AA) en thème clair comme sombre. */
@Composable
private fun immersiveColorScheme(): ColorScheme = MaterialTheme.colorScheme.copy(
    background = Color(0xFF0B0B12),
    onBackground = Color(0xFFF2F2F7),
    surface = Color(0xFF16161F),
    onSurface = Color(0xFFF2F2F7),
    surfaceVariant = Color(0xFF22222E),
    onSurfaceVariant = Color(0xFFC9C9D6)
)

/**
 * Enregistre le décalage karaoké dans le AudioViewModel du même écran (même propriétaire de
 * ViewModel que l'écran d'accueil). Le ViewModel n'est résolu qu'au moment du clic.
 * Un appelant peut fournir son propre callback via le paramètre de LyricsScreen.
 */
@Composable
private fun rememberKaraokeOffsetSetter(): (Long) -> Unit {
    val owner = LocalViewModelStoreOwner.current
    return remember(owner) {
        { offsetMs: Long ->
            owner?.let { ViewModelProvider(it)[AudioViewModel::class.java].setKaraokeOffsetMs(offsetMs) }
            Unit
        }
    }
}

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
    onKaraokeOffsetChange: (Long) -> Unit = rememberKaraokeOffsetSetter(),
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val animationsEnabled = rememberAnimationsEnabled()

    var showImportDialog by remember { mutableStateOf(false) }
    var showOptionsSheet by remember { mutableStateOf(false) }
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

    // Défilement automatique : mis en pause 3 s après un geste de l'utilisateur sur la liste.
    var lastTouchMs by remember { mutableLongStateOf(0L) }
    var isFollowing by remember { mutableStateOf(true) }
    LaunchedEffect(lastTouchMs) {
        if (lastTouchMs != 0L) {
            isFollowing = false
            delay(3000)
            isFollowing = true
        }
    }

    // Masquage automatique après 4 secondes d'inactivité pendant la lecture
    LaunchedEffect(lastInteractionTime, isPlaying, areControlsVisible, lyricsData.lines.size) {
        if (isPlaying && areControlsVisible && lyricsData.lines.isNotEmpty()) {
            delay(4000)
            areControlsVisible = false
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
        isFollowing = true
        if (lyricsData.lines.isNotEmpty()) {
            listState.scrollToItem(0)
        }
    }

    // Calcul de l'index actif à partir de la position fournie par ExoPlayer.
    // La recherche est dichotomique et reste légère même avec beaucoup de lignes.
    val effectiveKaraokePosition = (currentPositionMs - karaokeOffsetMs).coerceAtLeast(0L)
    val activeLineIndex = lyricsData.findActiveLineIndex(effectiveKaraokePosition)

    // Défilement fluide : la ligne active est ramenée au centre de l'écran.
    // L'index de liste est décalé de 1 à cause de l'espaceur de tête.
    LaunchedEffect(activeLineIndex, isFollowing, lyricsData.lines.size) {
        if (!isFollowing || activeLineIndex !in lyricsData.lines.indices) return@LaunchedEffect
        val lazyIndex = activeLineIndex + 1
        // Laisser la mise en page se terminer (changement de taille des lignes) avant de mesurer.
        withFrameNanos { }
        var item = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == lazyIndex }
        if (item == null) {
            listState.scrollToItem(lazyIndex)
            withFrameNanos { }
            item = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == lazyIndex }
        }
        if (item != null) {
            val info = listState.layoutInfo
            val viewportCenter = (info.viewportStartOffset + info.viewportEndOffset) / 2f
            val delta = item.offset + item.size / 2f - viewportCenter
            if (animationsEnabled) {
                listState.animateScrollBy(delta, tween(durationMillis = 450, easing = FastOutSlowInEasing))
            } else {
                listState.scrollBy(delta)
            }
        }
    }

    MaterialTheme(colorScheme = immersiveColorScheme()) {
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
                // 1. Fond immersif : pochette floutée (API 31+), sinon simple halo coloré
                val canBlur = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                if (canBlur && !track?.albumArtUri.isNullOrBlank()) {
                    AsyncImage(
                        model = track?.albumArtUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(60.dp)
                            .alpha(0.55f)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        MusicProVioletPrimary.copy(alpha = 0.28f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }

                // Voile sombre : garantit le contraste du texte blanc sur n'importe quelle pochette
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.40f),
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.62f),
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.88f)
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
                    BoxWithConstraints(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        val halfHeight = maxHeight / 2

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
                            // Liste défilante des paroles synchronisées.
                            // Deux espaceurs d'une demi-hauteur permettent de centrer même la première et la dernière ligne.
                            LazyColumn(
                                state = listState,
                                contentPadding = PaddingValues(horizontal = 24.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("lyrics_lazy_column")
                                    .pointerInput(Unit) {
                                        awaitPointerEventScope {
                                            while (true) {
                                                awaitPointerEvent(PointerEventPass.Initial)
                                                val now = System.currentTimeMillis()
                                                if (now - lastTouchMs > 500L) {
                                                    lastTouchMs = now
                                                    registerInteraction()
                                                }
                                            }
                                        }
                                    }
                            ) {
                                item(key = "lyrics_top_spacer") {
                                    Spacer(modifier = Modifier.height(halfHeight))
                                }
                                itemsIndexed(lyricsData.lines) { index, line ->
                                    LyricLineItem(
                                        line = line,
                                        isActive = index == activeLineIndex,
                                        baseFontSize = karaokeFontSize,
                                        activeColorName = karaokeActiveColor,
                                        animationsEnabled = animationsEnabled,
                                        onClick = {
                                            registerInteraction()
                                            isFollowing = true
                                            onSeekTo((line.timeMs + karaokeOffsetMs).coerceAtLeast(0L))
                                        }
                                    )
                                }
                                item(key = "lyrics_bottom_spacer") {
                                    Spacer(modifier = Modifier.height(halfHeight))
                                }
                            }

                            // Dégradés de fondu en haut et en bas pour un défilement naturel
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp)
                                    .align(Alignment.TopCenter)
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.background.copy(alpha = 0.85f),
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
                                                MaterialTheme.colorScheme.background.copy(alpha = 0.85f)
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
                            },
                            onOpenOptions = {
                                registerInteraction()
                                showOptionsSheet = true
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

        // Feuille du bas : bouton Karaoké + décalage (−/+ 100 ms)
        if (showOptionsSheet) {
            LyricsOptionsSheet(
                offsetMs = karaokeOffsetMs,
                onOffsetChange = onKaraokeOffsetChange,
                onOpenKaraoke = onOpenKaraokeSettings,
                onDismiss = { showOptionsSheet = false }
            )
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
                            text = cleanLyricsUserMessage(groqProgressMessage.ifBlank { "Création des paroles en cours..." }),
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
                        text = cleanLyricsUserMessage(groqErrorMessage),
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
}

/** Feuille du bas : accès au mode karaoké et réglage fin du décalage (pas de 100 ms). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LyricsOptionsSheet(
    offsetMs: Long,
    onOffsetChange: (Long) -> Unit,
    onOpenKaraoke: () -> Unit,
    onDismiss: () -> Unit
) {
    val minusDescription = stringResource(R.string.lyrics_offset_minus_cd)
    val plusDescription = stringResource(R.string.lyrics_offset_plus_cd)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .testTag("lyrics_options_sheet")
        ) {
            Text(
                text = stringResource(R.string.lyrics_options_title),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    onDismiss()
                    onOpenKaraoke()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("lyrics_karaoke_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MusicProCyanNeon),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.lyrics_karaoke),
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.lyrics_karaoke_hint),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.lyrics_offset_title),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { onOffsetChange((offsetMs - 100L).coerceIn(-5000L, 5000L)) },
                    enabled = offsetMs > -5000L,
                    modifier = Modifier
                        .size(48.dp)
                        .semantics { contentDescription = minusDescription }
                        .testTag("lyrics_offset_minus"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("−", fontSize = 22.sp)
                }
                Text(
                    text = stringResource(R.string.lyrics_offset_value, offsetMs.toInt()),
                    color = MusicProCyanNeon,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                OutlinedButton(
                    onClick = { onOffsetChange((offsetMs + 100L).coerceIn(-5000L, 5000L)) },
                    enabled = offsetMs < 5000L,
                    modifier = Modifier
                        .size(48.dp)
                        .semantics { contentDescription = plusDescription }
                        .testTag("lyrics_offset_plus"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("+", fontSize = 20.sp)
                }
            }

            TextButton(
                onClick = { onOffsetChange(0L) },
                enabled = offsetMs != 0L,
                modifier = Modifier
                    .align(Alignment.End)
                    .heightIn(min = 48.dp)
            ) {
                Text(
                    text = stringResource(R.string.lyrics_offset_reset),
                    color = if (offsetMs != 0L) MusicProCyanNeon else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
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
                    contentDescription = "Rechercher des paroles en ligne",
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

/**
 * Ligne de paroles. La ligne active est centrée, en gras et plus grande (28 sp par défaut) ;
 * toutes les autres sont à 50 % d'opacité. Les tailles ne sont pas animées afin que le
 * centrage automatique reste stable ; seule la couleur est fondue.
 */
@Composable
private fun LyricLineItem(
    line: LyricLine,
    isActive: Boolean,
    baseFontSize: Float = 18f,
    activeColorName: String = "cyan",
    animationsEnabled: Boolean = true,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    val targetColor = if (isActive) {
        when (activeColorName) {
            "violet" -> MusicProVioletLight
            "white" -> Color.White
            else -> MusicProCyanNeon
        }
    } else {
        Color.White.copy(alpha = 0.5f)
    }
    val textColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(
            durationMillis = if (animationsEnabled) 280 else 0,
            easing = FastOutSlowInEasing
        ),
        label = "lyric_color"
    )

    val fontSize = if (isActive) maxOf(28f, baseFontSize + 10f) else baseFontSize

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(12.dp))
            .semantics { selected = isActive }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            )
            .padding(horizontal = 8.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = line.text.ifBlank { "…" },
            fontSize = fontSize.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
            color = textColor,
            textAlign = TextAlign.Center,
            lineHeight = (fontSize * 1.3f).sp,
            modifier = Modifier.fillMaxWidth()
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
                text = "Rechercher des paroles en ligne",
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
    onSeekTo: (Long) -> Unit,
    onOpenOptions: () -> Unit
) {
    val effectivePos = currentPositionMs.coerceAtLeast(0L)
    val safeDuration = durationMs.coerceAtLeast(1L)
    var isSeeking by remember(track?.id) { mutableStateOf(false) }
    var seekPositionMs by remember(track?.id) { mutableStateOf(effectivePos.coerceAtMost(safeDuration)) }

    LaunchedEffect(effectivePos, safeDuration, isSeeking) {
        if (!isSeeking) {
            seekPositionMs = effectivePos.coerceAtMost(safeDuration)
        }
    }

    val visiblePositionMs = if (isSeeking) seekPositionMs else effectivePos.coerceAtMost(safeDuration)

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
            // Barre de progression fine et tactile
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatTimestamp(visiblePositionMs),
                    fontSize = 12.sp,
                    color = MusicProCyanNeon,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = formatTimestamp(durationMs.coerceAtLeast(0L)),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .semantics {
                        val fraction = (visiblePositionMs.toFloat() / safeDuration.toFloat()).coerceIn(0f, 1f)
                        progressBarRangeInfo = ProgressBarRangeInfo(fraction, 0f..1f)
                        contentDescription = "Position de lecture"
                        stateDescription = "${formatTimestamp(visiblePositionMs)} sur ${formatTimestamp(safeDuration)}"
                    }
                    .pointerInput(safeDuration) {
                        detectTapGestures { offset ->
                            val fraction = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                            onSeekTo((safeDuration * fraction).toLong())
                        }
                    }
                    .pointerInput(safeDuration) {
                        detectHorizontalDragGestures(
                            onDragStart = { offset ->
                                isSeeking = true
                                seekPositionMs = (safeDuration * (offset.x / size.width.toFloat()).coerceIn(0f, 1f)).toLong()
                            },
                            onHorizontalDrag = { change, _ ->
                                change.consume()
                                seekPositionMs = (safeDuration * (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)).toLong()
                            },
                            onDragEnd = {
                                val target = seekPositionMs.coerceIn(0L, safeDuration)
                                isSeeking = false
                                onSeekTo(target)
                            },
                            onDragCancel = {
                                isSeeking = false
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.14f))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth((visiblePositionMs.toFloat() / safeDuration.toFloat()).coerceIn(0f, 1f))
                        .height(4.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(MusicProCyanNeon)
                        .align(Alignment.CenterStart)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = { onSeekTo((visiblePositionMs - 10_000L).coerceAtLeast(0L)) },
                    modifier = Modifier.heightIn(min = 40.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text("−10 s", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                }
                IconButton(
                    onClick = onOpenOptions,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("lyrics_options_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = stringResource(R.string.lyrics_options_cd),
                        tint = MusicProCyanNeon,
                        modifier = Modifier.size(22.dp)
                    )
                }
                TextButton(
                    onClick = { onSeekTo((visiblePositionMs + 10_000L).coerceAtMost(safeDuration)) },
                    modifier = Modifier.heightIn(min = 40.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text("+10 s", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
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

private fun cleanLyricsUserMessage(message: String): String {
    return message
        .replace("Groq", "", ignoreCase = true)
        .replace("Whisper large-v3", "service de transcription", ignoreCase = true)
        .replace("Whisper", "transcription", ignoreCase = true)
        .replace("lrclib.net", "service de paroles", ignoreCase = true)
        .replace("API REST", "service en ligne", ignoreCase = true)
        .replace("API", "service en ligne", ignoreCase = true)
        .replace("ID3", "fichier audio", ignoreCase = true)
        .replace("SYLT", "", ignoreCase = true)
        .replace("LRC", "paroles synchronisées", ignoreCase = true)
        .replace(Regex("""[\x{1F1E6}-\x{1F1FF}\x{1F300}-\x{1FAFF}\x{2600}-\x{27BF}]"""), "")
        .replace(Regex("""\s{2,}"""), " ")
        .trim()
}

private fun formatTimestamp(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
