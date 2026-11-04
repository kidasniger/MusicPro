package com.example.ui.chat

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.local.AudioTrackEntity
import com.example.data.local.ChatDao
import com.example.data.local.ChatEntity
import com.example.data.local.ChatMessageEntity
import com.example.data.local.MusicProDatabase
import com.example.data.local.PlaylistEntity
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.security.GroqApiKeyStore
import com.example.groq.GroqChatMessage
import com.example.groq.GroqChatRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Orchestration du chat IA (lot 091–100) :
 *  - 094 : streaming des tokens en mémoire ([streamingText]), message assistant
 *    écrit dans Room à la fin ; bouton Stop gardant la partie déjà générée ;
 *  - 097 : historique Room complet (conversations + messages), titre en 5 mots ;
 *  - 098 : pouces haut/bas stockés localement et affinant l'ordre des suggestions ;
 *  - 099 : modèle et ton mémorisés PAR conversation ;
 *  - 100 : garde-fous — 4 000 caractères, erreurs 429 / clé absente / réseau,
 *    contexte musical limité à titre, artiste, album, genre et soumis au
 *    consentement (Réglages › Confidentialité).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val database = MusicProDatabase.getInstance(application)
    private val chatDao: ChatDao = database.chatDao()
    private val trackDao = database.audioTrackDao()
    private val playlistDao = database.playlistDao()
    private val keyStore = GroqApiKeyStore.getInstance(application)
    private val preferences = UserPreferencesRepository(application)

    /** Conversation ouverte ; null = nouvel écran vierge (aucune ligne en base). */
    private val currentConversationId = MutableStateFlow<Long?>(null)

    val currentConversation: StateFlow<ChatEntity?> = currentConversationId
        .flatMapLatest { id ->
            if (id == null) flowOf(null) else chatDao.observeConversation(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val messages: StateFlow<List<ChatMessageEntity>> = currentConversationId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else chatDao.observeMessages(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val conversations: StateFlow<List<ChatEntity>> = chatDao.observeConversations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** 097 — recherche de l'historique (titre, insensible aux filtres Room). */
    var historySearch by mutableStateOf("")
        private set
    val filteredConversations: StateFlow<List<ChatEntity>> =
        combine(chatDao.observeConversations(), snapshotFlow { historySearch }) { list, query ->
            val q = query.trim()
            if (q.isEmpty()) list else list.filter { it.title.contains(q, ignoreCase = true) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** 094 — état du streaming ; le texte courant vit en mémoire, pas en base. */
    var streaming by mutableStateOf(false)
        private set
    var streamingText by mutableStateOf("")
        private set

    /** 100 — dernière erreur en langage simple ; bouton « Réessayer » si non null. */
    var lastError by mutableStateOf<ChatErrorKind?>(null)
        private set

    /** 099 — choix courants avant la première conversation ; ensuite par conversation. */
    var pendingModel by mutableStateOf(ChatModel.FAST)
        private set
    var pendingTone by mutableStateOf(ChatTone.NEUTRAL)
        private set

    /** 100 — consentement « envoyer le contexte musical » (Réglages › Confidentialité). */
    val aiContextConsent: StateFlow<Boolean> = preferences.aiContextConsent
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    /** 100 — l'utilisateur a voulu joindre un morceau sans consentement : invite à régler. */
    var consentRequested by mutableStateOf(false)
        private set

    /** 096 — bibliothèque locale pour le mapping des morceaux cités. */
    val libraryTracks: StateFlow<List<AudioTrackEntity>> = flow {
        emit(trackDao.getAllTracksSnapshot())
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val playlists: StateFlow<List<PlaylistEntity>> = playlistDao.getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** 093 — ordre des cartes de suggestion, affiné par les pouces (098). */
    var suggestionOrder by mutableStateOf(ChatSuggestion.entries.toList())
        private set

    private var streamJob: Job? = null

    init {
        refreshSuggestionOrder()
    }

    // ----- Navigation dans l'historique (097) -----

    fun startNewConversation() {
        streamJob?.cancel()
        currentConversationId.value = null
        streamingText = ""
        streaming = false
        lastError = null
        consentRequested = false
    }

    fun selectConversation(id: Long) {
        if (id == currentConversationId.value) return
        streamJob?.cancel()
        streamingText = ""
        streaming = false
        lastError = null
        currentConversationId.value = id
    }

    fun setSearchQuery(query: String) {
        historySearch = query
    }

    fun renameConversation(id: Long, title: String) {
        val cleaned = title.trim().take(64)
        if (cleaned.isEmpty()) return
        viewModelScope.launch { chatDao.renameConversation(id, cleaned) }
    }

    fun deleteConversation(id: Long) {
        viewModelScope.launch {
            chatDao.deleteConversationById(id)
            if (currentConversationId.value == id) currentConversationId.value = null
        }
    }

    /** 097 — « Tout effacer » (Réglages › Confidentialité). */
    fun clearAllConversations() {
        viewModelScope.launch {
            chatDao.deleteAllMessages()
            chatDao.deleteAllConversations()
            currentConversationId.value = null
        }
    }

    // ----- Choix du modèle et du ton (099) -----

    fun setModel(model: ChatModel) {
        pendingModel = model
        currentConversationId.value?.let { id ->
            viewModelScope.launch { chatDao.updateModel(id, model.id) }
        }
    }

    fun setTone(tone: ChatTone) {
        pendingTone = tone
        currentConversationId.value?.let { id ->
            viewModelScope.launch { chatDao.updateTone(id, tone.id) }
        }
    }

    // ----- Envoi, streaming, régénération (094/100) -----

    /**
     * Envoie [prompt] : 4 000 caractères maximum (100), message utilisateur
     * écrit immédiatement dans Room, puis streaming des tokens (094).
     */
    fun send(prompt: String, suggestion: ChatSuggestion? = null) {
        val text = prompt.trim().take(CHAT_MAX_INPUT_CHARS)
        if (text.isEmpty() || streaming) return
        lastError = null
        consentRequested = false
        viewModelScope.launch {
            val conversationId = currentConversationId.value
                ?: createConversation(text, suggestion)
            chatDao.insertMessage(
                ChatMessageEntity(
                    conversationId = conversationId,
                    role = ROLE_USER,
                    content = text,
                    createdAt = System.currentTimeMillis()
                )
            )
            launchStream(conversationId)
        }
    }

    /** 094 — Stop : annule le flux en gardant la partie déjà générée. */
    fun stopStreaming() {
        streamJob?.cancel()
    }

    /**
     * 100 — « Réessayer » : renvoie la dernière requête utilisateur.
     * 098 — « Régénérer » : remplace la dernière réponse par une nouvelle.
     */
    fun regenerate() {
        if (streaming) return
        val id = currentConversationId.value ?: return
        lastError = null
        viewModelScope.launch {
            val msgs = chatDao.getMessagesOnce(id)
            val last = msgs.lastOrNull()
            if (last != null && last.role == ROLE_ASSISTANT) {
                chatDao.deleteMessageById(last.id)
            }
            if (msgs.any { it.role == ROLE_USER }) {
                launchStream(id)
            }
        }
    }

    // ----- Actions sur un message (098) -----

    /** Pouce haut/bas : bascule (re-cliquer retire l'avis). Stocké localement. */
    fun setFeedback(message: ChatMessageEntity, thumbsUp: Boolean) {
        val target = if (thumbsUp) 1 else -1
        val newFeedback = if (message.feedback == target) 0 else target
        viewModelScope.launch {
            chatDao.setMessageFeedback(message.id, newFeedback)
            refreshSuggestionOrder()
        }
    }

    // ----- Contexte musical joint via « + » (092/100) -----

    /**
     * Joint un morceau au champ : UNIQUEMENT titre, artiste, album et genre,
     * et seulement si le consentement est donné ; sinon demande de le régler.
     */
    fun trackAttachment(track: AudioTrackEntity): String? {
        if (!aiContextConsent.value) {
            consentRequested = true
            return null
        }
        consentRequested = false
        return buildTrackAttachment(track.title, track.artist, track.album, track.genre)
    }

    /** Joint une playlist (30 morceaux maximum, mêmes 4 champs). */
    fun playlistAttachment(playlist: PlaylistEntity, onReady: (String?) -> Unit) {
        if (!aiContextConsent.value) {
            consentRequested = true
            onReady(null)
            return
        }
        consentRequested = false
        viewModelScope.launch {
            val tracks = playlistDao.getTracksForPlaylistOnce(playlist.id)
            onReady(buildPlaylistAttachment(playlist, tracks))
        }
    }

    fun dismissConsentRequest() {
        consentRequested = false
    }

    // ----- Suggestions affinées (093/098) -----

    fun refreshSuggestionOrder() {
        viewModelScope.launch {
            val scores = chatDao.suggestionScores()
                .associate { it.suggestionKey to it.score }
            suggestionOrder = orderSuggestions(scores)
        }
    }

    // ----- Internes -----

    private suspend fun createConversation(firstMessage: String, suggestion: ChatSuggestion?): Long {
        val now = System.currentTimeMillis()
        val id = chatDao.insertConversation(
            ChatEntity(
                title = chatTitleFrom(firstMessage),
                model = pendingModel.id,
                tone = pendingTone.id,
                suggestionKey = suggestion?.id.orEmpty(),
                createdAt = now,
                updatedAt = now
            )
        )
        currentConversationId.value = id
        return id
    }

    private fun launchStream(conversationId: Long) {
        streaming = true
        streamingText = ""
        streamJob = viewModelScope.launch {
            try {
                val history = buildHistory(conversationId)
                val model = ChatModel.fromId(chatDao.getConversationOnce(conversationId)?.model)
                GroqChatRepository.stream(keyStore.getApiKey(), history, model.groqModel)
                    .collect { fragment -> streamingText += fragment }
                val final = streamingText.ifBlank {
                    getApplication<Application>().getString(R.string.chat_empty_response)
                }
                chatDao.insertMessage(
                    ChatMessageEntity(
                        conversationId = conversationId,
                        role = ROLE_ASSISTANT,
                        content = final,
                        createdAt = System.currentTimeMillis()
                    )
                )
                chatDao.touchConversation(conversationId)
            } catch (cancelled: CancellationException) {
                // 094 — Stop : garde la partie déjà générée, sans message d'erreur.
                if (streamingText.isNotBlank()) {
                    chatDao.insertMessage(
                        ChatMessageEntity(
                            conversationId = conversationId,
                            role = ROLE_ASSISTANT,
                            content = streamingText,
                            createdAt = System.currentTimeMillis()
                        )
                    )
                    chatDao.touchConversation(conversationId)
                }
                throw cancelled
            } catch (error: Exception) {
                // 100 — erreur en langage simple ; « Réessayer » est proposé.
                lastError = classifyChatError(error)
            } finally {
                streaming = false
                streamingText = ""
                streamJob = null
            }
        }
    }

    private suspend fun buildHistory(conversationId: Long): List<GroqChatMessage> {
        val stored = chatDao.getMessagesOnce(conversationId)
        val hasAttachedContext = stored.any { it.role == ROLE_USER && it.content.contains(ATTACHMENT_MARKER) }
        val system = GroqChatMessage(
            role = "system",
            content = buildSystemPrompt(
                tone = ChatTone.fromId(chatDao.getConversationOnce(conversationId)?.tone),
                hasAttachedContext = hasAttachedContext
            )
        )
        return buildList {
            add(system)
            stored.takeLast(HISTORY_LIMIT).forEach { add(GroqChatMessage(it.role, it.content)) }
        }
    }

    companion object {
        const val ROLE_USER = "user"
        const val ROLE_ASSISTANT = "assistant"

        /** Marqueur d'un contexte musical joint dans un message utilisateur (100). */
        const val ATTACHMENT_MARKER = "— Morceau joint :"

        /** Taille de l'historique envoyé au modèle (fenêtre glissante). */
        const val HISTORY_LIMIT = 20

        /** Morceaux joints au maximum pour une playlist (100). */
        const val PLAYLIST_ATTACH_LIMIT = 30
    }
}

/**
 * 098 — réordonne les cartes de suggestion : celles qui ont récolté des pouces
 * hauts montent, celles baissées descendent ; à score égal, ordre d'origine.
 */
internal fun orderSuggestions(scores: Map<String, Int>): List<ChatSuggestion> =
    ChatSuggestion.entries.sortedByDescending { scores[it.id] ?: 0 }

/** 100 — joint une playlist en ne citant que titre et artiste (30 morceaux max). */
internal fun buildPlaylistAttachment(playlist: PlaylistEntity, tracks: List<AudioTrackEntity>): String =
    buildString {
        append("— Playlist jointe : ").append(playlist.name.trim())
        append(" (").append(tracks.size).append(" morceaux)")
        tracks.take(ChatViewModel.PLAYLIST_ATTACH_LIMIT).forEach { track ->
            append("\n  · ").append(track.title.trim()).append(" — ").append(track.artist.trim())
        }
    }
