@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.example.ui.chat

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.data.local.AudioTrackEntity
import com.example.data.local.ChatEntity
import com.example.data.local.ChatMessageEntity
import com.example.ui.components.OfflineBanner
import com.example.ui.components.isOnline
import com.example.ui.components.rememberIsOnline
import com.example.ui.navigation.ScrollToTopEffect
import com.example.ui.theme.MusicProTerracotta
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

/** Largeur de lecture maximale du chat sur tablette (mise à jour 091). */
private val ChatReadingWidth = 640.dp

/** Couleurs d'attachement disponible dans la pilule (mise à jour 092). */
private enum class AttachKind { TRACK, PLAYLIST }

/**
 * Écran de conversation avec l'assistant IA, refondu par le lot 091–100 :
 * bulles utilisateur à droite et réponses pleine largeur à gauche (091),
 * pilule de saisie extensible (092), suggestions de démarrage (093),
 * streaming avec curseur clignotant (094), Markdown léger (095),
 * cartes de morceaux (096), historique dans un tiroir (097),
 * actions sur message et pouces (098), modèle et ton par conversation (099),
 * garde-fous et erreurs gracieuses (100).
 */
@Composable
fun ChatScreen(
    onPlayTrack: (AudioTrackEntity) -> Unit = {}
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val viewModel: ChatViewModel = viewModel()
    val messages by viewModel.messages.collectAsState()
    val currentConversation by viewModel.currentConversation.collectAsState()
    val streamingText = viewModel.streamingText
    val streaming = viewModel.streaming
    val lastError = viewModel.lastError
    val libraryTracks by viewModel.libraryTracks.collectAsState()
    val suggestionOrder = viewModel.suggestionOrder

    val drawerState = rememberDrawerState(androidx.compose.material3.DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val connectivity = rememberIsOnline()

    var input by rememberSaveable { mutableStateOf("") }
    var attachPicker by remember { mutableStateOf<AttachKind?>(null) }
    var actionsMessage by remember { mutableStateOf<ChatMessageEntity?>(null) }
    var renameTarget by remember { mutableStateOf<ChatEntity?>(null) }
    var linkPreview by remember { mutableStateOf<String?>(null) }
    var tuneMenuOpen by remember { mutableStateOf(false) }
    val inputFocus = remember { FocusRequester() }

    val listState = rememberLazyListState()
    // 094 — ne suit le bas que si l'utilisateur n'a pas remonté la liste manuellement.
    val isAtBottom by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()
            last == null || last.index >= info.totalItemsCount - 2
        }
    }
    LaunchedEffect(messages.size, streamingText.length) {
        if ((messages.isNotEmpty() || streamingText.isNotEmpty()) && isAtBottom) {
            listState.scrollToItem(listState.layoutInfo.totalItemsCount - 1)
        }
    }

    fun sendCurrent(suggestion: ChatSuggestion? = null) {
        if (input.isNotBlank() && !streaming) {
            viewModel.send(input, suggestion)
            input = ""
            scope.launch {
                if (listState.layoutInfo.totalItemsCount > 0) {
                    listState.scrollToItem(listState.layoutInfo.totalItemsCount - 1)
                }
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ChatHistoryDrawer(
                conversations = viewModel.filteredConversations.collectAsState().value,
                activeId = currentConversation?.id,
                searchQuery = viewModel.historySearch,
                onSearch = viewModel::setSearchQuery,
                onSelect = { id ->
                    viewModel.selectConversation(id)
                    scope.launch { drawerState.close() }
                },
                onNew = {
                    viewModel.startNewConversation()
                    scope.launch { drawerState.close() }
                },
                onRename = { renameTarget = it },
                onDelete = { viewModel.deleteConversation(it) }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .imePadding()
        ) {
            ChatHeader(
                modelLabel = ChatModel.fromId(
                    currentConversation?.model ?: viewModel.pendingModel.id
                ).groqModel,
                onOpenHistory = { scope.launch { drawerState.open() } },
                onNewConversation = { viewModel.startNewConversation() },
                tuneMenuOpen = tuneMenuOpen,
                onTuneMenu = { tuneMenuOpen = it },
                selectedModel = ChatModel.fromId(currentConversation?.model ?: viewModel.pendingModel.id),
                selectedTone = ChatTone.fromId(currentConversation?.tone ?: viewModel.pendingTone.id),
                onModel = viewModel::setModel,
                onTone = viewModel::setTone
            )

            // 088/100 — bandeau « Hors connexion » : le chat est la fonction qui exige Internet.
            OfflineBanner(isOnline = connectivity.value, onRetry = { connectivity.value = context.isOnline() })

            val emptyChat = messages.isEmpty() && !streaming && streamingText.isEmpty()
            if (emptyChat) {
                ChatSuggestionsContent(
                    order = suggestionOrder,
                    onPick = { text ->
                        input = text
                        inputFocus.requestFocus()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterHorizontally)
                )
            } else {
                com.example.ui.navigation.ScrollToTopEffect(
                    com.example.ui.home.NavigationSection.CHAT.name,
                    listState
                )
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .widthIn(max = ChatReadingWidth)
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        top = 14.dp,
                        bottom = 12.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    itemsIndexed(messages, key = { _, message -> message.id }) { _, message ->
                        if (message.role == ChatViewModel.ROLE_USER) {
                            ChatUserBubble(content = message.content)
                        } else {
                            ChatAssistantMessage(
                                message = message,
                                libraryTracks = libraryTracks,
                                onPlayTrack = onPlayTrack,
                                onLinkClick = { linkPreview = it },
                                onLongPress = { actionsMessage = message },
                                onFeedback = { up -> viewModel.setFeedback(message, up) }
                            )
                        }
                    }
                    if (streaming) {
                        item(key = "streaming") {
                            ChatStreamingBlock(streamingText = streamingText)
                        }
                    }
                    if (lastError != null) {
                        item(key = "error") {
                            ChatErrorRow(
                                kind = lastError,
                                onRetry = { viewModel.regenerate() }
                            )
                        }
                    }
                }
            }

            if (viewModel.consentRequested) {
                Text(
                    text = stringResource(R.string.chat_consent_needed),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 4.dp)
                )
            }

            ChatInputBar(
                input = input,
                onInput = { input = it.take(CHAT_MAX_INPUT_CHARS) },
                streaming = streaming,
                focusRequester = inputFocus,
                onSend = { sendCurrent() },
                onStop = viewModel::stopStreaming,
                onAttachTrack = { attachPicker = AttachKind.TRACK },
                onAttachPlaylist = { attachPicker = AttachKind.PLAYLIST },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                text = stringResource(R.string.chat_privacy_hint),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 8.dp)
            )
        }
    }

    // ----- Feuilles et boîtes de dialogue -----

    if (attachPicker != null) {
        ModalBottomSheet(onDismissRequest = { attachPicker = null }) {
            if (attachPicker == AttachKind.TRACK) {
                ChatTrackPicker(
                    tracks = libraryTracks,
                    onPick = { track ->
                        viewModel.trackAttachment(track)?.let { attachment ->
                            input = (input + "\n" + attachment).trim()
                            inputFocus.requestFocus()
                        }
                        attachPicker = null
                    }
                )
            } else {
                ChatPlaylistPicker(
                    playlists = viewModel.playlists.collectAsState().value,
                    onPick = { playlist ->
                        viewModel.playlistAttachment(playlist) { attachment ->
                            attachment?.let {
                                input = (input + "\n" + it).trim()
                            }
                        }
                        attachPicker = null
                    }
                )
            }
        }
    }

    actionsMessage?.let { message ->
        val isLast = messages.lastOrNull()?.id == message.id
        ModalBottomSheet(onDismissRequest = { actionsMessage = null }) {
            Text(
                text = message.content.lineSequence().firstOrNull().orEmpty().take(80),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.chat_action_copy)) },
                leadingContent = {
                    Icon(Icons.Default.ContentCopy, null)
                },
                modifier = Modifier.combinedClickable(onClick = {
                    clipboard.setText(AnnotatedString(message.content))
                    actionsMessage = null
                })
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.chat_action_regenerate)) },
                leadingContent = { Icon(Icons.Default.Refresh, null) },
                modifier = Modifier
                    .combinedClickable(enabled = isLast, onClick = {
                        actionsMessage = null
                        viewModel.regenerate()
                    })
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.chat_action_read_aloud)) },
                leadingContent = { Icon(Icons.AutoMirrored.Filled.VolumeUp, null) },
                modifier = Modifier.combinedClickable(onClick = {
                    actionsMessage = null
                    ChatTts.speak(context, message.content)
                })
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.chat_action_share)) },
                leadingContent = { Icon(Icons.Default.Share, null) },
                modifier = Modifier.combinedClickable(onClick = {
                    actionsMessage = null
                    val send = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, message.content)
                    }
                    context.startActivity(Intent.createChooser(send, null))
                })
            )
            Spacer(Modifier.height(12.dp))
        }
    }

    renameTarget?.let { conversation ->
        var renamed by remember(conversation.id) { mutableStateOf(conversation.title) }
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text(stringResource(R.string.chat_rename)) },
            text = {
                OutlinedTextField(
                    value = renamed,
                    onValueChange = { renamed = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.renameConversation(conversation.id, renamed)
                        renameTarget = null
                    },
                    enabled = renamed.isNotBlank()
                ) { Text(stringResource(R.string.chat_rename_save)) }
            },
            dismissButton = {
                TextButton(onClick = { renameTarget = null }) {
                    Text(stringResource(R.string.chat_cancel))
                }
            }
        )
    }

    linkPreview?.let { url ->
        // 095 — aperçu dans l'app avant de quitter : on voit l'URL, on décide.
        ModalBottomSheet(onDismissRequest = { linkPreview = null }) {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text(
                    text = stringResource(R.string.chat_link_preview),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = url,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilledTonalButton(onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        linkPreview = null
                    }) { Text(stringResource(R.string.chat_link_open)) }
                    OutlinedButton(onClick = {
                        clipboard.setText(AnnotatedString(url))
                        linkPreview = null
                    }) { Text(stringResource(R.string.chat_link_copy)) }
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

/**
 * En-tête du chat (099) : titre, nom du modèle en petit, tiroir d'historique,
 * nouvelle conversation, sélecteur compact modèle + ton.
 */
@Composable
private fun ChatHeader(
    modelLabel: String,
    onOpenHistory: () -> Unit,
    onNewConversation: () -> Unit,
    tuneMenuOpen: Boolean,
    onTuneMenu: (Boolean) -> Unit,
    selectedModel: ChatModel,
    selectedTone: ChatTone,
    onModel: (ChatModel) -> Unit,
    onTone: (ChatTone) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 18.dp, end = 6.dp, top = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.chat_assistant_name),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = modelLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onOpenHistory) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = stringResource(R.string.chat_history_open)
            )
        }
        IconButton(onClick = onNewConversation) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.chat_new_conversation)
            )
        }
        Box {
            IconButton(onClick = { onTuneMenu(true) }) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = stringResource(R.string.chat_model_and_tone)
                )
            }
            DropdownMenu(expanded = tuneMenuOpen, onDismissRequest = { onTuneMenu(false) }) {
                Text(
                    text = stringResource(R.string.chat_model_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                ChatModel.entries.forEach { model ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = model == selectedModel,
                                    onClick = {
                                        onModel(model)
                                        onTuneMenu(false)
                                    }
                                )
                                Text(
                                    text = stringResource(
                                        if (model == ChatModel.FAST) {
                                            R.string.chat_model_fast
                                        } else {
                                            R.string.chat_model_precise
                                        }
                                    )
                                )
                            }
                        },
                        onClick = {
                            onModel(model)
                            onTuneMenu(false)
                        }
                    )
                }
                HorizontalDivider()
                Text(
                    text = stringResource(R.string.chat_tone_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                ChatTone.entries.forEach { tone ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = tone == selectedTone,
                                    onClick = {
                                        onTone(tone)
                                        onTuneMenu(false)
                                    }
                                )
                                Text(
                                    text = stringResource(
                                        when (tone) {
                                            ChatTone.NEUTRAL -> R.string.chat_tone_neutral
                                            ChatTone.WARM -> R.string.chat_tone_warm
                                            ChatTone.CONCISE -> R.string.chat_tone_concise
                                        }
                                    )
                                )
                            }
                        },
                        onClick = {
                            onTone(tone)
                            onTuneMenu(false)
                        }
                    )
                }
            }
        }
    }
}

