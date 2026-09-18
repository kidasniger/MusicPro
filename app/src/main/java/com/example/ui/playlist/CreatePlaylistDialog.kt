package com.example.ui.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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

@Composable
fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, description: String) -> Unit,
    initialName: String = "",
    initialDescription: String = "",
    isEditing: Boolean = false
) {
    var name by remember { mutableStateOf(initialName) }
    var description by remember { mutableStateOf(initialDescription) }
    var isError by remember { mutableStateOf(false) }

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
                        text = if (isEditing) "Renommer la playlist" else "Nouvelle playlist",
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
                    label = { Text("Nom de la playlist") },
                    placeholder = { Text("Ex: Synthwave Nuit, Workout, Favoris...", color = MusicProTextMuted) },
                    singleLine = true,
                    isError = isError,
                    supportingText = {
                        if (isError) {
                            Text("Le nom ne peut pas être vide", color = MusicProError, fontSize = 12.sp)
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
                    label = { Text("Description (optionnel)") },
                    placeholder = { Text("Ex: Ma sélection préférée", color = MusicProTextMuted) },
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

                Spacer(modifier = Modifier.height(24.dp))

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
                        Text("Annuler")
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = {
                            if (name.trim().isBlank()) {
                                isError = true
                            } else {
                                onConfirm(name.trim(), description.trim())
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MusicProVioletPrimary
                        ),
                        modifier = Modifier.testTag("confirm_playlist_button")
                    ) {
                        Text(if (isEditing) "Enregistrer" else "Créer")
                    }
                }
            }
        }
    }
}
