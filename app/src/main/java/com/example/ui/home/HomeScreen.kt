package com.example.ui.home

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
import com.example.permissions.PermissionUtils
import com.example.ui.audio.AudioViewModel
import com.example.ui.audio.LibraryTab
import com.example.ui.components.EmptyAudioStateView
import com.example.ui.library.LibraryScreen
import com.example.ui.library.TrackRowItem
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
    audioViewModel: AudioViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentSection by remember { mutableStateOf(NavigationSection.HOME) }

    val tracks by audioViewModel.tracks.collectAsStateWithLifecycle()
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
    val favorites by audioViewModel.favorites.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MusicProBackground,
        bottomBar = {
            Column(modifier = Modifier.navigationBarsPadding()) {
                // Persistent Mini Player if there is a track
                val activeTrack = currentTrack ?: tracks.firstOrNull()
                if (activeTrack != null) {
                    MiniPlayerBar(
                        track = activeTrack,
                        isPlaying = isPlaying,
                        progressMs = progressMs,
                        onPlayPauseToggle = { audioViewModel.togglePlayPause() },
                        onNext = { audioViewModel.playNext() },
                        onPrevious = { audioViewModel.playPrevious() },
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
}

@Composable
private fun HomeExplorerContent(
    tracks: List<AudioTrackEntity>,
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
                        text = "Morceaux récents",
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
            items(tracks.take(10), key = { it.id }) { track ->
                val isCurrent = currentPlayingTrack?.id == track.id
                val isFav = favorites.contains(track.id)

                TrackRowItem(
                    index = tracks.indexOf(track) + 1,
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
    modifier: Modifier = Modifier
) {
    val progressFraction = if (track.duration > 0) {
        (progressMs.toFloat() / track.duration.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Surface(
        modifier = modifier
            .fillMaxWidth()
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
            }
        }
    }
}
