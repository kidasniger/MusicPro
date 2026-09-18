package com.example.ui.home

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.platform.LocalContext
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
import com.example.ui.components.EmptyAudioStateView
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
import com.example.ui.search.SearchScreen
import com.example.ui.theme.MusicProBackground
import com.example.ui.theme.MusicProCardBackground
import com.example.ui.theme.MusicProCyanLight
import com.example.ui.theme.MusicProCyanNeon
import com.example.ui.theme.MusicProCyanVibrant
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
import com.example.ui.theme.MusicProVioletPastel
import com.example.ui.theme.MusicProVioletPrimary
import com.example.ui.theme.MusicProVioletVibrant

enum class NavigationSection(val label: String, val icon: ImageVector) {
    HOME("Accueil", Icons.Default.Home),
    LIBRARY("Bibliothèque", Icons.AutoMirrored.Filled.QueueMusic),
    SEARCH("Recherche", Icons.Default.Search)
}

@Composable
fun HomeScreen(
    onOpenSettings: () -> Unit,
    onOpenOnboarding: () -> Unit = {},
    initialOpenNowPlaying: Boolean = false,
    audioViewModel: AudioViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentSection by remember { mutableStateOf(NavigationSection.HOME) }
    var isNowPlayingOpen by remember { mutableStateOf(initialOpenNowPlaying) }
    var isLyricsOpen by remember { mutableStateOf(false) }
    var isLrclibSearchOpen by remember { mutableStateOf(false) }

    // État et vérification automatique des mises à jour au démarrage
    val updateCheckState by settingsViewModel.updateCheckState.collectAsStateWithLifecycle()
    val downloadState by settingsViewModel.downloadState.collectAsStateWithLifecycle()
    var isUpdateDialogDismissed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        settingsViewModel.checkForUpdates()
    }

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
    val favorites by audioViewModel.favorites.collectAsStateWithLifecycle()
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

    val activeTrack = currentTrack ?: tracks.firstOrNull()

    // Gestion intelligente du retour arrière :
    // 1. Fermer les dialogues / sous-écrans ouverts (Paroles, Recherche Lrclib, Plein écran Now Playing, Détail Playlist)
    // 2. Si on est sur l'onglet Bibliothèque ou Recherche, revenir à l'onglet Accueil
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
            selectedPlaylist != null -> {
                audioViewModel.selectPlaylist(null)
            }
            searchQuery.isNotBlank() -> {
                audioViewModel.setSearchQuery("")
            }
            currentSection != NavigationSection.HOME -> {
                currentSection = NavigationSection.HOME
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

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MusicProBackground,
            bottomBar = {
                Column(modifier = Modifier.navigationBarsPadding()) {
                    // Persistent Mini Player if there is a track
                    if (activeTrack != null) {
                        MiniPlayerBar(
                            track = activeTrack,
                            isPlaying = isPlaying,
                            progressMs = progressMs,
                            onPlayPauseToggle = { audioViewModel.togglePlayPause() },
                            onNext = { audioViewModel.playNext() },
                            onPrevious = { audioViewModel.playPrevious() },
                            onClick = { isNowPlayingOpen = true },
                            onOpenLyrics = { isLyricsOpen = true },
                            modifier = Modifier.testTag("mini_player_bar")
                        )
                    }

                    // Bottom Navigation Bar with neon accents
                    MusicProBottomNavBar(
                        currentSection = currentSection,
                        onSelectSection = { currentSection = it }
                    )
                }
            }
        ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentSection) {
                NavigationSection.HOME -> {
                    HomeExplorerContent(
                        tracks = tracks,
                        recentTracks = recentTracks,
                        albumsCount = albums.size,
                        artistsCount = artists.size,
                        foldersCount = folders.size,
                        isScanning = isScanning,
                        currentPlayingTrack = currentTrack,
                        isPlaying = isPlaying,
                        favorites = favorites,
                        onOpenSettings = onOpenSettings,
                        onOpenOnboarding = onOpenOnboarding,
                        onNavigateToLibrary = { currentSection = NavigationSection.LIBRARY },
                        onNavigateToSearch = { currentSection = NavigationSection.SEARCH },
                        onTrackClick = { audioViewModel.playTrack(it) },
                        onToggleFavorite = { audioViewModel.toggleFavorite(it) },
                        onRefreshScan = { audioViewModel.refreshScan() },
                        onLoadDemoTracks = { audioViewModel.loadDemoTracks() }
                    )
                }
                NavigationSection.LIBRARY -> {
                    LibraryScreen(
                        tracks = tracks,
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
                        onRefreshScan = { audioViewModel.refreshScan() },
                        onLoadDemoTracks = { audioViewModel.loadDemoTracks() }
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
                        onToggleFavorite = { audioViewModel.toggleFavorite(it) }
                    )
                }
            }
        }
    }

    // Modal plein écran Now Playing avec animations néon
    AnimatedVisibility(
        visible = isNowPlayingOpen,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
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
            onGenerateDemoLyrics = { activeTrack?.let { audioViewModel.generateDemoLyrics(it) } },
            onImportLrcText = { text -> activeTrack?.let { audioViewModel.importLrcText(it, text) } },
            onOpenLrclibSearch = { isLrclibSearchOpen = true },
            onStartGroqTranscription = { activeTrack?.let { audioViewModel.startGroqTranscription(it) } },
            isGroqTranscribing = isGroqTranscribing,
            groqProgressMessage = groqProgressMessage,
            groqErrorMessage = groqErrorMessage,
            onClearGroqError = { audioViewModel.clearGroqError() },
            onOpenSettings = onOpenSettings
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
                    audioViewModel.applyGroqResult(track, res)
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
        val saveFeedback by audioViewModel.saveFeedbackMessage.collectAsStateWithLifecycle()

        LrclibSearchScreen(
            track = activeTrack,
            searchState = searchState,
            saveFeedback = saveFeedback,
            onBack = {
                isLrclibSearchOpen = false
                audioViewModel.resetLrclibSearch()
            },
            onSearch = { title, artist, durationSec ->
                audioViewModel.searchOnlineLyrics(title, artist, durationSec)
            },
            onSelectAndSave = { result ->
                activeTrack?.let { track ->
                    audioViewModel.applyLrclibResult(track, result)
                }
            },
            onClearFeedback = { audioViewModel.clearSaveFeedback() }
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
                }
            )
        }
    }

    // Dialogue d'ajout de morceaux à la playlist active
    if (isAddTracksToPlaylistOpen && selectedPlaylist != null) {
        AddTracksToPlaylistDialog(
            allTracks = tracks,
            existingTrackIds = selectedPlaylistTracks.map { it.id }.toSet(),
            playlistName = selectedPlaylist!!.name,
            onDismiss = { isAddTracksToPlaylistOpen = false },
            onAddTracks = { trackIds ->
                audioViewModel.addTracksToPlaylist(selectedPlaylist!!.id, trackIds)
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

    // Dialogue d'alerte de nouvelle mise à jour disponible au lancement
    val currentUpdate = updateCheckState
    if (currentUpdate is UpdateCheckState.UpdateAvailable && !isUpdateDialogDismissed) {
        AppUpdateDialog(
            updateInfo = currentUpdate,
            downloadState = downloadState,
            onDismiss = {
                isUpdateDialogDismissed = true
            },
            onDownloadAndInstall = { downloadUrl ->
                settingsViewModel.downloadAndInstallUpdate(downloadUrl)
            },
            onInstallExisting = {
                settingsViewModel.installExistingApk()
            }
        )
    }
}
}

@Composable
private fun HomeExplorerContent(
    tracks: List<AudioTrackEntity>,
    recentTracks: List<AudioTrackEntity>,
    albumsCount: Int,
    artistsCount: Int,
    foldersCount: Int,
    isScanning: Boolean,
    currentPlayingTrack: AudioTrackEntity?,
    isPlaying: Boolean,
    favorites: Set<Long>,
    onOpenSettings: () -> Unit,
    onOpenOnboarding: () -> Unit,
    onNavigateToLibrary: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onTrackClick: (AudioTrackEntity) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onRefreshScan: () -> Unit,
    onLoadDemoTracks: () -> Unit
) {
    val context = LocalContext.current

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
                            .background(MusicProSurfaceElevated, CircleShape)
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "MusicPro",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MusicProTextPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0x3310B981)
                            ) {
                                Text(
                                    text = "HORS LIGNE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MusicProSuccess,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Lecteur Audio Haute Définition",
                            fontSize = 11.sp,
                            color = MusicProTextMuted
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onOpenOnboarding,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MusicProSurfaceElevated)
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
                            .background(MusicProSurfaceElevated)
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "Morceaux",
                    count = tracks.size.toString(),
                    icon = Icons.Default.MusicNote,
                    color = MusicProCyanNeon,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToLibrary
                )
                StatCard(
                    title = "Albums",
                    count = albumsCount.toString(),
                    icon = Icons.Default.Album,
                    color = MusicProVioletLight,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToLibrary
                )
                StatCard(
                    title = "Artistes",
                    count = artistsCount.toString(),
                    icon = Icons.Default.Person,
                    color = MusicProGreenEmerald,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToLibrary
                )
            }
        }

        // État des permissions
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MusicProSurface),
                border = BorderStroke(1.dp, Color(0x3310B981)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("permissions_active_status_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MusicProSuccess,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Base Room locale synchronisée",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MusicProTextPrimary
                        )
                        Text(
                            text = if (tracks.isEmpty()) "Aucun morceau encore indexé" else "${tracks.size} pistes prêtes à être jouées sans connexion",
                            fontSize = 11.sp,
                            color = MusicProTextSecondary
                        )
                    }
                    IconButton(
                        onClick = onRefreshScan,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x2600D4FF))
                            .size(36.dp)
                            .testTag("home_refresh_scan_action")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Scanner",
                            tint = MusicProCyanNeon,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Si aucun morceau en cache
        if (tracks.isEmpty() && !isScanning) {
            item {
                EmptyAudioStateView(
                    onRefreshScan = onRefreshScan,
                    onLoadDemoTracks = onLoadDemoTracks,
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
                        color = MusicProTextPrimary
                    )
                    Text(
                        text = "Voir tout",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MusicProCyanNeon,
                        modifier = Modifier
                            .clickable(onClick = onNavigateToLibrary)
                            .padding(4.dp)
                    )
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
            .background(MusicProSurfaceElevated)
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
            color = MusicProTextPrimary
        )
        Text(
            text = title,
            fontSize = 10.sp,
            color = MusicProTextSecondary
        )
    }
}