/**
 * 091 — bulle utilisateur à droite : « surface » arrondie 20 dp,
 * coin bas-droit à 6 dp.
 */
@Composable
private fun ChatUserBubble(content: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = 20.dp,
                bottomEnd = 6.dp
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.widthIn(max = ChatReadingWidth * 0.8f)
        ) {
            Text(
                text = content,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
    }
}

/**
 * 091/095/096/098 — réponse de l'assistant : pas de bulle, texte pleine largeur
 * sur le fond (interligne 1,5), Markdown léger, cartes de morceaux cités,
 * appui long pour les actions, pouces haut/bas sous la réponse terminée.
 * TalkBack : contenu fusionné + actions personnalisées (098).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChatAssistantMessage(
    message: ChatMessageEntity,
    libraryTracks: List<AudioTrackEntity>,
    onPlayTrack: (AudioTrackEntity) -> Unit,
    onLinkClick: (String) -> Unit,
    onLongPress: () -> Unit,
    onFeedback: (Boolean) -> Unit
) {
    val copyLabel = stringResource(R.string.chat_action_copy)
    val readLabel = stringResource(R.string.chat_action_read_aloud)
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val mentionedTracks = remember(message.content, libraryTracks) {
        findMentionedTracks(message.content, libraryTracks)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = {}, onLongClick = onLongPress)
            .semantics(mergeDescendants = true) {
                customActions = listOf(
                    CustomAccessibilityAction(copyLabel) {
                        clipboard.setText(AnnotatedString(message.content))
                        true
                    },
                    CustomAccessibilityAction(readLabel) {
                        ChatTts.speak(context, message.content)
                        true
                    }
                )
            }
    ) {
        Text(
            text = stringResource(R.string.chat_assistant_name),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        MarkdownContent(
            markdown = message.content,
            onLinkClick = onLinkClick,
            modifier = Modifier.padding(top = 6.dp)
        )
        if (mentionedTracks.isNotEmpty()) {
            ChatTrackCards(
                tracks = mentionedTracks,
                onPlayTrack = onPlayTrack,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
        // 098 — pouces discrets sous chaque réponse terminée.
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 2.dp)
        ) {
            IconButton(onClick = { onFeedback(true) }, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Default.ThumbUp,
                    contentDescription = stringResource(R.string.chat_feedback_up),
                    tint = if (message.feedback == 1) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(16.dp)
                )
            }
            IconButton(onClick = { onFeedback(false) }, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Default.ThumbDown,
                    contentDescription = stringResource(R.string.chat_feedback_down),
                    tint = if (message.feedback == -1) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/** 094 — bloc de génération en cours : tokens au fil de l'eau + curseur clignotant. */
