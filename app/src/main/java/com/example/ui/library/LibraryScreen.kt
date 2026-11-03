package com.example.ui.library

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Badge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.components.EqualizerIndicator
import com.example.ui.components.pressFeedback
import com.example.ui.components.glass.SharedCoverVisibility
import com.example.ui.components.glass.sharedCover
import com.example.ui.home.NavigationSection
import com.example.ui.navigation.ScrollToTopEffect
import com.example.data.local.AudioTrackEntity
import com.example.data.local.PlaylistSummary
import com.example.data.preferences.LibrarySortMode
import com.example.ui.audio.AlbumSummary
import com.example.ui.audio.ArtistSummary
import com.example.ui.audio.SearchUtils
import com.example.ui.detail.DetailRouter
import com.example.ui.audio.FolderSummary
import com.example.ui.audio.LibraryTab
import com.example.ui.components.AlphabetIndexer
import com.example.ui.components.EmptyAudioStateView
import com.example.ui.components.EmptyStateVariant
import com.example.ui.components.LibrarySortSheet
import com.example.ui.components.ScanProgressBanner
import com.example.ui.components.SkeletonTracksList
import com.example.ui.components.minItemsForIndex
import com.example.ui.playlist.PlaylistsScreen
import com.example.ui.theme.MusicProCyanLight
import com.example.ui.theme.MusicProCyanNeon
import com.example.ui.theme.MusicProCyanVibrant
import com.example.ui.theme.MusicProGreenEmerald
import com.example.ui.theme.MusicProPrimaryGradient
import com.example.ui.theme.MusicProVioletGlow
import com.example.ui.theme.MusicProVioletLight
import com.example.ui.theme.MusicProVioletPrimary
import com.example.ui.theme.MusicProVioletVibrant
import com.example.ui.theme.LocalMusicProListRowHeight
import kotlinx.coroutines.launch

