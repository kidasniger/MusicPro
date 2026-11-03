package com.example.ui.nowplaying

import android.content.Context
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.widget.Toast
import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.SpeakerGroup
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.AudioTrackEntity
import com.example.data.preferences.PlayerPresentation
import com.example.lyrics.LyricsData
import com.example.ui.components.glass.GlassMaterials
import com.example.ui.components.glass.GlassSurface
import com.example.ui.components.glass.LocalReduceAnimations
import com.example.ui.components.glass.rememberAnimationsEnabled
import com.example.ui.components.glass.rememberSystemAnimationsEnabled
import com.example.ui.components.AnchoredSheet
import com.example.ui.components.glass.sharedCover
import com.example.ui.theme.MusicProCyanGlow
import com.example.ui.theme.MusicProCyanLight
import com.example.ui.theme.MusicProCyanNeon
import com.example.ui.theme.MusicProFavorite
import com.example.ui.theme.MusicProGreenEmerald
import com.example.ui.theme.MusicProPrimaryGradient
import com.example.ui.theme.MusicProVioletGlow
import com.example.ui.theme.MusicProVioletLight
import com.example.ui.theme.MusicProVioletPrimary
import com.example.ui.theme.darkened
import com.example.ui.theme.rememberTrackDominantColor
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/** Onglets inférieurs du lecteur (mise à jour 045). */
enum class PlayerTab { LECTURE, PAROLES, EGALISEUR, INFOS }

