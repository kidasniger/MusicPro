package com.example.ui.detail

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.R
import com.example.data.local.AudioTrackEntity
import com.example.ui.album.AlbumScreen
import com.example.ui.album.albumTracksOf

/** Clés des écrans de détail ouverts depuis l'accueil (stockables dans un `rememberSaveable`). */
object DetailTarget {
    private const val AlbumPrefix = "album:"

    fun albumKey(name: String): String = AlbumPrefix + name

    fun albumName(key: String?): String? =
        key?.takeIf { it.startsWith(AlbumPrefix) }?.removePrefix(AlbumPrefix)
}

/**
 * Hôte des écrans de détail plein écran (album dès la mise à jour 023, artiste ensuite) : un seul point
 * de branchement dans `HomeScreen`.
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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val albumName = DetailTarget.albumName(targetKey)
    // Garde le dernier nom affiché pendant le fondu de sortie.
    val lastAlbum = remember { mutableStateOf<String?>(null) }
    if (albumName != null) lastAlbum.value = albumName
    val shownAlbum = albumName ?: lastAlbum.value

    AnimatedVisibility(
        visible = albumName != null,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        if (shownAlbum != null) {
            val albumTracks = remember(tracks, shownAlbum) { albumTracksOf(tracks, shownAlbum) }
            AlbumScreen(
                albumName = shownAlbum,
                tracks = albumTracks,
                currentPlayingTrack = currentPlayingTrack,
                isPlaying = isPlaying,
                onBack = onClose,
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
}
