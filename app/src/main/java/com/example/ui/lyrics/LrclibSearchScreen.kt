package com.example.ui.lyrics

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AudioTrackEntity
import com.example.lyrics.remote.LrclibSearchResult
import com.example.ui.audio.LrclibSearchUiState
import com.example.ui.theme.MusicProBackground
import com.example.ui.theme.MusicProCardBackground
import com.example.ui.theme.MusicProCyanLight
import com.example.ui.theme.MusicProCyanNeon
import com.example.ui.theme.MusicProPrimaryGradient
import com.example.ui.theme.MusicProSurfaceElevated
import com.example.ui.theme.MusicProSurfaceVariant
import com.example.ui.theme.MusicProTextMuted
import com.example.ui.theme.MusicProTextPrimary
import com.example.ui.theme.MusicProTextSecondary
import com.example.ui.theme.MusicProVioletGlow
import com.example.ui.theme.MusicProVioletLight
import com.example.ui.theme.MusicProVioletPastel
import com.example.ui.theme.MusicProVioletPrimary

@Composable
fun LrclibSearchScreen(
    track: AudioTrackEntity?,
    searchState: LrclibSearchUiState,
    saveFeedback: String?,
    onBack: () -> Unit,
    onSearch: (title: String, artist: String, durationSec: Int?) -> Unit,
    onSelectAndSave: (LrclibSearchResult) -> Unit,
    onClearFeedback: () -> Unit,
    modifier: Modifier = Modifier
) {
    var titleQuery by remember(track) { mutableStateOf(track?.title ?: "") }
    var artistQuery by remember(track) { mutableStateOf(track?.artist ?: "") }
    val trackDurationSec = track?.duration?.takeIf { it > 0 }?.let { (it / 1000).toInt() }

    // Déclenchement automatique de la première recherche à l'ouverture de l'écran
    LaunchedEffect(track) {
        if (track != null && (track.title.isNotBlank() || track.artist.isNotBlank())) {
            onSearch(track.title, track.artist, trackDurationSec)
        }
    }

    // Interception de la touche retour pour revenir à l'écran précédent
    BackHandler {
        onBack()
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MusicProBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // 1. Barre supérieure
            LrclibTopBar(
                currentTrackTitle = track?.title ?: "Recherche de paroles",
                onBack = onBack
            )

            // 2. Bannière de feedback si sauvegarde réussie
            AnimatedVisibility(
                visible = !saveFeedback.isNullOrBlank(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                saveFeedback?.let { feedbackMsg ->
                    SaveFeedbackBanner(
                        message = feedbackMsg,
                        onDismiss = onClearFeedback
                    )
                }
            }

            // 3. Formulaire de recherche
            SearchFormCard(
                title = titleQuery,
                artist = artistQuery,
                durationSec = trackDurationSec,
                audioPath = track?.path,
                isLoading = searchState is LrclibSearchUiState.Loading,
                onTitleChange = { titleQuery = it },
                onArtistChange = { artistQuery = it },
                onPerformSearch = {
                    onSearch(titleQuery, artistQuery, trackDurationSec)
                }
            )

            // 4. Zone de résultats / états
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (searchState) {
                    is LrclibSearchUiState.Idle -> {
                        IdleSearchPrompt(
                            onTriggerSearch = {
                                onSearch(titleQuery, artistQuery, trackDurationSec)
                            }
                        )
                    }

                    is LrclibSearchUiState.Loading -> {
                        LoadingResultsView()
                    }

                    is LrclibSearchUiState.Empty -> {
                        EmptyResultsView(
                            title = searchState.queryTitle,
                            artist = searchState.queryArtist,
                            onRetryBroadSearch = {
                                // Tenter une recherche avec seulement le titre
                                onSearch(titleQuery, "", null)
                            }
                        )
                    }

                    is LrclibSearchUiState.Error -> {
                        ErrorResultsView(
                            errorMessage = searchState.message,
                            onRetry = {
                                onSearch(titleQuery, artistQuery, trackDurationSec)
                            }
                        )
                    }

                    is LrclibSearchUiState.Success -> {
                        SearchResultsList(
                            results = searchState.results,
                            trackDurationSec = trackDurationSec,
                            audioPath = track?.path,
                            onSelect = onSelectAndSave
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LrclibTopBar(
    currentTrackTitle: String,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MusicProSurfaceElevated)
                .testTag("lrclib_back_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Retour",
                tint = MusicProCyanNeon,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Recherche lrclib.net",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MusicProTextPrimary
            )
            Text(
                text = currentTrackTitle,
                fontSize = 12.sp,
                color = MusicProTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Badge indicatif source API
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MusicProVioletPrimary.copy(alpha = 0.25f),
            border = BorderStroke(1.dp, MusicProCyanNeon.copy(alpha = 0.4f))
        ) {
            Text(
                text = "API REST",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MusicProCyanNeon,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun SaveFeedbackBanner(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("save_feedback_banner"),
        colors = CardDefaults.cardColors(containerColor = MusicProSurfaceElevated),
        border = BorderStroke(1.dp, MusicProCyanNeon),
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
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MusicProCyanNeon.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MusicProCyanNeon,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = message,
                fontSize = 12.sp,
                color = MusicProTextPrimary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Text("✕", color = MusicProTextMuted, fontSize = 12.sp)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchFormCard(
    title: String,
    artist: String,
    durationSec: Int?,
    audioPath: String?,
    isLoading: Boolean,
    onTitleChange: (String) -> Unit,
    onArtistChange: (String) -> Unit,
    onPerformSearch: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(10.dp, spotColor = MusicProVioletGlow),
        colors = CardDefaults.cardColors(containerColor = MusicProSurfaceElevated),
        border = BorderStroke(1.dp, MusicProVioletPrimary.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Champs de saisie
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    label = { Text("Titre", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("lrclib_input_title"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MusicProCyanNeon,
                        unfocusedBorderColor = MusicProVioletPrimary.copy(alpha = 0.4f),
                        focusedTextColor = MusicProTextPrimary,
                        unfocusedTextColor = MusicProTextPrimary,
                        focusedLabelColor = MusicProCyanNeon,
                        unfocusedLabelColor = MusicProTextSecondary
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = artist,
                    onValueChange = onArtistChange,
                    label = { Text("Artiste", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("lrclib_input_artist"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MusicProCyanNeon,
                        unfocusedBorderColor = MusicProVioletPrimary.copy(alpha = 0.4f),
                        focusedTextColor = MusicProTextPrimary,
                        unfocusedTextColor = MusicProTextPrimary,
                        focusedLabelColor = MusicProCyanNeon,
                        unfocusedLabelColor = MusicProTextSecondary
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Badges d'informations contextuelles (durée & cible d'écriture)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (durationSec != null && durationSec > 0) {
                    val m = durationSec / 60
                    val s = durationSec % 60
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MusicProVioletPrimary.copy(alpha = 0.15f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = MusicProVioletLight,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Durée : %02d:%02d (%ds)".format(m, s, durationSec),
                                fontSize = 11.sp,
                                color = MusicProVioletPastel
                            )
                        }
                    }
                }

                // Affichage du format cible de sauvegarde
                val isMp3 = audioPath?.endsWith(".mp3", ignoreCase = true) == true
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = (if (isMp3) MusicProCyanNeon else MusicProVioletPrimary).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (isMp3) "Cible : Tag ID3 SYLT (MP3)" else "Cible : Fichier .lrc compagnon",
                        fontSize = 11.sp,
                        color = if (isMp3) MusicProCyanNeon else MusicProVioletLight,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bouton de recherche
            Button(
                onClick = onPerformSearch,
                enabled = !isLoading && (title.isNotBlank() || artist.isNotBlank()),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("lrclib_search_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MusicProCyanNeon),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.Black,
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Recherche en cours...", color = Color.Black, fontWeight = FontWeight.Bold)
                } else {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Rechercher sur lrclib.net", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SearchResultsList(
    results: List<LrclibSearchResult>,
    trackDurationSec: Int?,
    audioPath: String?,
    onSelect: (LrclibSearchResult) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("lrclib_results_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${results.size} version(s) trouvée(s)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MusicProCyanNeon
                )
                Text(
                    text = "Appuyez sur un résultat pour l'aperçu",
                    fontSize = 11.sp,
                    color = MusicProTextMuted
                )
            }
        }

        items(results) { item ->
            LrclibResultCard(
                result = item,
                trackDurationSec = trackDurationSec,
                isMp3 = audioPath?.endsWith(".mp3", ignoreCase = true) == true,
                onSelect = { onSelect(item) }
            )
        }
    }
}

@Composable
private fun LrclibResultCard(
    result: LrclibSearchResult,
    trackDurationSec: Int?,
    isMp3: Boolean,
    onSelect: () -> Unit
) {
    var isPreviewExpanded by remember { mutableStateOf(false) }

    val durationDiff = if (trackDurationSec != null && result.duration != null) {
        Math.abs(result.duration.toInt() - trackDurationSec)
    } else null
    val isExactDurationMatch = durationDiff != null && durationDiff <= 3

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
                1.dp,
                if (result.hasSyncedLyrics) MusicProCyanNeon.copy(alpha = 0.4f) else MusicProVioletPrimary.copy(alpha = 0.3f),
                RoundedCornerShape(14.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = MusicProSurfaceElevated)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // En-tête : Titre, Artiste, Durée
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = result.displayTitle,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MusicProTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = result.displayArtist,
                        fontSize = 13.sp,
                        color = MusicProCyanLight,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!result.displayAlbum.isNullOrBlank()) {
                        Text(
                            text = result.displayAlbum ?: "",
                            fontSize = 11.sp,
                            color = MusicProTextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Badge durée
                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isExactDurationMatch) MusicProCyanNeon.copy(alpha = 0.2f) else MusicProSurfaceVariant
                    ) {
                        Text(
                            text = result.durationFormatted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isExactDurationMatch) MusicProCyanNeon else MusicProTextSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    if (isExactDurationMatch) {
                        Text(
                            text = "Durée idéale",
                            fontSize = 9.sp,
                            color = MusicProCyanNeon,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Badges format (Synchronisé vs Non synchronisé)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (result.hasSyncedLyrics) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MusicProCyanNeon.copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, MusicProCyanNeon.copy(alpha = 0.6f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "⚡ Synchronisé (LRC)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MusicProCyanNeon
                            )
                        }
                    }
                } else if (result.hasPlainLyrics) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MusicProVioletPrimary.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "📄 Texte brut",
                            fontSize = 11.sp,
                            color = MusicProVioletPastel,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                if (result.instrumental == true) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MusicProVioletPastel.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Morceau Instrumental",
                            fontSize = 10.sp,
                            color = MusicProVioletPastel,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Zone d'aperçu dépliable des paroles
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { isPreviewExpanded = !isPreviewExpanded }
                    .background(MusicProBackground.copy(alpha = 0.5f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        tint = MusicProCyanNeon,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isPreviewExpanded) "Masquer l'aperçu" else "Aperçu des paroles trouvées",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MusicProTextPrimary
                    )
                }
                Icon(
                    imageVector = if (isPreviewExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MusicProTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(
                visible = isPreviewExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                LyricsPreviewContent(
                    syncedLyrics = result.syncedLyrics,
                    plainLyrics = result.plainLyrics
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bouton de sélection et écriture
            Button(
                onClick = onSelect,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .testTag("select_lyrics_button_${result.id ?: 0}"),
                colors = ButtonDefaults.buttonColors(containerColor = MusicProVioletPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CloudDownload,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isMp3 && result.hasSyncedLyrics) {
                        "Appliquer & Écrire Tag ID3 SYLT"
                    } else {
                        "Appliquer & Sauvegarder .lrc"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun LyricsPreviewContent(
    syncedLyrics: String?,
    plainLyrics: String?
) {
    val displayContent = when {
        !syncedLyrics.isNullOrBlank() -> syncedLyrics
        !plainLyrics.isNullOrBlank() -> plainLyrics
        else -> "Aucun aperçu disponible."
    }

    val lines = displayContent.lines().take(20)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .heightIn(max = 180.dp),
        shape = RoundedCornerShape(8.dp),
        color = Color.Black.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, MusicProVioletPrimary.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .verticalScroll(rememberScrollState())
        ) {
            lines.forEach { line ->
                if (line.startsWith("[") && line.contains("]")) {
                    // Ligne synchronisée avec timestamp
                    val bracketEnd = line.indexOf(']')
                    val timestamp = line.substring(0, bracketEnd + 1)
                    val lyricText = line.substring(bracketEnd + 1).trim()

                    Row(modifier = Modifier.padding(vertical = 1.dp)) {
                        Text(
                            text = timestamp,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = MusicProCyanNeon.copy(alpha = 0.85f),
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = lyricText.ifBlank { "♪" },
                            fontSize = 11.sp,
                            color = MusicProTextPrimary
                        )
                    }
                } else {
                    Text(
                        text = line,
                        fontSize = 11.sp,
                        color = MusicProTextSecondary,
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                }
            }
            if (displayContent.lines().size > 20) {
                Text(
                    text = "... (${displayContent.lines().size - 20} lignes supplémentaires)",
                    fontSize = 10.sp,
                    color = MusicProTextMuted,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun LoadingResultsView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            color = MusicProCyanNeon,
            strokeWidth = 3.dp,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Recherche des paroles...",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MusicProTextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Interrogation du catalogue lrclib.net",
            fontSize = 12.sp,
            color = MusicProTextSecondary
        )
    }
}

@Composable
private fun EmptyResultsView(
    title: String,
    artist: String,
    onRetryBroadSearch: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MusicProSurfaceElevated)
                .border(2.dp, MusicProVioletPrimary.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = null,
                tint = MusicProCyanNeon,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Aucun résultat trouvé",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MusicProTextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Aucune parole ne correspond à \"$title\" de \"$artist\" sur lrclib.net.",
            fontSize = 13.sp,
            color = MusicProTextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onRetryBroadSearch,
            colors = ButtonDefaults.buttonColors(containerColor = MusicProVioletPrimary),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Rechercher uniquement par titre", fontSize = 12.sp, color = Color.White)
        }
    }
}

@Composable
private fun ErrorResultsView(
    errorMessage: String,
    onRetry: () -> Unit
) {
    val isNetworkOff = errorMessage.contains("Internet", ignoreCase = true) ||
            errorMessage.contains("connexion", ignoreCase = true)
    val isTimeout = errorMessage.contains("Timeout", ignoreCase = true) ||
            errorMessage.contains("délai", ignoreCase = true)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MusicProSurfaceElevated)
                .border(2.dp, Color(0xFFEF4444).copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when {
                    isNetworkOff -> Icons.Default.CloudOff
                    isTimeout -> Icons.Default.HourglassBottom
                    else -> Icons.Default.Warning
                },
                contentDescription = null,
                tint = Color(0xFFF87171),
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = when {
                isNetworkOff -> "Pas de connexion Internet"
                isTimeout -> "Délai d'attente dépassé (Timeout)"
                else -> "Erreur lrclib.net"
            },
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MusicProTextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = errorMessage,
            fontSize = 13.sp,
            color = MusicProTextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(22.dp))

        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = MusicProCyanNeon),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Réessayer la recherche", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
private fun IdleSearchPrompt(
    onTriggerSearch: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Subtitles,
            contentDescription = null,
            tint = MusicProCyanNeon,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Recherchez des paroles en ligne",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MusicProTextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Recherchez directement sur lrclib.net par titre et artiste pour trouver des paroles synchronisées.",
            fontSize = 12.sp,
            color = MusicProTextSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(18.dp))
        Button(
            onClick = onTriggerSearch,
            colors = ButtonDefaults.buttonColors(containerColor = MusicProCyanNeon),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Lancer la recherche", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}
