package com.example.ui.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.AudioTrackEntity
import com.example.data.security.GroqApiKeyStore
import com.example.ui.theme.MusicProCardBackground
import com.example.ui.theme.MusicProCyanNeon
import com.example.ui.theme.MusicProError
import com.example.ui.theme.MusicProPrimaryGradient
import com.example.ui.theme.MusicProSurfaceElevated
import com.example.ui.theme.MusicProTextMuted
import com.example.ui.theme.MusicProTextPrimary
import com.example.ui.theme.MusicProTextSecondary
import com.example.ui.theme.MusicProVioletGlow
import com.example.ui.theme.MusicProVioletPrimary
import kotlinx.coroutines.launch

/**
 * Mise à jour 069 — création de playlist, éventuellement assistée par l'IA :
 * quand [libraryTracks] et [onConfirmWithTracks] sont fournis, un bouton
 * « Générer avec l'IA » permet de décrire une ambiance et de recevoir un
 * aperçu à cocher/décocher de morceaux choisis UNIQUEMENT dans la
 * bibliothèque locale (aucun morceau externe n'est inventé).
 */
@Composable
fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, description: String) -> Unit,
    initialName: String = "",
    initialDescription: String = "",
    isEditing: Boolean = false,
    libraryTracks: List<AudioTrackEntity> = emptyList(),
    onConfirmWithTracks: ((name: String, description: String, trackIds: List<Long>) -> Unit)? = null
) {
    var name by remember { mutableStateOf(initialName) }
    var description by remember { mutableStateOf(initialDescription) }
    var isError by remember { mutableStateOf(false) }
    val aiAvailable = !isEditing && onConfirmWithTracks != null && libraryTracks.isNotEmpty()
    var aiMode by remember { mutableStateOf(false) }
    var ambiance by remember { mutableStateOf("") }
    var generating by remember { mutableStateOf(false) }
    var aiResults by remember { mutableStateOf<List<AudioTrackEntity>?>(null) }
    var checkedIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var aiError by remember { mutableStateOf(false) }
    var aiKeyMissing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun launchGeneration() {
        val store = GroqApiKeyStore.getInstance(context)
        if (!store.hasApiKey()) {
            aiKeyMissing = true
            return
        }
        aiKeyMissing = false
        aiError = false
        generating = true
        aiResults = null
        scope.launch {
            val result = AiPlaylistAssistant.generate(store, ambiance, libraryTracks)
            generating = false
            result.onSuccess { picked ->
                aiResults = picked
                checkedIds = picked.map { it.id }.toSet()
            }.onFailure {
                aiError = true
            }
        }
    }

    fun confirm() {
        if (name.trim().isBlank()) {
            isError = true
            return
        }
        if (aiMode && onConfirmWithTracks != null && aiResults != null) {
            val picked = aiResults.orEmpty().filter { it.id in checkedIds }
            if (picked.isEmpty()) return
            onConfirmWithTracks(name.trim(), description.trim(), picked.map { it.id })
        } else {
            onConfirm(name.trim(), description.trim())
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(24.dp, RoundedCornerShape(20.dp), spotColor = MusicProVioletGlow)
                .border(1.dp, MusicProVioletPrimary.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .testTag("create_playlist_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MusicProCardBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlaylistAdd,
                        contentDescription = null,
                        tint = MusicProCyanNeon,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = stringResource(if (isEditing) R.string.playlist_rename_title else R.string.playlist_new),
                        color = MusicProTextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (isError && it.isNotBlank()) isError = false
                    },
                    label = { Text(stringResource(R.string.playlist_name_label)) },
                    placeholder = { Text(stringResource(R.string.playlist_name_placeholder), color = MusicProTextMuted) },
                    singleLine = true,
                    isError = isError,
                    supportingText = {
                        if (isError) {
                            Text(stringResource(R.string.playlist_name_empty), color = MusicProError, fontSize = 12.sp)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MusicProTextPrimary,
                        unfocusedTextColor = MusicProTextPrimary,
                        focusedBorderColor = MusicProCyanNeon,
                        unfocusedBorderColor = MusicProVioletPrimary.copy(alpha = 0.5f),
                        focusedLabelColor = MusicProCyanNeon,
                        unfocusedLabelColor = MusicProTextSecondary,
                        cursorColor = MusicProCyanNeon,
                        focusedContainerColor = MusicProSurfaceElevated,
                        unfocusedContainerColor = MusicProSurfaceElevated
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("playlist_name_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.playlist_description_label)) },
                    placeholder = { Text(stringResource(R.string.playlist_description_placeholder), color = MusicProTextMuted) },
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MusicProTextPrimary,
                        unfocusedTextColor = MusicProTextPrimary,
                        focusedBorderColor = MusicProCyanNeon,
                        unfocusedBorderColor = MusicProVioletPrimary.copy(alpha = 0.5f),
                        focusedLabelColor = MusicProCyanNeon,
                        unfocusedLabelColor = MusicProTextSecondary,
                        cursorColor = MusicProCyanNeon,
                        focusedContainerColor = MusicProSurfaceElevated,
                        unfocusedContainerColor = MusicProSurfaceElevated
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("playlist_description_input")
                )

                if (aiAvailable) {
                    Spacer(modifier = Modifier.height(12.dp))

                    if (!aiMode) {
                        // Entrée « Générer avec l'IA ».
                        TextButton(
                            onClick = { aiMode = true },
                            modifier = Modifier.testTag("ai_playlist_toggle")
                        ) {
                            Text(
                                text = stringResource(R.string.ai_generate_button),
                                color = MusicProCyanNeon,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        // Panneau IA : ambiance, génération, aperçu à cocher.
                        OutlinedTextField(
                            value = ambiance,
                            onValueChange = { ambiance = it },
                            label = { Text(stringResource(R.string.ai_ambiance_label), color = MusicProTextSecondary) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MusicProTextPrimary,
                                unfocusedTextColor = MusicProTextPrimary,
                                focusedBorderColor = MusicProCyanNeon,
                                unfocusedBorderColor = MusicProVioletPrimary.copy(alpha = 0.5f),
                                cursorColor = MusicProCyanNeon,
                                focusedContainerColor = MusicProSurfaceElevated,
                                unfocusedContainerColor = MusicProSurfaceElevated
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ai_ambiance_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Button(
                                onClick = { launchGeneration() },
                                enabled = ambiance.isNotBlank() && !generating,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MusicProVioletPrimary),
                                modifier = Modifier.testTag("ai_playlist_generate")
                            ) {
                                Text(stringResource(R.string.ai_generate), fontWeight = FontWeight.Bold)
                            }
                            if (generating) {
                                Spacer(modifier = Modifier.width(12.dp))
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.ai_generating),
                                    color = MusicProTextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        if (aiKeyMissing) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stringResource(R.string.ai_need_key),
                                color = MusicProError,
                                fontSize = 12.sp
                            )
                        }
                        if (aiError) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stringResource(R.string.ai_error),
                                color = MusicProError,
                                fontSize = 12.sp
                            )
                        }

                        aiResults?.let { picked ->
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = stringResource(R.string.ai_preview_title),
                                color = MusicProTextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 260.dp)
                                    .testTag("ai_playlist_preview")
                            ) {
                                items(picked, key = { it.id }) { track ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Checkbox(
                                            checked = track.id in checkedIds,
                                            onCheckedChange = { checked ->
                                                checkedIds = if (checked) {
                                                    checkedIds + track.id
                                                } else {
                                                    checkedIds - track.id
                                                }
                                            }
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = track.title,
                                                color = MusicProTextPrimary,
                                                fontSize = 13.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = track.artist,
                                                color = MusicProTextSecondary,
                                                fontSize = 11.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MusicProTextSecondary
                        ),
                        modifier = Modifier.testTag("cancel_playlist_button")
                    ) {
                        Text(stringResource(R.string.playlist_cancel))
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = { confirm() },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MusicProVioletPrimary
                        ),
                        modifier = Modifier.testTag("confirm_playlist_button")
                    ) {
                        Text(
                            stringResource(
                                if (aiMode && aiResults != null) R.string.ai_create
                                else if (isEditing) R.string.settings_save
                                else R.string.playlist_create
                            )
                        )
                    }
                }
            }
        }
    }
}
