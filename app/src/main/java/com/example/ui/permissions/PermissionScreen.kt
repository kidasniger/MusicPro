package com.example.ui.permissions

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.permissions.PermissionUiState
import com.example.permissions.PermissionUtils
import com.example.ui.theme.MusicProBackground
import com.example.ui.theme.MusicProBackgroundDeep
import com.example.ui.theme.MusicProCyanNeon
import com.example.ui.theme.MusicProCyanVibrant
import com.example.ui.theme.MusicProFavorite
import com.example.ui.theme.MusicProPrimaryGradient
import com.example.ui.theme.MusicProSurface
import com.example.ui.theme.MusicProSurfaceVariant
import com.example.ui.theme.MusicProTextMuted
import com.example.ui.theme.MusicProTextPrimary
import com.example.ui.theme.MusicProTextSecondary
import com.example.ui.theme.MusicProVioletGlow
import com.example.ui.theme.MusicProVioletPastel
import com.example.ui.theme.MusicProVioletPrimary
import com.example.ui.theme.MusicProVioletVibrant

@Composable
fun PermissionScreen(
    state: PermissionUiState,
    onRequestPermissions: (Map<String, Boolean>) -> Unit,
    onManualCheck: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val permissionsToRequest = PermissionUtils.getAppPermissions()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        onRequestPermissions(result)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MusicProBackgroundDeep,
                        MusicProBackground,
                        Color(0xFF0D0D1A)
                    )
                )
            )
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 40.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Logo & Branding
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Logo container with neon glow
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(110.dp)
                        .shadow(
                            elevation = 24.dp,
                            shape = CircleShape,
                            ambientColor = MusicProVioletGlow,
                            spotColor = MusicProCyanNeon
                        )
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF241647), Color(0xFF121224))
                            ),
                            shape = CircleShape
                        )
                        .border(
                            width = 2.dp,
                            brush = Brush.linearGradient(
                                listOf(MusicProVioletPrimary, MusicProCyanNeon)
                            ),
                            shape = CircleShape
                        )
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.musicpro_logo_cutout),
                        contentDescription = "Logo MusicPro",
                        modifier = Modifier
                            .size(72.dp)
                            .testTag("app_logo_image")
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "MusicPro",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MusicProTextPrimary,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Neon badge
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0x338A2BE2),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x668A2BE2))
                ) {
                    Text(
                        text = "LECTEUR AUDIO 100% HORS LIGNE",
                        color = MusicProCyanNeon,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Pour vous offrir une expérience musicale fluide, privée et sans connexion, MusicPro a besoin de quelques autorisations sur votre appareil.",
                    fontSize = 14.sp,
                    color = MusicProTextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Permission Cards
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Card 1: Audio files permission
                val audioPermissionName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    "READ_MEDIA_AUDIO (Android 13+)"
                } else {
                    "READ_EXTERNAL_STORAGE (Android 12-)"
                }

                PermissionDetailCard(
                    icon = Icons.Default.LibraryMusic,
                    iconColor = MusicProVioletVibrant,
                    title = "Accès aux fichiers audio",
                    permissionTag = audioPermissionName,
                    description = "Indispensable pour indexer, lire vos morceaux de musique locaux (MP3, FLAC, AAC, WAV) et charger les pochettes d'albums sans Internet.",
                    isGranted = state.isAudioGranted,
                    isRequired = true,
                    modifier = Modifier.testTag("permission_card_audio")
                )

                // Card 2: Notifications permission
                PermissionDetailCard(
                    icon = Icons.Default.NotificationsActive,
                    iconColor = MusicProCyanVibrant,
                    title = "Contrôle en arrière-plan",
                    permissionTag = "POST_NOTIFICATIONS (Android 13+)",
                    description = "Affiche la notification persistante de lecture avec commandes média (Pause, Suivant, Précédent) et écran de verrouillage.",
                    isGranted = state.isNotificationGranted,
                    isRequired = false,
                    modifier = Modifier.testTag("permission_card_notifications")
                )

                // Refusal / Explanatory Alert Card
                AnimatedVisibility(
                    visible = state.isDeniedExplanationNeeded,
                    enter = fadeIn() + slideInVertically()
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF261019)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MusicProFavorite.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("permission_denied_explanation_card")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MusicProFavorite.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Attention",
                                        tint = MusicProFavorite,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Accès audio requis",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MusicProTextPrimary
                                    )
                                    Text(
                                        text = "Permission refusée par le système",
                                        fontSize = 12.sp,
                                        color = MusicProFavorite
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "L'accès à votre stockage audio est indispensable pour que MusicPro fonctionne. Sans cette permission, l'application ne peut pas détecter vos musiques.\n\nVeuillez autoriser l'accès dans les Paramètres système de l'application.",
                                fontSize = 13.sp,
                                color = MusicProTextSecondary,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = { PermissionUtils.openAppSettings(context) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MusicProFavorite
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("open_settings_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Ouvrir les Paramètres Système",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Footer Actions
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!state.isDeniedExplanationNeeded) {
                    // Primary Request Button
                    Button(
                        onClick = {
                            permissionLauncher.launch(permissionsToRequest.toTypedArray())
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .shadow(
                                elevation = 12.dp,
                                shape = RoundedCornerShape(14.dp),
                                spotColor = MusicProCyanNeon
                            )
                            .background(
                                brush = MusicProPrimaryGradient,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .testTag("request_permission_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Accorder les autorisations",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                } else {
                    // Retry Button after opening settings
                    OutlinedButton(
                        onClick = onManualCheck,
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MusicProCyanVibrant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("recheck_permissions_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = MusicProCyanNeon,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Vérifier à nouveau",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MusicProCyanNeon
                        )
                    }
                }

                Text(
                    text = "🔒 Aucune donnée audio n'est transférée ni partagée sur Internet",
                    fontSize = 11.sp,
                    color = MusicProTextMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun PermissionDetailCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    permissionTag: String,
    description: String,
    isGranted: Boolean,
    isRequired: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MusicProSurface
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isGranted) Color(0xFF10B981).copy(alpha = 0.6f) else Color(0x33FFFFFF)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconColor.copy(alpha = 0.15f))
                    .border(
                        1.dp,
                        iconColor.copy(alpha = 0.35f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MusicProTextPrimary
                    )

                    if (isGranted) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0x3310B981),
                            modifier = Modifier.size(22.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Accordée",
                                tint = Color(0xFF10B981),
                                modifier = Modifier
                                    .padding(3.dp)
                                    .size(16.dp)
                            )
                        }
                    } else if (isRequired) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0x268A2BE2)
                        ) {
                            Text(
                                text = "REQUIS",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MusicProVioletPastel,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = permissionTag,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = MusicProCyanNeon,
                    letterSpacing = 0.4.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = MusicProTextSecondary,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
