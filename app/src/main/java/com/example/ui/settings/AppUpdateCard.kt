package com.example.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MusicProBackground
import com.example.ui.theme.MusicProCardBackground
import com.example.ui.theme.MusicProCyanLight
import com.example.ui.theme.MusicProCyanNeon
import com.example.ui.theme.MusicProError
import com.example.ui.theme.MusicProSuccess
import com.example.ui.theme.MusicProSurfaceElevated
import com.example.ui.theme.MusicProTextMuted
import com.example.ui.theme.MusicProTextPrimary
import com.example.ui.theme.MusicProTextSecondary
import com.example.ui.theme.MusicProVioletGlow
import com.example.ui.theme.MusicProVioletLight
import com.example.ui.theme.MusicProVioletPrimary
import com.example.updater.AppUpdateManager
import com.example.updater.DownloadState
import com.example.updater.UpdateCheckState

@Composable
fun AppUpdateCard(
    currentVersion: String,
    updateCheckState: UpdateCheckState,
    downloadState: DownloadState,
    onCheckForUpdates: () -> Unit,
    onDownloadAndInstall: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(18.dp), spotColor = MusicProVioletGlow)
            .border(1.dp, MusicProSurfaceElevated, RoundedCornerShape(18.dp))
            .testTag("app_update_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MusicProCardBackground)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MusicProCyanNeon.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SystemUpdate,
                        contentDescription = null,
                        tint = MusicProCyanNeon,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Mises à jour de l'application",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MusicProTextPrimary
                    )
                    Text(
                        text = "Dépôt : ${AppUpdateManager.GITHUB_OWNER}/${AppUpdateManager.GITHUB_REPO}",
                        fontSize = 11.sp,
                        color = MusicProTextMuted
                    )
                }

                // Version badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MusicProSurfaceElevated,
                    border = BorderStroke(1.dp, MusicProCyanLight.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "v$currentVersion",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MusicProCyanLight,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action / Status Section
            when (updateCheckState) {
                is UpdateCheckState.Idle -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Vérifier si une nouvelle version est publiée",
                            fontSize = 12.sp,
                            color = MusicProTextSecondary,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = onCheckForUpdates,
                            colors = ButtonDefaults.buttonColors(containerColor = MusicProVioletPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("check_updates_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Vérifier", fontSize = 12.sp)
                        }
                    }
                }

                is UpdateCheckState.Checking -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MusicProCyanNeon,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Interrogation de GitHub Releases...",
                            fontSize = 12.sp,
                            color = MusicProTextSecondary
                        )
                    }
                }

                is UpdateCheckState.UpToDate -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = MusicProSuccess.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, MusicProSuccess.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MusicProSuccess,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Votre application est à jour",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MusicProSuccess
                                )
                                Text(
                                    text = "Version v${updateCheckState.currentVersion} installée",
                                    fontSize = 11.sp,
                                    color = MusicProTextMuted
                                )
                            }
                            OutlinedButton(
                                onClick = onCheckForUpdates,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, MusicProSuccess.copy(alpha = 0.5f))
                            ) {
                                Text("Re-tester", fontSize = 11.sp, color = MusicProSuccess)
                            }
                        }
                    }
                }

                is UpdateCheckState.Error -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = MusicProError.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, MusicProError.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = MusicProError,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = updateCheckState.message,
                                    fontSize = 12.sp,
                                    color = MusicProTextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = onCheckForUpdates,
                                colors = ButtonDefaults.buttonColors(containerColor = MusicProVioletPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Réessayer", fontSize = 11.sp)
                            }
                        }
                    }
                }

                is UpdateCheckState.UpdateAvailable -> {
                    val info = updateCheckState
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MusicProVioletPrimary.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, MusicProVioletLight.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.NewReleases,
                                    contentDescription = null,
                                    tint = MusicProCyanNeon,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Nouvelle version v${info.latestVersion} disponible !",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MusicProTextPrimary
                                    )
                                    val sizeMb = if (info.apkSize > 0) {
                                        " • ${(info.apkSize / (1024 * 1024 * 1.0)).let { "%.1f".format(it) }} Mo"
                                    } else ""
                                    Text(
                                        text = "Actuelle : v${info.currentVersion}$sizeMb",
                                        fontSize = 11.sp,
                                        color = MusicProCyanLight
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Notes de version
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = MusicProBackground.copy(alpha = 0.7f)
                            ) {
                                Text(
                                    text = info.releaseNotes,
                                    fontSize = 11.sp,
                                    color = MusicProTextSecondary,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // État du téléchargement
                            when (downloadState) {
                                is DownloadState.Idle -> {
                                    Button(
                                        onClick = { onDownloadAndInstall(info.downloadUrl) },
                                        colors = ButtonDefaults.buttonColors(containerColor = MusicProCyanNeon),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("download_update_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CloudDownload,
                                            contentDescription = null,
                                            tint = MusicProBackground,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Télécharger & Installer la mise à jour",
                                            color = MusicProBackground,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }

                                is DownloadState.Downloading -> {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Téléchargement de l'APK...",
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
                                        Spacer(modifier = Modifier.height(6.dp))
                                        LinearProgressIndicator(
                                            progress = { downloadState.progressPercent / 100f },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(8.dp)
                                                .clip(RoundedCornerShape(4.dp)),
                                            color = MusicProCyanNeon,
                                            trackColor = MusicProSurfaceElevated
                                        )
                                    }
                                }

                                is DownloadState.Downloaded -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = MusicProSuccess,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Téléchargement terminé ! Installation en cours...",
                                            fontSize = 12.sp,
                                            color = MusicProSuccess
                                        )
                                    }
                                }

                                is DownloadState.Error -> {
                                    Text(
                                        text = downloadState.message,
                                        fontSize = 12.sp,
                                        color = MusicProError
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Button(
                                        onClick = { onDownloadAndInstall(info.downloadUrl) },
                                        colors = ButtonDefaults.buttonColors(containerColor = MusicProVioletPrimary),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Réessayer le téléchargement", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