/**
 * Écran de lecture immersif (mises à jour 041 à 050) :
 * - 041 : pochette pleine largeur en haut, sans cadre, fondue vers le fond par un dégradé
 *   vertical de 160 dp ; fond = couleur dominante de la pochette assombrie de 35 %, mise à
 *   jour en 400 ms à chaque morceau ;
 * - 042 : titre (2 lignes) et artiste en pilule de verre, trois boutons ronds de 40 dp à
 *   droite (Radio, File d'attente, J'aime) avec cœur animé (1→1,25→1, 220 ms) et vibration ;
 * - 043 : barre de progression fine (4 dp, 10 dp au toucher), zone tactile de 40 dp, temps
 *   flottant au-dessus du doigt, écoulié à gauche et restant/total à droite, sans `Slider` ;
 * - 044 : lecture/pause 88 dp, précédent/suivant 64 dp, aléatoire/répéter en icônes 28 dp
 *   avec point d'état sous l'icône (accessibilité) ;
 * - 045 : pilule d'onglets en bas (Lecture, Paroles, Réglages audio, Infos) avec glissement
 *   horizontal et fondu, onglet actif en capsule plus claire ;
 * - 046 : pilule « Passer à la vidéo » au-dessus du titre quand une vidéo existe ;
 * - 047 : glisser vers le bas = réduire, double tap gauche/droite = ±10 s avec onde, appui
 *   long = menu (playlist, album, artiste, partager) ;
 * - 048 : présentation pochette / vinyle / minimal choisie dans Réglages › Lecture ;
 * - 049 : icône appareils en haut à droite → feuille « Sortie audio » + volume −/+ ;
 * - 050 : minuterie de sommeil (5 à 60 min ou fin du morceau) avec pastille de compte à
 *   rebours près du titre et fondu de volume sur les 10 dernières secondes.
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
    presentationMode: PlayerPresentation = PlayerPresentation.POCHETTE,
    hasVideo: Boolean = false,
    onOpenVideo: () -> Unit = {},
    onOpenRadio: () -> Unit = {},
    onOpenAlbum: (String) -> Unit = {},
    onOpenArtist: (String) -> Unit = {},
    onSeekBy: (Long) -> Unit = {},
    eqEnabled: Boolean = false,
    eqPreset: String = "Flat",
    eqLevels: List<Int> = listOf(0, 0, 0, 0, 0),
    onEqEnabledChange: (Boolean) -> Unit = {},
    onEqPresetChange: (String) -> Unit = {},
    onEqBandChange: (Int, Int) -> Unit = { _, _ -> },
    onEqReset: () -> Unit = {},
    sleepRemainingMs: Long = 0L,
    sleepMinutes: Int = 0,
    sleepEndOfTrack: Boolean = false,
    onSetSleepTimerMinutes: (Int) -> Unit = {},
    onSetSleepTimerEndOfTrack: () -> Unit = {},
    onCancelSleepTimer: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager }
    val maxVolume = remember { audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15 }
    var currentVolume by remember {
        mutableFloatStateOf(audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC)?.toFloat() ?: 10f)
    }

    val animationsEnabled = rememberAnimationsEnabled() &&
        rememberSystemAnimationsEnabled() && !LocalReduceAnimations.current
    val animationMillis = if (animationsEnabled) 400 else 0

    var showSpeedSheet by remember { mutableStateOf(false) }
    var showSleepSheet by remember { mutableStateOf(false) }
    var showDevicesSheet by remember { mutableStateOf(false) }
    var showTrackMenu by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }

    val effectiveDuration = if (durationMs > 0) durationMs else (track?.duration ?: 1L).coerceAtLeast(1L)
    val displayPositionMs = progressMs.coerceIn(0L, effectiveDuration)
    var isSeeking by remember(track?.id) { mutableStateOf(false) }
    var seekPositionMs by remember(track?.id) { mutableStateOf(displayPositionMs) }

    LaunchedEffect(displayPositionMs, isSeeking) {
        if (!isSeeking) {
            seekPositionMs = displayPositionMs
        }
    }

    val visiblePositionMs = if (isSeeking) seekPositionMs else displayPositionMs

    // Onglets du bas (045), réinitialisés à Lecture à chaque changement de morceau non, gardés.
    var selectedTab by remember { mutableStateOf(PlayerTab.LECTURE) }

    // La rotation continue du vinyle est supprimée en mode réduction des animations (035/048).
    val vinylRotation = if (rememberAnimationsEnabled()) {
        val infiniteTransition = rememberInfiniteTransition(label = "vinyl_rotation")
        val rotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 18000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "vinyl_angle"
        )
        rotation
    } else {
        0f
    }

    // 041 — Fond immersif : couleur dominante de la pochette assombrie de 35 %.
    val isDarkTheme = true
    val dominantColor by rememberTrackDominantColor(
        artworkUri = track?.albumArtUri.takeIf { presentationMode != PlayerPresentation.MINIMAL },
        isDark = isDarkTheme
    )
    val backgroundColor = MaterialTheme.colorScheme.background
    val immersiveBackground by animateColorAsState(
        targetValue = dominantColor?.darkened(0.35f) ?: backgroundColor,
        animationSpec = tween(durationMillis = animationMillis),
        label = "np_background"
    )

    // 049 — Toast discret quand un appareil audio se connecte/déconnecte.
    var audioDevicesVersion by remember { mutableIntStateOf(0) }
    DisposableEffect(Unit) {
        val callback = object : AudioDeviceCallback() {
            private fun toastFor(devices: Array<out AudioDeviceInfo>, added: Boolean) {
                val message = when {
                    devices.any {
                        it.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES || it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET
                    } -> context.getString(
                        if (added) R.string.np_toast_headphones_connected else R.string.np_toast_headphones_disconnected
                    )
                    devices.any {
                        it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP || it.type == AudioDeviceInfo.TYPE_BLE_HEADSET
                    } -> context.getString(
                        if (added) R.string.np_toast_bluetooth_connected else R.string.np_toast_bluetooth_disconnected
                    )
                    else -> null
                }
                if (message != null) {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>) {
                audioDevicesVersion++
                toastFor(addedDevices, added = true)
            }

            override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>) {
                audioDevicesVersion++
                toastFor(removedDevices, added = false)
            }
        }
        audioManager?.registerAudioDeviceCallback(callback, null)
        onDispose { audioManager?.unregisterAudioDeviceCallback(callback) }
    }

    // 042 — Animation du cœur : échelle 1 → 1,25 → 1 en 220 ms + vibration légère.
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val heartScale = remember { Animatable(1f) }
    fun animateHeart() {
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        scope.launch {
            heartScale.snapTo(1f)
            heartScale.animateTo(1.25f, tween(durationMillis = 110))
            heartScale.animateTo(1f, tween(durationMillis = 110))
        }
    }

    // Mise à jour 073 — retour prédictif Android 14 : la pochette rétrécit pendant le
    // geste (progress 0→1) et revient à sa taille si l'utilisateur annule le geste.
    // Sur les versions sans retour prédictif, le flux se termine immédiatement :
    // l'écran se ferme comme avec un BackHandler classique.
    var predictiveBackProgress by remember { mutableFloatStateOf(0f) }
    PredictiveBackHandler {
        try {
            it.collect { event -> predictiveBackProgress = event.progress }
            predictiveBackProgress = 0f
            onBack()
        } catch (e: CancellationException) {
            // Geste annulé : la pochette reprend sa taille d'origine.
            predictiveBackProgress = 0f
            throw e
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = immersiveBackground
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val screenHeight = maxHeight
            val availableHeight = maxHeight
            val isCompactScreen = screenHeight < 620.dp

            // Mise à jour 085 — le lecteur passe en deux colonnes (pochette | contrôles) en
            // paysage : les deux sections sont extraites dans des slots composables et disposées
            // selon l'orientation (en colonne unique en portrait, exactement comme avant).
            val coverSection: @Composable ColumnScope.() -> Unit = {
                // 1. Pochette immersive pleine largeur (041) — cachée en présentation minimaliste.
                val coverHeight = when {
                    screenHeight < 600.dp -> availableHeight * 0.32f
                    screenHeight < 720.dp -> availableHeight * 0.36f
                    else -> availableHeight * 0.40f
                }

                if (presentationMode != PlayerPresentation.MINIMAL) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(coverHeight)
                            .graphicsLayer {
                                val p = predictiveBackProgress.coerceIn(0f, 1f)
                                scaleX = 1f - 0.08f * p
                                scaleY = 1f - 0.08f * p
                                alpha = 1f - 0.25f * p
                            }
                    ) {
                        ImmersiveCover(
                            track = track,
                            presentationMode = presentationMode,
                            isPlaying = isPlaying,
                            progressMs = displayPositionMs,
                            vinylRotation = vinylRotation,
                            animationsEnabled = animationsEnabled,
                            backgroundColor = immersiveBackground,
                            onBack = onBack,
                            onPlayPause = onPlayPause,
                            onNext = onNext,
                            onPrevious = onPrevious,
                            onSeekBy = onSeekBy,
                            onOpenTrackMenu = { showTrackMenu = true }
                        )

                        // Dégradé vertical de 160 dp : la pochette se fond dans le fond (041).
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    Brush.verticalGradient(
                                        0f to Color.Transparent,
                                        0.55f to Color.Transparent,
                                        1f to immersiveBackground,
                                        startY = 0f,
                                        endY = Float.POSITIVE_INFINITY
                                    )
                                )
                        )

                        // Voile sombre discret derrière la barre supérieure pour la lisibilité.
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Black.copy(alpha = 0.30f), Color.Transparent)
                                    )
                                )
                        )

                        PlayerTopBar(
                            onBack = onBack,
                            onOpenDevices = { showDevicesSheet = true },
                            onOpenOverflow = { showOverflowMenu = true },
                            showOverflowMenu = showOverflowMenu,
                            onDismissOverflow = { showOverflowMenu = false },
                            onOpenSleepSheet = { showSleepSheet = true },
                            onOpenSpeedSheet = { showSpeedSheet = true },
                            onOpenKaraoke = onOpenKaraokeSettings,
                            onAddToPlaylist = onAddToPlaylist,
                            onShare = onShare,
                            sleepActive = sleepRemainingMs > 0L || sleepEndOfTrack
                        )
                    }
                } else {
                    // Présentation minimaliste : barre supérieure sur fond uni.
                    PlayerTopBar(
                        onBack = onBack,
                        onOpenDevices = { showDevicesSheet = true },
                        onOpenOverflow = { showOverflowMenu = true },
                        showOverflowMenu = showOverflowMenu,
                        onDismissOverflow = { showOverflowMenu = false },
                        onOpenSleepSheet = { showSleepSheet = true },
                        onOpenSpeedSheet = { showSpeedSheet = true },
                        onOpenKaraoke = onOpenKaraokeSettings,
                        onAddToPlaylist = onAddToPlaylist,
                        onShare = onShare,
                        sleepActive = sleepRemainingMs > 0L || sleepEndOfTrack
                    )
                }
                }

                val controlsSection: @Composable ColumnScope.() -> Unit = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(6.dp))

                    // 050 — Pastille de compte à rebours de la minuterie, près du titre.
                    if (sleepRemainingMs > 0L || sleepEndOfTrack) {
                        SleepCountdownPill(
                            remainingMs = sleepRemainingMs,
                            endOfTrack = sleepEndOfTrack,
                            onClick = { showSleepSheet = true }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    // 046 — Pilule « Passer à la vidéo » au-dessus du titre.
                    if (hasVideo) {
                        VideoPill(onClick = onOpenVideo)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // 042 — Pilule de verre titre/artiste + Radio / File d'attente / J'aime.
                    TitleArtistPill(
                        track = track,
                        isFavorite = isFavorite,
                        heartScale = heartScale.value,
                        onToggleFavorite = {
                            onToggleFavorite()
                            animateHeart()
                        },
                        onOpenRadio = onOpenRadio,
                        onOpenQueue = onOpenQueue
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 043 — Barre de progression fine et tactile.
                    FineProgressBar(
                        positionMs = visiblePositionMs,
                        durationMs = effectiveDuration,
                        isSeeking = isSeeking,
                        onSeekStart = { fraction ->
                            isSeeking = true
                            seekPositionMs = (effectiveDuration * fraction).toLong()
                        },
                        onSeekDrag = { fraction ->
                            seekPositionMs = (effectiveDuration * fraction).toLong()
                        },
                        onSeekEnd = {
                            isSeeking = false
                            onSeekTo(seekPositionMs.coerceIn(0L, effectiveDuration))
                        },
                        onSeekTap = { fraction ->
                            onSeekTo((effectiveDuration * fraction).toLong())
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 044 — Boutons de transport en verre.
                    TransportRow(
                        isPlaying = isPlaying,
                        isShuffleEnabled = isShuffleEnabled,
                        repeatMode = repeatMode,
                        onPlayPause = onPlayPause,
                        onNext = onNext,
                        onPrevious = onPrevious,
                        onToggleShuffle = onToggleShuffle,
                        onToggleRepeat = onToggleRepeat
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 045 — Contenu de l'onglet actif, glissement horizontal + fondu.
                    val tabTransition = if (animationsEnabled) 250 else 0
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = {
                            val forward = targetState.ordinal >= initialState.ordinal
                            val direction = if (forward) 1 else -1
                            (
                                slideInHorizontally(tween(tabTransition)) { full -> direction * full } +
                                    fadeIn(tween(tabTransition))
                                ) togetherWith (
                                slideOutHorizontally(tween(tabTransition)) { full -> -direction * full } +
                                    fadeOut(tween(tabTransition))
                                )
                        },
                        label = "np_tabs_content",
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (isCompactScreen) Modifier.height(150.dp)
                                else Modifier.weight(1f)
                            )
                    ) { tab ->
                        when (tab) {
                            PlayerTab.LECTURE -> LectureTabContent(
                                lyricsData = lyricsData,
                                positionMs = displayPositionMs,
                                onOpenLyrics = onOpenLyrics
                            )
                            PlayerTab.PAROLES -> LyricsTabContent(
                                lyricsData = lyricsData,
                                positionMs = displayPositionMs,
                                animationsEnabled = animationsEnabled,
                                onOpenLyrics = onOpenLyrics
                            )
                            PlayerTab.EGALISEUR -> EqualizerPanel(
                                enabled = eqEnabled,
                                preset = eqPreset,
                                levels = eqLevels,
                                onEnabledChange = onEqEnabledChange,
                                onPresetChange = onEqPresetChange,
                                onBandChange = onEqBandChange,
                                onReset = onEqReset
                            )
                            PlayerTab.INFOS -> InfoTabContent(track = track, durationMs = effectiveDuration)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 045 — Pilule d'onglets en verre.
                    PlayerTabsPill(
                        selected = selectedTab,
                        onSelect = { selectedTab = it }
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                }
                }

                val isLandscapePlayer = maxHeight < 600.dp && maxWidth >= 600.dp
                if (isLandscapePlayer) {
                    // Paysage : pochette à gauche, contrôles défilables à droite.
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .navigationBarsPadding()
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(0.45f)
                                .fillMaxHeight(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            coverSection()
                        }
                        Column(
                            modifier = Modifier
                                .weight(0.55f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState())
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            controlsSection()
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(
                                if (isCompactScreen) Modifier.verticalScroll(rememberScrollState())
                                else Modifier
                            )
                            .navigationBarsPadding(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        coverSection()
                        controlsSection()
                    }
                }
        }
    }

    // Feuille de vitesse de lecture.
    if (showSpeedSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSpeedSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            SpeedSheetContent(
                playbackSpeed = playbackSpeed,
                onSetSpeed = onSetSpeed,
                onClose = { showSpeedSheet = false }
            )
        }
    }

    // 050/074 — Feuille de la minuterie de sommeil : deux ancrages (mi-hauteur et
    // plein écran), poignée 36×4 dp, fermeture par glissement, voile à 40 %.
    // Le contenu ne défile que lorsque la feuille est pleine.
    if (showSleepSheet) {
        AnchoredSheet(onDismissed = { showSleepSheet = false }) { isFull ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState(), enabled = isFull)
            ) {
                SleepTimerSheetContent(
                    active = sleepRemainingMs > 0L,
                    endOfTrack = sleepEndOfTrack,
                    remainingMs = sleepRemainingMs,
                    selectedMinutes = sleepMinutes,
                    onSetMinutes = { minutes ->
                        onSetSleepTimerMinutes(minutes)
                        showSleepSheet = false
                    },
                    onSetEndOfTrack = {
                        onSetSleepTimerEndOfTrack()
                        showSleepSheet = false
                    },
                    onCancel = {
                        onCancelSleepTimer()
                        showSleepSheet = false
                    }
                )
            }
        }
    }

    // 049 — Feuille « Sortie audio ».
    if (showDevicesSheet) {
        AudioOutputSheet(
            audioManager = audioManager,
            devicesVersion = audioDevicesVersion,
            volumePercent = ((currentVolume / maxVolume.toFloat()) * 100f).roundToInt(),
            onVolumeUp = {
                val newVol = (currentVolume + 1f).coerceAtMost(maxVolume.toFloat())
                currentVolume = newVol
                audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, newVol.toInt(), 0)
            },
            onVolumeDown = {
                val newVol = (currentVolume - 1f).coerceAtLeast(0f)
                currentVolume = newVol
                audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, newVol.toInt(), 0)
            },
            onDismiss = { showDevicesSheet = false }
        )
    }

    // 047 — Menu d'appui long sur la pochette.
    if (showTrackMenu) {
        ModalBottomSheet(
            onDismissRequest = { showTrackMenu = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            TrackMenuSheetContent(
                track = track,
                onAddToPlaylist = {
                    showTrackMenu = false
                    onAddToPlaylist()
                },
                onOpenAlbum = {
                    showTrackMenu = false
                    track?.album?.let(onOpenAlbum)
                },
                onOpenArtist = {
                    showTrackMenu = false
                    track?.artist?.let(onOpenArtist)
                },
                onShare = {
                    showTrackMenu = false
                    onShare()
                }
            )
        }
    }
}

/** Barre supérieure : réduire (↓), menu ⋯ (minuterie, vitesse, karaoké, playlist, partager), appareils. */
@Composable
private fun PlayerTopBar(
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
                            tint = if (sleepActive) MusicProCyanNeon else MaterialTheme.colorScheme.onSurfaceVariant,
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

/**
 * Zone centrale : pochette carrée (défaut), vinyle ou minimal (048).
 * Gestes 047 : glisser vers le bas = réduire ; double tap gauche/droite = ±10 s avec onde ;
 * appui long = menu. En vinyle, le geste horizontal reste réservé au morceau précédent/suivant.
 */
@Composable
private fun ImmersiveCover(
    track: AudioTrackEntity?,
    presentationMode: PlayerPresentation,
    isPlaying: Boolean,
    progressMs: Long,
    vinylRotation: Float,
    animationsEnabled: Boolean,
    backgroundColor: Color,
    onBack: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeekBy: (Long) -> Unit,
    onOpenTrackMenu: () -> Unit
) {
    when (presentationMode) {
        PlayerPresentation.VINYLE -> VinylPresentation(
            track = track,
            isPlaying = isPlaying,
            progressMs = progressMs,
            vinylRotation = vinylRotation,
            onNext = onNext,
            onPrevious = onPrevious,
            onPlayPause = onPlayPause
        )
        PlayerPresentation.MINIMAL -> MinimalPresentation(
            track = track,
            backgroundColor = backgroundColor
        )
        PlayerPresentation.POCHETTE -> SquareCoverPresentation(
            track = track,
            isPlaying = isPlaying,
            animationsEnabled = animationsEnabled,
            onBack = onBack,
            onPlayPause = onPlayPause,
            onSeekBy = onSeekBy,
            onOpenTrackMenu = onOpenTrackMenu
        )
    }
}

/** Présentation par défaut : pochette carrée pleine largeur, gestes complets (041/047/048). */
@Composable
private fun SquareCoverPresentation(
    track: AudioTrackEntity?,
    isPlaying: Boolean,
    animationsEnabled: Boolean,
    onBack: () -> Unit,
    onPlayPause: () -> Unit,
    onSeekBy: (Long) -> Unit,
    onOpenTrackMenu: () -> Unit
) {
    // Onde du double tap (047) : côté gauche/droite, s'estompe en 400 ms.
    var rippleTrigger by remember { mutableIntStateOf(0) }
    var rippleSideStart by remember { mutableStateOf(true) }
    val rippleAlpha = remember { Animatable(0f) }

    LaunchedEffect(rippleTrigger) {
        if (rippleTrigger > 0) {
            rippleAlpha.snapTo(0.55f)
            rippleAlpha.animateTo(0f, tween(durationMillis = 400))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(track?.id) {
                var dragY = 0f
                detectVerticalDragGestures(
                    onDragStart = { dragY = 0f },
                    onVerticalDrag = { change, amount ->
                        change.consume()
                        dragY += amount
                    },
                    onDragEnd = {
                        if (dragY > 90.dp.toPx()) onBack()
                        dragY = 0f
                    },
                    onDragCancel = { dragY = 0f }
                )
            }
            .pointerInput(track?.id) {
                detectTapGestures(
                    onTap = { onPlayPause() },
                    onDoubleTap = { offset ->
                        val isLeft = offset.x < size.width / 2f
                        rippleSideStart = isLeft
                        rippleTrigger++
                        onSeekBy(if (isLeft) -10_000L else 10_000L)
                    },
                    onLongPress = { onOpenTrackMenu() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        var imageLoadError by remember(track?.id) { mutableStateOf(false) }

        if (!track?.albumArtUri.isNullOrBlank() && !imageLoadError) {
            AsyncImage(
                model = track?.albumArtUri,
                contentDescription = track?.title,
                contentScale = ContentScale.Crop,
                onError = { imageLoadError = true },
                modifier = Modifier
                    .fillMaxSize()
                    .sharedCover()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .sharedCover()
                    .background(MusicProPrimaryGradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Album,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(84.dp)
                )
            }
        }

        // Onde visuelle du double tap.
        if (rippleAlpha.value > 0f) {
            Box(
                modifier = Modifier
                    .align(if (rippleSideStart) Alignment.CenterStart else Alignment.CenterEnd)
                    .fillMaxSize(0.5f)
                    .graphicsLayer { alpha = rippleAlpha.value }
                    .background(
                        Brush.radialGradient(
                            listOf(Color.White.copy(alpha = 0.5f), Color.White.copy(alpha = 0f))
                        ),
                        CircleShape
                    )
            )
        }
    }
}

/** Présentation vinyle (048) : disque 33 tours, geste horizontal = morceau précédent/suivant. */
@Composable
private fun VinylPresentation(
    track: AudioTrackEntity?,
    isPlaying: Boolean,
    progressMs: Long,
    vinylRotation: Float,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val vinylSize = minOf(maxWidth * 0.72f, maxHeight * 0.88f).coerceAtLeast(150.dp)
        Box(
            modifier = Modifier
                .size(vinylSize)
                .pointerInput(track?.id) {
                    var horizontalDrag = 0f
                    detectHorizontalDragGestures(
                        onDragStart = { horizontalDrag = 0f },
                        onHorizontalDrag = { change, amount ->
                            change.consume()
                            horizontalDrag += amount
                        },
                        onDragEnd = {
                            when {
                                horizontalDrag > 80f -> onPrevious()
                                horizontalDrag < -80f -> onNext()
                            }
                            horizontalDrag = 0f
                        },
                        onDragCancel = { horizontalDrag = 0f }
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
            Box(
                modifier = Modifier
                    .size(vinylSize * 0.88f)
                    .clip(CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(vinylSize * 0.74f)
                    .clip(CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f), CircleShape)
            )

            val centerArtSize = vinylSize * 0.62f
            Box(
                modifier = Modifier
                    .sharedCover()
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

                Box(
                    modifier = Modifier
                        .size(vinylSize * 0.10f)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.background)
                        .border(1.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.50f), CircleShape)
                )
            }
        }
    }
}

/** Présentation minimaliste (048) : texte seul sur fond uni, sans pochette. */
@Composable
private fun MinimalPresentation(
    track: AudioTrackEntity?,
    backgroundColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = track?.title ?: stringResource(R.string.np_no_track),
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
        )
        Text(
            text = track?.artist ?: stringResource(R.string.np_player_name),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MusicProCyanLight,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
        )
    }
}

/** Pilule de verre : titre (2 lignes max) + artiste, boutons ronds 40 dp Radio/File/J'aime (042). */
@Composable
private fun TitleArtistPill(
    track: AudioTrackEntity?,
    isFavorite: Boolean,
    heartScale: Float,
    onToggleFavorite: () -> Unit,
    onOpenRadio: () -> Unit,
    onOpenQueue: () -> Unit
) {
    GlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(32.dp),
        material = GlassMaterials.regular()
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track?.title ?: stringResource(R.string.np_no_track),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 17.sp
                )
                Text(
                    text = track?.artist ?: stringResource(R.string.np_player_name),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MusicProCyanLight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Round40Button(
                icon = Icons.Default.Radio,
                contentDescription = stringResource(R.string.np_radio),
                onClick = onOpenRadio
            )
            Round40Button(
                icon = Icons.AutoMirrored.Filled.QueueMusic,
                contentDescription = stringResource(R.string.np_queue),
                onClick = onOpenQueue
            )
            Round40Button(
                icon = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = stringResource(R.string.np_favorite),
                tint = if (isFavorite) MusicProFavorite else MaterialTheme.colorScheme.onSurfaceVariant,
                onClick = onToggleFavorite,
                testTag = "now_playing_favorite_button",
                scale = heartScale
            )
        }
    }
}