@Composable
private fun ChatStreamingBlock(streamingText: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.chat_assistant_name),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        if (streamingText.isEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp
                )
                Text(
                    text = stringResource(R.string.chat_thinking),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 10.dp)
                )
            }
        } else {
            MarkdownContent(markdown = streamingText, modifier = Modifier.padding(top = 6.dp))
            BlinkingCursor(modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun BlinkingCursor(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "chat_cursor")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 550, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "chat_cursor_alpha"
    )
    Box(
        modifier = modifier
            .size(width = 3.dp, height = 18.dp)
            .alpha(alpha)
            .background(MaterialTheme.colorScheme.primary)
    )
}

/** 100 — erreur en langage simple + « Réessayer » ; le texte cite Réglages si clé absente. */
@Composable
private fun ChatErrorRow(
    kind: ChatErrorKind?,
    onRetry: () -> Unit
) {
    val messageRes = when (kind) {
        ChatErrorKind.NO_KEY -> R.string.chat_error_no_key
        ChatErrorKind.RATE_LIMIT -> R.string.chat_error_rate_limit
        ChatErrorKind.NETWORK -> R.string.chat_error_offline
        ChatErrorKind.SERVER -> R.string.chat_error_server
        null, ChatErrorKind.GENERAL -> R.string.chat_error_friendly
    }
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = stringResource(messageRes),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            TextButton(onClick = onRetry) {
                Text(stringResource(R.string.chat_retry))
            }
        }
    }
}

