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
import androidx.compose.material.icons.filled.AutoAwesome
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
    aiQuerySuggestion: Pair<String, String>? = null,
    onAiAssist: (title: String, artist: String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var titleQuery by remember(track) { mutableStateOf(track?.title ?: "") }
    var artistQuery by remember(track) { mutableStateOf(track?.artist ?: "") }
    val trackDurationSec = track?.duration?.takeIf { it > 0 }?.let { (it / 1000).toInt() }

    LaunchedEffect(aiQuerySuggestion) {
        aiQuerySuggestion?.let { (suggestedTitle, suggestedArtist) ->
            if (suggestedTitle.isNotBlank()) titleQuery = suggestedTitle
            if (suggestedArtist.isNotBlank()) artistQuery = suggestedArtist
        }
    }

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
                isAiLoading = searchState is LrclibSearchUiState.AiLoading,
                aiQuerySuggestion = aiQuerySuggestion,
                onTitleChange = { titleQuery = it },
                onArtistChange = { artistQuery = it },
                onPerformSearch = {
                    onSearch(titleQuery, artistQuery, trackDurationSec)
                },
                onAiAssist = { onAiAssist(titleQuery, artistQuery) }
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

                    is LrclibSearchUiState.Loading,
                    is LrclibSearchUiState.AiLoading -> {
                        LoadingResultsView(
                            message = if (searchState is LrclibSearchUiState.AiLoading) {
                                "Recherche améliorée en cours..."
                            } else {
                                "Recherche des paroles..."
                            }
                        )
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
                text = "Recherche de paroles",
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