/** Bouton rond de 40 dp des pilules (042). */
@Composable
private fun Round40Button(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    testTag: String? = null,
    scale: Float = 1f
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier
                .size(20.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
        )
    }
}

/** Pilule « Passer à la vidéo » (046). */
@Composable
private fun VideoPill(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, MusicProCyanNeon.copy(alpha = 0.5f)),
        modifier = Modifier.testTag("now_playing_video_pill")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.SpeakerGroup,
                contentDescription = null,
                tint = MusicProCyanNeon,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = stringResource(R.string.np_switch_to_video),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MusicProCyanNeon,
                modifier = Modifier.padding(start = 6.dp)
            )
        }
    }
}

/** Pastille de compte à rebours de la minuterie (050). */
@Composable
private fun SleepCountdownPill(
    remainingMs: Long,
    endOfTrack: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MusicProVioletPrimary.copy(alpha = 0.25f),
        border = BorderStroke(1.dp, MusicProCyanNeon.copy(alpha = 0.55f)),
        modifier = Modifier.testTag("now_playing_sleep_pill")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = null,
                tint = MusicProCyanNeon,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = if (endOfTrack) {
                    stringResource(R.string.np_sleep_end_of_track)
                } else {
                    formatTime(remainingMs)
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MusicProCyanNeon,
                modifier = Modifier.padding(start = 5.dp)
            )
        }
    }
}

