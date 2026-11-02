package com.example.ui.home

import android.app.Activity
import android.util.Log
import android.widget.Toast
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
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.outlined.Radio
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.AudioTrackEntity
import com.example.data.local.PlaylistSummary
import com.example.permissions.PermissionUtils
import com.example.ui.audio.AudioViewModel
import com.example.ui.audio.LibraryTab
import com.example.ui.components.AppUpdateDialog
import com.example.ui.components.FavoritesScreen
import com.example.ui.components.PlaceholderScreen
import com.example.ui.components.QueueSheet
import com.example.ui.components.EqualizerSheet
import com.example.ui.components.KaraokeSettingsDialog
import com.example.ui.components.EmptyAudioStateView
import com.example.ui.components.glass.GlassBackdropHost
import com.example.ui.components.glass.GlassMiniPlayer
import com.example.ui.components.glass.GlassNavBar
import com.example.ui.components.glass.GlassNavItem
import com.example.ui.components.glass.SharedCoverHost
import com.example.ui.components.glass.SharedCoverVisibility
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
import com.example.ui.theme.MusicProCyanLight
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
import com.example.ui.whatsnew.NewScreen

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
    SEARCH(R.string.nav_search, Icons.Outlined.Search, Icons.Default.Search)
}

@Composable
fun HomeScreen(
    onOpenSettings: () -> Unit,
    onOpenOnboarding: () -> Unit = {},
    initialOpenNowPlaying: Boolean = false,
    initialOpenQueue: Boolean = false,
    initialOpenLyrics: Boolean = false,
    mediaUiRequestId: Long = 0L,
    audioViewModel: AudioViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentSectionName by rememberSaveable { mutableStateOf(NavigationSection.HOME.name) }
    val currentSection = NavigationSection.entries.firstOrNull { it.name == currentSectionName }
        ?: NavigationSection.HOME
    var isNowPlayingOpen by rememberSaveable { mutableStateOf(initialOpenNowPlaying) }
    var isLyricsOpen by rememberSaveable { mutableStateOf(false) }
    var isLrclibSearchOpen by rememberSaveable { mutableStateOf(false) }
    var isFavoritesOpen by rememberSaveable { mutableStateOf(false) }
    var isQueueOpen by rememberSaveable { mutableStateOf(false) }
    var isEqualizerOpen by rememberSaveable { mutableStateOf(false) }
    var isKaraokeSettingsOpen by rememberSaveable { mutableStateOf(false) }

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
    val eqEnabled by audioViewModel.eqEnabled.collectAsStateWithLifecycle()
    val eqPreset by audioViewModel.eqPreset.collectAsStateWithLifecycle()
    val eqLevels by audioViewModel.eqLevels.collectAsStateWithLifecycle()
    val karaokeFontSize by audioViewModel.karaokeFontSize.collectAsStateWithLifecycle()
    val karaokeActiveColor by audioViewModel.karaokeActiveColor.collectAsStateWithLifecycle()
    val karaokeOffsetMs by audioViewModel.karaokeOffsetMs.collectAsStateWithLifecycle()
    val lyricsData by audioViewModel.lyricsData.collectAsStateWithLifecycle()
    val isLyricsLoading by audioViewModel.isLyricsLoading.collectAsStateWithLifecycle()

    val isGroqTranscribing by audioViewModel.isGroqTranscribing.collectAsStateWithLifecycle()
    val groqProgressMessage by audioViewModel.groqProgressMessage.collectAsStateWithLifecycle()
    val groqErrorMessage by audioViewModel.groqErrorMessage.collectAsStateWithLifecycle()
    val groqTranscriptionResult by audioViewModel.groqTranscriptionResult.collectAsStateWithLifecycle()

    val playlists by audioViewModel.playlists.collectAsStateWithLifecycle()
    val selectedPlaylist by audioViewModel.selectedPlaylist.collectAsStateWithLifecycle()
    val selectedPlaylistTracks by audioViewModel.selectedPlaylistTracks.collectAsStateWithLifecycle()
    var isAddTracksToPlaylistOpen by remember { mutableStateOf(false) }
    var trackForAddToPlaylistChooser by remember { mutableStateOf<AudioTrackEntity?>(null) }

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

    // Gestion intelligente du retour arrière :
    // 1. Fermer les dialogues / sous-écrans ouverts (Paroles, Recherche Lrclib, Plein écran Now Playing, Détail Playlist)
    // 2. Si on est sur un autre onglet que l'Accueil, revenir à l'onglet Accueil
    // 3. Si on est déjà sur l'onglet Accueil, demander deux retours pour quitter l'application
    var lastBackPressTime by remember { mutableStateOf(0L) }

    BackHandler {
        when {
            isLrclibSearchOpen -> {
                isLrclibSearchOpen = false
            }
            isLyricsOpen -> {
                isLyricsOpen = false
            }
            isNowPlayingOpen -> {
                isNowPlayingOpen = false
            }
            isAddTracksToPlaylistOpen -> {
                isAddTracksToPlaylistOpen = false
            }
            trackForAddToPlaylistChooser != null -> {
                trackForAddToPlaylistChooser = null
            }
            isKaraokeSettingsOpen -> isKaraokeSettingsOpen = false
            isEqualizerOpen -> isEqualizerOpen = false
            isQueueOpen -> isQueueOpen = false
            isFavoritesOpen -> isFavoritesOpen = false
            selectedPlaylist != null -> {
                audioViewModel.selectPlaylist(null)
            }
            searchQuery.isNotBlank() -> {
                audioViewModel.setSearchQuery("")
            }
            currentSection != NavigationSection.HOME -> {
                currentSectionName = NavigationSection.HOME.name
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

            GlassBackdropHost(
                modifier = Modifier.fillMaxSize(),
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
                        when (currentSection) {
                            NavigationSection.HOME -> {
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
                                    onOpenSettings = onOpenSettings,
                                    onOpenOnboarding = onOpenOnboarding,
                                    onNavigateToLibrary = { currentSectionName = NavigationSection.LIBRARY.name },
                                    onNavigateToRecent = {
                                        currentSectionName = NavigationSection.LIBRARY.name
                                        audioViewModel.selectTab(LibraryTab.RECENT)
                                    },
                                    onNavigateToSearch = { currentSectionName = NavigationSection.SEARCH.name },
                                    onOpenFavorites = { isFavoritesOpen = true },
                                    onTrackClick = { audioViewModel.playTrack(it) },
                                    onToggleFavorite = { audioViewModel.toggleFavorite(it) },
                                    onRefreshScan = { audioViewModel.refreshScan() }
                                )
                            }
                            NavigationSection.NEW -> {
                                // Mise à jour 021 : ajouts récents, jamais écoutés, albums récents.
                                NewScreen(
                                    tracks = tracks,
                                    currentPlayingTrack = currentTrack,
                                    isScanning = isScanning,
                                    onPlayTrack = { track, list -> audioViewModel.playTrack(track, list) },
                                    onScan = { audioViewModel.refreshScan() }
                                )
                            }
                            NavigationSection.RADIO -> {
                                // Mise à jour 022 : mixes automatiques générés localement.
                                RadioScreen(
                                    tracks = tracks,
                                    favorites = favorites,
                                    currentPlayingTrack = currentTrack,
                                    isPlaying = isPlaying,
                                    onPlayMix = { mixTracks -> audioViewModel.playPlaylist(mixTracks) }
                                )
                            }
                            NavigationSection.LIBRARY -> {
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
                                    onPlaylistClick = { pl -> audioViewModel.selectPlaylist(pl.id) },
                                    onPlayPlaylistDirectly = { pl -> audioViewModel.playPlaylistDirectly(pl.id) },
                                    onCreatePlaylist = { name, desc -> audioViewModel.createPlaylist(name, desc) },
                                    onRenamePlaylist = { id, name, desc -> audioViewModel.updatePlaylistName(id, name, desc) },
                                    onDeletePlaylist = { id -> audioViewModel.deletePlaylist(id) },
                                    onSelectTab = { audioViewModel.selectTab(it) },
                                    onTrackClick = { audioViewModel.playTrack(it) },
                                    onToggleFavorite = { audioViewModel.toggleFavorite(it) },
                                    onRefreshScan = { audioViewModel.refreshScan() }
                                )
                            }
                            NavigationSection.SEARCH -> {
                                SearchScreen(
                                    query = searchQuery,
                                    filter = searchFilter,
                                    searchResults = searchResults,
                                    allTracks = tracks,
                                    currentPlayingTrack = currentTrack,
                                    isPlaying = isPlaying,
                                    favorites = favorites,
                                    onQueryChange = { audioViewModel.setSearchQuery(it) },
                                    onFilterChange = { audioViewModel.setSearchFilter(it) },
                                    onTrackClick = { audioViewModel.playTrack(it) },
                                    onToggleFavorite = { audioViewModel.toggleFavorite(it) },
                                    searchHistory = searchHistory,
                                    onSearchSubmit = { audioViewModel.addSearchToHistory(it) },
                                    onClearSearchHistory = { audioViewModel.clearSearchHistory() },
                                    playlists = playlists,
                                    onPlaylistClick = { playlist ->
                                        audioViewModel.selectPlaylist(playlist.id)
                                        currentSectionName = NavigationSection.LIBRARY.name
                                        audioViewModel.selectTab(LibraryTab.PLAYLISTS)
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

                        // Barre de navigation flottante en pilule de verre
                        GlassNavBar(
                            items = navItems,
                            selectedKey = currentSection.name,
                            onSelect = { currentSectionName = it }
                        )
                    }
                }
            )
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
                onAddToPlaylist = { activeTrack?.let { trackForAddToPlaylistChooser = it } },
                onShare = {
                    activeTrack?.let { track ->
                        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_TEXT, track.title + " — " + track.artist)
                        }
                        context.startActivity(android.content.Intent.createChooser(shareIntent, "Partager le morceau"))
                    }
                },
                lyricsData = lyricsData,
                onOpenLyrics = { isLyricsOpen = true }
            )
        }

        // Modal plein écran Paroles Synchronisées (LRC / ID3 SYLT)
        AnimatedVisibility(
            visible = isLyricsOpen,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LyricsScreen(
                track = activeTrack,
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

        // Dialogue pour ajouter un morceau spécifique à une playlist
        trackForAddToPlaylistChooser?.let { trk ->
            AddToPlaylistChooserDialog(
                track = trk,
                playlists = playlists,
                onDismiss = { trackForAddToPlaylistChooser = null },
                onSelectPlaylist = { plId ->
                    audioViewModel.addTracksToPlaylist(plId, listOf(trk.id))
                    trackForAddToPlaylistChooser = null
                },
                onCreateAndAdd = { name, desc ->
                    audioViewModel.createPlaylist(name, desc, initialTrackIds = listOf(trk.id))
                    trackForAddToPlaylistChooser = null
                }
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
    onOpenSettings: () -> Unit,
    onOpenOnboarding: () -> Unit,
    onNavigateToLibrary: () -> Unit,
    onNavigateToRecent: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onOpenFavorites: () -> Unit = {},
    onTrackClick: (AudioTrackEntity) -> Unit,
    onToggleFavorite: (Long) -> Unit,
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

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // En-tête Top App Bar
        item {
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

                    Column {
                        Text(
                            text = "MusicPro",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
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

        // Bandeau statistiques du cache Room
        item {
            Surface(
                onClick = onNavigateToLibrary,
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MusicProVioletPrimary.copy(alpha = 0.28f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("home_library_summary")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MusicProCyanNeon.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.AutoMirrored.Filled.QueueMusic, null, tint = MusicProCyanNeon, modifier = Modifier.size(21.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("Votre bibliothèque", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                            Text("${tracks.size} morceaux • $albumsCount albums • $artistsCount artistes", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = "Ouvrir la bibliothèque", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    onClick = onOpenFavorites,
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MusicProVioletPrimary.copy(alpha = 0.35f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Favorite, contentDescription = "Favoris", tint = Color(0xFFFF4081), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("Favoris", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                            Text(favorites.size.toString() + " titres", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Surface(
                    onClick = onNavigateToLibrary,
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MusicProCyanNeon.copy(alpha = 0.25f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.QueueMusic, null, tint = MusicProCyanNeon, modifier = Modifier.size(19.dp))
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("Bibliothèque", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                            Text(tracks.size.toString() + " titres", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        if (albums.isNotEmpty() || playlists.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Collections", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(Modifier.width(8.dp))
                    Text("Albums ${albums.size}", fontSize = 12.sp, color = MusicProVioletLight)
                    Spacer(Modifier.width(6.dp))
                    Text("Playlists ${playlists.size}", fontSize = 12.sp, color = MusicProCyanNeon)
                }
            }
            if (albums.isNotEmpty()) {
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(albums.take(8), key = { it.name }) { album ->
                            Surface(
                                onClick = onNavigateToLibrary,
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MusicProVioletPrimary.copy(alpha = 0.24f)),
                                modifier = Modifier.width(150.dp)
                            ) {
                                Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(48.dp).clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                                        if (!album.coverUri.isNullOrBlank()) AsyncImage(model = album.coverUri, contentDescription = album.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) else Icon(Icons.Default.Album, null, tint = MusicProVioletLight)
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(album.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                        Text(album.artist, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (playlists.isNotEmpty()) {
                item {
                    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(playlists.take(6), key = { it.id }) { playlist ->
                            Surface(onClick = onNavigateToLibrary, shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant, border = BorderStroke(1.dp, MusicProCyanNeon.copy(alpha = 0.20f))) {
                                Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.MusicNote, contentDescription = null, tint = MusicProCyanNeon, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text(playlist.name, fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                            }
                        }
                    }
                }
            }
        }

        // Si aucun morceau en cache
        if (tracks.isEmpty() && !isScanning) {
            item {
                EmptyAudioStateView(
                    onRefreshScan = onRefreshScan,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            val displayedRecentTracks = if (recentTracks.isNotEmpty()) recentTracks else tracks.take(10)
            val sectionTitle = if (recentTracks.isNotEmpty()) "Récemment écoutés" else "Morceaux récents"

            // Section Récents / Bibliothèque
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = sectionTitle,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(
                        onClick = onNavigateToRecent,
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Text(
                            text = "Voir tout",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MusicProCyanNeon
                        )
                    }
                }
            }

            // Liste des morceaux
            items(displayedRecentTracks, key = { it.id }) { track ->
                val isCurrent = currentPlayingTrack?.id == track.id
                val isFav = favorites.contains(track.id)

                TrackRowItem(
                    index = displayedRecentTracks.indexOf(track) + 1,
                    track = track,
                    isCurrent = isCurrent,
                    isPlaying = isPlaying && isCurrent,
                    isFavorite = isFav,
                    onClick = { onTrackClick(track) },
                    onToggleFavorite = { onToggleFavorite(track.id) }
                )
            }
        }
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
