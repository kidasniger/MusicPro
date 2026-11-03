package com.example.ui.detail

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.data.local.AudioTrackEntity
import com.example.ui.adaptive.LocalWindowWidthClass
import com.example.ui.adaptive.widthSupportsTwoPane
import com.example.ui.album.AlbumScreen
import com.example.ui.album.albumTracksOf
import com.example.ui.artist.ArtistFavoritesViewModel
import com.example.ui.artist.ArtistScreen
import com.example.ui.artist.artistTracksOf
import com.example.ui.audio.AudioViewModel
import com.example.ui.components.TrackActionsSheet
import com.example.ui.components.TrackShareCardDialog
import com.example.ui.components.glass.SharedCoverVisibility
import com.example.ui.playlist.AddToPlaylistChooserDialog
import com.example.ui.theme.MusicProCyanNeon
import androidx.compose.ui.graphics.toArgb

/** Clés des écrans de détail ouverts depuis l'accueil (stockables dans un `rememberSaveable`). */
object DetailTarget {
    private const val AlbumPrefix = "album:"
    private const val ArtistPrefix = "artist:"

    fun albumKey(name: String): String = AlbumPrefix + name

    fun albumName(key: String?): String? =
        key?.takeIf { it.startsWith(AlbumPrefix) }?.removePrefix(AlbumPrefix)

    fun artistKey(name: String): String = ArtistPrefix + name

    fun artistName(key: String?): String? =
        key?.takeIf { it.startsWith(ArtistPrefix) }?.removePrefix(ArtistPrefix)
}

/**
 * Hôte des écrans de détail plein écran (album dès la mise à jour 023, artiste dès la 024) : un seul
 * point de branchement dans `HomeScreen`.
 *
 * Il accepte aussi les demandes de [DetailRouter] (ex. un artiste ouvert depuis l'écran Album) et
 * garde un parent d'un niveau : le retour d'un album ouvert depuis un artiste revient à l'artiste.
 */