/**
 * Barre de progression fine et tactile (043) : 4 dp (10 dp au toucher), zone tactile de 40 dp,
 * écoulié à gauche, restant/total à droite, aperçu flottant du temps pendant le glissement.
 */
@Composable
private fun FineProgressBar(
    positionMs: Long,
    durationMs: Long,
    isSeeking: Boolean,
    onSeekStart: (Float) -> Unit,
    onSeekDrag: (Float) -> Unit,
    onSeekEnd: () -> Unit,
    onSeekTap: (Float) -> Unit
) {
    val fraction = (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    val barHeight by animateFloatAsState(
        targetValue = if (isSeeking) 10f else 4f,
        animationSpec = tween(durationMillis = 120),
        label = "np_bar_height"
    )
    val remainingMs = (durationMs - positionMs).coerceAtLeast(0L)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                formatTime(positionMs),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MusicProCyanLight
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "−" + formatTime(remainingMs),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MusicProCyanLight
                )
                Text(
                    " / " + formatTime(durationMs),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .semantics {
                    progressBarRangeInfo = ProgressBarRangeInfo(fraction, 0f..1f)
                    contentDescription = "Position de lecture"
                    stateDescription = "${formatTime(positionMs)} sur ${formatTime(durationMs)}"
                }
                .pointerInput(durationMs) {
                    detectTapGestures { offset ->
                        onSeekTap((offset.x / size.width.toFloat()).coerceIn(0f, 1f))
                    }
                }
                .pointerInput(durationMs) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            onSeekStart((offset.x / size.width.toFloat()).coerceIn(0f, 1f))
                        },
                        onHorizontalDrag = { change, _ ->
                            change.consume()
                            onSeekDrag((change.position.x / size.width.toFloat()).coerceIn(0f, 1f))
                        },
                        onDragEnd = { onSeekEnd() },
                        onDragCancel = { onSeekEnd() }
                    )
                },
            contentAlignment = Alignment.CenterStart
        ) {
            // Aperçu flottant du temps au-dessus du doigt (043).
            if (isSeeking) {
                Row(modifier = Modifier.matchParentSize()) {
                    Box(modifier = Modifier.weight(fraction, fill = true))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.75f),
                        modifier = Modifier.offsetFromTop()
                    ) {
                        Text(
                            text = formatTime(positionMs),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Box(modifier = Modifier.weight(1f - fraction, fill = true))
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(barHeight.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f))
                    .align(Alignment.CenterStart)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(barHeight.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(MusicProCyanNeon)
                    .align(Alignment.CenterStart)
            )
        }
    }
}

