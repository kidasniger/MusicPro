package com.example.ui.settings

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.data.backup.BackupFormatException
import com.example.data.backup.SettingsBackup
import com.example.ui.audio.AudioViewModel
import com.example.ui.components.EqualizerSheet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.preferences.AppThemeMode
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import com.example.data.preferences.AppearanceMode
import com.example.ui.components.ThemeRevealHost
import com.example.ui.components.ThemeRevealState
import com.example.ui.components.beginReveal
import com.example.ui.components.rememberThemeRevealState
import com.example.data.preferences.AppStyle
import com.example.data.preferences.TextSizePreference
import com.example.data.preferences.ListDensityPreference
import com.example.data.security.GroqApiKeyStore
import com.example.groq.GroqTranscriptionManager
import com.example.ui.theme.MusicProCyanLight
import com.example.ui.theme.MusicProCyanNeon
import com.example.ui.theme.MusicProError
import com.example.ui.theme.MusicProSuccess
import com.example.ui.theme.MusicProVioletGlow
import com.example.ui.theme.MusicProVioletLight
import com.example.ui.theme.MusicProVioletPrimary
import com.example.ui.theme.MusicProWarning
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = viewModel(),
    onOpenGlassSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val apiKeyStore = remember { GroqApiKeyStore.getInstance(context) }

    // États du ViewModel (Apparence, Style, Accent, Couleurs dynamiques, Cache)
    val appearance by settingsViewModel.appearanceMode.collectAsStateWithLifecycle()
    val appStyle by settingsViewModel.appStyle.collectAsStateWithLifecycle()
    val accentChoice by settingsViewModel.accentChoice.collectAsStateWithLifecycle()
    val eveningStart by settingsViewModel.eveningStartMinutes.collectAsStateWithLifecycle()
    val eveningEnd by settingsViewModel.eveningEndMinutes.collectAsStateWithLifecycle()
    val isDynamicColor by settingsViewModel.isDynamicColor.collectAsStateWithLifecycle()
    val autoPlay by settingsViewModel.autoPlay.collectAsStateWithLifecycle()
    val gaplessPlayback by settingsViewModel.gaplessPlayback.collectAsStateWithLifecycle()
    val resumePlayback by settingsViewModel.resumePlayback.collectAsStateWithLifecycle()
    val playerPresentation by settingsViewModel.playerPresentation.collectAsStateWithLifecycle()
    val textSizePreference by settingsViewModel.textSizePreference.collectAsStateWithLifecycle()
    val listDensityPreference by settingsViewModel.listDensityPreference.collectAsStateWithLifecycle()
    val reduceAnimations by settingsViewModel.reduceAnimations.collectAsStateWithLifecycle()
    val cacheSize by settingsViewModel.cacheSize.collectAsStateWithLifecycle()
    val isClearingCache by settingsViewModel.isClearingCache.collectAsStateWithLifecycle()
    val isCleaningLibrary by settingsViewModel.isCleaningLibrary.collectAsStateWithLifecycle()
    val cacheClearMessage by settingsViewModel.cacheClearMessage.collectAsStateWithLifecycle()

    // Son / égaliseur (mise à jour 037) : le ViewModel audio n'est créé qu'à l'ouverture de
    // la feuille, pour ne pas instancier MediaController depuis les tests Robolectric.
    var showEqualizerSheet by remember { mutableStateOf(false) }

    // Mise à jour 078 — révélation circulaire du nouveau thème depuis le bouton touché.
    val themeReveal = rememberThemeRevealState()

    // États sauvegarde des réglages (mise à jour 038)
    var pendingImportBackup by remember { mutableStateOf<SettingsBackup?>(null) }
    var backupFeedback by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val json = settingsViewModel.exportSettingsJson()
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.use { stream ->
                            stream.write(json.toByteArray(Charsets.UTF_8))
                        } ?: throw IllegalStateException("OutputStream indisponible")
                    }
                    backupFeedback = Pair(true, context.getString(R.string.settings_backup_exported))
                } catch (e: Exception) {
                    backupFeedback = Pair(false, context.getString(R.string.settings_backup_export_failed))
                }
            }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val text = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            stream.readBytes().toString(Charsets.UTF_8)
                        } ?: ""
                    }
                    backupFeedback = null
                    pendingImportBackup = settingsViewModel.parseImportedSettings(text)
                } catch (e: BackupFormatException) {
                    backupFeedback = Pair(false, context.getString(R.string.settings_backup_invalid, e.message ?: ""))
                } catch (e: Exception) {
                    backupFeedback = Pair(false, context.getString(R.string.settings_backup_import_failed))
                }
            }
        }
    }

    // Réinitialisation par section (mise à jour 040)
    var resetSection by remember { mutableStateOf<SettingsSectionReset?>(null) }

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
    var expandedGroups by remember { mutableStateOf(setOf("APPARENCE")) }
    var settingsSearch by remember { mutableStateOf("") }
    val normalizedSearch = settingsSearch.trim().lowercase()
    val searchResults = remember(normalizedSearch) {
        settingsSearchIndex.filter { entry ->
            normalizedSearch.isNotEmpty() &&
                (context.getString(entry.labelRes).lowercase().contains(normalizedSearch) ||
                 entry.keywords.any { it.lowercase().contains(normalizedSearch) })
        }
    }
    val matchingGroups = remember(searchResults) { searchResults.map { it.group }.toSet() }
    androidx.compose.runtime.LaunchedEffect(matchingGroups, normalizedSearch) {
        if (normalizedSearch.isNotEmpty()) expandedGroups = expandedGroups + matchingGroups
    }

    // Interception du bouton retour Android pour revenir proprement à l'écran précédent
    BackHandler {
        when {
            showEqualizerSheet -> showEqualizerSheet = false
            resetSection != null -> resetSection = null
            pendingImportBackup != null -> pendingImportBackup = null
            showClearCacheDialog -> showClearCacheDialog = false
            else -> onBack()
        }
    }

    ThemeRevealHost(state = themeReveal, modifier = modifier.fillMaxSize()) {
    Scaffold(
        modifier = Modifier
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
            val haptic = LocalHapticFeedback.current
            QuickAppearanceCard(
                appearance = appearance,
                style = appStyle,
                reveal = themeReveal,
                onAppearanceSelected = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    settingsViewModel.setAppearanceMode(it)
                },
                onStyleSelected = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    settingsViewModel.setAppStyle(it)
                }
            )
            OutlinedTextField(
                value = settingsSearch,
                onValueChange = { settingsSearch = it },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).testTag("settings_search_field"),
                singleLine = true,
                placeholder = { Text(stringResource(R.string.settings_search_hint)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = stringResource(R.string.settings_search_cd)) },
                trailingIcon = {
                    if (settingsSearch.isNotEmpty()) {
                        IconButton(onClick = { settingsSearch = "" }) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.settings_clear_search))
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
            if (normalizedSearch.isNotEmpty()) {
                if (searchResults.isEmpty()) {
                    Text(
                        text = stringResource(R.string.settings_no_results),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 12.dp)) {
                        searchResults.forEach { result ->
                            SettingsSearchResultRow(
                                label = context.getString(result.labelRes),
                                group = result.group,
                                query = normalizedSearch,
                                onClick = { expandedGroups = expandedGroups + result.group }
                            )
                        }
                    }
                }
            }
            if (normalizedSearch.isEmpty() || "APPARENCE" in matchingGroups) SettingsGroupCard(
                title = stringResource(R.string.settings_group_appearance),
                value = stringResource(R.string.settings_group_appearance_desc),
                icon = Icons.Default.AutoAwesome,
                expanded = "APPARENCE" in expandedGroups,
                onClick = { expandedGroups = toggleSettingsGroup(expandedGroups, "APPARENCE") },
                resetItems = SettingsSectionReset.APPARENCE.itemRes,
                onResetRequest = { resetSection = SettingsSectionReset.APPARENCE }
            ) {
                LanguageSettingsCard()
                Spacer(modifier = Modifier.height(8.dp))
                AppearanceSettingsCard(
                    appearance = appearance,
                    onAppearanceSelected = { settingsViewModel.setAppearanceMode(it) },
                    style = appStyle,
                    onStyleSelected = { settingsViewModel.setAppStyle(it) },
                    accent = accentChoice,
                    onAccentSelected = { settingsViewModel.setAccentChoice(it) },
                    eveningStartMinutes = eveningStart,
                    eveningEndMinutes = eveningEnd,
                    onEveningStartChange = { settingsViewModel.setEveningStartMinutes(it) },
                    onEveningEndChange = { settingsViewModel.setEveningEndMinutes(it) },
                    isDynamicColor = isDynamicColor,
                    onDynamicColorChanged = { settingsViewModel.setDynamicColor(it) },
                    textSize = textSizePreference,
                    onTextSizeSelected = { settingsViewModel.setTextSizePreference(it) },
                    listDensity = listDensityPreference,
                    onListDensitySelected = { settingsViewModel.setListDensityPreference(it) },
                    reduceAnimations = reduceAnimations,
                    onReduceAnimationsChanged = { settingsViewModel.setReduceAnimations(it) }
                )
                Spacer(modifier = Modifier.height(10.dp))
                GlassSettingsEntry(onClick = onOpenGlassSettings)
            }

            if (normalizedSearch.isEmpty() || "LECTURE" in matchingGroups) SettingsGroupCard(
                title = stringResource(R.string.settings_group_playback),
                value = stringResource(R.string.settings_group_playback_desc),
                icon = Icons.Default.MusicNote,
                expanded = "LECTURE" in expandedGroups,
                onClick = { expandedGroups = toggleSettingsGroup(expandedGroups, "LECTURE") },
                resetItems = SettingsSectionReset.LECTURE.itemRes,
                onResetRequest = { resetSection = SettingsSectionReset.LECTURE }
            ) {
                SettingsActionRow(
                    icon = Icons.Default.VolumeUp,
                    title = stringResource(R.string.settings_sound_title),
                    value = stringResource(R.string.settings_sound_desc),
                    tag = "settings_sound_row",
                    onClick = { showEqualizerSheet = true }
                )
                Spacer(modifier = Modifier.height(8.dp))
                PlaybackSettingsCard(
                    autoPlay = autoPlay,
                    gaplessPlayback = gaplessPlayback,
                    resumePlayback = resumePlayback,
                    onAutoPlayChanged = { settingsViewModel.setAutoPlay(it) },
                    onGaplessChanged = { settingsViewModel.setGaplessPlayback(it) },
                    onResumeChanged = { settingsViewModel.setResumePlayback(it) },
                    presentation = playerPresentation,
                    onPresentationSelected = { settingsViewModel.setPlayerPresentation(it) }
                )
            }

            if (normalizedSearch.isEmpty() || "PAROLES" in matchingGroups) SettingsGroupCard(
                title = stringResource(R.string.settings_group_lyrics),
                value = stringResource(R.string.settings_group_lyrics_desc),
                icon = Icons.Default.GraphicEq,
                expanded = "PAROLES" in expandedGroups,
                onClick = { expandedGroups = toggleSettingsGroup(expandedGroups, "PAROLES") },
                resetItems = SettingsSectionReset.PAROLES.itemRes,
                onResetRequest = { resetSection = SettingsSectionReset.PAROLES }
            ) {
                SettingsInfoRow(stringResource(R.string.settings_lyrics_info))
            }

            if (normalizedSearch.isEmpty() || "INTELLIGENCE ARTIFICIELLE" in matchingGroups) SettingsGroupCard(
                title = stringResource(R.string.settings_group_ai),
                value = stringResource(if (isConfigured) R.string.settings_groq_configured else R.string.settings_groq_setup),
                icon = Icons.Default.AutoAwesome,
                expanded = "INTELLIGENCE ARTIFICIELLE" in expandedGroups,
                onClick = { expandedGroups = toggleSettingsGroup(expandedGroups, "INTELLIGENCE ARTIFICIELLE") },
                resetItems = SettingsSectionReset.INTELLIGENCE_ARTIFICIELLE.itemRes,
                onResetRequest = { resetSection = SettingsSectionReset.INTELLIGENCE_ARTIFICIELLE }
            ) {
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
                            apiKeyInput = clip.getItemAt(0).text?.toString().orEmpty().trim()
                            statusFeedbackMessage = null
                        }
                    },
                    onSaveKey = {
                        keyboardController?.hide()
                        if (apiKeyInput.isNotBlank()) {
                            if (apiKeyStore.setApiKey(apiKeyInput)) {
                                isConfigured = true
                                maskedKey = apiKeyStore.getMaskedApiKey()
                                statusFeedbackMessage = Pair(true, context.getString(R.string.settings_groq_key_saved))
                            } else {
                                statusFeedbackMessage = Pair(false, context.getString(R.string.settings_groq_key_save_failed))
                            }
                        } else {
                            statusFeedbackMessage = Pair(false, context.getString(R.string.settings_groq_key_required))
                        }
                    },
                    onDeleteKey = {
                        apiKeyStore.clearApiKey()
                        apiKeyInput = ""
                        isConfigured = false
                        maskedKey = ""
                        statusFeedbackMessage = Pair(true, context.getString(R.string.settings_groq_key_deleted))
                    },
                    onTestKey = {
                        keyboardController?.hide()
                        val keyToTest = apiKeyInput.ifBlank { apiKeyStore.getApiKey() }
                        if (keyToTest.isBlank()) {
                            statusFeedbackMessage = Pair(false, context.getString(R.string.settings_groq_key_enter_first))
                        } else {
                            isTestingKey = true
                            scope.launch {
                                val result = GroqTranscriptionManager.testApiKey(keyToTest)
                                isTestingKey = false
                                statusFeedbackMessage = if (result.isSuccess) Pair(true, context.getString(R.string.settings_groq_connection_success)) else Pair(false, result.exceptionOrNull()?.message ?: context.getString(R.string.settings_groq_test_failed))
                            }
                        }
                    }
                )
            }

            if (normalizedSearch.isEmpty() || "STOCKAGE" in matchingGroups) SettingsGroupCard(
                title = stringResource(R.string.settings_group_storage),
                value = stringResource(R.string.settings_storage_desc),
                icon = Icons.Default.Storage,
                expanded = "STOCKAGE" in expandedGroups,
                onClick = { expandedGroups = toggleSettingsGroup(expandedGroups, "STOCKAGE") }
            ) {
                SettingsActionRow(
                    icon = Icons.Default.Save,
                    title = stringResource(R.string.settings_export_title),
                    value = stringResource(R.string.settings_export_desc),
                    tag = "settings_export_row",
                    onClick = {
                        backupFeedback = null
                        exportLauncher.launch("musicpro-reglages.json")
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                SettingsActionRow(
                    icon = Icons.Default.Refresh,
                    title = stringResource(R.string.settings_import_title),
                    value = stringResource(R.string.settings_import_desc),
                    tag = "settings_import_row",
                    onClick = {
                        backupFeedback = null
                        importLauncher.launch(arrayOf("*/*"))
                    }
                )
                backupFeedback?.let { (ok, message) ->
                    Text(
                        text = message,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        fontSize = 12.sp,
                        color = if (ok) MusicProSuccess else MusicProError
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
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
            }

            if (normalizedSearch.isEmpty() || "NOTIFICATIONS" in matchingGroups) SettingsGroupCard(
                title = stringResource(R.string.settings_group_notifications),
                value = stringResource(R.string.settings_notifications_desc),
                icon = Icons.Default.Warning,
                expanded = "NOTIFICATIONS" in expandedGroups,
                onClick = { expandedGroups = toggleSettingsGroup(expandedGroups, "NOTIFICATIONS") }
            ) {
                SettingsInfoRow(stringResource(R.string.settings_notifications_info))
            }

            if (normalizedSearch.isEmpty() || "À PROPOS" in matchingGroups) SettingsGroupCard(
                title = stringResource(R.string.settings_group_about),
                value = stringResource(R.string.settings_about_desc),
                icon = Icons.Default.Info,
                expanded = "À PROPOS" in expandedGroups,
                onClick = { expandedGroups = toggleSettingsGroup(expandedGroups, "À PROPOS") }
            ) {
                AppUpdateCard(
                    currentVersion = settingsViewModel.currentVersionName,
                    updateCheckState = updateCheckState,
                    downloadState = downloadState,
                    onCheckForUpdates = { settingsViewModel.checkForUpdates() },
                    onDownloadAndInstall = { downloadUrl ->
                        settingsViewModel.downloadAndInstallUpdate(downloadUrl)
                    },
                    onInstallExisting = { settingsViewModel.installExistingApk() }
                )
                Spacer(modifier = Modifier.height(10.dp))
                AboutCard(currentVersion = settingsViewModel.currentVersionName)
                Spacer(modifier = Modifier.height(10.dp))
                PermissionsCard(onNavigateToPermissions = onNavigateToPermissions)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Dialogue de confirmation pour vider le cache
    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.settings_clear_cache_title),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.settings_clear_cache_body),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    Text(stringResource(R.string.settings_clear), fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) {
                    Text(stringResource(R.string.settings_cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Feuille de confirmation de réinitialisation par section (mise à jour 040)
    resetSection?.let { section ->
        ModalBottomSheet(
            onDismissRequest = { resetSection = null },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_reset_sheet_title),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(R.string.settings_reset_intro),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
                section.itemRes.forEach { itemRes ->
                    Text(
                        text = "• " + stringResource(itemRes),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp, bottom = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { resetSection = null },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("reset_cancel_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.settings_cancel))
                    }
                    Button(
                        onClick = {
                            when (section) {
                                SettingsSectionReset.APPARENCE -> settingsViewModel.resetAppearanceSection()
                                SettingsSectionReset.LECTURE -> settingsViewModel.resetPlaybackSection()
                                SettingsSectionReset.PAROLES -> settingsViewModel.resetKaraokeSection()
                                SettingsSectionReset.INTELLIGENCE_ARTIFICIELLE -> {
                                    apiKeyStore.clearApiKey()
                                    apiKeyInput = ""
                                    isConfigured = false
                                    maskedKey = ""
                                }
                            }
                            resetSection = null
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("reset_confirm_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MusicProError)
                    ) {
                        Text(stringResource(R.string.settings_reset))
                    }
                }
            }
        }
    }

    // Aperçu et confirmation d'import des réglages (mise à jour 038)
    pendingImportBackup?.let { backup ->
        AlertDialog(
            onDismissRequest = { pendingImportBackup = null },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = stringResource(R.string.settings_import_summary_title),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ImportSummaryLine(stringResource(R.string.reset_item_theme), backup.appearance.name.lowercase())
                    ImportSummaryLine(stringResource(R.string.reset_item_style), backup.appStyle.name.lowercase() + " · " + backup.accentChoice.name.lowercase())
                    ImportSummaryLine(stringResource(R.string.reset_item_textsize), backup.textSize.label)
                    ImportSummaryLine(stringResource(R.string.reset_item_density), backup.listDensity.label)
                    ImportSummaryLine(stringResource(R.string.reset_item_glass), backup.glass.material.name.lowercase())
                    ImportSummaryLine(stringResource(R.string.reset_item_karaoke), backup.karaoke.activeColor)
                    ImportSummaryLine(
                        stringResource(R.string.settings_import_favorites),
                        backup.favorites.trackIds.size.toString() + " · " +
                            backup.favorites.artists.size.toString() + " " +
                            stringResource(R.string.settings_import_artists)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val toApply = backup
                        pendingImportBackup = null
                        scope.launch {
                            try {
                                settingsViewModel.applyImportedSettings(toApply)
                                backupFeedback = Pair(true, context.getString(R.string.settings_backup_imported))
                            } catch (e: Exception) {
                                backupFeedback = Pair(false, context.getString(R.string.settings_backup_import_failed))
                            }
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("import_confirm_button")
                ) {
                    Text(stringResource(R.string.settings_import_apply), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingImportBackup = null }) {
                    Text(stringResource(R.string.settings_cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    // Feuille de l'égaliseur (mise à jour 037)
    if (showEqualizerSheet) {
        val audioViewModel: AudioViewModel = viewModel()
        val eqEnabled by audioViewModel.eqEnabled.collectAsStateWithLifecycle()
        val eqPreset by audioViewModel.eqPreset.collectAsStateWithLifecycle()
        val eqLevels by audioViewModel.eqLevels.collectAsStateWithLifecycle()
        EqualizerSheet(
            enabled = eqEnabled,
            preset = eqPreset,
            levels = eqLevels,
            onEnabledChange = audioViewModel::setEqualizerEnabled,
            onPresetChange = audioViewModel::setEqualizerPreset,
            onBandChange = audioViewModel::setEqualizerBand,
            onReset = { audioViewModel.resetEqualizer() },
            onClose = { showEqualizerSheet = false }
        )
    }
    }
}




@Composable
private fun QuickAppearanceCard(
    appearance: AppearanceMode,
    style: AppStyle,
    reveal: ThemeRevealState,
    onAppearanceSelected: (AppearanceMode) -> Unit,
    onStyleSelected: (AppStyle) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).testTag("quick_appearance_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(stringResource(R.string.settings_quick_appearance), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                QuickAppearanceOption(
                    label = stringResource(R.string.settings_quick_light), selected = appearance == AppearanceMode.LIGHT,
                    background = Color(0xFFF4F5F8), foreground = Color(0xFF252936),
                    accent = Color(0xFF6478D3), modifier = Modifier.weight(1f),
                    tag = "quick_theme_light"
                ) { origin ->
                    // Mise à jour 078 — révélation circulaire depuis le bouton touché.
                    reveal.beginReveal(origin) { onAppearanceSelected(AppearanceMode.LIGHT) }
                }
                QuickAppearanceOption(
                    label = stringResource(R.string.settings_quick_dark), selected = appearance == AppearanceMode.DARK,
                    background = Color(0xFF171923), foreground = Color(0xFFF1F2F8),
                    accent = Color(0xFF9B8CFF), modifier = Modifier.weight(1f),
                    tag = "quick_theme_dark"
                ) { origin ->
                    reveal.beginReveal(origin) { onAppearanceSelected(AppearanceMode.DARK) }
                }
                QuickAppearanceOption(
                    label = stringResource(R.string.settings_quick_auto), selected = appearance == AppearanceMode.AUTO,
                    background = MaterialTheme.colorScheme.surfaceVariant,
                    foreground = MaterialTheme.colorScheme.onSurfaceVariant,
                    accent = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f), tag = "quick_theme_auto",
                    splitPreview = true
                ) { origin ->
                    reveal.beginReveal(origin) { onAppearanceSelected(AppearanceMode.AUTO) }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                QuickAppearanceOption(
                    label = stringResource(R.string.settings_quick_minimal), selected = style == AppStyle.MINIMAL,
                    background = Color(0xFFF0F1F4), foreground = Color(0xFF333642),
                    accent = Color(0xFF7C8498), modifier = Modifier.weight(1f),
                    tag = "quick_style_minimal"
                ) { onStyleSelected(AppStyle.MINIMAL) }
                QuickAppearanceOption(
                    label = stringResource(R.string.settings_quick_neon), selected = style == AppStyle.NEON,
                    background = Color(0xFF171329), foreground = Color(0xFFF5F0FF),
                    accent = Color(0xFFB45CFF), modifier = Modifier.weight(1f),
                    tag = "quick_style_neon"
                ) { onStyleSelected(AppStyle.NEON) }
                QuickAppearanceOption(
                    label = stringResource(R.string.settings_quick_glass), selected = style == AppStyle.GLASS,
                    background = Color(0xFF24364B), foreground = Color(0xFFF3F8FF),
                    accent = Color(0xFF8BD8F5), modifier = Modifier.weight(1f),
                    tag = "quick_style_glass"
                ) { onStyleSelected(AppStyle.GLASS) }
            }
        }
    }
}

@Composable
private fun QuickAppearanceOption(
    label: String,
    selected: Boolean,
    background: Color,
    foreground: Color,
    accent: Color,
    modifier: Modifier = Modifier,
    tag: String,
    splitPreview: Boolean = false,
    onClick: (Offset) -> Unit
) {
    var optionCenter by remember { mutableStateOf(Offset.Zero) }
    Column(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                optionCenter = coordinates.positionInRoot() +
                    Offset(coordinates.size.width / 2f, coordinates.size.height / 2f)
            }
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.09f) else Color.Transparent)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick(optionCenter) }
            .padding(7.dp)
            .testTag(tag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(42.dp).clip(RoundedCornerShape(9.dp))
                .background(background).border(1.dp, foreground.copy(alpha = 0.12f), RoundedCornerShape(9.dp))
        ) {
            if (splitPreview) {
                Row(Modifier.fillMaxSize()) {
                    Box(Modifier.weight(1f).fillMaxSize().background(Color(0xFFF4F5F8)))
                    Box(Modifier.weight(1f).fillMaxSize().background(Color(0xFF171923)))
                }
            }
            Column(
                modifier = Modifier.align(Alignment.Center).padding(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(Modifier.fillMaxWidth(0.72f).height(4.dp).clip(RoundedCornerShape(4.dp)).background(if (splitPreview) Color(0xFF858B9A) else foreground.copy(alpha = 0.7f)))
                Box(Modifier.fillMaxWidth(0.48f).height(4.dp).clip(RoundedCornerShape(4.dp)).background(accent))
            }
            if (selected) {
                Icon(Icons.Default.Check, contentDescription = stringResource(R.string.settings_selected),
                    tint = if (splitPreview) MaterialTheme.colorScheme.primary else accent,
                    modifier = Modifier.align(Alignment.TopEnd).padding(3.dp).size(13.dp))
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            softWrap = false
        )
    }
}

private data class SettingsSearchEntry(
    val labelRes: Int,
    val group: String,
    val keywords: List<String>
)

private val settingsSearchIndex = listOf(
    SettingsSearchEntry(R.string.language_title, "APPARENCE", listOf("language", "français", "english", "العربية", "hausa", "zarma", "rtl")),
    SettingsSearchEntry(R.string.appearance_title, "APPARENCE", listOf("theme", "thème", "clair", "sombre", "apparence", "mode")),
    SettingsSearchEntry(R.string.style_title, "APPARENCE", listOf("style", "glass", "liquid glass", "verre")),
    SettingsSearchEntry(R.string.accent_title, "APPARENCE", listOf("couleur", "color", "accent", "teinte")),
    SettingsSearchEntry(R.string.dynamic_color_title, "APPARENCE", listOf("couleur", "color", "dynamique", "palette")),
    SettingsSearchEntry(R.string.appearance_text_size, "APPARENCE", listOf("texte", "taille", "police", "font", "petit", "grand")),
    SettingsSearchEntry(R.string.appearance_list_density, "APPARENCE", listOf("densité", "density", "compact", "compacte", "confortable", "hauteur", "ligne")),
    SettingsSearchEntry(R.string.appearance_reduce_animations, "APPARENCE", listOf("animations", "animation", "mouvement", "motion", "parallaxe", "vinyle", "flou")),
    SettingsSearchEntry(R.string.glass_quality_title, "APPARENCE", listOf("flou", "blur", "frost", "transparence", "verre", "glass")),
    SettingsSearchEntry(R.string.settings_autoplay, "LECTURE", listOf("lecture", "lecteur", "auto play", "autoplay")),
    SettingsSearchEntry(R.string.settings_gapless, "LECTURE", listOf("gapless", "enchaînement", "transition")),
    SettingsSearchEntry(R.string.settings_resume, "LECTURE", listOf("reprise", "continuer", "resume")),
    SettingsSearchEntry(R.string.settings_presentation, "LECTURE", listOf("présentation", "pochette", "vinyle", "vinyl", "minimal", "apparience lecteur", "player")),
    SettingsSearchEntry(R.string.settings_lyrics_assistance, "PAROLES", listOf("lyrics", "synchronisation", "texte")),
    SettingsSearchEntry(R.string.settings_groq_setup, "INTELLIGENCE ARTIFICIELLE", listOf("ia", "intelligence artificielle", "api", "groq", "clé")),
    SettingsSearchEntry(R.string.settings_storage_local, "STOCKAGE", listOf("cache", "stockage", "mémoire", "temporaire")),
    SettingsSearchEntry(R.string.settings_clean_deleted, "STOCKAGE", listOf("bibliothèque", "nettoyer", "analyse", "rescan")),
    SettingsSearchEntry(R.string.settings_group_notifications, "NOTIFICATIONS", listOf("alertes", "notifications")),
    SettingsSearchEntry(R.string.settings_group_about, "À PROPOS", listOf("version", "update", "mise à jour", "apk")),
    SettingsSearchEntry(R.string.settings_system_permissions, "À PROPOS", listOf("autorisation", "accès", "permission", "système"))
)

@Composable
private fun SettingsSearchResultRow(
    label: String,
    group: String,
    query: String,
    onClick: () -> Unit
) {
    val start = label.lowercase().indexOf(query)
    val annotated = androidx.compose.ui.text.buildAnnotatedString {
        if (start >= 0) {
            append(label.substring(0, start))
            pushStyle(androidx.compose.ui.text.SpanStyle(
                background = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            ))
            append(label.substring(start, start + query.length))
            pop()
            append(label.substring(start + query.length))
        } else append(label)
    }
    Surface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(annotated, modifier = Modifier.weight(1f), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
            val groupLabelRes = when (group) { "APPARENCE" -> R.string.settings_group_appearance; "LECTURE" -> R.string.settings_group_playback; "PAROLES" -> R.string.settings_group_lyrics; "INTELLIGENCE ARTIFICIELLE" -> R.string.settings_group_ai; "STOCKAGE" -> R.string.settings_group_storage; "NOTIFICATIONS" -> R.string.settings_group_notifications; else -> R.string.settings_group_about }
            Text(stringResource(groupLabelRes), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun toggleSettingsGroup(current: Set<String>, group: String): Set<String> =
    if (group in current) current - group else current + group

@Composable
private fun SettingsGroupCard(
    title: String,
    value: String,
    icon: ImageVector,
    expanded: Boolean,
    onClick: () -> Unit,
    resetItems: List<Int> = emptyList(),
    onResetRequest: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(onClick = onClick)
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(19.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.1.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = value,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = if (expanded) stringResource(R.string.settings_collapse) else stringResource(R.string.settings_expand),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(20.dp)
                        .then(if (expanded) Modifier else Modifier)
                )
                // Mise à jour 040 : menu « Réinitialiser cette section »
                if (resetItems.isNotEmpty() && onResetRequest != null) {
                    var resetMenuOpen by remember { mutableStateOf(false) }
                    Box {
                        IconButton(
                            onClick = { resetMenuOpen = true },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("reset_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.settings_reset_section),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        DropdownMenu(expanded = resetMenuOpen, onDismissRequest = { resetMenuOpen = false }) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.settings_reset_section)) },
                                onClick = {
                                    resetMenuOpen = false
                                    onResetRequest?.invoke()
                                }
                            )
                        }
                    }
                }
            }
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    content()
                }
            }
        }
    }
}

@Composable
private fun SettingsInfoRow(message: String) {
    Text(
        text = message,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/** Mise à jour 040 : sections proposant « Réinitialiser cette section » et ce qui sera remis. */
private enum class SettingsSectionReset(val tag: String, val itemRes: List<Int>) {
    APPARENCE(
        "APPARENCE",
        listOf(
            R.string.reset_item_theme,
            R.string.reset_item_style,
            R.string.reset_item_evening,
            R.string.reset_item_dyn,
            R.string.reset_item_textsize,
            R.string.reset_item_density,
            R.string.reset_item_anim,
            R.string.reset_item_glass
        )
    ),
    LECTURE("LECTURE", listOf(R.string.reset_item_playback, R.string.reset_item_presentation)),
    PAROLES("PAROLES", listOf(R.string.reset_item_karaoke)),
    INTELLIGENCE_ARTIFICIELLE("INTELLIGENCE ARTIFICIELLE", listOf(R.string.reset_item_groq))
}

/** Ligne de réglage standard : pastille icône 36 dp + libellé + valeur + chevron. */
@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    value: String,
    tag: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag(tag),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(19.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    value,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Libellé français du préréglage d'égaliseur (clés internes du moteur audio). */
private fun eqPresetLabelRes(key: String): Int = when (key) {
    "Bass Boost" -> R.string.eq_preset_bass
    "Vocal" -> R.string.eq_preset_vocal
    "Classical" -> R.string.eq_preset_classical
    "Pop" -> R.string.eq_preset_pop
    "Rock" -> R.string.eq_preset_rock
    else -> R.string.eq_preset_normal
}

/** Ligne « libellé : valeur » du résumé d'import (mise à jour 038). */
@Composable
private fun ImportSummaryLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