@Composable
private fun MusicProBottomNavBar(
    currentSection: NavigationSection,
    onSelectSection: (NavigationSection) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MusicProSurface)
            .border(1.dp, Color(0x1AFFFFFF))
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavigationSection.entries.forEach { section ->
            val isSelected = section == currentSection
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onSelectSection(section) }
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("nav_tab_${section.name.lowercase()}"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = section.icon,
                    contentDescription = section.label,
                    tint = if (isSelected) MusicProCyanNeon else MusicProTextMuted,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = section.label,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) MusicProCyanNeon else MusicProTextMuted
                )
            }
        }
    }
}

@Composable
private fun MiniPlayerBar(
    track: AudioTrackEntity,
    isPlaying: Boolean,
    progressMs: Long,
    onPlayPauseToggle: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onClick: () -> Unit = {},
    onOpenLyrics: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val progressFraction = if (track.duration > 0) {
        (progressMs.toFloat() / track.duration.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .shadow(16.dp, spotColor = MusicProVioletGlow),
        color = MusicProSurfaceElevated,
        border = BorderStroke(1.dp, MusicProVioletPrimary.copy(alpha = 0.35f))
    ) {
        Column {
            // Glowing progress line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp)
                    .background(Color(0x33FFFFFF))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressFraction)
                        .height(2.5.dp)
                        .background(MusicProPrimaryGradient)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mini album art
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MusicProSurfaceVariant)
                        .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!track.albumArtUri.isNullOrBlank()) {
                        AsyncImage(
                            model = track.albumArtUri,
                            contentDescription = track.album,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = MusicProCyanNeon,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MusicProTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${track.artist} • ${track.getAudioFormat()}",
                        fontSize = 11.sp,
                        color = MusicProCyanNeon,
                        maxLines = 1
                    )
                }

                IconButton(
                    onClick = onPrevious,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Précédent",
                        tint = MusicProTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onPlayPauseToggle,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MusicProVioletPrimary)
                        .testTag("mini_player_play_pause_button")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Lecture",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onNext,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Suivant",
                        tint = MusicProTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (onOpenLyrics != null) {
                    IconButton(
                        onClick = onOpenLyrics,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("mini_player_lyrics_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Subtitles,
                            contentDescription = "Paroles",
                            tint = MusicProCyanNeon,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