/** Mise à jour 055 — libellé localisé d'une pastille de la bibliothèque. */
@Composable
private fun LibraryTab.displayLabel(): String = when (this) {
    LibraryTab.ALL -> stringResource(R.string.library_filter_all)
    LibraryTab.TRACKS -> stringResource(R.string.library_filter_tracks)
    LibraryTab.ALBUMS -> stringResource(R.string.library_filter_albums)
    LibraryTab.ARTISTS -> stringResource(R.string.library_filter_artists)
    LibraryTab.PLAYLISTS -> stringResource(R.string.library_filter_playlists)
    LibraryTab.FAVORITES -> stringResource(R.string.library_filter_favorites)
    LibraryTab.DOWNLOADS -> stringResource(R.string.library_filter_downloads)
    // Pastilles internes jamais affichées (compatibilité d'état sauvegardé).
    LibraryTab.RECENT -> "Récents"
    LibraryTab.FOLDERS -> "Dossiers"
}
@Composable
fun LibraryScreen(
    tracks: List<AudioTrackEntity>,
    recentTracks: List<AudioTrackEntity> = emptyList(),
    albums: List<AlbumSummary>,
    artists: List<ArtistSummary>,
    folders: List<FolderSummary>,
    selectedTab: LibraryTab,
    isScanning: Boolean,
    statusMessage: String?,
    currentPlayingTrack: AudioTrackEntity?,
    isPlaying: Boolean,
    favorites: Set<Long>,
    playlists: List<PlaylistSummary> = emptyList(),
    librarySort: LibrarySortMode = LibrarySortMode.TITLE_AZ,
    onPlaylistClick: (PlaylistSummary) -> Unit = {},
    onPlayPlaylistDirectly: (PlaylistSummary) -> Unit = {},
    onCreatePlaylist: (String, String) -> Unit = { _, _ -> },
    onCreatePlaylistWithTracks: (String, String, List<Long>) -> Unit = { _, _, _ -> },
    onRenamePlaylist: (Long, String, String) -> Unit = { _, _, _ -> },
    onDeletePlaylist: (Long) -> Unit = {},
    onSelectTab: (LibraryTab) -> Unit,
    onSelectSort: (LibrarySortMode) -> Unit = {},
    onTrackClick: (AudioTrackEntity) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onPlayTracks: (List<AudioTrackEntity>, Boolean) -> Unit = { list, _ -> list.firstOrNull()?.let(onTrackClick) },
    onAddToQueue: (AudioTrackEntity) -> Unit = {},
    onPlayNextTracks: (List<AudioTrackEntity>) -> Unit = {},
    onOpenTrackActions: (AudioTrackEntity) -> Unit = {},
    onAddTracksToPlaylist: (List<AudioTrackEntity>) -> Unit = {},
    onDeleteTracks: (List<AudioTrackEntity>) -> Unit = {},
    onRefreshScan: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
    )

    var selectedAlbum by remember { mutableStateOf<AlbumSummary?>(null) }
    var selectedArtist by remember { mutableStateOf<ArtistSummary?>(null) }
    var selectedFolder by remember { mutableStateOf<FolderSummary?>(null) }

    // Mise à jour 055 — tri mémorisé appliqué à chaque liste.
    val sortedTracks = remember(tracks, librarySort) { LibrarySorting.sortTracks(tracks, librarySort) }
    val sortedAlbums = remember(albums, librarySort) { LibrarySorting.sortAlbums(albums, librarySort) }
    val sortedArtists = remember(artists, librarySort) { LibrarySorting.sortArtists(artists, librarySort) }
    val sortedRecent = remember(recentTracks, librarySort) { LibrarySorting.sortTracks(recentTracks, librarySort) }
    val favoriteTracks = remember(sortedTracks, favorites) { sortedTracks.filter { favorites.contains(it.id) } }
    val downloadTracks = remember(sortedTracks) {
        sortedTracks.filter {
            it.folder.contains("download", ignoreCase = true) ||
                it.path.contains("download", ignoreCase = true)
        }
    }

    // Mise à jour 060 — sélection multi-morceaux ; l'état survit à la rotation.
    var isSelectionMode by rememberSaveable { mutableStateOf(false) }
    var selectedTrackIds by rememberSaveable { mutableStateOf(LinkedHashSet<Long>()) }
    var isSortSheetOpen by remember { mutableStateOf(false) }

    val displayedTracks: List<AudioTrackEntity> = when (selectedTab) {
        LibraryTab.RECENT -> sortedRecent
        LibraryTab.TRACKS, LibraryTab.ALL -> sortedTracks
        LibraryTab.FAVORITES -> favoriteTracks
        LibraryTab.DOWNLOADS -> downloadTracks
        LibraryTab.PLAYLISTS, LibraryTab.ALBUMS, LibraryTab.ARTISTS, LibraryTab.FOLDERS -> emptyList()
    }
    val selectedTracks = remember(sortedTracks, selectedTrackIds) {
        sortedTracks.filter { selectedTrackIds.contains(it.id) }
    }

    fun enterSelection(trackId: Long) {
        isSelectionMode = true
        selectedTrackIds = LinkedHashSet(setOf(trackId))
    }

    fun toggleSelection(trackId: Long) {
        val next = LinkedHashSet(selectedTrackIds)
        if (!next.add(trackId)) next.remove(trackId)
        selectedTrackIds = next
        if (next.isEmpty()) isSelectionMode = false
    }

    fun exitSelection() {
        isSelectionMode = false
        selectedTrackIds = LinkedHashSet()
    }

    // Le retour quitte d'abord le mode sélection (au-dessus du BackHandler de HomeScreen).
    BackHandler(enabled = isSelectionMode) { exitSelection() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("library_screen")
    ) {
        // En-tête Bibliothèque avec statut et bouton de rafraîchissement
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(R.string.library_title),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isScanning) MusicProCyanNeon else MusicProGreenEmerald)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isScanning) "Analyse de la bibliothèque en cours…" else "${tracks.size} titres • Bibliothèque à jour",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Bouton de rafraîchissement manuel du cache
            IconButton(
                onClick = onRefreshScan,
                enabled = !isScanning,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .testTag("library_refresh_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Actualiser la bibliothèque",
                    tint = MusicProCyanNeon,
                    modifier = Modifier
                        .size(22.dp)
                        .then(if (isScanning) Modifier.rotate(rotationAngle) else Modifier)
                )
            }
        }

        // Mise à jour 057 — compteur animé « 128 morceaux trouvés… » pendant le scan.
        AnimatedVisibility(visible = isScanning) {
            ScanProgressBanner(foundCount = tracks.size)
        }

        // Indicateur d'activité de scan
        AnimatedVisibility(visible = isScanning) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = MusicProCyanNeon,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }

        // Mise à jour 055 — pastilles défilantes + icône de tri ↑↓ à droite.
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LazyRow(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(LibraryTab.visibleTabs, key = { it.name }) { tab ->
                    // RECENT (cible interne) met visuellement en avant « Tout ».
                    val isSelected =
                        tab == selectedTab ||
                            (tab == LibraryTab.ALL && selectedTab == LibraryTab.RECENT)
                    Box(
                        modifier = Modifier
                            .heightIn(min = 44.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) MusicProPrimaryGradient
                                else SolidColor(MaterialTheme.colorScheme.surfaceVariant)
                            )
                            .border(
                                1.dp,
                                if (isSelected) MusicProVioletGlow
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                                RoundedCornerShape(20.dp)
                            )
                            .selectable(
                                selected = isSelected,
                                role = Role.Tab,
                                onClick = {
                                    selectedAlbum = null
                                    selectedArtist = null
                                    selectedFolder = null
                                    exitSelection()
                                    onSelectTab(tab)
                                }
                            )
                            .padding(horizontal = 14.dp)
                            .testTag("library_tab_${tab.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.displayLabel(),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }

            IconButton(
                onClick = { isSortSheetOpen = true },
                modifier = Modifier
                    .padding(end = 8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .testTag("library_sort_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Sort,
                    contentDescription = stringResource(R.string.library_sort_action),
                    tint = MusicProCyanNeon,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Contenu principal selon l'onglet, barre contextuelle de sélection en bas.
        Box(modifier = Modifier.weight(1f)) {
            val trackTabEmpty = displayedTracks.isEmpty() && !isScanning
            when {
                (selectedTab == LibraryTab.TRACKS || selectedTab == LibraryTab.ALL ||
                    selectedTab == LibraryTab.RECENT || selectedTab == LibraryTab.DOWNLOADS) &&
                    trackTabEmpty -> {
                    EmptyAudioStateView(
                        modifier = Modifier.fillMaxSize(),
                        variant = EmptyStateVariant.NO_TRACKS,
                        onPrimaryAction = onRefreshScan
                    )
                }
                selectedTab == LibraryTab.FAVORITES && favoriteTracks.isEmpty() && !isScanning -> {
                    EmptyAudioStateView(
                        modifier = Modifier.fillMaxSize(),
                        variant = EmptyStateVariant.NO_FAVORITES,
                        onPrimaryAction = { onSelectTab(LibraryTab.ALL) }
                    )
                }
                selectedTab == LibraryTab.PLAYLISTS -> {
                    PlaylistsScreen(
                        playlists = playlists,
                        onPlaylistClick = onPlaylistClick,
                        onPlayPlaylistDirectly = onPlayPlaylistDirectly,
                        onCreatePlaylist = onCreatePlaylist,
                        // Mise à jour 069 — bibliothèque locale transmise pour la génération IA.
                        onCreatePlaylistWithTracks = onCreatePlaylistWithTracks,
                        libraryTracks = tracks,
                        onRenamePlaylist = onRenamePlaylist,
                        onDeletePlaylist = onDeletePlaylist
                    )
                }
                selectedTab == LibraryTab.ALBUMS -> {
                    if (selectedAlbum != null) {
                        val albumTracks = tracks.filter { it.album == selectedAlbum!!.name }
                        AlbumDetailView(
                            album = selectedAlbum!!,
                            tracks = albumTracks,
                            currentPlayingTrack = currentPlayingTrack,
                            isPlaying = isPlaying,
                            favorites = favorites,
                            onBack = { selectedAlbum = null },
                            onPlayAll = { onPlayTracks(albumTracks, false) },
                            onShuffle = { onPlayTracks(albumTracks, true) },
                            onAddToQueue = onAddToQueue,
                            onTrackClick = onTrackClick,
                            onToggleFavorite = onToggleFavorite
                        )
                    } else if (isScanning && albums.isEmpty()) {
                        // Mise à jour 057 — squelettes pendant le scan initial.
                        SkeletonTracksList(reduceAnimations = false)
                    } else {
                        AlbumsGrid(
                            albums = sortedAlbums,
                            onAlbumClick = { album ->
                                // Mise à jour 023 : la grille ouvre l'écran Album.
                                DetailRouter.openAlbum(album.name)
                            }
                        )
                    }
                }
                selectedTab == LibraryTab.ARTISTS -> {
                    if (selectedArtist != null) {
                        val artistTracks = tracks.filter { it.artist == selectedArtist!!.name }
                        ArtistDetailView(
                            artist = selectedArtist!!,
                            tracks = artistTracks,
                            currentPlayingTrack = currentPlayingTrack,
                            isPlaying = isPlaying,
                            favorites = favorites,
                            onBack = { selectedArtist = null },
                            onPlayAll = { onPlayTracks(artistTracks, false) },
                            onShuffle = { onPlayTracks(artistTracks, true) },
                            onAddToQueue = onAddToQueue,
                            onTrackClick = onTrackClick,
                            onToggleFavorite = onToggleFavorite
                        )
                    } else if (isScanning && artists.isEmpty()) {
                        SkeletonTracksList(reduceAnimations = false)
                    } else {
                        ArtistsList(
                            artists = sortedArtists,
                            tracks = tracks,
                            onArtistClick = { artist ->
                                // Mise à jour 024 : la liste ouvre l'écran Artiste.
                                DetailRouter.openArtist(artist.name)
                            }
                        )
                    }
                }
                selectedTab == LibraryTab.FOLDERS -> {
                    if (selectedFolder != null) {
                        val folderTracks = tracks.filter { it.folder == selectedFolder!!.name }
                        FolderDetailView(
                            folder = selectedFolder!!,
                            tracks = folderTracks,
                            currentPlayingTrack = currentPlayingTrack,
                            isPlaying = isPlaying,
                            favorites = favorites,
                            onBack = { selectedFolder = null },
                            onPlayAll = { onPlayTracks(folderTracks, false) },
                            onShuffle = { onPlayTracks(folderTracks, true) },
                            onAddToQueue = onAddToQueue,
                            onTrackClick = onTrackClick,
                            onToggleFavorite = onToggleFavorite
                        )
                    } else {
                        FoldersList(
                            folders = folders,
                            tracks = tracks,
                            onFolderClick = { folder ->
                                selectedFolder = folder
                            }
                        )
                    }
                }
                isScanning && displayedTracks.isEmpty() -> {
                    // Mise à jour 057 — squelettes reprenant la forme des lignes.
                    SkeletonTracksList(reduceAnimations = false)
                }
                else -> {
                    TracksList(
                        tracks = displayedTracks,
                        currentPlayingTrack = currentPlayingTrack,
                        isPlaying = isPlaying,
                        favorites = favorites,
                        inSelectionMode = isSelectionMode,
                        selectedIds = selectedTrackIds,
                        onTrackClick = onTrackClick,
                        onToggleFavorite = onToggleFavorite,
                        onAddToQueue = onAddToQueue,
                        onTrackLongPress = { enterSelection(it.id) },
                        onToggleTrackSelected = { toggleSelection(it.id) },
                        onOpenActions = onOpenTrackActions
                    )
                }
            }

            // Mise à jour 060 — barre contextuelle de sélection multiple.
            androidx.compose.animation.AnimatedVisibility(
                visible = isSelectionMode,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("library_selection_bar"),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MusicProVioletGlow.copy(alpha = 0.5f)),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.selection_count, selectedTrackIds.size),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MusicProCyanNeon,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 10.dp),
                            maxLines = 1
                        )
                        IconButton(
                            onClick = {
                                selectedTrackIds = LinkedHashSet(displayedTracks.map { it.id })
                            },
                            modifier = Modifier.testTag("selection_select_all")
                        ) {
                            Icon(
                                Icons.Default.DoneAll,
                                contentDescription = stringResource(R.string.selection_select_all),
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            enabled = selectedTrackIds.isNotEmpty(),
                            onClick = {
                                onAddTracksToPlaylist(selectedTracks)
                                exitSelection()
                            },
                            modifier = Modifier.testTag("selection_add_playlist")
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.PlaylistAdd,
                                contentDescription = stringResource(R.string.selection_add_playlist),
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            enabled = selectedTrackIds.isNotEmpty(),
                            onClick = {
                                onPlayNextTracks(selectedTracks)
                                exitSelection()
                            },
                            modifier = Modifier.testTag("selection_play_next")
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.QueueMusic,
                                contentDescription = stringResource(R.string.selection_play_next),
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            enabled = selectedTrackIds.isNotEmpty(),
                            onClick = {
                                onDeleteTracks(selectedTracks)
                                exitSelection()
                            },
                            modifier = Modifier.testTag("selection_delete")
                        ) {
                            Icon(
                                Icons.Default.DeleteOutline,
                                contentDescription = stringResource(R.string.selection_delete),
                                tint = Color(0xFFE57373),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = { exitSelection() },
                            modifier = Modifier.testTag("selection_exit")
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = stringResource(R.string.selection_exit),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Mise à jour 055 — feuille du bas du tri, dernier choix mémorisé.
    if (isSortSheetOpen) {
        LibrarySortSheet(
            currentSort = librarySort,
            onSelect = {
                onSelectSort(it)
                isSortSheetOpen = false
            },
            onDismiss = { isSortSheetOpen = false }
        )
    }
}
@Composable
private fun TracksList(
    tracks: List<AudioTrackEntity>,
    currentPlayingTrack: AudioTrackEntity?,
    isPlaying: Boolean,
    favorites: Set<Long>,
    inSelectionMode: Boolean = false,
    selectedIds: Set<Long> = emptySet(),
    onTrackClick: (AudioTrackEntity) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onAddToQueue: (AudioTrackEntity) -> Unit = {},
    onTrackLongPress: (AudioTrackEntity) -> Unit = {},
    onToggleTrackSelected: (AudioTrackEntity) -> Unit = {},
    onOpenActions: (AudioTrackEntity) -> Unit = {}
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Mise à jour 071 — second tap sur l'onglet Bibliothèque : retour en haut animé.
    ScrollToTopEffect(NavigationSection.LIBRARY.name, listState)

    // Mise à jour 056 — index A–Z : première position de chaque lettre.
    val letterIndex = remember(tracks) {
        val map = LinkedHashMap<Char, Int>()
        tracks.forEachIndexed { position, track ->
            val letter = SearchUtils.firstLetterOf(track.title)
            if (!map.containsKey(letter)) map[letter] = position
        }
        map
    }
    val letters = remember(letterIndex) {
        SearchUtils.lettersFor(tracks.map { it.title })
    }
    val showIndexer = tracks.size >= minItemsForIndex

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                bottom = 120.dp,
                top = 4.dp,
                end = if (showIndexer) 26.dp else 0.dp
            )
        ) {
            itemsIndexed(tracks, key = { _, track -> track.id }) { index, track ->
                val isCurrent = currentPlayingTrack?.id == track.id
                val isFav = favorites.contains(track.id)

                TrackRowItem(
                    index = index + 1,
                    track = track,
                    isCurrent = isCurrent,
                    isPlaying = isPlaying,
                    isFavorite = isFav,
                    inSelectionMode = inSelectionMode,
                    selected = selectedIds.contains(track.id),
                    onClick = {
                        if (inSelectionMode) onToggleTrackSelected(track) else onTrackClick(track)
                    },
                    onToggleFavorite = { onToggleFavorite(track.id) },
                    onSwipeLeft = { onAddToQueue(track) },
                    onSwipeRight = { onToggleFavorite(track.id) },
                    onLongPress = { onTrackLongPress(track) },
                    onToggleSelected = { onToggleTrackSelected(track) },
                    onOpenActions = { onOpenActions(track) }
                )
            }
        }

        // Mise à jour 056 — index A–Z glissable, masqué si moins de 40 éléments.
        if (showIndexer && letters.isNotEmpty()) {
            var currentLetter by remember { mutableStateOf<Char?>(null) }
            AlphabetIndexer(
                letters = letters,
                currentLetter = { currentLetter },
                onLetterSelected = { letter ->
                    currentLetter = letter
                    letterIndex[letter]?.let { target ->
                        scope.launch { listState.scrollToItem(target) }
                    }
                },
                onDragEnded = { currentLetter = null },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(34.dp)
                    .heightIn(max = 470.dp)
                    .testTag("alphabet_indexer")
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TrackRowItem(
    index: Int,
    track: AudioTrackEntity,
    isCurrent: Boolean,
    isPlaying: Boolean,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {},
    inSelectionMode: Boolean = false,
    selected: Boolean = false,
    onLongPress: () -> Unit = {},
    onToggleSelected: () -> Unit = {},
    onOpenActions: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val targetRowHeight = LocalMusicProListRowHeight.current
    val compactRow = targetRowHeight < 60.dp
    // Mise à jour 075 — échelle 0,97 au toucher, voile 8 % au lieu du ripple.
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .pressFeedback(interactionSource)
            .fillMaxWidth()
            .heightIn(min = targetRowHeight)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                // Mise à jour 060 — appui long = mode sélection.
                onLongClick = onLongPress
            )
            .background(
                when {
                    inSelectionMode && selected -> MusicProCyanNeon.copy(alpha = 0.14f)
                    isCurrent -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
                    else -> Color.Transparent
                }
            )
            .padding(horizontal = 16.dp, vertical = if (compactRow) 3.dp else 9.dp)
            .testTag("track_item_${track.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // En mode sélection : case ronde animée à la place du numéro.
        if (inSelectionMode) {
            val checkScale by animateFloatAsState(
                targetValue = if (selected) 1f else 0.82f,
                animationSpec = tween(durationMillis = 180),
                label = "selection_check_scale"
            )
            Box(
                modifier = Modifier
                    .width(28.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .scale(checkScale)
                        .clip(CircleShape)
                        .background(if (selected) MusicProCyanNeon else Color.Transparent)
                        .border(
                            2.dp,
                            if (selected) MusicProCyanNeon else MaterialTheme.colorScheme.onSurfaceVariant,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (selected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        } else {
            // Numéro ou icône de lecture animée
            Box(
                modifier = Modifier.width(28.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (isCurrent && isPlaying) {
                    // Mise à jour 076 — égaliseur 3 barres animé, figé à la pause.
                    EqualizerIndicator(
                        color = MusicProCyanNeon,
                        isPlaying = isPlaying,
                        modifier = Modifier.size(width = 16.dp, height = 18.dp)
                    )
                } else {
                    Text(
                        text = "%02d".format(index),
                        fontSize = 12.sp,
                        color = if (isCurrent) MusicProCyanNeon else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Pochette d'album ou icône avec gradient
        var imageLoadError by remember(track.id) { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .size(if (compactRow) 38.dp else 46.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    1.dp,
                    if (isCurrent) MusicProCyanNeon else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                    RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (!track.albumArtUri.isNullOrBlank() && !imageLoadError) {
                AsyncImage(
                    model = track.albumArtUri,
                    contentDescription = track.album,
                    contentScale = ContentScale.Crop,
                    onError = { imageLoadError = true },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = if (isCurrent) MusicProCyanNeon else MusicProVioletLight,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Titre et Artiste
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                fontSize = 14.sp,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isCurrent) MusicProCyanNeon else MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(3.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = track.artist,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Badge Format Audio (FLAC, MP3, etc.)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (track.getAudioFormat() == "FLAC") Color(0x3300E676)
                            else Color(0x268A2BE2)
                        )
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = track.getAudioFormat(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (track.getAudioFormat() == "FLAC") MusicProGreenEmerald else MusicProVioletLight
                    )
                }

                if (track.hasSyncedLyrics) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0x2600D4FF))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "Paroles",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MusicProCyanNeon
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        if (inSelectionMode) {
            // En mode sélection, la ligne entière bascule la case : rien à droite.
        } else {
            // Durée
            Text(
                text = track.formatDuration(),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Bouton favori
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favori",
                    tint = if (isFavorite) Color(0xFFFF4081) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Mise à jour 059 — menu ⋯ ouvrant la feuille d'actions du morceau.
            IconButton(
                onClick = onOpenActions,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("track_actions_${track.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Actions du morceau",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
@Composable
private fun AlbumsGrid(
    albums: List<AlbumSummary>,
    onAlbumClick: (AlbumSummary) -> Unit
) {
    // Mise à jour 052 — grille adaptative : 150 dp par cellule ; la densité de
    // colonnes (2 à 6+) s'ajuste automatiquement à la tablette et au paysage.
    val gridState = rememberLazyGridState()
    ScrollToTopEffect(NavigationSection.LIBRARY.name, gridState)
    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Adaptive(150.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 120.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(albums, key = { it.name }) { album ->
            AlbumGridItem(album = album, onClick = { onAlbumClick(album) })
        }
    }
}

@Composable
private fun AlbumGridItem(
    album: AlbumSummary,
    onClick: () -> Unit
) {
    var imageLoadError by remember(album.name) { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(10.dp)
            .testTag("album_item_${album.name}")
    ) {
        // Mise à jour 052 — pochette carrée (ratio 1:1), coins 12 dp.
        // Mise à jour 072 — élément partagé avec l'en-tête de l'écran Album.
        SharedCoverVisibility(visible = true) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .sharedCover("album:${album.name}")
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(MusicProVioletPrimary.copy(alpha = 0.5f), MusicProCyanVibrant.copy(alpha = 0.3f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!album.coverUri.isNullOrBlank() && !imageLoadError) {
                    AsyncImage(
                        model = album.coverUri,
                        contentDescription = album.name,
                        contentScale = ContentScale.Crop,
                        onError = { imageLoadError = true },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Album,
                        contentDescription = null,
                        tint = MusicProCyanNeon,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = album.name,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = album.artist,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "${album.trackCount} titres",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MusicProCyanLight
        )
    }
}

@Composable
private fun ArtistsList(
    artists: List<ArtistSummary>,
    tracks: List<AudioTrackEntity>,
    onArtistClick: (ArtistSummary) -> Unit
) {
    // Mise à jour 056 — index A–Z sur les longues listes d'artistes.
    val artistsListState = rememberLazyListState()
    ScrollToTopEffect(NavigationSection.LIBRARY.name, artistsListState)
    val scope = rememberCoroutineScope()
    val letterIndex = remember(artists) {
        val map = LinkedHashMap<Char, Int>()
        artists.forEachIndexed { position, artist ->
            val letter = SearchUtils.firstLetterOf(artist.name)
            if (!map.containsKey(letter)) map[letter] = position
        }
        map
    }
    val letters = remember(letterIndex) {
        SearchUtils.lettersFor(artists.map { it.name })
    }
    val showIndexer = artists.size >= minItemsForIndex

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = artistsListState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                bottom = 120.dp,
                top = 6.dp,
                end = if (showIndexer) 26.dp else 0.dp
            )
        ) {
            items(artists, key = { it.name }) { artist ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onArtistClick(artist) }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .testTag("artist_item_${artist.name}"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar rond artiste
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(MusicProVioletPrimary, MusicProCyanVibrant)
                                )
                            )
                            .border(1.5.dp, MusicProCyanNeon, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = artist.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${artist.trackCount} morceaux • ${artist.albumsCount} album(s)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = MusicProCyanNeon,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        if (showIndexer && letters.isNotEmpty()) {
            var currentLetter by remember { mutableStateOf<Char?>(null) }
            AlphabetIndexer(
                letters = letters,
                currentLetter = { currentLetter },
                onLetterSelected = { letter ->
                    currentLetter = letter
                    letterIndex[letter]?.let { target ->
                        scope.launch { artistsListState.scrollToItem(target) }
                    }
                },
                onDragEnded = { currentLetter = null },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(34.dp)
                    .heightIn(max = 470.dp)
                    .testTag("alphabet_indexer")
            )
        }
    }
}
@Composable
private fun FoldersList(
    folders: List<FolderSummary>,
    tracks: List<AudioTrackEntity>,
    onFolderClick: (FolderSummary) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp, top = 6.dp)
    ) {
        items(folders, key = { it.name }) { folder ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onFolderClick(folder) }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("folder_item_${folder.name}"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, Color(0x3300D4FF), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = MusicProCyanNeon,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = folder.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = folder.samplePath,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${folder.trackCount} titres",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MusicProCyanLight
                    )
                }
            }
        }
    }
}

@Composable
private fun AlbumDetailView(
    album: AlbumSummary,
    tracks: List<AudioTrackEntity>,
    currentPlayingTrack: AudioTrackEntity?,
    isPlaying: Boolean,
    favorites: Set<Long>,
    onBack: () -> Unit,
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit = {},
    onAddToQueue: (AudioTrackEntity) -> Unit = {},
    onTrackClick: (AudioTrackEntity) -> Unit,
    onToggleFavorite: (Long) -> Unit
) {
    val extraListState = rememberLazyListState()
    ScrollToTopEffect(NavigationSection.LIBRARY.name, extraListState)
    LazyColumn(
        state = extraListState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retour aux albums",
                        tint = MusicProCyanNeon
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Albums",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                var imageLoadError by remember(album.name) { mutableStateOf(false) }
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MusicProVioletPrimary, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!album.coverUri.isNullOrBlank() && !imageLoadError) {
                        AsyncImage(
                            model = album.coverUri,
                            contentDescription = album.name,
                            contentScale = ContentScale.Crop,
                            onError = { imageLoadError = true },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Album,
                            contentDescription = null,
                            tint = MusicProCyanNeon,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = album.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = album.artist,
                        fontSize = 13.sp,
                        color = MusicProVioletLight
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${album.trackCount} titre(s) disponible(s)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onPlayAll,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MusicProVioletPrimary)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Lire", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                androidx.compose.material3.OutlinedButton(
                    onClick = onShuffle,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Shuffle, contentDescription = null, tint = MusicProCyanNeon, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Aléatoire", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        itemsIndexed(tracks, key = { _, track -> track.id }) { index, track ->
            val isCurrent = currentPlayingTrack?.id == track.id
            val isFav = favorites.contains(track.id)

            TrackRowItem(
                index = index + 1,
                track = track,
                isCurrent = isCurrent,
                isPlaying = isPlaying,
                isFavorite = isFav,
                onClick = { onTrackClick(track) },
                onToggleFavorite = { onToggleFavorite(track.id) }
            )
        }
    }
}

@Composable
private fun ArtistDetailView(
    artist: ArtistSummary,
    tracks: List<AudioTrackEntity>,
    currentPlayingTrack: AudioTrackEntity?,
    isPlaying: Boolean,
    favorites: Set<Long>,
    onBack: () -> Unit,
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit = {},
    onAddToQueue: (AudioTrackEntity) -> Unit = {},
    onTrackClick: (AudioTrackEntity) -> Unit,
    onToggleFavorite: (Long) -> Unit
) {
    val extraListState = rememberLazyListState()
    ScrollToTopEffect(NavigationSection.LIBRARY.name, extraListState)
    LazyColumn(
        state = extraListState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retour aux artistes",
                        tint = MusicProCyanNeon
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Artistes",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(MusicProPrimaryGradient)
                        .border(2.dp, MusicProCyanNeon, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = artist.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${artist.trackCount} morceau(x) • ${artist.albumsCount} album(s)",
                        fontSize = 12.sp,
                        color = MusicProCyanLight
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onPlayAll,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MusicProVioletPrimary)
                ) {
                    Text("Lire", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                androidx.compose.material3.OutlinedButton(
                    onClick = onShuffle,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Shuffle, contentDescription = null, tint = MusicProCyanNeon, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(5.dp)); Text("Aléatoire", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                }
            }
        }

        itemsIndexed(tracks, key = { _, track -> track.id }) { index, track ->
            val isCurrent = currentPlayingTrack?.id == track.id
            val isFav = favorites.contains(track.id)

            TrackRowItem(
                index = index + 1,
                track = track,
                isCurrent = isCurrent,
                isPlaying = isPlaying,
                isFavorite = isFav,
                onClick = { onTrackClick(track) },
                onToggleFavorite = { onToggleFavorite(track.id) }
            )
        }
    }
}

@Composable
private fun FolderDetailView(
    folder: FolderSummary,
    tracks: List<AudioTrackEntity>,
    currentPlayingTrack: AudioTrackEntity?,
    isPlaying: Boolean,
    favorites: Set<Long>,
    onBack: () -> Unit,
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit = {},
    onAddToQueue: (AudioTrackEntity) -> Unit = {},
    onTrackClick: (AudioTrackEntity) -> Unit,
    onToggleFavorite: (Long) -> Unit
) {
    val extraListState = rememberLazyListState()
    ScrollToTopEffect(NavigationSection.LIBRARY.name, extraListState)
    LazyColumn(
        state = extraListState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retour aux dossiers",
                        tint = MusicProCyanNeon
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Dossiers",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.5.dp, MusicProCyanNeon, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = MusicProCyanNeon,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = folder.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = folder.samplePath,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${folder.trackCount} fichier(s) audio",
                        fontSize = 12.sp,
                        color = MusicProVioletLight
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onPlayAll,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MusicProVioletPrimary)
                ) { Text("Lire", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                androidx.compose.material3.OutlinedButton(
                    onClick = onShuffle,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Aléatoire", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            }
        }

        itemsIndexed(tracks, key = { _, track -> track.id }) { index, track ->
            val isCurrent = currentPlayingTrack?.id == track.id
            val isFav = favorites.contains(track.id)

            TrackRowItem(
                index = index + 1,
                track = track,
                isCurrent = isCurrent,
                isPlaying = isPlaying,
                isFavorite = isFav,
                onClick = { onTrackClick(track) },
                onToggleFavorite = { onToggleFavorite(track.id) }
            )
        }
    }
}

