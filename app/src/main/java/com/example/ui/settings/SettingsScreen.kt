package com.example.ui.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.preferences.AppThemeMode
import com.example.data.security.GroqApiKeyStore
import com.example.groq.GroqTranscriptionManager
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
import com.example.ui.theme.MusicProWarning
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val apiKeyStore = remember { GroqApiKeyStore.getInstance(context) }

    // États du ViewModel (Thème, Dynamic Color, Cache)
    val currentTheme by settingsViewModel.themeMode.collectAsStateWithLifecycle()
    val isDynamicColor by settingsViewModel.isDynamicColor.collectAsStateWithLifecycle()
    val cacheSize by settingsViewModel.cacheSize.collectAsStateWithLifecycle()
    val isClearingCache by settingsViewModel.isClearingCache.collectAsStateWithLifecycle()
    val cacheClearMessage by settingsViewModel.cacheClearMessage.collectAsStateWithLifecycle()

    // États de mise à jour de l'application
    val updateCheckState by settingsViewModel.updateCheckState.collectAsStateWithLifecycle()
    val downloadState by settingsViewModel.downloadState.collectAsStateWithLifecycle()

    // États de la clé API Groq
    var apiKeyInput by remember { mutableStateOf(apiKeyStore.getApiKey()) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfigured by remember { mutableStateOf(apiKeyStore.hasApiKey()) }
    var maskedKey by remember { mutableStateOf(apiKeyStore.getMaskedApiKey()) }
    var isTestingKey by remember { mutableStateOf(false) }
    var statusFeedbackMessage by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

    // Dialog de confirmation pour effacer le cache
    var showClearCacheDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            SettingsTopBar(onBack = onBack)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Section 1 : Personnalisation & Thème
            ThemeSelectionCard(
                currentTheme = currentTheme,
                onThemeSelected = { settingsViewModel.setThemeMode(it) },
                isDynamicColor = isDynamicColor,
                onDynamicColorChanged = { settingsViewModel.setDynamicColor(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Section 2 : Configuration Clé API Groq (Whisper large-v3)
            GroqApiKeyCard(
                apiKeyInput = apiKeyInput,
                isConfigured = isConfigured,
                maskedKey = maskedKey,
                isPasswordVisible = isPasswordVisible,
                isTestingKey = isTestingKey,
                statusFeedback = statusFeedbackMessage,
                onApiKeyChange = {
                    apiKeyInput = it
                    statusFeedbackMessage = null
                },
                onToggleVisibility = { isPasswordVisible = !isPasswordVisible },
                onPasteClipboard = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = clipboard.primaryClip
                    if (clip != null && clip.itemCount > 0) {
                        val text = clip.getItemAt(0).text?.toString().orEmpty().trim()
                        apiKeyInput = text
                        statusFeedbackMessage = null
                    }
                },
                onSaveKey = {
                    keyboardController?.hide()
                    if (apiKeyInput.isNotBlank()) {
                        apiKeyStore.setApiKey(apiKeyInput)
                        isConfigured = true
                        maskedKey = apiKeyStore.getMaskedApiKey()
                        statusFeedbackMessage = Pair(true, "Clé enregistrée avec succès via EncryptedSharedPreferences (AES-256 GCM) !")
                    } else {
                        statusFeedbackMessage = Pair(false, "Veuillez saisir une clé API valide.")
                    }
                },
                onDeleteKey = {
                    apiKeyStore.clearApiKey()
                    apiKeyInput = ""
                    isConfigured = false
                    maskedKey = ""
                    statusFeedbackMessage = Pair(true, "Clé API supprimée avec succès.")
                },
                onTestKey = {
                    keyboardController?.hide()
                    val keyToTest = apiKeyInput.ifBlank { apiKeyStore.getApiKey() }
                    if (keyToTest.isBlank()) {
                        statusFeedbackMessage = Pair(false, "Veuillez d'abord saisir une clé API à tester.")
                        return@GroqApiKeyCard
                    }
                    isTestingKey = true
                    statusFeedbackMessage = null
                    scope.launch {
                        val result = GroqTranscriptionManager.testApiKey(keyToTest)
                        isTestingKey = false
                        if (result.isSuccess) {
                            statusFeedbackMessage = Pair(true, "Connexion réussie ! Clé Groq valide pour Whisper large-v3.")
                        } else {
                            val err = result.exceptionOrNull()?.message ?: "Échec du test de clé"
                            statusFeedbackMessage = Pair(false, err)
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Section 3 : Stockage & Cache local
            CacheManagementCard(
                cacheSize = cacheSize,
                isClearing = isClearingCache,
                feedbackMessage = cacheClearMessage,
                onRequestClear = { showClearCacheDialog = true },
                onRefresh = { settingsViewModel.refreshCacheSize() },
                onDismissFeedback = { settingsViewModel.dismissCacheMessage() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Section 4 : Mises à jour de l'application (GitHub Releases)
            AppUpdateCard(
                currentVersion = settingsViewModel.currentVersionName,
                updateCheckState = updateCheckState,
                downloadState = downloadState,
                onCheckForUpdates = { settingsViewModel.checkForUpdates() },
                onDownloadAndInstall = { downloadUrl ->
                    settingsViewModel.downloadAndInstallUpdate(downloadUrl)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Section 5 : À propos de MusicPro
            AboutCard(currentVersion = settingsViewModel.currentVersionName)

            Spacer(modifier = Modifier.height(16.dp))

            // Section 6 : Permissions Système
            PermissionsCard(onNavigateToPermissions = onNavigateToPermissions)

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Dialogue de confirmation pour vider le cache
    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            title = {
                Text(
                    text = "Vider le cache ?",
                    fontWeight = FontWeight.Bold,
                    color = MusicProTextPrimary
                )
            },
            text = {
                Text(
                    text = "Cette action supprimera les pochettes d'album temporaires et les segments audio transcrits en cache. Vos morceaux et playlists ne seront pas modifiés.",
                    fontSize = 13.sp,
                    color = MusicProTextSecondary,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearCacheDialog = false
                        settingsViewModel.clearCache()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MusicProError),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("confirm_clear_cache_button")
                ) {
                    Text("Effacer", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) {
                    Text("Annuler", color = MusicProTextSecondary)
                }
            },
            containerColor = MusicProCardBackground,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun SettingsTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(MusicProSurfaceElevated)
                .testTag("settings_back_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Retour",
                tint = MusicProTextPrimary
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Paramètres",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MusicProTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Thème, IA Whisper & Stockage",
                fontSize = 12.sp,
                color = MusicProCyanNeon,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Section 1: Gestion du Thème de l'application
 */
@Composable
private fun ThemeSelectionCard(
    currentTheme: AppThemeMode,
    onThemeSelected: (AppThemeMode) -> Unit,
    isDynamicColor: Boolean,
    onDynamicColorChanged: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(18.dp), spotColor = MusicProVioletGlow)
            .border(1.dp, MusicProVioletPrimary.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            .testTag("theme_settings_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MusicProCardBackground)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // En-tête
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(MusicProVioletPrimary.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = MusicProCyanNeon,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Apparence & Thème",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MusicProTextPrimary
                    )
                    Text(
                        text = "Sélectionnez l'ambiance visuelle de MusicPro",
                        fontSize = 11.sp,
                        color = MusicProTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Grille/Ligne des 3 thèmes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeOptionItem(
                    title = "Sombre Néon",
                    subtitle = "Recommandé",
                    isSelected = currentTheme == AppThemeMode.DARK,
                    accentColor = MusicProCyanNeon,
                    containerColor = Color(0xFF130924),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("theme_dark_button"),
                    onClick = { onThemeSelected(AppThemeMode.DARK) }
                )

                ThemeOptionItem(
                    title = "Clair",
                    subtitle = "Épuré",
                    isSelected = currentTheme == AppThemeMode.LIGHT,
                    accentColor = MusicProVioletPrimary,
                    containerColor = Color(0xFFF3F4F8),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("theme_light_button"),
                    onClick = { onThemeSelected(AppThemeMode.LIGHT) }
                )

                ThemeOptionItem(
                    title = "Système",
                    subtitle = "Auto",
                    isSelected = currentTheme == AppThemeMode.SYSTEM,
                    accentColor = MusicProVioletLight,
                    containerColor = Color(0xFF1E1530),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("theme_system_button"),
                    onClick = { onThemeSelected(AppThemeMode.SYSTEM) }
                )
            }

            // Option Dynamic Color (Android 12+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MusicProBackground.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, MusicProSurfaceElevated)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = "Couleurs dynamiques (Material You)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MusicProTextPrimary
                            )
                            Text(
                                text = "Adapte les teintes à votre fond d'écran Android",
                                fontSize = 10.sp,
                                color = MusicProTextMuted
                            )
                        }

                        Switch(
                            checked = isDynamicColor,
                            onCheckedChange = onDynamicColorChanged,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = MusicProCyanNeon,
                                uncheckedTrackColor = MusicProSurfaceElevated
                            ),
                            modifier = Modifier.testTag("dynamic_color_switch")
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeOptionItem(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    accentColor: Color,
    containerColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) accentColor else MusicProSurfaceElevated
        )
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) accentColor else Color.Transparent)
                    .border(
                        1.dp,
                        if (isSelected) accentColor else MusicProTextMuted,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (containerColor == Color(0xFFF3F4F8)) Color(0xFF101018) else MusicProTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = if (containerColor == Color(0xFFF3F4F8)) Color(0xFF555566) else MusicProTextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Section 2: Configuration Clé API Groq
 */
@Composable
private fun GroqApiKeyCard(
    apiKeyInput: String,
    isConfigured: Boolean,
    maskedKey: String,
    isPasswordVisible: Boolean,
    isTestingKey: Boolean,
    statusFeedback: Pair<Boolean, String>?,
    onApiKeyChange: (String) -> Unit,
    onToggleVisibility: () -> Unit,
    onPasteClipboard: () -> Unit,
    onSaveKey: () -> Unit,
    onDeleteKey: () -> Unit,
    onTestKey: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(18.dp), spotColor = MusicProVioletGlow)
            .border(
                1.dp,
                if (isConfigured) MusicProCyanNeon.copy(alpha = 0.5f) else MusicProVioletPrimary.copy(alpha = 0.3f),
                RoundedCornerShape(18.dp)
            )
            .testTag("groq_settings_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MusicProCardBackground)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // En-tête de section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MusicProVioletPrimary.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MusicProCyanNeon,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Clé API Groq Whisper",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MusicProTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Modèle Whisper large-v3 par IA",
                            fontSize = 11.sp,
                            color = MusicProTextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Badge statut
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isConfigured) MusicProSuccess.copy(alpha = 0.15f) else MusicProWarning.copy(alpha = 0.15f),
                    border = BorderStroke(
                        1.dp,
                        if (isConfigured) MusicProSuccess.copy(alpha = 0.5f) else MusicProWarning.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isConfigured) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isConfigured) MusicProSuccess else MusicProWarning,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isConfigured) "Configurée" else "Non définie",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isConfigured) MusicProSuccess else MusicProWarning
                        )
                    }
                }
            }

            if (isConfigured && maskedKey.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MusicProBackground.copy(alpha = 0.7f),
                    border = BorderStroke(1.dp, MusicProSurfaceElevated)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MusicProCyanNeon,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Clé : $maskedKey",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = MusicProTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Champ de saisie
            OutlinedTextField(
                value = apiKeyInput,
                onValueChange = onApiKeyChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("groq_api_key_input"),
                label = { Text("Clé API Groq (gsk_...)") },
                placeholder = { Text("gsk_xxxxxxxxxxxxxxxxxxxx") },
                singleLine = true,
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onSaveKey() }),
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onPasteClipboard) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = "Coller depuis le presse-papiers",
                                tint = MusicProCyanLight,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(onClick = onToggleVisibility) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (isPasswordVisible) "Masquer la clé" else "Afficher la clé",
                                tint = MusicProVioletLight,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MusicProCyanNeon,
                    unfocusedBorderColor = MusicProSurfaceElevated,
                    focusedLabelColor = MusicProCyanNeon,
                    unfocusedLabelColor = MusicProTextMuted,
                    focusedTextColor = MusicProTextPrimary,
                    unfocusedTextColor = MusicProTextPrimary,
                    cursorColor = MusicProCyanNeon
                )
            )

            // Message de retour
            AnimatedVisibility(visible = statusFeedback != null) {
                statusFeedback?.let { (isSuccess, message) ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSuccess) MusicProSuccess.copy(alpha = 0.12f) else MusicProError.copy(alpha = 0.12f),
                        border = BorderStroke(
                            1.dp,
                            if (isSuccess) MusicProSuccess.copy(alpha = 0.4f) else MusicProError.copy(alpha = 0.4f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isSuccess) MusicProSuccess else MusicProError,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = message,
                                fontSize = 11.sp,
                                color = if (isSuccess) MusicProSuccess else MusicProError,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Boutons d'action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onSaveKey,
                    modifier = Modifier
                        .weight(1.3f)
                        .height(44.dp)
                        .testTag("save_groq_key_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MusicProCyanNeon)
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Enregistrer",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }

                OutlinedButton(
                    onClick = onTestKey,
                    enabled = !isTestingKey,
                    modifier = Modifier
                        .weight(1.1f)
                        .height(44.dp)
                        .testTag("test_groq_key_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MusicProVioletLight)
                ) {
                    if (isTestingKey) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MusicProVioletLight,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.NetworkCheck,
                            contentDescription = null,
                            tint = MusicProVioletLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Tester",
                            color = MusicProVioletLight,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }
                }

                if (isConfigured) {
                    IconButton(
                        onClick = onDeleteKey,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MusicProSurfaceElevated)
                            .testTag("delete_groq_key_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Effacer la clé",
                            tint = MusicProError,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Guide rapide d'obtention de la clé
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MusicProBackground.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInBrowser,
                        contentDescription = null,
                        tint = MusicProCyanLight,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Clé gratuite en 1 clic sur console.groq.com. Vos fichiers audio volumineux (>22 Mo) sont automatiquement découpés sans perte pour respecter les limites de l'API.",
                        fontSize = 11.sp,
                        color = MusicProTextSecondary,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

/**
 * Section 3: Stockage & Cache local
 */
@Composable
private fun CacheManagementCard(
    cacheSize: String,
    isClearing: Boolean,
    feedbackMessage: String?,
    onRequestClear: () -> Unit,
    onRefresh: () -> Unit,
    onDismissFeedback: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(18.dp), spotColor = MusicProVioletGlow)
            .border(1.dp, MusicProVioletPrimary.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            .testTag("cache_settings_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MusicProCardBackground)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // En-tête
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MusicProVioletPrimary.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = MusicProCyanNeon,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Stockage & Cache",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MusicProTextPrimary
                        )
                        Text(
                            text = "Vignettes et fichiers temporaires",
                            fontSize = 11.sp,
                            color = MusicProTextMuted
                        )
                    }
                }

                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.size(32.dp).testTag("refresh_cache_size_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Actualiser la taille",
                        tint = MusicProTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Cartouche affichant la taille calculée
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MusicProBackground.copy(alpha = 0.7f),
                border = BorderStroke(1.dp, MusicProSurfaceElevated)
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
                            text = "Espace cache occupé",
                            fontSize = 11.sp,
                            color = MusicProTextMuted
                        )
                        Text(
                            text = cacheSize,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MusicProCyanNeon
                        )
                    }

                    OutlinedButton(
                        onClick = onRequestClear,
                        enabled = !isClearing,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MusicProError.copy(alpha = 0.7f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MusicProError),
                        modifier = Modifier.testTag("clear_cache_button")
                    ) {
                        if (isClearing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = MusicProError,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Vider",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Message de retour temporaire
            AnimatedVisibility(visible = feedbackMessage != null) {
                feedbackMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = MusicProSuccess.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, MusicProSuccess.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MusicProSuccess,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = msg,
                                    fontSize = 11.sp,
                                    color = MusicProSuccess
                                )
                            }
                            IconButton(
                                onClick = onDismissFeedback,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Text("×", fontSize = 16.sp, color = MusicProTextSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Section 5: À propos de MusicPro
 */
@Composable
private fun AboutCard(currentVersion: String = "1.0") {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(18.dp), spotColor = MusicProVioletGlow)
            .border(1.dp, MusicProSurfaceElevated, RoundedCornerShape(18.dp))
            .testTag("about_settings_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MusicProCardBackground)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Logo et En-tête de l'application
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MusicProVioletPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "MusicPro",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MusicProTextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MusicProCyanNeon.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, MusicProCyanNeon.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "v$currentVersion",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MusicProCyanNeon,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Lecteur Audio Haute Définition & IA Paroles",
                        fontSize = 11.sp,
                        color = MusicProTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Liste des technologies intégrées
            TechBadgeRow(title = "Moteur Audio", value = "AndroidX Media3 ExoPlayer")
            TechBadgeRow(title = "Interface UI", value = "Jetpack Compose M3 (Fluid)")
            TechBadgeRow(title = "Moteur IA", value = "Groq LPU (Whisper large-v3)")
            TechBadgeRow(title = "Widget Écran d'accueil", value = "Jetpack Glance Responsive")
            TechBadgeRow(title = "Persistance", value = "Room Database SQLite")
            TechBadgeRow(title = "Sécurité", value = "EncryptedSharedPreferences AES-256")

            Spacer(modifier = Modifier.height(12.dp))

            // Engagement confidentialité
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MusicProBackground.copy(alpha = 0.6f)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = MusicProSuccess,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Vos fichiers audio et métadonnées restent 100% locaux. Seules les requêtes de paroles expressément demandées interrogent Groq ou LRCLIB.",
                        fontSize = 11.sp,
                        color = MusicProTextSecondary,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun TechBadgeRow(title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            color = MusicProTextMuted,
            modifier = Modifier.weight(1f, fill = false)
        )
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MusicProCyanLight,
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Section 5: Permissions système
 */
@Composable
private fun PermissionsCard(onNavigateToPermissions: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(18.dp), spotColor = MusicProVioletGlow)
            .border(1.dp, MusicProSurfaceElevated, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MusicProCardBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    text = "Permissions du système",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MusicProTextPrimary
                )
                Text(
                    text = "Accès aux fichiers audio locaux et notifications",
                    fontSize = 11.sp,
                    color = MusicProTextMuted
                )
            }

            OutlinedButton(
                onClick = onNavigateToPermissions,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, MusicProVioletLight),
                modifier = Modifier.testTag("manage_permissions_button")
            ) {
                Text(
                    text = "Gérer",
                    color = MusicProVioletLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
