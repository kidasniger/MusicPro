package com.example.ui.detail

import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.data.local.AudioTrackEntity
import com.example.ui.album.AlbumScreen
import com.example.ui.album.albumTracksOf
import com.example.ui.artist.ArtistFavoritesViewModel
import com.example.ui.artist.ArtistScreen
import com.example.ui.artist.artistTracksOf
import com.example.ui.audio.AudioViewModel
import com.example.ui.theme.MusicProTokens

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
    val reduceMotion = remember {
        Settings.Global.getFloat(
            context.applicationContext.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        ) == 0f
    }
    val enter = fadeIn(if (reduceMotion) snap() else tween(MusicProTokens.Duration.Normal))
    val exit = fadeOut(if (reduceMotion) snap() else tween(MusicProTokens.Duration.Normal))

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

    AnimatedVisibility(
        visible = albumName != null,
        enter = enter,
        exit = exit,
        modifier = modifier
    ) {
        if (shownAlbum != null) {
            val albumTracks = remember(tracks, shownAlbum) { albumTracksOf(tracks, shownAlbum) }
            AlbumScreen(
                albumName = shownAlbum,
                tracks = albumTracks,
                currentPlayingTrack = currentPlayingTrack,
                isPlaying = isPlaying,
                onBack = closeDetail,
                onPlay = { shuffle -> onPlay(albumTracks, shuffle) },
                onTrackClick = { track -> onTrackClick(track, albumTracks) },
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

    val artistName = DetailTarget.artistName(effectiveKey)
    val lastArtist = remember { mutableStateOf<String?>(null) }
    if (artistName != null) lastArtist.value = artistName
    val shownArtist = artistName ?: lastArtist.value
    val favoriteArtists by artistFavoritesViewModel.favoriteArtists.collectAsStateWithLifecycle()

    AnimatedVisibility(
        visible = artistName != null,
        enter = enter,
        exit = exit,
        modifier = modifier
    ) {
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
                onOpenArtist = { openDetail(DetailTarget.artistKey(it)) }
            )
        }
    }
}