@Composable
fun DetailHost(
    targetKey: String?,
    tracks: List<AudioTrackEntity>,
    currentPlayingTrack: AudioTrackEntity?,
    isPlaying: Boolean,
    onPlay: (List<AudioTrackEntity>, Boolean) -> Unit,
    onTrackClick: (AudioTrackEntity, List<AudioTrackEntity>) -> Unit,
    onCreatePlaylist: (String, List<Long>) -> Unit,
    onSearch: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    audioViewModel: AudioViewModel = viewModel(),
    artistFavoritesViewModel: ArtistFavoritesViewModel = viewModel()
) {
    val context = LocalContext.current

    // Écran ouvert depuis un autre détail ou via DetailRouter, et son parent.
    var overrideKey by rememberSaveable { mutableStateOf<String?>(null) }
    var parentKey by rememberSaveable { mutableStateOf<String?>(null) }
    val effectiveKey = overrideKey ?: targetKey

    // Quand l'accueil ouvre ou ferme un détail lui-même, on repart de sa clé (sans effacer l'état
    // restauré après une rotation de l'écran).
    var seenTargetKey by rememberSaveable { mutableStateOf(targetKey) }
    if (seenTargetKey != targetKey) {
        seenTargetKey = targetKey
        overrideKey = null
        parentKey = null
    }

    val request by DetailRouter.request.collectAsState()
    LaunchedEffect(request) {
        request?.let { key ->
            parentKey = effectiveKey
            overrideKey = key
            DetailRouter.consume()
        }
    }

    // Mises à jour 059/065/066/070 — feuille d'actions, choix de playlist et carte de partage
    // hébergées localement pour les écrans de détail.
    val favorites by audioViewModel.favorites.collectAsStateWithLifecycle()
    val playCounts by audioViewModel.playCountsByTrackId.collectAsStateWithLifecycle()
    val playlists by audioViewModel.playlists.collectAsStateWithLifecycle()
    var trackForActions by remember { mutableStateOf<AudioTrackEntity?>(null) }
    var tracksForPlaylistChooser by remember { mutableStateOf<List<AudioTrackEntity>>(emptyList()) }
    var trackForShareCard by remember { mutableStateOf<AudioTrackEntity?>(null) }

    val openDetail: (String) -> Unit = { key ->
        parentKey = effectiveKey
        overrideKey = key
    }
    val closeDetail: () -> Unit = {
        if (overrideKey != null) {
            overrideKey = parentKey?.takeIf { it != targetKey }
            parentKey = null
        } else {
            onClose()
        }
    }

    // Prioritaire sur le retour de l'accueil : il est enregistré après lui.
    BackHandler(enabled = effectiveKey != null, onBack = closeDetail)

    val albumName = DetailTarget.albumName(effectiveKey)
    // Garde le dernier nom affiché pendant le fondu de sortie.
    val lastAlbum = remember { mutableStateOf<String?>(null) }
    if (albumName != null) lastAlbum.value = albumName
    val shownAlbum = albumName ?: lastAlbum.value

    // Mise à jour 072 — SharedCoverVisibility fournit la portée d'AnimatedVisibility à
    // Modifier.sharedCover : la pochette de l'en-tête Album est un élément partagé avec
    // la pochette de la ligne de liste à l'origine de l'ouverture. Sans correspondance,
    // le repli reste le fondu de la feuille hôte.
    SharedCoverVisibility(
        visible = albumName != null,
        modifier = modifier
    ) {
        TwoPaneDetailPane {
        if (shownAlbum != null) {
            val albumTracks = remember(tracks, shownAlbum) { albumTracksOf(tracks, shownAlbum) }
            AlbumScreen(
                albumName = shownAlbum,
                tracks = albumTracks,
                currentPlayingTrack = currentPlayingTrack,
                isPlaying = isPlaying,
                onBack = closeDetail,
                onPlay = { shuffle -> onPlay(albumTracks, shuffle) },
                onTogglePlayback = { audioViewModel.togglePlayPause() },
                onTrackClick = { track -> onTrackClick(track, albumTracks) },
                onOpenTrackActions = { track -> trackForActions = track },
                onDownload = {
                    Toast.makeText(context, R.string.album_download_local, Toast.LENGTH_SHORT).show()
                },
                onAddAlbumToPlaylist = {
                    onCreatePlaylist(shownAlbum, albumTracks.map { it.id })
                    Toast.makeText(
                        context,
                        context.getString(R.string.album_added_playlist, shownAlbum),
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onSearch = { onSearch(shownAlbum) },
                onShare = {
                    val text = buildString {
                        append(shownAlbum)
                        albumTracks.firstOrNull()?.artist?.let { append(" — ").append(it) }
                        append("\n\n")
                        albumTracks.forEachIndexed { index, track ->
                            append(index + 1).append(". ").append(track.title).append("\n")
                        }
                    }
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, text)
                    }
                    context.startActivity(
                        Intent.createChooser(intent, context.getString(R.string.album_share))
                    )
                }
            )
        }
        }
    }

    val artistName = DetailTarget.artistName(effectiveKey)
    val lastArtist = remember { mutableStateOf<String?>(null) }
    if (artistName != null) lastArtist.value = artistName
    val shownArtist = artistName ?: lastArtist.value
    val favoriteArtists by artistFavoritesViewModel.favoriteArtists.collectAsStateWithLifecycle()

    SharedCoverVisibility(
        visible = artistName != null,
        modifier = modifier
    ) {
        TwoPaneDetailPane {
        if (shownArtist != null) {
            val artistTracks = remember(tracks, shownArtist) { artistTracksOf(tracks, shownArtist) }
            ArtistScreen(
                artistName = shownArtist,
                artistTracks = artistTracks,
                allTracks = tracks,
                currentPlayingTrack = currentPlayingTrack,
                isPlaying = isPlaying,
                isFavorite = shownArtist in favoriteArtists,
                onBack = closeDetail,
                onPlay = { onPlay(artistTracks, false) },
                onToggleFavorite = { artistFavoritesViewModel.toggle(shownArtist) },
                onTrackClick = { track -> onTrackClick(track, artistTracks) },
                onAddToQueue = { track, playNext -> audioViewModel.addToQueue(track, playNext) },
                onOpenAlbum = { openDetail(DetailTarget.albumKey(it)) },
                onOpenArtist = { openDetail(DetailTarget.artistKey(it)) },
                playCounts = playCounts
            )
        }
        }
    }

    // Feuille d'actions d'un morceau (mise à jour 065 — menu ⋯ des lignes d'album).
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
                trackForActions = null
                openDetail(DetailTarget.albumKey(actionTrack.album))
            },
            onGoToArtist = {
                trackForActions = null
                openDetail(DetailTarget.artistKey(actionTrack.artist))
            },
            onOpenLyrics = { trackForActions = null },
            onShare = {
                trackForActions = null
                trackForShareCard = actionTrack
            },
            onDelete = { trackForActions = null }
        )
    }

    // Choix de la playlist cible (mise à jour 059).
    if (tracksForPlaylistChooser.isNotEmpty()) {
        AddToPlaylistChooserDialog(
            tracks = tracksForPlaylistChooser,
            playlists = playlists,
            onDismiss = { tracksForPlaylistChooser = emptyList() },
            onSelectPlaylist = { playlistId ->
                audioViewModel.addTracksToPlaylist(playlistId, tracksForPlaylistChooser.map { it.id })
                tracksForPlaylistChooser = emptyList()
            },
            onCreateAndAdd = { name, description ->
                audioViewModel.createPlaylist(name, description, tracksForPlaylistChooser.map { it.id })
                tracksForPlaylistChooser = emptyList()
            }
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
}

/**
 * Mise à jour 085 — deux volets sur les grandes largeurs (tablette, pliable déplié) : le détail
 * occupe le volet droit dans une surface de thème, tandis que la zone gauche reste vide et
 * transparente — la liste de l'accueil reste visible et cliquable dessous (un Box sans
 * gestion de pointeurs ne consomme pas les appuis).
 */
@Composable
private fun TwoPaneDetailPane(content: @Composable () -> Unit) {
    if (widthSupportsTwoPane(LocalWindowWidthClass.current)) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(0.42f))
            Surface(
                modifier = Modifier
                    .weight(0.58f)
                    .fillMaxHeight(),
                color = MaterialTheme.colorScheme.background,
                tonalElevation = 3.dp
            ) {
                content()
            }
        }
    } else {
        content()
    }
}