/** Décalage vertical de l'aperçu flottant au-dessus de la barre. */
private fun Modifier.offsetFromTop(): Modifier = this.then(
    Modifier.graphicsLayer { translationY = -18.dp.toPx() }
)

/** 044 — Transport : aléatoire 28 dp, précédent 64 dp, lecture 88 dp, suivant 64 dp, répéter 28 dp. */
@Composable
private fun TransportRow(
    isPlaying: Boolean,
    isShuffleEnabled: Boolean,
    repeatMode: Int,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .testTag("now_playing_transport_row"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StateDotButton(
            icon = Icons.Default.Shuffle,
            contentDescription = stringResource(R.string.np_shuffle),
            active = isShuffleEnabled,
            size = 28.dp,
            onClick = onToggleShuffle
        )

        TransportCircle(size = 64.dp, onClick = onPrevious, testTag = "now_playing_previous_button") {
            Icon(
                Icons.Default.SkipPrevious,
                contentDescription = stringResource(R.string.np_previous),
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(32.dp)
            )
        }

        TransportCircle(
            size = 88.dp,
            onClick = onPlayPause,
            testTag = "now_playing_play_pause_button",
            background = MusicProPrimaryGradient
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = stringResource(
                    if (isPlaying) R.string.np_pause else R.string.np_play
                ),
                tint = Color.White,
                modifier = Modifier.size(42.dp)
            )
        }

        TransportCircle(size = 64.dp, onClick = onNext, testTag = "now_playing_next_button") {
            Icon(
                Icons.Default.SkipNext,
                contentDescription = stringResource(R.string.np_next),
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(32.dp)
            )
        }

        StateDotButton(
            icon = if (repeatMode == androidx.media3.common.Player.REPEAT_MODE_ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
            contentDescription = stringResource(R.string.np_repeat),
            active = repeatMode != androidx.media3.common.Player.REPEAT_MODE_OFF,
            size = 28.dp,
            onClick = onToggleRepeat
        )
    }
}

