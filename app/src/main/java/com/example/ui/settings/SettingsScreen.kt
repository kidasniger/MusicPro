package com.example.ui.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.security.GroqApiKeyStore
import com.example.groq.GroqTranscriptionManager
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
import com.example.ui.theme.MusicProWarning
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val apiKeyStore = remember { GroqApiKeyStore.getInstance(context) }

    var apiKeyInput by remember { mutableStateOf(apiKeyStore.getApiKey()) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfigured by remember { mutableStateOf(apiKeyStore.hasApiKey()) }
    var maskedKey by remember { mutableStateOf(apiKeyStore.getMaskedApiKey()) }

    var isTestingKey by remember { mutableStateOf(false) }
    var statusFeedbackMessage by remember { mutableStateOf<Pair<Boolean, String>?>(null) } // Pair(isSuccess, message)

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(MusicProBackground)
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = MusicProBackground,
        topBar = {
            SettingsTopBar(onBack = onBack)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Section 1: Configuration de l'API Groq (Whisper large-v3)
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
                        statusFeedbackMessage = Pair(true, "Clé enregistrée avec succès via EncryptedSharedPreferences (AES-256) !")
                    } else {
                        statusFeedbackMessage = Pair(false, "Veuillez saisir une clé API valide.")
                    }
                },
                onDeleteKey = {
                    apiKeyStore.clearApiKey()
                    apiKeyInput = ""
                    isConfigured = false
                    maskedKey = ""
                    statusFeedbackMessage = Pair(true, "Clé supprimée avec succès.")
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

            Spacer(modifier = Modifier.height(20.dp))

            // Section 2: Spécifications du modèle & Limites de l'API
            ModelSpecsCard()

            Spacer(modifier = Modifier.height(20.dp))

            // Section 3: Sécurité & Chiffrement
            SecurityCard()

            Spacer(modifier = Modifier.height(20.dp))

            // Section 4: Permissions & Système
            PermissionsCard(onNavigateToPermissions = onNavigateToPermissions)

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun SettingsTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
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

        Column {
            Text(
                text = "Paramètres",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MusicProTextPrimary
            )
            Text(
                text = "Intelligence Artificielle & Configuration",
                fontSize = 12.sp,
                color = MusicProCyanNeon
            )
        }
    }
}

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
            .shadow(12.dp, RoundedCornerShape(20.dp), spotColor = MusicProVioletGlow)
            .border(
                1.dp,
                if (isConfigured) MusicProCyanNeon.copy(alpha = 0.5f) else MusicProVioletPrimary.copy(alpha = 0.3f),
                RoundedCornerShape(20.dp)
            )
            .testTag("groq_settings_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MusicProCardBackground)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // En-tête de section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
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
                            text = "Clé API Groq (Whisper large-v3)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MusicProTextPrimary
                        )
                        Text(
                            text = "Transcription & Synchronisation des paroles par IA",
                            fontSize = 11.sp,
                            color = MusicProTextMuted
                        )
                    }
                }

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
                            fontSize = 11.sp,
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
                            text = "Clé chiffrée : $maskedKey",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = MusicProTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(onClick = onToggleVisibility) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (isPasswordVisible) "Masquer la clé" else "Afficher la clé",
                                tint = MusicProVioletLight,
                                modifier = Modifier.size(20.dp)
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

            // Message de retour (Succès ou Erreur)
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
                                fontSize = 12.sp,
                                color = if (isSuccess) MusicProSuccess else MusicProError
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Ligne de boutons d'action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bouton Enregistrer
                Button(
                    onClick = onSaveKey,
                    modifier = Modifier
                        .weight(1.3f)
                        .height(46.dp)
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
                        fontSize = 12.sp
                    )
                }

                // Bouton Tester
                OutlinedButton(
                    onClick = onTestKey,
                    enabled = !isTestingKey,
                    modifier = Modifier
                        .weight(1.2f)
                        .height(46.dp)
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
                            fontSize = 12.sp
                        )
                    }
                }

                // Bouton Supprimer
                if (isConfigured) {
                    IconButton(
                        onClick = onDeleteKey,
                        modifier = Modifier
                            .size(46.dp)
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

            Spacer(modifier = Modifier.height(14.dp))

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
                        text = "Obtenez votre clé gratuite en quelques secondes sur console.groq.com (section API Keys). Le modèle Whisper large-v3 offre une transcription ultra-rapide avec horodatages précis.",
                        fontSize = 11.sp,
                        color = MusicProTextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ModelSpecsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MusicProCardBackground),
        border = BorderStroke(1.dp, MusicProSurfaceElevated)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MusicProVioletLight,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Spécifications Whisper large-v3",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MusicProTextPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            SpecItem(title = "Modèle utilisé", value = "whisper-large-v3 (via Groq LPU)")
            SpecItem(title = "Limite de fichier direct", value = "25 Mo (Découpage automatique actif si > 22 Mo)")
            SpecItem(title = "Granularité des timestamps", value = "Segment par segment (alignement LRC)")
            SpecItem(title = "Formats supportés", value = "MP3, FLAC, M4A, WAV, OGG, MPEG")
            SpecItem(title = "Méthode d'enregistrement", value = "Tag ID3 SYLT si MP3, sinon fichier .lrc compagnon")
        }
    }
}

@Composable
private fun SpecItem(title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontSize = 12.sp, color = MusicProTextMuted)
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MusicProCyanLight,
            modifier = Modifier.weight(1f, fill = false),
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun SecurityCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MusicProCardBackground),
        border = BorderStroke(1.dp, MusicProSurfaceElevated)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = MusicProSuccess,
                modifier = Modifier
                    .size(20.dp)
                    .padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Chiffrement matériel sécurisé",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MusicProTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Votre clé API Groq est chiffrée au repos à l'aide de MasterKey AES-256 GCM via EncryptedSharedPreferences (Android Keystore). Elle ne quitte jamais votre appareil autrement que pour interroger l'API officielle de Groq.",
                    fontSize = 11.sp,
                    color = MusicProTextSecondary,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun PermissionsCard(onNavigateToPermissions: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MusicProCardBackground),
        border = BorderStroke(1.dp, MusicProSurfaceElevated)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
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
