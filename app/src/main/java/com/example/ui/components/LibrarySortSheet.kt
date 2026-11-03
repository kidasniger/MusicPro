package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.preferences.LibrarySortMode
import com.example.ui.theme.MusicProCyanNeon

/**
 * Mise à jour 055 — feuille du bas « tri de la bibliothèque », ouverte par
 * l'icône ↑↓ à droite des pastilles. Le choix est mémorisé (DataStore).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibrarySortSheet(
    currentSort: LibrarySortMode,
    onSelect: (LibrarySortMode) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .padding(bottom = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Sort,
                    contentDescription = null,
                    tint = MusicProCyanNeon
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.library_sort_sheet_title),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            SortOptionRow(
                label = stringResource(R.string.library_sort_title),
                selected = currentSort == LibrarySortMode.TITLE_AZ,
                onClick = { onSelect(LibrarySortMode.TITLE_AZ) }
            )
            SortOptionRow(
                label = stringResource(R.string.library_sort_recent),
                selected = currentSort == LibrarySortMode.RECENT,
                onClick = { onSelect(LibrarySortMode.RECENT) }
            )
            SortOptionRow(
                label = stringResource(R.string.library_sort_duration),
                selected = currentSort == LibrarySortMode.DURATION,
                onClick = { onSelect(LibrarySortMode.DURATION) }
            )
            SortOptionRow(
                label = stringResource(R.string.library_sort_most_played),
                selected = currentSort == LibrarySortMode.MOST_PLAYED,
                onClick = { onSelect(LibrarySortMode.MOST_PLAYED) }
            )
        }
    }
}

@Composable
private fun SortOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) MusicProCyanNeon else MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MusicProCyanNeon,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
