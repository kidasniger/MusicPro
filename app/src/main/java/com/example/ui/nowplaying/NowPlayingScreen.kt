package com.example.ui.nowplaying

import android.content.Context
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.widget.Toast
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.AudioTrackEntity
import com.example.data.preferences.PlayerPresentation
import com.example.lyrics.LyricsData
import com.example.ui.components.glass.LocalReduceAnimations
import com.example.ui.components.glass.rememberAnimationsEnabled
import com.example.ui.components.glass.rememberSystemAnimationsEnabled
import com.example.ui.components.AnchoredSheet
import com.example.ui.theme.darkened
import com.example.ui.theme.rememberTrackDominantColor
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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
 *   rebours près du titre et fondu de volume sur les 10 dernières secondes ;
 * - 101 : aperçu karaoké avec lueur douce dans l'onglet Lecture, espace animé d'invitation
 *   quand aucune parole n'existe, transport équilibré (pastilles rondes pour aléatoire et
 *   répéter), vinyle entièrement animé avec reflet qui balaie la surface, et titres affichés
 *   sans compteur de doublon « (0) ».
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
                                animationsEnabled = animationsEnabled,
                                isPlaying = isPlaying,
                                vinylRotation = vinylRotation,
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
