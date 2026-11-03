package com.example.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.security.GroqApiKeyStore
import com.example.groq.GroqChatMessage
import com.example.groq.GroqChatRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private data class ChatUiMessage(val role: String, val content: String)

@Composable
fun ChatScreen() {
    val context = LocalContext.current
    val keyStore = remember(context) { GroqApiKeyStore.getInstance(context) }
    val messages = remember { mutableStateListOf<ChatUiMessage>() }
    var input by rememberSaveable { mutableStateOf("") }
    var streaming by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var streamJob by remember { mutableStateOf<Job?>(null) }

    val suggestions = listOf(
        stringResource(R.string.chat_suggestion_playlist),
        stringResource(R.string.chat_suggestion_lyrics),
        stringResource(R.string.chat_suggestion_explain)
    )

    fun sendMessage(raw: String) {
        val prompt = raw.trim()
        if (prompt.isEmpty() || streaming) return
        input = ""
        messages.add(ChatUiMessage("user", prompt))
        val history = buildList {
            add(
                GroqChatMessage(
                    role = "system",
                    content = "Tu es l'assistant musical de MusicPro. Réponds en français, " +
                        "de façon claire et concise. Aide à découvrir la musique, composer des " +
                        "playlists et comprendre les morceaux. Ne prétends pas avoir recherché " +
                        "des paroles ou créé une playlist dans l'application si tu ne l'as pas fait. " +
                        "Pour des paroles complètes protégées, propose plutôt un résumé ou un extrait bref."
                )
            )
            messages.takeLast(20).forEach { add(GroqChatMessage(it.role, it.content)) }
        }
        val assistantIndex = messages.size
        messages.add(ChatUiMessage("assistant", ""))
        streaming = true
        streamJob = scope.launch {
            try {
                GroqChatRepository.stream(keyStore.getApiKey(), history).collect { fragment ->
                    val previous = messages[assistantIndex].content
                    messages[assistantIndex] = messages[assistantIndex].copy(content = previous + fragment)
                }
                if (messages[assistantIndex].content.isBlank()) {
                    messages[assistantIndex] = messages[assistantIndex].copy(
                        content = context.getString(R.string.chat_empty_response)
                    )
                }
            } catch (error: Exception) {
                messages[assistantIndex] = messages[assistantIndex].copy(
                    content = error.message ?: context.getString(R.string.chat_error)
                )
            } finally {
                streaming = false
                streamJob = null
            }
        }
    }

    LaunchedEffect(messages.size, messages.lastOrNull()?.content) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp)
    ) {
        if (messages.isEmpty()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
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
                    modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                )
                suggestions.forEach { suggestion ->
                    AssistChip(
                        onClick = { sendMessage(suggestion) },
                        label = { Text(suggestion) },
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }
        } else {
            com.example.ui.navigation.ScrollToTopEffect(
                com.example.ui.home.NavigationSection.CHAT.name,
                listState
            )
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = listState,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    top = 20.dp,
                    bottom = 18.dp
                ),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                itemsIndexed(messages) { _, message ->
                    if (message.role == "user") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                shape = RoundedCornerShape(22.dp)
                            ) {
                                Text(
                                    text = message.content,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                )
                            }
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = stringResource(R.string.chat_assistant_name),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = message.content.ifBlank {
                                    if (streaming) context.getString(R.string.chat_thinking) else ""
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                            if (streaming && messages.lastIndex == messages.indexOf(message) &&
                                message.content.isBlank()
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .padding(top = 10.dp)
                                        .size(18.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    }
                }
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 12.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(start = 18.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 10.dp),
                    enabled = !streaming,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { sendMessage(input) }),
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
                IconButton(
                    onClick = {
                        if (streaming) streamJob?.cancel() else sendMessage(input)
                    },
                    enabled = streaming || input.isNotBlank()
                ) {
                    Icon(
                        imageVector = if (streaming) Icons.Default.Stop else Icons.AutoMirrored.Filled.Send,
                        contentDescription = stringResource(
                            if (streaming) R.string.chat_stop else R.string.chat_send
                        ),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
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
