package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.MusicProBackground
import com.example.ui.theme.MusicProCardBackground
import com.example.ui.theme.MusicProCyanLight
import com.example.ui.theme.MusicProCyanNeon
import com.example.ui.theme.MusicProError
import com.example.ui.theme.MusicProSuccess
import com.example.ui.theme.MusicProSurface
import com.example.ui.theme.MusicProSurfaceElevated
import com.example.ui.theme.MusicProTextMuted
import com.example.ui.theme.MusicProTextPrimary
import com.example.ui.theme.MusicProTextSecondary
import com.example.ui.theme.MusicProVioletGlow
import com.example.ui.theme.MusicProVioletLight
import com.example.ui.theme.MusicProVioletPrimary
import com.example.updater.DownloadState
import com.example.updater.UpdateCheckState

@Composable
fun AppUpdateDialog(
    updateInfo: UpdateCheckState.UpdateAvailable,
    downloadState: DownloadState,
    onDismiss: () -> Unit,
    onDownloadAndInstall: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDownloading = downloadState is DownloadState.Downloading
    val isDownloaded = downloadState is DownloadState.Downloaded

    Dialog(
        onDismissRequest = {
            // Empêche la fermeture accidentelle pendant le téléchargement
            if (!isDownloading) onDismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = !isDownloading,
            dismissOnClickOutside = !isDownloading
        )
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .shadow(24.dp, RoundedCornerShape(24.dp), spotColor = MusicProVioletGlow)
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(MusicProVioletLight.copy(alpha = 0.6f), MusicProCyanNeon.copy(alpha = 0.3f))
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .testTag("app_update_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MusicProCardBackground
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // En-tête avec halo lumineux et icône
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(MusicProVioletPrimary, MusicProCyanNeon)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SystemUpdate,
                        contentDescription = "Mise à jour disponible",
                        tint = MusicProBackground,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Titre
                Text(
                    text = "Mise à jour disponible !",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MusicProTextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Une nouvelle version de MusicPro est prête pour vous.",
                    fontSize = 13.sp,
                    color = MusicProTextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Cartouche comparatif de versions
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MusicProSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MusicProSurfaceElevated)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Actuelle",
                                fontSize = 11.sp,
                                color = MusicProTextMuted
                            )
                            Text(
                                text = "v${updateInfo.currentVersion}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MusicProTextSecondary
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.NewReleases,
                            contentDescription = null,
                            tint = MusicProCyanNeon,
                            modifier = Modifier.size(20.dp)
                        )

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Nouvelle",
                                fontSize = 11.sp,
                                color = MusicProCyanLight
                            )
                            val sizeText = if (updateInfo.apkSize > 0) {
                                " • ${(updateInfo.apkSize / (1024 * 1024 * 1.0)).let { "%.1f".format(it) }} Mo"
                            } else ""
                            Text(
                                text = "v${updateInfo.latestVersion}$sizeText",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MusicProCyanNeon
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Notes de version avec ascenseur si long
                Text(
                    text = "Nouveautés & Corrections :",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MusicProTextSecondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                )

                val scrollState = rememberScrollState()
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 130.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = MusicProBackground.copy(alpha = 0.6f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MusicProSurfaceElevated)
                ) {
                    Text(
                        text = updateInfo.releaseNotes.ifBlank { "Mise à jour et améliorations de stabilité." },
                        fontSize = 12.sp,
                        color = MusicProTextSecondary,
                        modifier = Modifier
                            .padding(10.dp)
                            .verticalScroll(scrollState)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Zone de progression du téléchargement ou boutons d'actions
                when (downloadState) {
                    is DownloadState.Idle -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MusicProSurfaceElevated),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("update_dialog_dismiss_button")
                            ) {
                                Text(
                                    text = "Plus tard",
                                    color = MusicProTextSecondary,
                                    fontSize = 13.sp
                                )
                            }

                            Button(
                                onClick = { onDownloadAndInstall(updateInfo.downloadUrl) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MusicProCyanNeon
                                ),
                                modifier = Modifier
                                    .weight(1.4f)
                                    .height(44.dp)
                                    .testTag("update_dialog_install_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDownload,
                                    contentDescription = null,
                                    tint = MusicProBackground,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Mettre à jour",
                                    color = MusicProBackground,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    is DownloadState.Downloading -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Téléchargement en cours...",
                                    fontSize = 12.sp,
                                    color = MusicProTextPrimary
                                )
                                Text(
                                    text = "${downloadState.progressPercent}%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MusicProCyanNeon
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { downloadState.progressPercent / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = MusicProCyanNeon,
                                trackColor = MusicProSurfaceElevated
                            )

                            val downloadedMb = (downloadState.downloadedBytes / (1024.0 * 1024.0))
                            val totalMb = (downloadState.totalBytes / (1024.0 * 1024.0))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "%.1f Mo / %.1f Mo".format(downloadedMb, totalMb),
                                fontSize = 11.sp,
                                color = MusicProTextMuted
                            )
                        }
                    }

                    is DownloadState.Downloaded -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MusicProSuccess,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Téléchargé ! Lancement de l'installateur...",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MusicProSuccess
                            )
                        }
                    }

                    is DownloadState.Error -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = MusicProError,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = downloadState.message,
                                    fontSize = 12.sp,
                                    color = MusicProError,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onDismiss,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Fermer", fontSize = 12.sp, color = MusicProTextSecondary)
                                }
                                Button(
                                    onClick = { onDownloadAndInstall(updateInfo.downloadUrl) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MusicProVioletPrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Réessayer", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
