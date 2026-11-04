package com.example.ui.home

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import java.io.File
import java.util.Calendar
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.rotate
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.outlined.Radio
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.TextButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.glass.rememberAnimationsEnabled
import com.example.ui.navigation.ScrollToTopEffect
import com.example.ui.navigation.TabScrollToTopBus
import com.example.util.ShortcutActions
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.AudioTrackEntity
import com.example.data.local.PlaylistSummary
import com.example.data.preferences.PlayerPresentation
import com.example.data.preferences.LibrarySortMode
import com.example.permissions.PermissionUtils
import com.example.ui.adaptive.LocalWindowWidthClass
import com.example.ui.adaptive.widthSupportsSideRail
import com.example.ui.artwork.preloadArtwork
import com.example.ui.audio.AudioViewModel
import com.example.ui.audio.LibraryTab
import com.example.ui.components.AppUpdateDialog
import com.example.ui.components.FavoritesScreen
import com.example.ui.components.PlaceholderScreen
import com.example.ui.components.QueueSheet
import com.example.ui.components.EqualizerSheet
import com.example.ui.components.KaraokeSettingsDialog
import com.example.ui.components.EmptyAudioStateView
import com.example.ui.components.EmptyStateVariant
import com.example.ui.components.SkeletonTracksList
import com.example.ui.components.TrackActionsSheet
import com.example.ui.components.TrackShareCardDialog
import com.example.ui.components.glass.GlassBackdropHost
import com.example.ui.components.glass.GlassMiniPlayer
import com.example.ui.components.glass.GlassNavBar
import com.example.ui.components.glass.GlassNavItem
import com.example.ui.components.glass.SharedCoverHost
import com.example.ui.components.glass.SharedCoverVisibility
import com.example.ui.detail.DetailHost
import com.example.ui.detail.DetailTarget
import com.example.ui.settings.SettingsViewModel
import com.example.updater.UpdateCheckState
import com.example.ui.library.LibraryScreen
import com.example.ui.library.TrackRowItem
import com.example.ui.playlist.AddTracksToPlaylistDialog
import com.example.ui.playlist.AddToPlaylistChooserDialog
import com.example.ui.playlist.PlaylistDetailScreen
import com.example.ui.lyrics.GroqPreviewDialog
import com.example.ui.lyrics.LrclibSearchScreen
import com.example.ui.lyrics.LyricsScreen
import com.example.ui.nowplaying.NowPlayingScreen
import com.example.ui.radio.RadioScreen
import com.example.ui.search.SearchScreen
import com.example.ui.stats.StatsScreen
import com.example.ui.chat.ChatScreen
import com.example.ui.theme.MusicProCyanLight
import androidx.compose.ui.graphics.toArgb
import com.example.ui.theme.MusicProCyanNeon
import com.example.ui.theme.MusicProCyanVibrant
import com.example.ui.theme.MusicProGreenEmerald
import com.example.ui.theme.MusicProPrimaryGradient
import com.example.ui.theme.MusicProSuccess
import com.example.ui.theme.MusicProVioletGlow
import com.example.ui.theme.MusicProVioletLight
import com.example.ui.theme.MusicProVioletPastel
import com.example.ui.theme.MusicProVioletPrimary
import com.example.ui.theme.MusicProVioletVibrant
import com.example.ui.nowplaying.VideoMatcher
import com.example.ui.nowplaying.VideoPlayerScreen
import com.example.ui.whatsnew.NewScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

/**
 * Sections de la barre de navigation flottante, dans l'ordre d'affichage.
 * Les noms (HOME, LIBRARY, SEARCH) sont conservés : ils servent de clé de sauvegarde d'état.
 */
enum class NavigationSection(
    @StringRes val labelRes: Int,
    val icon: ImageVector,
    val selectedIcon: ImageVector
) {
    HOME(R.string.nav_home, Icons.Outlined.Home, Icons.Default.Home),
    NEW(R.string.nav_new, Icons.Outlined.NewReleases, Icons.Default.NewReleases),
    RADIO(R.string.nav_radio, Icons.Outlined.Radio, Icons.Default.Radio),
    LIBRARY(R.string.nav_library, Icons.AutoMirrored.Outlined.QueueMusic, Icons.AutoMirrored.Filled.QueueMusic),
    SEARCH(R.string.nav_search, Icons.Outlined.Search, Icons.Default.Search),
    STATS(R.string.nav_stats, Icons.Outlined.BarChart, Icons.Default.BarChart),
    CHAT(R.string.nav_chat, Icons.Outlined.ChatBubbleOutline, Icons.Filled.ChatBubble)
}

