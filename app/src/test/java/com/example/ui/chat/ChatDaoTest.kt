package com.example.ui.chat

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.ChatEntity
import com.example.data.local.ChatMessageEntity
import com.example.data.local.MusicProDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Mise à jour 097/098 — DAO du chat sur Room en mémoire : historique ordonné,
 * recherche, renommage, pouces haut/bas, suppression par conversation et
 * « Tout effacer », scores d'affinage des suggestions.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ChatDaoTest {

    private lateinit var database: MusicProDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, MusicProDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun conversation(title: String, suggestionKey: String = "") = ChatEntity(
        title = title,
        model = "fast",
        tone = "neutral",
        suggestionKey = suggestionKey,
        createdAt = 1L,
        updatedAt = 1L
    )

    @Test
    fun `messages ordonnés par création`() = runBlocking {
        val dao = database.chatDao()
        val id = dao.insertConversation(conversation("Historique"))
        dao.insertMessage(ChatMessageEntity(conversationId = id, role = "user", content = "Un", createdAt = 10L))
        dao.insertMessage(ChatMessageEntity(conversationId = id, role = "assistant", content = "Deux", createdAt = 20L))
        dao.insertMessage(ChatMessageEntity(conversationId = id, role = "user", content = "Trois", createdAt = 30L))

        val messages = dao.observeMessages(id).first()
        assertEquals(listOf("Un", "Deux", "Trois"), messages.map { it.content })
    }

    @Test
    fun `renommage met à jour l'horodatage`() = runBlocking {
        val dao = database.chatDao()
        val id = dao.insertConversation(conversation("Avant"))
        dao.renameConversation(id, "Après", updatedAt = 500L)
        val renamed = dao.getConversationOnce(id)!!
        assertEquals("Après", renamed.title)
        assertEquals(500L, renamed.updatedAt)
    }

    @Test
    fun `suppression d'une conversation efface ses messages en cascade`() = runBlocking {
        val dao = database.chatDao()
        val kept = dao.insertConversation(conversation("Gardée"))
        val removed = dao.insertConversation(conversation("Supprimée"))
        dao.insertMessage(ChatMessageEntity(conversationId = kept, role = "user", content = "a", createdAt = 1L))
        dao.insertMessage(ChatMessageEntity(conversationId = removed, role = "user", content = "b", createdAt = 2L))

        dao.deleteConversationById(removed)
        assertEquals(1, dao.observeConversations().first().size)
        assertEquals(1, dao.observeMessages(kept).first().size)
        assertTrue(dao.observeMessages(removed).first().isEmpty())
    }

    @Test
    fun `tout effacer vide conversations et messages`() = runBlocking {
        val dao = database.chatDao()
        val id = dao.insertConversation(conversation("À supprimer"))
        dao.insertMessage(ChatMessageEntity(conversationId = id, role = "user", content = "x", createdAt = 1L))

        dao.deleteAllMessages()
        dao.deleteAllConversations()

        assertTrue(dao.observeConversations().first().isEmpty())
        assertTrue(dao.observeMessages(id).first().isEmpty())
    }

    @Test
    fun `pouces haut bas et scores d'affinage`() = runBlocking {
        val dao = database.chatDao()
        val id = dao.insertConversation(conversation("Playlist", suggestionKey = "playlist"))
        val messageId = dao.insertMessage(
            ChatMessageEntity(conversationId = id, role = "assistant", content = "réponse", createdAt = 1L)
        )
        dao.setMessageFeedback(messageId, 1)
        assertEquals(1, dao.observeMessages(id).first().single().feedback)
        dao.setMessageFeedback(messageId, -1)
        assertEquals(-1, dao.observeMessages(id).first().single().feedback)
        dao.setMessageFeedback(messageId, 0)
        assertEquals(0, dao.observeMessages(id).first().single().feedback)

        dao.setMessageFeedback(messageId, 1)
        val scores = dao.suggestionScores()
        assertEquals(1, scores.size)
        assertEquals("playlist", scores.single().suggestionKey)
        assertEquals(1, scores.single().score)
    }

    @Test
    fun `modèle et ton mémorisés par conversation`() = runBlocking {
        val dao = database.chatDao()
        val id = dao.insertConversation(conversation("Choix"))
        dao.updateModel(id, "precis", updatedAt = 2L)
        dao.updateTone(id, "concise", updatedAt = 3L)
        val updated = dao.getConversationOnce(id)!!
        assertEquals("precis", updated.model)
        assertEquals("concise", updated.tone)
    }
}