/** Bouton circulaire de transport (verre simple) avec taille paramétrable (044). */
@Composable
private fun TransportCircle(
    size: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
    testTag: String? = null,
    background: Brush? = null,
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = background?.let { Color.Transparent }
            ?: MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
        border = if (background == null) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)) else null,
        modifier = Modifier
            .size(size)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (background != null) {
                        Modifier.background(background, CircleShape)
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center,
            content = { Row(verticalAlignment = Alignment.CenterVertically, content = content) }
        )
    }
}

/** Icône 28 dp d'état (aléatoire/répéter) avec point sous l'icône quand actif (044, accessibilité). */
@Composable
private fun StateDotButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    active: Boolean,
    size: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (active) MusicProCyanNeon else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(size)
        )
        // Point d'état : visible uniquement quand le réglage est actif.
        Box(
            modifier = Modifier
                .padding(top = 3.dp)
                .size(4.dp)
                .background(
                    if (active) MusicProCyanNeon else Color.Transparent,
                    CircleShape
                )
        )
    }
}

/** 045 — Onglet Lecture : aperçu de la ligne karaoké synchronisée. */
@Composable
private fun LectureTabContent(
    lyricsData: LyricsData,
    positionMs: Long,
    onOpenLyrics: () -> Unit
) {
    val activeLineIndex = remember(lyricsData, positionMs) {
        lyricsData.findActiveLineIndex(positionMs)
    }
    val activeLineText = if (activeLineIndex in lyricsData.lines.indices) {
        lyricsData.lines[activeLineIndex].text
    } else null

    Surface(
        onClick = onOpenLyrics,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f),
        border = BorderStroke(
            1.dp,
            if (activeLineText != null) MusicProCyanNeon.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("now_playing_lyrics_preview_card")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Subtitles,
                contentDescription = null,
                tint = if (activeLineText != null) MusicProCyanNeon else MusicProVioletLight,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = activeLineText?.ifBlank { "…" }
                    ?: stringResource(R.string.np_open_synced_lyrics),
                fontSize = 12.sp,
                fontWeight = if (activeLineText != null) FontWeight.SemiBold else FontWeight.Normal,
                color = if (activeLineText != null) MusicProCyanNeon else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            )
            Text(
                text = if (lyricsData.lines.isNotEmpty()) {
                    stringResource(R.string.np_lyrics_synced)
                } else {
                    stringResource(R.string.np_lyrics_open)
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MusicProVioletLight,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

/** 045 — Onglet Paroles : liste synchronisée défilante, ligne active surlignée. */
@Composable
private fun LyricsTabContent(
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
                    tint = MusicProVioletLight,
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
                    color = if (isActive) MusicProCyanNeon else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/** 045 — Onglet Infos : fiche technique du morceau. */
@Composable
private fun InfoTabContent(track: AudioTrackEntity?, durationMs: Long) {
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
private fun PlayerTabsPill(
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
        color = if (isSelected) MusicProCyanNeon.copy(alpha = 0.22f) else Color.Transparent,
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
                tint = if (isSelected) MusicProCyanNeon else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(17.dp)
            )
            Text(
                text = stringResource(labelRes),
                fontSize = 9.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MusicProCyanNeon else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

/** Contenu de la feuille de vitesse de lecture. */
@Composable
private fun SpeedSheetContent(
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
                Text(stringResource(R.string.np_close), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MusicProCyanNeon)
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
                    color = if (isSelected) MusicProVioletPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) MusicProCyanNeon else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
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
private fun SleepTimerSheetContent(
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
            Icon(Icons.Default.Timer, contentDescription = null, tint = MusicProCyanNeon, modifier = Modifier.size(20.dp))
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
                    color = MusicProCyanNeon,
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
        color = if (selected) MusicProVioletPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
        border = BorderStroke(
            1.dp,
            if (selected) MusicProCyanNeon else MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)
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

/** 047 — Menu d'appui long sur la pochette : playlist, album, artiste, partager. */
@Composable
private fun TrackMenuSheetContent(
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
            Icon(icon, contentDescription = null, tint = MusicProCyanNeon, modifier = Modifier.size(20.dp))
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

private fun formatTime(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}