/** 092 — pilule de saisie : 52 dp, jusqu'à 6 lignes, « + », Envoyer terracotta. */
@Composable
private fun ChatInputBar(
    input: String,
    onInput: (String) -> Unit,
    streaming: Boolean,
    focusRequester: FocusRequester,
    onSend: () -> Unit,
    onStop: () -> Unit,
    onAttachTrack: () -> Unit,
    onAttachPlaylist: () -> Unit,
    modifier: Modifier = Modifier
) {
    var attachMenuOpen by remember { mutableStateOf(false) }
    Surface(
        modifier = modifier
            .widthIn(max = ChatReadingWidth)
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 2.dp
    ) {
        Column(Modifier.padding(horizontal = 4.dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Box {
                    IconButton(onClick = { attachMenuOpen = true }, enabled = !streaming) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.chat_attach)
                        )
                    }
                    DropdownMenu(
                        expanded = attachMenuOpen,
                        onDismissRequest = { attachMenuOpen = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.chat_attach_track)) },
                            onClick = {
                                attachMenuOpen = false
                                onAttachTrack()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.chat_attach_playlist)) },
                            onClick = {
                                attachMenuOpen = false
                                onAttachPlaylist()
                            }
                        )
                    }
                }
                BasicTextField(
                    value = input,
                    onValueChange = onInput,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 52.dp)
                        .padding(vertical = 16.dp)
                        .focusRequester(focusRequester),
                    enabled = !streaming,
                    maxLines = 6,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { onSend() }),
                    decorationBox = { innerTextField ->
                        Box {
                            if (input.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.chat_input_hint),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            innerTextField()
                        }
                    }
                )
                FilledIconButton(
                    onClick = if (streaming) onStop else onSend,
                    enabled = streaming || input.isNotBlank(),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MusicProTerracotta,
                        contentColor = Color.White,
                        disabledContainerColor = MusicProTerracotta.copy(alpha = 0.35f),
                        disabledContentColor = Color.White
                    ),
                    modifier = Modifier
                        .padding(end = 4.dp, bottom = 4.dp)
                        .size(44.dp)
                ) {
                    Icon(
                        imageVector = if (streaming) Icons.Default.Stop else Icons.AutoMirrored.Filled.Send,
                        contentDescription = stringResource(
                            if (streaming) R.string.chat_stop else R.string.chat_send
                        )
                    )
                }
            }
            // 100 — compteur discret au-delà de 80 % de la limite.
            if (input.length >= CHAT_COUNTER_VISIBLE_FROM) {
                Text(
                    text = input.length.toString() + " / " + CHAT_MAX_INPUT_CHARS,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(end = 14.dp, bottom = 6.dp)
                )
            }
        }
    }
}

