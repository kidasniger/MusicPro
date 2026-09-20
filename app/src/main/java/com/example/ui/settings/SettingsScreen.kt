package com.example.ui.settings

import androidx.activity.compose.BackHandler
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import com.example.R
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
    val isCleaningLibrary by settingsViewModel.isCleaningLibrary.collectAsStateWithLifecycle()
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

    // Interception du bouton retour Android pour revenir proprement à l'écran précédent
    BackHandler {
        if (showClearCacheDialog) {
            showClearCacheDialog = false
        } else {
            onBack()
        }
    }

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
                        if (apiKeyStore.setApiKey(apiKeyInput)) {
                            isConfigured = true
                            maskedKey = apiKeyStore.getMaskedApiKey()
                            statusFeedbackMessage = Pair(true, "Clé enregistrée avec succès via EncryptedSharedPreferences (AES-256 GCM) !")
                        } else {
                            statusFeedbackMessage = Pair(false, "Impossible d'initialiser le stockage chiffré sur cet appareil. La clé n'a pas été enregistrée.")
                        }
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
                isCleaningLibrary = isCleaningLibrary,
                feedbackMessage = cacheClearMessage,
                onRequestClear = { showClearCacheDialog = true },
                onCleanLibrary = { settingsViewModel.cleanAndRescanLibrary() },
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
                },
                onInstallExisting = {
                    settingsViewModel.installExistingApk()
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
