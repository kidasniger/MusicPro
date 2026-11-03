package com.example.ui.search

import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.AudioTrackEntity
import com.example.data.local.PlaylistSummary
import com.example.ui.audio.AlbumSummary
import com.example.ui.audio.ArtistSummary
import com.example.ui.audio.SearchFilter
import com.example.ui.audio.SearchUtils
import com.example.ui.components.EmptyAudioStateView
import com.example.ui.components.EmptyStateVariant
import com.example.ui.library.TrackRowItem
import com.example.ui.theme.MusicProCyanLight
import com.example.ui.theme.MusicProCyanNeon
import com.example.ui.theme.MusicProGreenEmerald
import com.example.ui.theme.MusicProVioletLight
import com.example.ui.theme.MusicProVioletPrimary
import java.util.Calendar
import java.util.Locale

/**
 * Mise à jour 053 — recherche en pilule de 48 dp (loupe, indicatif
 * « Titres, artistes, albums », effacer, micro) et résultats groupés
 * (Meilleur résultat, Titres, Albums, Artistes, Playlists) rafraîchis
 * après 200 ms d'inactivité (lissage côté AudioViewModel).
 * Mise à jour 054 — historique récent (puces supprimables une à une,
 * « Tout effacer ») puis suggestions de genres et de décennies.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    query: String,
    filter: SearchFilter,
    searchResults: List<AudioTrackEntity>,
    allTracks: List<AudioTrackEntity>,
    albums: List<AlbumSummary> = emptyList(),
    artists: List<ArtistSummary> = emptyList(),
    currentPlayingTrack: AudioTrackEntity?,
    isPlaying: Boolean,
    favorites: Set<Long>,
    onQueryChange: (String) -> Unit,
    onFilterChange: (SearchFilter) -> Unit,
    onTrackClick: (AudioTrackEntity) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    searchHistory: List<String> = emptyList(),
    onSearchSubmit: (String) -> Unit = {},
    onRemoveSearchHistory: (String) -> Unit = {},
    onClearSearchHistory: () -> Unit = {},
    onOpenTrackActions: (AudioTrackEntity) -> Unit = {},
    onOpenAlbum: (String) -> Unit = {},
    onOpenArtist: (String) -> Unit = {},
    playlists: List<PlaylistSummary> = emptyList(),
    onPlaylistClick: (PlaylistSummary) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Requête lissée localement pour grouper albums/artistes au même rythme
    // que les titres (200 ms d'inactivité, cf. mise à jour 053).
    var settledQuery by remember { mutableStateOf(query) }
    LaunchedEffect(query) {
        if (query.isBlank()) {
            settledQuery = ""
        } else {
            kotlinx.coroutines.delay(200L)
            settledQuery = query
        }
    }

    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    // Recherche vocale (bouton micro de la pilule).
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val spoken = result.data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull()
        if (!spoken.isNullOrBlank()) {
            onQueryChange(spoken)
            onSearchSubmit(spoken)
        }
    }

    // Mise à jour 054 — suggestions de genres issus de la bibliothèque.
    val genreSuggestions = remember(allTracks) {
        val fromLibrary = allTracks
            .map { it.genre.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .take(8)
        if (fromLibrary.isNotEmpty()) {
            fromLibrary
        } else {
            listOf("Afrobeats", "Pop", "Rap", "Jazz", "Électro", "Classique", "Rock", "Soul")
        }
    }

    // Mise à jour 054 — décennies présentes dans la bibliothèque (date d'ajout).
    val decadeSuggestions = remember(allTracks) {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val decades = allTracks.map { track ->
            if (track.dateAdded > 0L) {
                val year = Calendar.getInstance().apply {
                    timeInMillis = track.dateAdded * 1000L
                }.get(Calendar.YEAR)
                year - year.mod(10)
            } else {
                null
            }
        }.filterNotNull().distinct().sortedDescending()
        if (decades.isNotEmpty()) decades.take(4) else listOf(currentYear - currentYear.mod(10))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("search_screen")
    ) {
        // En-tête Titre Recherche
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Recherche Locale",
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
                            .background(MusicProGreenEmerald)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Indexation instantanée hors-ligne",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Mise à jour 053 — pilule de recherche de 48 dp.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.24f),
                    RoundedCornerShape(24.dp)
                )
                .padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(R.string.search_pill_hint),
                tint = if (query.isNotBlank()) MusicProCyanNeon else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(start = 10.dp)
                    .size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.weight(1f)) {
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    cursorBrush = SolidColor(MusicProCyanNeon),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            focusManager.clearFocus()
                            onSearchSubmit(query)
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 44.dp)
                        .testTag("search_input_field"),
                    decorationBox = { innerTextField ->
                        Box(
                            contentAlignment = Alignment.CenterStart,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (query.isBlank()) {
                                Text(
                                    text = stringResource(R.string.search_pill_hint),
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
            if (query.isNotBlank()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("search_clear_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = stringResource(R.string.search_clear),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(
                onClick = {
                    runCatching {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(
                                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                            )
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                        }
                        speechLauncher.launch(intent)
                    }.onFailure {
                        Toast.makeText(
                            context,
                            context.getString(R.string.search_mic_unavailable),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .testTag("search_mic_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = stringResource(R.string.search_mic),
                    tint = MusicProVioletLight
                )
            }
        }

        // Résultats ou suggestions
        if (query.isBlank()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                if (searchHistory.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.History, contentDescription = null, tint = MusicProVioletLight, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.search_history_title),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        TextButton(
                            onClick = onClearSearchHistory,
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("search_clear_history_button")
                        ) {
                            Text(
                                text = stringResource(R.string.search_history_clear_all),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        searchHistory.take(20).forEach { historyItem ->
                            // Puce supprimable une à une (mise à jour 054).
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        onQueryChange(historyItem)
                                        onSearchSubmit(historyItem)
                                    }
                                    .padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    historyItem,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    maxLines = 1
                                )
                                IconButton(
                                    onClick = { onRemoveSearchHistory(historyItem) },
                                    modifier = Modifier
                                        .size(28.dp)
                                        .testTag("search_history_remove_$historyItem")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = stringResource(R.string.search_history_remove),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Suggestions : genres.
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = MusicProCyanNeon,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.search_suggest_genres),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    genreSuggestions.forEach { suggestion ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, MusicProVioletPrimary.copy(alpha = 0.14f), RoundedCornerShape(20.dp))
                                .clickable {
                                    onQueryChange(suggestion)
                                    onSearchSubmit(suggestion)
                                }
                                .heightIn(min = 48.dp)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("search_suggestion_$suggestion")
                        ) {
                            Text(
                                text = suggestion,
                                fontSize = 12.sp,
                                color = MusicProVioletLight,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Suggestions : décennies.
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = MusicProCyanNeon,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.search_suggest_decades),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    decadeSuggestions.forEach { decadeStart ->
                        val label = stringResource(R.string.search_decade_chip, decadeStart)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, MusicProVioletPrimary.copy(alpha = 0.14f), RoundedCornerShape(20.dp))
                                .clickable {
                                    onQueryChange(label)
                                    onSearchSubmit(label)
                                }
                                .heightIn(min = 48.dp)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("search_decade_$decadeStart")
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                color = MusicProVioletLight,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Carte d'info recherche locale
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                        .padding(18.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MusicProVioletLight,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Recherche 100% hors-ligne",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Votre recherche consulte directement votre bibliothèque, sans connexion Internet.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        } else {
            // Affichage des résultats groupés (mise à jour 053).
            val matchingPlaylists = playlists.filter { playlist ->
                playlist.name.lowercase().contains(settledQuery.trim().lowercase()) ||
                    playlist.description.lowercase().contains(settledQuery.trim().lowercase())
            }
            val matchingAlbums = remember(albums, settledQuery) {
                val q = settledQuery.trim().lowercase()
                if (q.isBlank()) emptyList()
                else albums.filter {
                    it.name.lowercase().contains(q) || it.artist.lowercase().contains(q)
                }
            }
            val matchingArtists = remember(artists, settledQuery) {
                val q = settledQuery.trim().lowercase()
                if (q.isBlank()) emptyList()
                else artists.filter { it.name.lowercase().contains(q) }
            }
            val bestIndex = remember(searchResults, settledQuery) {
                SearchUtils.bestMatchIndex(searchResults, settledQuery)
            }
            val bestMatch = searchResults.getOrNull(bestIndex)

            val hasAnyResult = searchResults.isNotEmpty() || matchingAlbums.isNotEmpty() ||
                matchingArtists.isNotEmpty() || matchingPlaylists.isNotEmpty()

            if (!hasAnyResult) {
                // Mise à jour 058 — état vide « aucun résultat » réutilisé.
                EmptyAudioStateView(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 40.dp),
                    variant = EmptyStateVariant.NO_RESULTS,
                    onPrimaryAction = { onQueryChange("") }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 120.dp, top = 4.dp)
                ) {
                    // Meilleur résultat
                    if (bestMatch != null) {
                        item(key = "hdr_best") {
                            GroupHeader(title = stringResource(R.string.search_group_best))
                        }
                        item(key = "best_${bestMatch.id}") {
                            BestResultCard(
                                track = bestMatch,
                                isPlaying = isPlaying && currentPlayingTrack?.id == bestMatch.id,
                                onClick = { onTrackClick(bestMatch) }
                            )
                        }
                    }

                    // Titres
                    if (searchResults.isNotEmpty()) {
                        item(key = "hdr_titles") {
                            GroupHeader(
                                title = stringResource(R.string.search_group_titles),
                                subtitle = searchResults.size.toString()
                            )
                        }
                        itemsIndexed(searchResults, key = { _, track -> "t_${track.id}" }) { index, track ->
                            val isCurrent = currentPlayingTrack?.id == track.id
                            val isFav = favorites.contains(track.id)
                            TrackRowItem(
                                index = index + 1,
                                track = track,
                                isCurrent = isCurrent,
                                isPlaying = isPlaying,
                                isFavorite = isFav,
                                onClick = { onTrackClick(track) },
                                onToggleFavorite = { onToggleFavorite(track.id) },
                                onOpenActions = { onOpenTrackActions(track) }
                            )
                        }
                    }

                    // Albums
                    if (matchingAlbums.isNotEmpty()) {
                        item(key = "hdr_albums") {
                            GroupHeader(
                                title = stringResource(R.string.search_group_albums),
                                subtitle = matchingAlbums.size.toString()
                            )
                        }
                        itemsIndexed(matchingAlbums, key = { _, album -> "a_${album.name}" }) { _, album ->
                            SummaryRow(
                                coverUri = album.coverUri,
                                title = album.name,
                                subtitle = album.artist,
                                fallbackIcon = Icons.Default.Album,
                                onClick = { onOpenAlbum(album.name) }
                            )
                        }
                    }

                    // Artistes
                    if (matchingArtists.isNotEmpty()) {
                        item(key = "hdr_artists") {
                            GroupHeader(
                                title = stringResource(R.string.search_group_artists),
                                subtitle = matchingArtists.size.toString()
                            )
                        }
                        itemsIndexed(matchingArtists, key = { _, artist -> "ar_${artist.name}" }) { _, artist ->
                            SummaryRow(
                                coverUri = null,
                                title = artist.name,
                                subtitle = "${artist.trackCount} titres • ${artist.albumsCount} album(s)",
                                fallbackIcon = Icons.Default.Person,
                                onClick = { onOpenArtist(artist.name) }
                            )
                        }
                    }

                    // Playlists
                    if (matchingPlaylists.isNotEmpty()) {
                        item(key = "hdr_playlists") {
                            GroupHeader(
                                title = stringResource(R.string.search_group_playlists),
                                subtitle = matchingPlaylists.size.toString()
                            )
                        }
                        itemsIndexed(matchingPlaylists, key = { _, playlist -> "p_${playlist.id}" }) { _, playlist ->
                            SummaryRow(
                                coverUri = null,
                                title = playlist.name,
                                subtitle = "${playlist.trackCount} morceau(s)",
                                fallbackIcon = Icons.AutoMirrored.Filled.QueueMusic,
                                onClick = { onPlaylistClick(playlist) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Mise à jour 053 — en-tête de groupe de résultats. */
