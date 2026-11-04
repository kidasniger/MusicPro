package com.example.ui.chat

import com.example.data.local.AudioTrackEntity
import com.example.data.local.PlaylistEntity
import com.example.groq.GroqException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

/**
 * Lot 091–100 — logique pure du chat : titre en 5 mots (097), consigne système
 * selon le ton (099), garde-fous 4 000 caractères (100), classification des
 * erreurs 429 / clé absente / réseau (100), affinage des suggestions (098).
 */
class ChatModelsTest {

    @Test
    fun `titre auto-généré en cinq mots`() {
        assertEquals(
            "Crée une playlist pour courir",
            chatTitleFrom("Crée une playlist pour courir à travers la ville")
        )
    }

    @Test
    fun `titre capitalisé et espaces réduits`() {
        assertEquals("Bonjour le monde", chatTitleFrom("  bonjour   le\tmonde  "))
    }

    @Test
    fun `titre tronqué au-delà de soixante-quatre caractères`() {
        val long = chatTitleFrom("a".repeat(200))
        assertTrue(long.length <= 64)
        assertTrue(long.endsWith("…"))
    }

    @Test
    fun `titre vide reste exploitable`() {
        assertEquals("Nouvelle conversation", chatTitleFrom("   "))
    }

    @Test
    fun `consigne système de base sans ton ni contexte`() {
        val prompt = buildSystemPrompt(ChatTone.NEUTRAL, hasAttachedContext = false)
        assertTrue(prompt.contains("assistant musical de MusicPro"))
        assertTrue(!prompt.contains("trois phrases"))
        assertTrue(!prompt.contains("à l'artiste"))
    }

    @Test
    fun `ton chaleureux et concis sont injectés`() {
        assertTrue(buildSystemPrompt(ChatTone.WARM, false).contains("chaleureux"))
        assertTrue(buildSystemPrompt(ChatTone.CONCISE, false).contains("trois phrases"))
    }

    @Test
    fun `contexte joint ajoute la règle de minimisation`() {
        val prompt = buildSystemPrompt(ChatTone.NEUTRAL, hasAttachedContext = true)
        assertTrue(prompt.contains("titre"))
        assertTrue(prompt.contains("jamais"))
    }

    @Test
    fun `limite de saisie à quatre mille caractères`() {
        assertEquals(4000, CHAT_MAX_INPUT_CHARS)
        assertEquals(3200, CHAT_COUNTER_VISIBLE_FROM)
    }

    @Test
    fun `erreurs classées en langage simple`() {
        assertEquals(ChatErrorKind.NO_KEY, classifyChatError(GroqException.InvalidApiKeyException()))
        assertEquals(ChatErrorKind.RATE_LIMIT, classifyChatError(GroqException.RateLimitExceededException()))
        assertEquals(ChatErrorKind.SERVER, classifyChatError(GroqException.ServerException(503)))
        assertEquals(ChatErrorKind.NETWORK, classifyChatError(IOException("offline")))
        assertEquals(ChatErrorKind.GENERAL, classifyChatError(IllegalStateException("odd")))
        assertEquals(ChatErrorKind.GENERAL, classifyChatError(null))
    }

    @Test
    fun `modèles et tons retombent sur des valeurs sûres`() {
        assertEquals(ChatModel.FAST, ChatModel.fromId(null))
        assertEquals(ChatModel.PRECISE, ChatModel.fromId("precis"))
        assertEquals("openai/gpt-oss-120b", ChatModel.PRECISE.groqModel)
        assertEquals(ChatTone.NEUTRAL, ChatTone.fromId("inconnu"))
    }

    @Test
    fun `suggestions réordonnées par score puis ordre d'origine`() {
        val order = orderSuggestions(mapOf(ChatSuggestion.TONIGHT.id to 5, ChatSuggestion.LYRICS.id to 2))
        assertEquals(
            listOf(ChatSuggestion.TONIGHT, ChatSuggestion.LYRICS, ChatSuggestion.PLAYLIST, ChatSuggestion.EXPLAIN),
            order
        )
    }

    @Test
    fun `attachement de morceau limité aux quatre champs`() {
        val attachment = buildTrackAttachment("Salina", "HC Turbionaire", "Métamorphose", "Zouk")
        assertEquals("— Morceau joint : Salina — HC Turbionaire — Métamorphose — Zouk", attachment)
        assertTrue(!attachment.contains("/storage"))
    }

    @Test
    fun `attachement de playlist cite titre et artiste, trente maximum`() {
        val playlist = PlaylistEntity(
            id = 1L,
            name = "Course du matin",
            description = "",
            createdAt = 1L,
            updatedAt = 1L
        )
        val tracks = (1L..35L).map { index ->
            AudioTrackEntity(
                id = index,
                title = "Piste $index",
                artist = "Artiste $index",
                album = "Album $index",
                duration = 60_000L,
                contentUri = "content://media/$index",
                path = "/storage/piste$index.mp3"
            )
        }
        val attachment = buildPlaylistAttachment(playlist, tracks)
        assertTrue(attachment.startsWith("— Playlist jointe : Course du matin (35 morceaux)"))
        assertTrue(attachment.contains("· Piste 30 — Artiste 30"))
        assertTrue(!attachment.contains("Piste 31"))
        assertTrue(!attachment.contains("/storage"))
    }
}