/** 093 — écran vierge : salutation + 4 cartes ; un tap remplit sans envoyer. */
@Composable
private fun ChatSuggestionsContent(
    order: List<ChatSuggestion>,
    onPick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val texts = mapOf(
        ChatSuggestion.PLAYLIST to stringResource(R.string.chat_suggestion_playlist),
        ChatSuggestion.LYRICS to stringResource(R.string.chat_suggestion_lyrics),
        ChatSuggestion.TONIGHT to stringResource(R.string.chat_suggestion_tonight),
        ChatSuggestion.EXPLAIN to stringResource(R.string.chat_suggestion_explain)
    )
    val icons = mapOf(
        ChatSuggestion.PLAYLIST to Icons.Default.DirectionsRun,
        ChatSuggestion.LYRICS to Icons.Default.Lyrics,
        ChatSuggestion.TONIGHT to Icons.Default.Nightlight,
        ChatSuggestion.EXPLAIN to Icons.Default.MenuBook
    )
    Column(
        modifier = modifier
            .widthIn(max = ChatReadingWidth)
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(30.dp)
        )
        Text(
            text = stringResource(R.string.chat_welcome_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 18.dp)
        )
        Text(
            text = stringResource(R.string.chat_welcome_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 22.dp)
        )
        val cards = order.ifEmpty { ChatSuggestion.entries.toList() }
        cards.chunked(2).forEach { rowCards ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowCards.forEach { suggestion ->
                    SuggestionCard(
                        icon = icons.getValue(suggestion),
                        label = texts.getValue(suggestion),
                        modifier = Modifier.weight(1f),
                        onClick = { onPick(texts.getValue(suggestion)) }
                    )
                }
                if (rowCards.size == 1) Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.size(10.dp))
        }
    }
}

