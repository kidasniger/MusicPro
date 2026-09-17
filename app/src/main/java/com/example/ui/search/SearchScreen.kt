package com.example.ui.search

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AudioTrackEntity
import com.example.ui.audio.SearchFilter
import com.example.ui.library.TrackRowItem
import com.example.ui.theme.MusicProBackground
import com.example.ui.theme.MusicProCardBackground
import com.example.ui.theme.MusicProCyanLight
import com.example.ui.theme.MusicProCyanNeon
import com.example.ui.theme.MusicProGreenEmerald
import com.example.ui.theme.MusicProPrimaryGradient
import com.example.ui.theme.MusicProSurface
import com.example.ui.theme.MusicProSurfaceElevated
import com.example.ui.theme.MusicProSurfaceVariant
import com.example.ui.theme.MusicProTextMuted
import com.example.ui.theme.MusicProTextPrimary
import com.example.ui.theme.MusicProTextSecondary
import com.example.ui.theme.MusicProVioletGlow
import com.example.ui.theme.MusicProVioletLight
import com.example.ui.theme.MusicProVioletPrimary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    query: String,
    filter: SearchFilter,
    searchResults: List<AudioTrackEntity>,
    allTracks: List<AudioTrackEntity>,
    currentPlayingTrack: AudioTrackEntity?,
    isPlaying: Boolean,
    favorites: Set<Long>,
    onQueryChange: (String) -> Unit,
    onFilterChange: (SearchFilter) -> Unit,
    onTrackClick: (AudioTrackEntity) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    val popularSuggestions = listOf(
        "Synthwave", "Neon", "Cyber", "Midnight", "Odyssey", "Electro", "Dreams", "Pop"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MusicProBackground)
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
                    color = MusicProTextPrimary
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
                        color = MusicProTextSecondary
                    )
                }
            }
        }

        // Champ de saisie Recherche avec glow et bouton d'effacement
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(
                        text = "Rechercher titre, artiste, album...",
                        fontSize = 14.sp,
                        color = MusicProTextMuted
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Recherche",
                        tint = if (query.isNotBlank()) MusicProCyanNeon else MusicProTextSecondary
                    )
                },
                trailingIcon = {
                    if (query.isNotBlank()) {
                        IconButton(
                            onClick = { onQueryChange("") },
                            modifier = Modifier.testTag("search_clear_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Effacer",
                                tint = MusicProTextSecondary
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MusicProSurfaceElevated,
                    unfocusedContainerColor = MusicProSurface,
                    focusedBorderColor = MusicProCyanNeon,
                    unfocusedBorderColor = Color(0x26FFFFFF),
                    focusedTextColor = MusicProTextPrimary,
                    unfocusedTextColor = MusicProTextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_input_field")
            )
        }

        // Puces de filtres : Tous / Titres / Artistes / Albums
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SearchFilter.entries.forEach { f ->
                val isSelected = f == filter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) MusicProPrimaryGradient
                            else Brush.linearGradient(listOf(MusicProSurface, MusicProSurface))
                        )
                        .border(
                            1.dp,
                            if (isSelected) MusicProVioletGlow else Color(0x1FFFFFFF),
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onFilterChange(f) }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                        .testTag("search_filter_${f.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = f.label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else MusicProTextSecondary
                    )
                }
            }
        }

        // Résultats ou suggestions
        if (query.isBlank()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = MusicProCyanNeon,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Suggestions rapides",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MusicProTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    popularSuggestions.forEach { suggestion ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(MusicProSurfaceVariant)
                                .border(1.dp, Color(0x228A2BE2), RoundedCornerShape(20.dp))
                                .clickable { onQueryChange(suggestion) }
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

                Spacer(modifier = Modifier.height(28.dp))

                // Carte d'info recherche locale
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MusicProCardBackground)
                        .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(16.dp))
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
                                color = MusicProTextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Les requêtes interrogent directement la base Room locale sans aucune connexion Internet ni latence réseau.",
                            fontSize = 12.sp,
                            color = MusicProTextSecondary,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        } else {
            // Affichage des résultats
            if (searchResults.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MusicProSurfaceElevated)
                            .border(1.dp, Color(0x26FFFFFF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = MusicProTextMuted,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Aucun résultat pour \"$query\"",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MusicProTextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Vérifiez l'orthographe ou essayez un filtre plus large comme \"Tous\".",
                        fontSize = 12.sp,
                        color = MusicProTextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Text(
                    text = "${searchResults.size} résultat(s) trouvé(s)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MusicProCyanNeon,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 120.dp, top = 2.dp)
                ) {
                    itemsIndexed(searchResults, key = { _, track -> track.id }) { index, track ->
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
        }
    }
}