@Composable
fun HomeScreen(
    onOpenSettings: () -> Unit,
    onOpenOnboarding: () -> Unit = {},
    initialOpenNowPlaying: Boolean = false,
    initialOpenQueue: Boolean = false,
    initialOpenLyrics: Boolean = false,
    initialShufflePlay: Boolean = false,
    initialOpenSearch: Boolean = false,
    mediaUiRequestId: Long = 0L,
    audioViewModel: AudioViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    // Mise à jour 071 — navigation à onglets : un NavHost imbriqué dont chaque
    // destination est un onglet. Avec `saveState`/`restoreState`, chaque onglet garde
    // sa pile, son état de défilement et son état sauvegardé.
    val tabNavController = rememberNavController()
    val tabEntry by tabNavController.currentBackStackEntryAsState()
    val currentSection = tabEntry?.destination?.route
        ?.let { route -> NavigationSection.entries.firstOrNull { it.name == route } }
        ?: NavigationSection.HOME

    // Un second tap sur l'onglet actif remonte en haut de la liste (défilement animé).
    val selectTab: (NavigationSection, Boolean) -> Unit = { section, fromUserTap ->
        if (section == currentSection) {
            if (fromUserTap) TabScrollToTopBus.request(section.name)
        } else {
            tabNavController.navigate(section.name) {
                popUpTo(tabNavController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }
    var isNowPlayingOpen by rememberSaveable { mutableStateOf(initialOpenNowPlaying) }
    var isLyricsOpen by rememberSaveable { mutableStateOf(false) }
    var isLrclibSearchOpen by rememberSaveable { mutableStateOf(false) }
    var isFavoritesOpen by rememberSaveable { mutableStateOf(false) }
    var isQueueOpen by rememberSaveable { mutableStateOf(false) }
    var isEqualizerOpen by rememberSaveable { mutableStateOf(false) }
    var isKaraokeSettingsOpen by rememberSaveable { mutableStateOf(false) }
    // Écran de détail plein écran ouvert (album, bientôt artiste) : voir DetailTarget.
    var openDetailKey by rememberSaveable { mutableStateOf<String?>(null) }

    // Onglets de la barre de navigation flottante
    val navItems = remember {
        NavigationSection.entries.map { section ->
            GlassNavItem(
                key = section.name,
                labelRes = section.labelRes,
                icon = section.icon,
                selectedIcon = section.selectedIcon,
                testTag = "nav_tab_" + section.name.lowercase()
            )
        }
    }

    // Ouvre l'écran demandé depuis la notification, y compris lorsque l'application
    // était déjà ouverte et reçoit un nouvel Intent via onNewIntent().
    LaunchedEffect(mediaUiRequestId) {
        if (initialOpenQueue || initialOpenLyrics || initialOpenNowPlaying) {
            openDetailKey = null
        }
        when {
            initialOpenQueue -> {
                isNowPlayingOpen = false
                isLyricsOpen = false
                isQueueOpen = true
            }
            initialOpenLyrics -> {
                isNowPlayingOpen = false
                isQueueOpen = false
                isLyricsOpen = true
            }
            initialOpenNowPlaying -> {
                isLyricsOpen = false
                isQueueOpen = false
                isNowPlayingOpen = true
            }
        }
    }

    // Les mises à jour sont vérifiées depuis les paramètres afin de ne jamais
    // bloquer ou faire tomber l'écran d'accueil lors de la publication d'une nouvelle release.
    val updateCheckState by settingsViewModel.updateCheckState.collectAsStateWithLifecycle()
    val downloadState by settingsViewModel.downloadState.collectAsStateWithLifecycle()
    var isUpdateDialogDismissed by remember { mutableStateOf(false) }

    val tracks by audioViewModel.tracks.collectAsStateWithLifecycle()
    val recentTracks by audioViewModel.recentTracks.collectAsStateWithLifecycle()
    val albums by audioViewModel.albumSummaries.collectAsStateWithLifecycle()
    val artists by audioViewModel.artistSummaries.collectAsStateWithLifecycle()
    val folders by audioViewModel.folderSummaries.collectAsStateWithLifecycle()
    val selectedLibraryTab by audioViewModel.selectedTab.collectAsStateWithLifecycle()
    // Mise à jour 055 — tri mémorisé de la bibliothèque.
    val librarySort by audioViewModel.librarySort.collectAsStateWithLifecycle()

    val searchQuery by audioViewModel.searchQuery.collectAsStateWithLifecycle()
    val searchFilter by audioViewModel.searchFilter.collectAsStateWithLifecycle()
    val searchResults by audioViewModel.searchResults.collectAsStateWithLifecycle()

    val isScanning by audioViewModel.isScanning.collectAsStateWithLifecycle()
    val statusMessage by audioViewModel.statusMessage.collectAsStateWithLifecycle()
    val currentTrack by audioViewModel.currentTrack.collectAsStateWithLifecycle()
    val isPlaying by audioViewModel.isPlaying.collectAsStateWithLifecycle()
    val progressMs by audioViewModel.progressMs.collectAsStateWithLifecycle()
    val durationMs by audioViewModel.durationMs.collectAsStateWithLifecycle()
    val repeatMode by audioViewModel.repeatMode.collectAsStateWithLifecycle()
    val isShuffleEnabled by audioViewModel.isShuffleEnabled.collectAsStateWithLifecycle()
    val playbackSpeed by audioViewModel.playbackSpeed.collectAsStateWithLifecycle()
    val playbackErrorMessage by audioViewModel.playbackErrorMessage.collectAsStateWithLifecycle()
    val favorites by audioViewModel.favorites.collectAsStateWithLifecycle()
    val searchHistory by audioViewModel.searchHistory.collectAsStateWithLifecycle()
    val queue by audioViewModel.queue.collectAsStateWithLifecycle()
    val queueIndex by audioViewModel.queueIndex.collectAsStateWithLifecycle()

    // Mise à jour 087 — précharge la pochette du morceau suivant (cache mémoire + disque)
    // pour qu'elle s'affiche instantanément au passage au titre suivant.
    val nextQueueTrack = queue.getOrNull(queueIndex + 1)
    val homeContext = LocalContext.current
    LaunchedEffect(nextQueueTrack?.id) {
        homeContext.preloadArtwork(nextQueueTrack?.albumArtUri)
    }
    val eqEnabled by audioViewModel.eqEnabled.collectAsStateWithLifecycle()
    val eqPreset by audioViewModel.eqPreset.collectAsStateWithLifecycle()
    val eqLevels by audioViewModel.eqLevels.collectAsStateWithLifecycle()
    val karaokeFontSize by audioViewModel.karaokeFontSize.collectAsStateWithLifecycle()
    val karaokeActiveColor by audioViewModel.karaokeActiveColor.collectAsStateWithLifecycle()
    val karaokeOffsetMs by audioViewModel.karaokeOffsetMs.collectAsStateWithLifecycle()
    val lyricsData by audioViewModel.lyricsData.collectAsStateWithLifecycle()
    val isLyricsLoading by audioViewModel.isLyricsLoading.collectAsStateWithLifecycle()

    // Mise à jour 048 — présentation du lecteur (pochette, vinyle, minimal).
    val playerPresentation by settingsViewModel.playerPresentation.collectAsStateWithLifecycle()

    val isGroqTranscribing by audioViewModel.isGroqTranscribing.collectAsStateWithLifecycle()
    val groqProgressMessage by audioViewModel.groqProgressMessage.collectAsStateWithLifecycle()
    val groqErrorMessage by audioViewModel.groqErrorMessage.collectAsStateWithLifecycle()
    val groqTranscriptionResult by audioViewModel.groqTranscriptionResult.collectAsStateWithLifecycle()

    val playlists by audioViewModel.playlists.collectAsStateWithLifecycle()
    val selectedPlaylist by audioViewModel.selectedPlaylist.collectAsStateWithLifecycle()
    val selectedPlaylistTracks by audioViewModel.selectedPlaylistTracks.collectAsStateWithLifecycle()
    var isAddTracksToPlaylistOpen by remember { mutableStateOf(false) }
    var tracksForPlaylistChooser by remember { mutableStateOf<List<AudioTrackEntity>>(emptyList()) }
    // Mise à jour 059 — morceau dont la feuille d'actions ⋯ est ouverte.
    var trackForActions by remember { mutableStateOf<AudioTrackEntity?>(null) }
    // Cible des paroles quand elles sont ouvertes depuis la feuille d'actions.
    var lyricsTargetTrack by remember { mutableStateOf<AudioTrackEntity?>(null) }
    var trackForShareCard by remember { mutableStateOf<AudioTrackEntity?>(null) }

    val saveFeedbackMessage by audioViewModel.saveFeedbackMessage.collectAsStateWithLifecycle()

    // Gestion de l'autorisation d'écriture Scoped Storage (style Musicolet, Android 11+)
    val writePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        val granted = result.resultCode == Activity.RESULT_OK
        audioViewModel.onWritePermissionResult(granted)
    }

    // Gestion de l'autorisation d'écriture classique (Android 10 et inférieur)
    val legacyPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        audioViewModel.onWritePermissionResult(granted)
    }

    // Écoute des requêtes d'autorisation système émises par le ViewModel
    LaunchedEffect(Unit) {
        audioViewModel.intentSenderRequest.collect { request ->
            try {
                writePermissionLauncher.launch(request)
            } catch (e: Exception) {
                Log.e("HomeScreen", "Erreur lancement IntentSenderRequest: ${e.message}", e)
                audioViewModel.onWritePermissionResult(false)
            }
        }
    }

    LaunchedEffect(Unit) {
        audioViewModel.legacyWritePermissionRequest.collect {
            legacyPermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    LaunchedEffect(saveFeedbackMessage) {
        saveFeedbackMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(playbackErrorMessage) {
        playbackErrorMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            audioViewModel.clearPlaybackErrorMessage()
        }
    }

    // Le mini-player et le popup doivent refléter exclusivement le dernier morceau connu du lecteur.
    // Ne jamais retomber sur le premier morceau indexé pendant la restauration au démarrage.
    val activeTrack = currentTrack

    // Mise à jour 046 — vidéo associée au morceau (même nom de base ou clip du même dossier).
    var videoPath by remember { mutableStateOf<String?>(null) }
    var isVideoOpen by remember { mutableStateOf(false) }
    var videoStartMs by rememberSaveable { mutableLongStateOf(0L) }
    LaunchedEffect(activeTrack?.id) {
        videoPath = withContext(Dispatchers.IO) {
            runCatching {
                val path = activeTrack?.path
                val dir = path?.let { File(it).parentFile } ?: return@runCatching null
                val siblings = dir.list()?.toList().orEmpty()
                VideoMatcher.findVideoFile(path, siblings)?.let { dir.resolve(it).absolutePath }
            }.getOrNull()
        }
    }

    // Mise à jour 050 — minuterie de sommeil : état hissé pour que le compte à rebours,
    // le fondu de volume des 10 dernières secondes et la pause continuent écran fermé.
    val isPlayingNow by rememberUpdatedState(isPlaying)
    var sleepDeadlineMs by rememberSaveable { mutableLongStateOf(0L) }
    var sleepMinutes by rememberSaveable { mutableIntStateOf(0) }
    var sleepEndOfTrack by rememberSaveable { mutableStateOf(false) }
    var sleepOriginalVolume by rememberSaveable { mutableIntStateOf(-1) }
    var sleepTick by remember { mutableLongStateOf(0L) }
    LaunchedEffect(sleepDeadlineMs) {
        if (sleepDeadlineMs <= 0L) return@LaunchedEffect
        while (true) {
            sleepTick = System.currentTimeMillis()
            if (sleepTick >= sleepDeadlineMs) break
            delay(500L)
        }
    }
    val sleepRemainingMs: Long =
        if (sleepDeadlineMs > 0L) (sleepDeadlineMs - sleepTick).coerceAtLeast(0L) else 0L

    // Fondu de volume sur les 10 dernières secondes, puis pause et restauration du volume.
    LaunchedEffect(sleepDeadlineMs) {
        if (sleepDeadlineMs <= 0L) return@LaunchedEffect
        val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        try {
            while (true) {
                val remaining = sleepDeadlineMs - System.currentTimeMillis()
                if (remaining <= 0L) break
                if (remaining < 10_000L && am != null && sleepOriginalVolume > 0) {
                    val scaled = (sleepOriginalVolume * (remaining / 10_000f))
                        .roundToInt().coerceIn(0, sleepOriginalVolume)
                    runCatching { am.setStreamVolume(AudioManager.STREAM_MUSIC, scaled, 0) }
                }
                delay(200L)
            }
        } finally {
            if (isPlayingNow) {
                audioViewModel.togglePlayPause()
            }
            if (am != null && sleepOriginalVolume >= 0) {
                runCatching { am.setStreamVolume(AudioManager.STREAM_MUSIC, sleepOriginalVolume, 0) }
            }
            sleepDeadlineMs = 0L
            sleepMinutes = 0
        }
    }

    // Mode « Fin du morceau » : pause dès que la piste arrive à son terme.
    LaunchedEffect(sleepEndOfTrack, progressMs, durationMs) {
        if (!sleepEndOfTrack || !isPlaying || durationMs <= 0L) return@LaunchedEffect
        if (progressMs >= durationMs - 150L) {
            audioViewModel.togglePlayPause()
            sleepEndOfTrack = false
        }
    }

    val restoreSleepVolume: () -> Unit = {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        if (am != null && sleepOriginalVolume >= 0) {
            runCatching { am.setStreamVolume(AudioManager.STREAM_MUSIC, sleepOriginalVolume, 0) }
        }
    }

    SharedCoverHost(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            // Hauteur de la zone flottante (mini-lecteur + barre de navigation) pour réserver
            // l'espace en bas du contenu.
            var overlayHeightPx by remember { mutableIntStateOf(0) }
            val overlayHeight = with(LocalDensity.current) { overlayHeightPx.toDp() }
            val layoutDirection = LocalLayoutDirection.current

            // Mise à jour 085 — sur tablette/pliable, le même contenu est disposé à droite d'un
            // rail latéral (au lieu de la barre basse) ; on extrait donc l'hôte en slot réutilisable.
            val glassAppContent: @Composable (Modifier) -> Unit = { hostModifier ->
            GlassBackdropHost(
                modifier = hostModifier,
                background = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                start = innerPadding.calculateStartPadding(layoutDirection),
                                top = innerPadding.calculateTopPadding(),
                                end = innerPadding.calculateEndPadding(layoutDirection),
                                bottom = overlayHeight
                            )
                    ) {
                        // Mise à jour 071 — NavHost imbriqué : un destination par onglet.
                        val tabFadeMs = if (rememberAnimationsEnabled()) 220 else 0
                        NavHost(
                            navController = tabNavController,
                            startDestination = NavigationSection.HOME.name,
                            modifier = Modifier.fillMaxSize(),
                            enterTransition = { fadeIn(tween(tabFadeMs)) },
                            exitTransition = { fadeOut(tween(tabFadeMs)) },
                            popEnterTransition = { fadeIn(tween(tabFadeMs)) },
                            popExitTransition = { fadeOut(tween(tabFadeMs)) }
                        ) {
                            composable(NavigationSection.HOME.name) {
                                HomeExplorerContent(
                                    tracks = tracks,
                                    recentTracks = recentTracks,
                                    albumsCount = albums.size,
                                    artistsCount = artists.size,
                                    foldersCount = folders.size,
                                    albums = albums,
                                    playlists = playlists,
                                    isScanning = isScanning,
                                    currentPlayingTrack = currentTrack,
                                    isPlaying = isPlaying,
                                    favorites = favorites,
                                    onResumePlayback = { if (isPlaying) isNowPlayingOpen = true else audioViewModel.togglePlayPause() },
                                    onOpenSettings = onOpenSettings,
                                    onOpenOnboarding = onOpenOnboarding,
                                    onNavigateToLibrary = { selectTab(NavigationSection.LIBRARY, false) },
                                    onNavigateToRecent = {
                                        audioViewModel.selectTab(LibraryTab.ALL)
                                        selectTab(NavigationSection.LIBRARY, false)
                                    },
                                    onNavigateToSearch = { selectTab(NavigationSection.SEARCH, false) },
                                    onNavigateToNew = { selectTab(NavigationSection.NEW, false) },
                                    onNavigateToPlaylists = {
                                        audioViewModel.selectTab(LibraryTab.PLAYLISTS)
                                        selectTab(NavigationSection.LIBRARY, false)
                                    },
                                    onOpenFavorites = { isFavoritesOpen = true },
                                    onOpenAlbum = { openDetailKey = DetailTarget.albumKey(it) },
                                    onTrackClick = { audioViewModel.playTrack(it) },
                                    onToggleFavorite = { audioViewModel.toggleFavorite(it) },
                                    onOpenTrackActions = { trackForActions = it },
                                    onRefreshScan = { audioViewModel.refreshScan() }
                                )
                            }
                            composable(NavigationSection.NEW.name) {
                                // Mise à jour 021 : ajouts récents, jamais écoutés, albums récents.
                                NewScreen(
                                    tracks = tracks,
                                    currentPlayingTrack = currentTrack,
                                    isScanning = isScanning,
                                    onPlayTrack = { track, list -> audioViewModel.playTrack(track, list) },
                                    onScan = { audioViewModel.refreshScan() },
                                    onOpenAlbum = { openDetailKey = DetailTarget.albumKey(it) }
                                )
                            }
                            composable(NavigationSection.RADIO.name) {
                                // Mise à jour 022 : mixes automatiques générés localement.
                                RadioScreen(
                                    tracks = tracks,
                                    favorites = favorites,
                                    currentPlayingTrack = currentTrack,
                                    isPlaying = isPlaying,
                                    onPlayMix = { mixTracks -> audioViewModel.playPlaylist(mixTracks) }
                                )
                            }
                            composable(NavigationSection.LIBRARY.name) {
                                LibraryScreen(
                                    tracks = tracks,
                                    recentTracks = recentTracks,
                                    albums = albums,
                                    artists = artists,
                                    folders = folders,
                                    selectedTab = selectedLibraryTab,
                                    isScanning = isScanning,
                                    statusMessage = statusMessage,
                                    currentPlayingTrack = currentTrack,
                                    isPlaying = isPlaying,
                                    favorites = favorites,
                                    playlists = playlists,
                                    librarySort = librarySort,
                                    onPlaylistClick = { pl -> audioViewModel.selectPlaylist(pl.id) },
                                    onPlayPlaylistDirectly = { pl -> audioViewModel.playPlaylistDirectly(pl.id) },
                                    onCreatePlaylist = { name, desc -> audioViewModel.createPlaylist(name, desc) },
                                    onCreatePlaylistWithTracks = { name, desc, ids ->
                                        // Mise à jour 069 — playlist générée avec l'IA (morceaux locaux).
                                        audioViewModel.createPlaylist(name, desc, ids)
                                    },
                                    onRenamePlaylist = { id, name, desc -> audioViewModel.updatePlaylistName(id, name, desc) },
                                    onDeletePlaylist = { id -> audioViewModel.deletePlaylist(id) },
                                    onSelectTab = { audioViewModel.selectTab(it) },
                                    onSelectSort = { audioViewModel.selectLibrarySort(it) },
                                    onTrackClick = { audioViewModel.playTrack(it) },
                                    onToggleFavorite = { audioViewModel.toggleFavorite(it) },
                                    onPlayNextTracks = { list -> list.forEach { audioViewModel.addToQueue(it, playNext = true) } },
                                    onOpenTrackActions = { trackForActions = it },
                                    onAddTracksToPlaylist = { tracksForPlaylistChooser = it },
                                    onDeleteTracks = { audioViewModel.requestDeleteTracks(it) },
                                    onRefreshScan = { audioViewModel.refreshScan() }
                                )
                            }
                            composable(NavigationSection.STATS.name) {
                                StatsScreen()
                            }
                            composable(NavigationSection.CHAT.name) {
                                // 096 — les morceaux cités par l'assistant se lancent d'un tap.
                                ChatScreen(onPlayTrack = { audioViewModel.playTrack(it) })
                            }
                            composable(NavigationSection.SEARCH.name) {
                                SearchScreen(
                                    query = searchQuery,
                                    filter = searchFilter,
                                    searchResults = searchResults,
                                    allTracks = tracks,
                                    albums = albums,
                                    artists = artists,
                                    currentPlayingTrack = currentTrack,
                                    isPlaying = isPlaying,
                                    favorites = favorites,
                                    onQueryChange = { audioViewModel.setSearchQuery(it) },
                                    onFilterChange = { audioViewModel.setSearchFilter(it) },
                                    onTrackClick = { audioViewModel.playTrack(it) },
                                    onToggleFavorite = { audioViewModel.toggleFavorite(it) },
                                    searchHistory = searchHistory,
                                    onSearchSubmit = { audioViewModel.addSearchToHistory(it) },
                                    onRemoveSearchHistory = { audioViewModel.removeSearchFromHistory(it) },
                                    onClearSearchHistory = { audioViewModel.clearSearchHistory() },
                                    onOpenTrackActions = { trackForActions = it },
                                    onOpenAlbum = { openDetailKey = DetailTarget.albumKey(it) },
                                    onOpenArtist = { openDetailKey = DetailTarget.artistKey(it) },
                                    playlists = playlists,
                                    onPlaylistClick = { playlist ->
                                        audioViewModel.selectPlaylist(playlist.id)
                                        audioViewModel.selectTab(LibraryTab.PLAYLISTS)
                                        selectTab(NavigationSection.LIBRARY, false)
                                    }
                                )
                            }
                        }
                    }
                },
                foreground = {
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .onSizeChanged { overlayHeightPx = it.height }
                    ) {
                        // Mini-lecteur flottant en verre (56 dp, 8 dp au-dessus de la barre)
                        if (activeTrack != null) {
                            GlassMiniPlayer(
                                visible = !isNowPlayingOpen,
                                track = activeTrack,
                                isPlaying = isPlaying,
                                onPlayPauseToggle = { audioViewModel.togglePlayPause() },
                                onNext = { audioViewModel.playNext() },
                                onPrevious = { audioViewModel.playPrevious() },
                                onOpen = { isNowPlayingOpen = true },
                                modifier = Modifier.testTag("mini_player_bar")
                            )
                        }

                        // Barre de navigation flottante en pilule de verre — remplacée par le
                        // rail latéral sur les grandes largeurs (mise à jour 085).
                        if (!widthSupportsSideRail(LocalWindowWidthClass.current)) GlassNavBar(
                            items = navItems,
                            selectedKey = currentSection.name,
                            onSelect = { key ->
                                // Mise à jour 071 — second tap = retour en haut animé.
                                selectTab(
                                    NavigationSection.entries.firstOrNull { it.name == key }
                                        ?: NavigationSection.HOME,
                                    true
                                )
                            }
                        )
                    }
                }
            )
            }

            if (widthSupportsSideRail(LocalWindowWidthClass.current)) {
                // Mise à jour 085 — rail latéral (tablette, pliable déplié).
                Row(modifier = Modifier.fillMaxSize()) {
                    NavigationRail(
                        containerColor = Color.Transparent,
                        modifier = Modifier.testTag("nav_rail")
                    ) {
                        navItems.forEach { item ->
                            NavigationRailItem(
                                selected = item.key == currentSection.name,
                                onClick = {
                                    selectTab(
                                        NavigationSection.entries.firstOrNull { it.name == item.key }
                                            ?: NavigationSection.HOME,
                                        true
                                    )
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (item.key == currentSection.name) {
                                            item.selectedIcon
                                        } else {
                                            item.icon
                                        },
                                        contentDescription = stringResource(item.labelRes)
                                    )
                                },
                                label = { Text(stringResource(item.labelRes)) }
                            )
                        }
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        glassAppContent(Modifier.fillMaxSize())
                    }
                }
            } else {
                glassAppContent(Modifier.fillMaxSize())
            }
        }

        // Gestion intelligente du retour arrière (mise à jour 071) :
        // 1. Fermer les dialogues / sous-écrans ouverts (Paroles, Recherche Lrclib, …)
        // 2. Revenir à l'onglet Accueil (en conservant l'état de l'onglet quitté)
        // 3. Sur l'Accueil, demander deux retours pour quitter l'application.
        // Déclaré APRÈS le NavHost à onglets (priorité plus haute que son rappel interne)
        // et AVANT les écrans plein écran qui déclarent leur propre handler.
        var lastBackPressTime by remember { mutableStateOf(0L) }
        BackHandler {
            when {
                isLrclibSearchOpen -> {
                    isLrclibSearchOpen = false
                }
                isLyricsOpen -> {
                    isLyricsOpen = false
                }
                isAddTracksToPlaylistOpen -> {
                    isAddTracksToPlaylistOpen = false
                }
                tracksForPlaylistChooser.isNotEmpty() -> {
                    tracksForPlaylistChooser = emptyList()
                }
                trackForActions != null -> {
                    trackForActions = null
                }
                isKaraokeSettingsOpen -> isKaraokeSettingsOpen = false
                isEqualizerOpen -> isEqualizerOpen = false
                isQueueOpen -> isQueueOpen = false
                isFavoritesOpen -> isFavoritesOpen = false
                openDetailKey != null -> openDetailKey = null
                selectedPlaylist != null -> {
                    audioViewModel.selectPlaylist(null)
                }
                searchQuery.isNotBlank() -> {
                    audioViewModel.setSearchQuery("")
                }
                currentSection != NavigationSection.HOME -> {
                    tabNavController.popBackStack(
                        NavigationSection.HOME.name,
                        inclusive = false,
                        saveState = true
                    )
                }
                else -> {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastBackPressTime < 2000) {
                        (context as? Activity)?.finish()
                    } else {
                        lastBackPressTime = currentTime
                        Toast.makeText(context, "Appuyez encore une fois pour quitter", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Mise à jour 080 — raccourcis d'application : lecture aléatoire et recherche.
        var shufflePlayHandled by rememberSaveable { mutableStateOf(false) }
        LaunchedEffect(initialShufflePlay, tracks.size) {
            if (initialShufflePlay && !shufflePlayHandled && tracks.isNotEmpty()) {
                shufflePlayHandled = true
                audioViewModel.playPlaylist(tracks, shuffle = true)
            }
        }
        LaunchedEffect(initialOpenSearch) {
            if (initialOpenSearch) selectTab(NavigationSection.SEARCH, false)
        }

        // Modal plein écran Now Playing avec transition partagée de la pochette
        SharedCoverVisibility(visible = isNowPlayingOpen) {
            NowPlayingScreen(
                track = activeTrack,
                isPlaying = isPlaying,
                progressMs = progressMs,
                durationMs = durationMs,
                repeatMode = repeatMode,
                isShuffleEnabled = isShuffleEnabled,
                playbackSpeed = playbackSpeed,
                isFavorite = favorites.contains(activeTrack?.id ?: -1L),
                onBack = { isNowPlayingOpen = false },
                onPlayPause = { audioViewModel.togglePlayPause() },
                onNext = { audioViewModel.playNext() },
                onPrevious = { audioViewModel.playPrevious() },
                onSeekTo = { audioViewModel.seekTo(it) },
                onToggleRepeat = { audioViewModel.toggleRepeatMode() },
                onToggleShuffle = { audioViewModel.toggleShuffle() },
                onToggleFavorite = { activeTrack?.let { audioViewModel.toggleFavorite(it.id) } },
                onSetSpeed = { audioViewModel.setPlaybackSpeed(it) },
                onOpenQueue = { isQueueOpen = true },
                onOpenEqualizer = { isEqualizerOpen = true },
                onOpenKaraokeSettings = { isKaraokeSettingsOpen = true },
                onAddToPlaylist = { activeTrack?.let { tracksForPlaylistChooser = listOf(it) } },
                onShare = {
                    // Mise à jour 070 — carte de morceau (avec/sans pochette) au lieu du texte.
                    activeTrack?.let { track -> trackForShareCard = track }
                },
                lyricsData = lyricsData,
                onOpenLyrics = { isLyricsOpen = true },
                presentationMode = playerPresentation,
                hasVideo = videoPath != null,
                onOpenVideo = {
                    videoStartMs = progressMs
                    if (isPlayingNow) {
                        audioViewModel.togglePlayPause()
                    }
                    isVideoOpen = true
                },
                onOpenRadio = {
                    isNowPlayingOpen = false
                    selectTab(NavigationSection.RADIO, false)
                },
                onOpenAlbum = { album ->
                    isNowPlayingOpen = false
                    openDetailKey = DetailTarget.albumKey(album)
                },
                onOpenArtist = { artist ->
                    isNowPlayingOpen = false
                    openDetailKey = DetailTarget.artistKey(artist)
                },
                onSeekBy = { deltaMs ->
                    audioViewModel.seekTo((progressMs + deltaMs).coerceAtLeast(0L))
                },
                eqEnabled = eqEnabled,
                eqPreset = eqPreset,
                eqLevels = eqLevels,
                onEqEnabledChange = { audioViewModel.setEqualizerEnabled(it) },
                onEqPresetChange = { audioViewModel.setEqualizerPreset(it) },
                onEqBandChange = { index, value -> audioViewModel.setEqualizerBand(index, value) },
                onEqReset = { audioViewModel.resetEqualizer() },
                sleepRemainingMs = sleepRemainingMs,
                sleepMinutes = sleepMinutes,
                sleepEndOfTrack = sleepEndOfTrack,
                onSetSleepTimerMinutes = { minutes ->
                    val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                    sleepOriginalVolume = am?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: -1
                    sleepDeadlineMs = System.currentTimeMillis() + minutes * 60_000L
                    sleepMinutes = minutes
                    sleepEndOfTrack = false
                },
                onSetSleepTimerEndOfTrack = {
                    sleepEndOfTrack = true
                    sleepDeadlineMs = 0L
                    sleepMinutes = 0
                },
                onCancelSleepTimer = {
                    restoreSleepVolume()
                    sleepDeadlineMs = 0L
                    sleepMinutes = 0
                    sleepEndOfTrack = false
                }
            )
        }

        // Mise à jour 046 — lecteur vidéo plein écran « Passer à la vidéo ».
        if (isVideoOpen && videoPath != null) {
            VideoPlayerScreen(
                videoUri = Uri.fromFile(File(videoPath!!)),
                startPositionMs = videoStartMs
            ) { finalPositionMs, videoWasPlaying ->
                isVideoOpen = false
                audioViewModel.seekTo(finalPositionMs)
                if (videoWasPlaying && !isPlayingNow) {
                    audioViewModel.togglePlayPause()
                }
            }
        }

        // Modal plein écran Paroles Synchronisées (LRC / ID3 SYLT)
        AnimatedVisibility(
            visible = isLyricsOpen,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LyricsScreen(
                track = lyricsTargetTrack ?: activeTrack,
                lyricsData = lyricsData,
                currentPositionMs = progressMs,
                durationMs = durationMs,
                isPlaying = isPlaying,
                isLoading = isLyricsLoading,
                onBack = { isLyricsOpen = false },
                onPlayPause = { audioViewModel.togglePlayPause() },
                onNext = { audioViewModel.playNext() },
                onPrevious = { audioViewModel.playPrevious() },
                onSeekTo = { audioViewModel.seekTo(it) },
                onImportLrcText = { text -> activeTrack?.let { audioViewModel.importLrcText(it, text) } },
                onEmbedLyricsInAudioFile = {
                    activeTrack?.let { track ->
                        audioViewModel.requestEmbedCurrentLyrics(track, lyricsData)
                    }
                },
                onOpenLrclibSearch = { isLrclibSearchOpen = true },
                onStartGroqTranscription = { activeTrack?.let { audioViewModel.startGroqTranscription(it) } },
                isGroqTranscribing = isGroqTranscribing,
                groqProgressMessage = groqProgressMessage,
                groqErrorMessage = groqErrorMessage,
                onClearGroqError = { audioViewModel.clearGroqError() },
                onOpenSettings = onOpenSettings,
                karaokeFontSize = karaokeFontSize,
                karaokeActiveColor = karaokeActiveColor,
                karaokeOffsetMs = karaokeOffsetMs,
                onOpenKaraokeSettings = { isKaraokeSettingsOpen = true }
            )
        }

        // Aperçu interactif du résultat Groq Whisper avant intégration ID3 SYLT / .LRC
        groqTranscriptionResult?.let { result ->
            GroqPreviewDialog(
                result = result,
                onDismiss = {
                    audioViewModel.dismissGroqPreview()
                },
                onIntegrate = { res ->
                    activeTrack?.let { track ->
                        audioViewModel.requestApplyGroq(track, res)
                    }
                }
            )
        }

        // Modal plein écran Recherche de paroles lrclib.net
        AnimatedVisibility(
            visible = isLrclibSearchOpen,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            val searchState by audioViewModel.lrclibSearchState.collectAsStateWithLifecycle()
            val aiQuerySuggestion by audioViewModel.aiLyricsQuerySuggestion.collectAsStateWithLifecycle()

            LrclibSearchScreen(
                track = activeTrack,
                searchState = searchState,
                saveFeedback = saveFeedbackMessage,
                onBack = {
                    isLrclibSearchOpen = false
                    audioViewModel.resetLrclibSearch()
                },
                onSearch = { title, artist, durationSec ->
                    audioViewModel.searchOnlineLyrics(title, artist, durationSec)
                },
                onSelectAndSave = { result ->
                    activeTrack?.let { track ->
                        audioViewModel.requestApplyLrclib(track, result)
                    }
                },
                onClearFeedback = { audioViewModel.clearSaveFeedback() },
                aiQuerySuggestion = aiQuerySuggestion,
                onAiAssist = { title, artist ->
                    activeTrack?.let { track ->
                        audioViewModel.suggestLrclibQueryWithAi(
                            track = track,
                            currentTitle = title,
                            currentArtist = artist,
                            durationSec = track.duration.takeIf { it > 0 }?.div(1000)?.toInt()
                        )
                    }
                }
            )
        }

        // Modal plein écran Détail de playlist
        AnimatedVisibility(
            visible = selectedPlaylist != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            selectedPlaylist?.let { pl ->
                PlaylistDetailScreen(
                    playlist = pl,
                    tracks = selectedPlaylistTracks,
                    currentPlayingTrack = currentTrack,
                    isPlaying = isPlaying,
                    onBack = { audioViewModel.selectPlaylist(null) },
                    onPlayAll = { startIndex, shuffle ->
                        audioViewModel.playPlaylist(selectedPlaylistTracks, startIndex, shuffle)
                    },
                    onTrackClick = { track, queue ->
                        audioViewModel.playTrack(track, queue)
                    },
                    onRemoveTrack = { trackId ->
                        audioViewModel.removeTrackFromPlaylist(pl.id, trackId)
                    },
                    onUndoRemoveTrack = { trackId ->
                        // Mise à jour 068 — « Annuler » du snackbar : réinsertion du morceau.
                        audioViewModel.addTracksToPlaylist(pl.id, listOf(trackId))
                    },
                    onReorderTracks = { orderedTrackIds ->
                        audioViewModel.reorderPlaylistTracks(pl.id, orderedTrackIds)
                    },
                    onDeletePlaylist = {
                        audioViewModel.deletePlaylist(pl.id)
                    },
                    onRenamePlaylist = { newName, newDesc ->
                        audioViewModel.updatePlaylistName(pl.id, newName, newDesc)
                    },
                    onOpenAddTracks = {
                        isAddTracksToPlaylistOpen = true
                    },
                    onShare = {
                        val shareText = buildString {
                            append(pl.name)
                            if (pl.description.isNotBlank()) append("\n\n").append(pl.description)
                            if (selectedPlaylistTracks.isNotEmpty()) {
                                append("\n\n")
                                selectedPlaylistTracks.forEachIndexed { index, track ->
                                    append(index + 1).append(". ").append(track.title).append(" — ").append(track.artist).append("\n")
                                }
                            }
                        }
                        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(android.content.Intent.createChooser(shareIntent, "Partager la playlist"))
                    }
                )
            }
        }

        // Dialogue d'ajout de morceaux à la playlist active
        selectedPlaylist?.takeIf { isAddTracksToPlaylistOpen }?.let { openPlaylist ->
            AddTracksToPlaylistDialog(
                allTracks = tracks,
                existingTrackIds = selectedPlaylistTracks.map { it.id }.toSet(),
                playlistName = openPlaylist.name,
                onDismiss = { isAddTracksToPlaylistOpen = false },
                onAddTracks = { trackIds ->
                    audioViewModel.addTracksToPlaylist(openPlaylist.id, trackIds)
                    isAddTracksToPlaylistOpen = false
                }
            )
        }

        // Dialogue pour ajouter un ou plusieurs morceaux à une playlist
        if (tracksForPlaylistChooser.isNotEmpty()) {
            AddToPlaylistChooserDialog(
                tracks = tracksForPlaylistChooser,
                playlists = playlists,
                onDismiss = { tracksForPlaylistChooser = emptyList() },
                onSelectPlaylist = { plId ->
                    audioViewModel.addTracksToPlaylist(plId, tracksForPlaylistChooser.map { it.id })
                    tracksForPlaylistChooser = emptyList()
                },
                onCreateAndAdd = { name, desc ->
                    audioViewModel.createPlaylist(
                        name,
                        desc,
                        initialTrackIds = tracksForPlaylistChooser.map { it.id }
                    )
                    tracksForPlaylistChooser = emptyList()
                }
            )
        }

        // Mise à jour 059 — feuille du bas des actions d'un morceau (menu ⋯ des lignes).
        trackForActions?.let { actionTrack ->
            TrackActionsSheet(
                track = actionTrack,
                isFavorite = favorites.contains(actionTrack.id),
                onDismiss = { trackForActions = null },
                onPlayNext = { audioViewModel.addToQueue(actionTrack, playNext = true) },
                onAddToQueue = { audioViewModel.addToQueue(actionTrack) },
                onAddToPlaylist = { tracksForPlaylistChooser = listOf(actionTrack) },
                onToggleFavorite = { audioViewModel.toggleFavorite(actionTrack.id) },
                onGoToAlbum = {
                    isNowPlayingOpen = false
                    openDetailKey = DetailTarget.albumKey(actionTrack.album)
                },
                onGoToArtist = {
                    isNowPlayingOpen = false
                    openDetailKey = DetailTarget.artistKey(actionTrack.artist)
                },
                onOpenLyrics = {
                    lyricsTargetTrack = actionTrack
                    isLyricsOpen = true
                },
                onShare = {
                    // Mise à jour 070 — carte de morceau (avec/sans pochette).
                    trackForShareCard = actionTrack
                },
                onDelete = { audioViewModel.requestDeleteTracks(listOf(actionTrack)) }
            )
        }

        // Mise à jour 070 — carte de morceau à partager (avec ou sans pochette).
        trackForShareCard?.let { track ->
            TrackShareCardDialog(
                track = track,
                accentArgb = MusicProCyanNeon.toArgb(),
                onDismiss = { trackForShareCard = null }
            )
        }

        if (isFavoritesOpen) {
            FavoritesScreen(
                tracks = tracks,
                favorites = favorites,
                currentTrack = currentTrack,
                isPlaying = isPlaying,
                onPlayAll = { audioViewModel.playPlaylist(tracks.filter { favorites.contains(it.id) }) },
                onShuffle = { audioViewModel.playPlaylist(tracks.filter { favorites.contains(it.id) }, shuffle = true) },
                onTrackClick = { audioViewModel.playTrack(it, tracks.filter { tr -> favorites.contains(tr.id) }) },
                onToggleFavorite = { audioViewModel.toggleFavorite(it) },
                onAddToQueue = { audioViewModel.addToQueue(it) },
                onBack = { isFavoritesOpen = false },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Mise à jour 023 : écrans de détail plein écran (album ; l'artiste suivra).
        DetailHost(
            targetKey = openDetailKey,
            tracks = tracks,
            currentPlayingTrack = currentTrack,
            isPlaying = isPlaying,
            onPlay = { list, shuffle -> audioViewModel.playPlaylist(list, 0, shuffle) },
            onTrackClick = { track, list -> audioViewModel.playTrack(track, list) },
            onCreatePlaylist = { name, ids ->
                audioViewModel.createPlaylist(name, "", initialTrackIds = ids)
            },
            onSearch = { query ->
                openDetailKey = null
                audioViewModel.setSearchQuery(query)
                selectTab(NavigationSection.SEARCH, false)
            },
            onClose = { openDetailKey = null }
        )

        if (isQueueOpen) {
            QueueSheet(
                queue = queue,
                currentIndex = queueIndex,
                isPlaying = isPlaying,
                onPlayTrack = { track -> audioViewModel.playQueuedTrack(track) },
                onRemove = { audioViewModel.removeFromQueue(it) },
                onMove = { from, to -> audioViewModel.moveQueueItem(from, to) },
                onClear = { audioViewModel.clearQueue() },
                onClose = { isQueueOpen = false }
            )
        }

        if (isEqualizerOpen) {
            EqualizerSheet(
                enabled = eqEnabled,
                preset = eqPreset,
                levels = eqLevels,
                onEnabledChange = { audioViewModel.setEqualizerEnabled(it) },
                onPresetChange = { audioViewModel.setEqualizerPreset(it) },
                onBandChange = { index, value -> audioViewModel.setEqualizerBand(index, value) },
                onReset = { audioViewModel.resetEqualizer() },
                onClose = { isEqualizerOpen = false }
            )
        }

        if (isKaraokeSettingsOpen) {
            KaraokeSettingsDialog(
                fontSize = karaokeFontSize,
                activeColor = karaokeActiveColor,
                offsetMs = karaokeOffsetMs,
                onFontSizeChange = { audioViewModel.setKaraokeFontSize(it) },
                onColorChange = { audioViewModel.setKaraokeActiveColor(it) },
                onOffsetChange = { audioViewModel.setKaraokeOffsetMs(it) },
                onReset = { audioViewModel.resetKaraokeSettings() },
                onDismiss = { isKaraokeSettingsOpen = false }
            )
        }

        // Les nouvelles releases ne sont plus affichées automatiquement au démarrage.
        // La vérification et l'installation restent disponibles depuis les paramètres.
    }
}

@Composable
private fun HomeExplorerContent(
    tracks: List<AudioTrackEntity>,
    recentTracks: List<AudioTrackEntity>,
    albumsCount: Int,
    artistsCount: Int,
    foldersCount: Int,
    albums: List<com.example.ui.audio.AlbumSummary> = emptyList(),
    playlists: List<com.example.data.local.PlaylistSummary> = emptyList(),
    isScanning: Boolean,
    currentPlayingTrack: AudioTrackEntity?,
    isPlaying: Boolean,
    favorites: Set<Long>,
    onResumePlayback: () -> Unit = {},
    onOpenSettings: () -> Unit,
    onOpenOnboarding: () -> Unit,
    onNavigateToLibrary: () -> Unit,
    onNavigateToRecent: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToNew: () -> Unit = {},
    onNavigateToPlaylists: () -> Unit = {},
    onOpenFavorites: () -> Unit = {},
    onOpenAlbum: (String) -> Unit = {},
    onTrackClick: (AudioTrackEntity) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onOpenTrackActions: (AudioTrackEntity) -> Unit = {},
    onRefreshScan: () -> Unit
) {
    val context = LocalContext.current

    val infiniteTransition = rememberInfiniteTransition(label = "scan_spin")
    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin_angle"
    )

    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 5..11 -> stringResource(R.string.home_greeting_morning)
        in 12..17 -> stringResource(R.string.home_greeting_afternoon)
        in 18..22 -> stringResource(R.string.home_greeting_evening)
        else -> stringResource(R.string.home_greeting_night)
    }

    // Mise à jour 051 — sections de l'accueil, données stables par identifiant.
    val favoriteTracks = remember(tracks, favorites) {
        tracks.filter { favorites.contains(it.id) }
    }
    val recentlyAddedTracks = remember(tracks) {
        tracks.sortedByDescending { it.dateAdded }
    }
    val recentSection = remember(recentTracks, tracks) {
        if (recentTracks.isNotEmpty()) recentTracks else tracks
    }

    // Mise à jour 071 — second tap sur l'onglet Accueil : retour en haut animé.
    val homeListState = androidx.compose.foundation.lazy.rememberLazyListState()
    ScrollToTopEffect(NavigationSection.HOME.name, homeListState)
    LazyColumn(
        state = homeListState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // En-tête Top App Bar
        item(key = "home_header") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .shadow(8.dp, CircleShape, spotColor = MusicProVioletGlow)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                            .border(1.5.dp, MusicProVioletPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.musicpro_logo_cutout),
                            contentDescription = "Logo",
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "MusicPro",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onRefreshScan,
                        enabled = !isScanning,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .testTag("home_refresh_scan_action")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Scanner les morceaux",
                            tint = if (isScanning) MusicProVioletLight else MusicProCyanNeon,
                            modifier = Modifier
                                .size(20.dp)
                                .rotate(if (isScanning) spinAngle else 0f)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onOpenOnboarding,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .testTag("home_onboarding_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                            contentDescription = "Revoir l'onboarding",
                            tint = MusicProVioletLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .testTag("home_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Paramètres de permissions",
                            tint = MusicProCyanNeon,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        item(key = "home_greeting") {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text(greeting, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                Text(stringResource(R.string.home_welcome_back), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Section « Reprendre » — la lecture en cours, titre 20 sp + lien « Tout voir ».
        if (currentPlayingTrack != null) {
            item(key = "home_section_resume_header") {
                HomeSectionHeader(
                    title = stringResource(R.string.home_section_resume),
                    onSeeAll = onNavigateToLibrary
                )
            }
            item(key = "home_section_resume_card") {
                Surface(
                    onClick = onResumePlayback,
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp).testTag("home_resume_playback")
                ) {
                    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.home_resume_playback), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                            Text(currentPlayingTrack.title, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Icon(Icons.Default.PlayArrow, contentDescription = "Reprendre", modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        // État vide / squelettes pendant le scan initial (mise à jour 057).
        if (tracks.isEmpty()) {
            if (isScanning) {
                item(key = "home_skeletons") {
                    SkeletonTracksList(rows = 6, reduceAnimations = false)
                }
            } else {
                item(key = "home_empty_state") {
                    EmptyAudioStateView(
                        modifier = Modifier.fillMaxWidth(),
                        variant = EmptyStateVariant.NO_TRACKS,
                        onPrimaryAction = onRefreshScan
                    )
                }
            }
        } else {
            // Section « Récemment joué » — LazyRow horizontale, clés stables.
            item(key = "home_section_recent_header") {
                HomeSectionHeader(
                    title = stringResource(R.string.home_section_recent),
                    onSeeAll = onNavigateToRecent
                )
            }
            item(key = "home_section_recent_row") {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(recentSection.take(12), key = { it.id }) { track ->
                        HomeTrackCard(
                            track = track,
                            isCurrent = currentPlayingTrack?.id == track.id,
                            onClick = { onTrackClick(track) }
                        )
                    }
                }
            }

            // Section « Vos favoris ».
            if (favoriteTracks.isNotEmpty()) {
                item(key = "home_section_favorites_header") {
                    HomeSectionHeader(
                        title = stringResource(R.string.home_section_favorites),
                        onSeeAll = onOpenFavorites
                    )
                }
                item(key = "home_section_favorites_row") {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(favoriteTracks.take(12), key = { it.id }) { track ->
                            HomeTrackCard(
                                track = track,
                                isCurrent = currentPlayingTrack?.id == track.id,
                                onClick = { onTrackClick(track) }
                            )
                        }
                    }
                }
            }

            // Section « Playlists pour vous ».
            if (playlists.isNotEmpty()) {
                item(key = "home_section_playlists_header") {
                    HomeSectionHeader(
                        title = stringResource(R.string.home_section_playlists_for_you),
                        onSeeAll = onNavigateToPlaylists
                    )
                }
                item(key = "home_section_playlists_row") {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(playlists.take(8), key = { it.id }) { playlist ->
                            HomePlaylistCard(
                                playlist = playlist,
                                onClick = onNavigateToPlaylists
                            )
                        }
                    }
                }
            }

            // Section « Ajouts récents » — les derniers morceaux ajoutés à la bibliothèque.
            item(key = "home_section_added_header") {
                HomeSectionHeader(
                    title = stringResource(R.string.home_section_added),
                    onSeeAll = onNavigateToNew
                )
            }
            item(key = "home_section_added_row") {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(recentlyAddedTracks.take(12), key = { it.id }) { track ->
                        HomeTrackCard(
                            track = track,
                            isCurrent = currentPlayingTrack?.id == track.id,
                            onClick = { onTrackClick(track) }
                        )
                    }
                }
            }
        }
    }
}

/** Mise à jour 051 — en-tête de section d'accueil : titre 20 sp + lien « Tout voir ». */
@Composable
private fun HomeSectionHeader(
    title: String,
    onSeeAll: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        TextButton(
            onClick = onSeeAll,
            modifier = Modifier.heightIn(min = 48.dp)
        ) {
            Text(
                text = stringResource(R.string.home_see_all),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MusicProCyanNeon
            )
        }
    }
}

/** Mise à jour 051 — carte de morceau horizontale (pochette carrée, titre, artiste). */
@Composable
private fun HomeTrackCard(
    track: AudioTrackEntity,
    isCurrent: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(128.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        var imageLoadError by remember(track.id) { mutableStateOf(false) }
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    1.dp,
                    if (isCurrent) MusicProCyanNeon else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (!track.albumArtUri.isNullOrBlank() && !imageLoadError) {
                AsyncImage(
                    model = track.albumArtUri,
                    contentDescription = track.title,
                    contentScale = ContentScale.Crop,
                    onError = { imageLoadError = true },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = MusicProVioletLight,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = track.title,
            fontSize = 13.sp,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
            color = if (isCurrent) MusicProCyanNeon else MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = track.artist,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** Mise à jour 051 — carte de playlist horizontale pour l'accueil. */
@Composable
private fun HomePlaylistCard(
    playlist: com.example.data.local.PlaylistSummary,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            MusicProVioletPrimary.copy(alpha = 0.55f),
                            MusicProCyanNeon.copy(alpha = 0.35f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = playlist.name,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "${playlist.trackCount} titres",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    count: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = count,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = title,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