@Composable
private fun GroupHeader(title: String, subtitle: String? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MusicProCyanNeon,
            modifier = Modifier.weight(1f)
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Mise à jour 053 — carte du meilleur résultat (grande pochette). */
@Composable
private fun BestResultCard(
    track: AudioTrackEntity,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MusicProVioletPrimary.copy(alpha = 0.3f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp)
            .testTag("search_best_result")
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (!track.albumArtUri.isNullOrBlank()) {
                    AsyncImage(
                        model = track.albumArtUri,
                        contentDescription = track.album,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier.size(64.dp)
                    )
                } else {
                    Icon(Icons.Default.MusicNote, null, tint = MusicProCyanNeon, modifier = Modifier.size(28.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = track.artist + " • " + track.album,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = if (isPlaying) MusicProCyanNeon else MusicProCyanLight,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/** Mise à jour 053 — ligne compacte album / artiste / playlist des résultats. */
@Composable
private fun SummaryRow(
    coverUri: String?,
    title: String,
    subtitle: String,
    fallbackIcon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (!coverUri.isNullOrBlank()) {
                    AsyncImage(
                        model = coverUri,
                        contentDescription = title,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier.size(44.dp)
                    )
                } else {
                    Icon(fallbackIcon, null, tint = MusicProCyanNeon, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text("›", fontSize = 22.sp, color = MusicProCyanNeon)
        }
    }
}