@Composable
private fun SuggestionCard(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(onClick = onClick, modifier = modifier) {
        Column(Modifier.padding(14.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

/** 097 — tiroir latéral : recherche, renommage, suppression par balayage. */
@Composable
private fun ChatHistoryDrawer(
    conversations: List<ChatEntity>,
    activeId: Long?,
    searchQuery: String,
    onSearch: (String) -> Unit,
    onSelect: (Long) -> Unit,
    onNew: () -> Unit,
    onRename: (ChatEntity) -> Unit,
    onDelete: (Long) -> Unit
) {
    val dateFormatter = remember {
        java.text.DateFormat.getDateInstance(java.text.DateFormat.SHORT, Locale.getDefault())
    }
    Column(
        modifier = Modifier
            .width(310.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface)
            .padding(top = 18.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp)
        ) {
            Text(
                text = stringResource(R.string.chat_history),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onNew) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.chat_new_conversation)
                )
            }
        }
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearch,
            singleLine = true,
            placeholder = { Text(stringResource(R.string.chat_search_hint)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearch("") }) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp)
        )
        androidx.compose.foundation.lazy.LazyColumn(Modifier.weight(1f)) {
            items(conversations, key = { it.id }) { conversation ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { value ->
                        if (value == SwipeToDismissBoxValue.EndToStart) {
                            onDelete(conversation.id)
                            true
                        } else {
                            false
                        }
                    }
                )
                SwipeToDismissBox(
                    state = dismissState,
                    enableDismissFromStartToEnd = false,
                    backgroundContent = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    MaterialTheme.colorScheme.errorContainer,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(end = 18.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.chat_delete_conversation),
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxSize()
                            .combinedClickable(onClick = { onSelect(conversation.id) })
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = conversation.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (conversation.id == activeId) {
                                    FontWeight.SemiBold
                                } else {
                                    FontWeight.Normal
                                },
                                color = if (conversation.id == activeId) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = dateFormatter.format(Date(conversation.updatedAt)),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { onRename(conversation) }, modifier = Modifier.size(40.dp)) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.chat_rename),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
            item {
                if (conversations.isEmpty()) {
                    Text(
                        text = stringResource(R.string.chat_history_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

/** 092 — choix d'un morceau à joindre en contexte (100 : rien d'autre que 4 champs). */
@Composable
private fun ChatTrackPicker(
    tracks: List<AudioTrackEntity>,
    onPick: (AudioTrackEntity) -> Unit
) {
    var query by remember { mutableStateOf("") }
    Column(Modifier.padding(bottom = 20.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            singleLine = true,
            placeholder = { Text(stringResource(R.string.chat_search_hint)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        )
        androidx.compose.foundation.lazy.LazyColumn {
            val filtered = tracks
                .filter { it.title.contains(query, ignoreCase = true) }
                .take(100)
            items(filtered, key = { it.id }) { track ->
                ListItem(
                    headlineContent = {
                        Text(track.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    },
                    supportingContent = { Text(track.artist, maxLines = 1) },
                    trailingContent = {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.combinedClickable(onClick = { onPick(track) })
                )
            }
        }
    }
}

/** 092 — choix d'une playlist à joindre en contexte. */
@Composable
private fun ChatPlaylistPicker(
    playlists: List<com.example.data.local.PlaylistEntity>,
    onPick: (com.example.data.local.PlaylistEntity) -> Unit
) {
    Column(Modifier.padding(bottom = 20.dp)) {
        Text(
            text = stringResource(R.string.chat_attach_playlist),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )
        androidx.compose.foundation.lazy.LazyColumn {
            items(playlists, key = { it.id }) { playlist ->
                ListItem(
                    headlineContent = { Text(playlist.name) },
                    modifier = Modifier.combinedClickable(onClick = { onPick(playlist) })
                )
            }
            item {
                if (playlists.isEmpty()) {
                    Text(
                        text = stringResource(R.string.chat_history_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
