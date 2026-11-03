package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.MusicOff
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.MusicProCyanNeon
import com.example.ui.theme.MusicProPrimaryGradient
import com.example.ui.theme.MusicProVioletGlow
import com.example.ui.theme.MusicProVioletPrimary

/**
 * Mise à jour 058 — variantes d'état vide (aucun morceau, aucune playlist,
 * aucun favori, aucun résultat). Une seule implémentation réutilisable :
 * icône ligne de 64 dp, phrase courte et action principale.
 */
enum class EmptyStateVariant(
    val icon: ImageVector,
    val titleRes: Int,
    val messageRes: Int,
    val actionRes: Int
) {
    NO_TRACKS(
        Icons.Outlined.MusicOff,
        R.string.empty_no_tracks_title,
        R.string.empty_no_tracks_desc,
        R.string.empty_no_tracks_action
    ),
    NO_PLAYLISTS(
        Icons.AutoMirrored.Outlined.QueueMusic,
        R.string.empty_no_playlists_title,
        R.string.empty_no_playlists_desc,
        R.string.empty_no_playlists_action
    ),
    NO_FAVORITES(
        Icons.Outlined.FavoriteBorder,
        R.string.empty_no_favorites_title,
        R.string.empty_no_favorites_desc,
        R.string.empty_no_favorites_action
    ),
    NO_RESULTS(
        Icons.Outlined.SearchOff,
        R.string.empty_no_results_title,
        R.string.empty_no_results_desc,
        R.string.empty_no_results_action
    )
}

@Composable
fun EmptyAudioStateView(
    modifier: Modifier = Modifier,
    variant: EmptyStateVariant = EmptyStateVariant.NO_TRACKS,
    onPrimaryAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 40.dp)
            .testTag("empty_audio_state_view"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icône ligne de 64 dp avec halo doux.
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(110.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .blur(26.dp)
                    .background(MusicProVioletGlow, CircleShape)
            )

            Box(
                modifier = Modifier
                    .size(96.dp)
                    .shadow(16.dp, RoundedCornerShape(26.dp), spotColor = MusicProVioletGlow)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(26.dp))
                    .border(
                        1.dp,
                        Brush.linearGradient(
                            listOf(MusicProVioletPrimary.copy(alpha = 0.6f), Color(0x26FFFFFF))
                        ),
                        RoundedCornerShape(26.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = variant.icon,
                    contentDescription = null,
                    tint = MusicProCyanNeon,
                    modifier = Modifier.size(64.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(variant.titleRes),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(variant.messageRes),
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        // Action principale (identique d'une variante à l'autre, libellé variable).
        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = { onPrimaryAction?.invoke() },
            enabled = onPrimaryAction != null,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .shadow(12.dp, RoundedCornerShape(14.dp), spotColor = MusicProVioletGlow)
                .background(MusicProPrimaryGradient, RoundedCornerShape(14.dp))
                .testTag("empty_state_action_button")
        ) {
            Text(
                text = stringResource(variant.actionRes),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}
