package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** Projection du score d'affinage des suggestions (mise à jour 098). */
data class ChatSuggestionScore(val suggestionKey: String, val score: Int)

/**
 * Mise à jour 097 — accès Room à l'historique des conversations de l'assistant IA.
 * Tout reste local : aucune table n'est synchronisée ni envoyée en ligne.
 */
@Dao
interface ChatDao {

    @Query("SELECT * FROM chat_conversations ORDER BY updatedAt DESC")
    fun observeConversations(): Flow<List<ChatEntity>>

    @Query(
        "SELECT * FROM chat_conversations WHERE title LIKE '%' || :query || '%' " +
            "ORDER BY updatedAt DESC"
    )
    fun searchConversations(query: String): Flow<List<ChatEntity>>

    @Query("SELECT * FROM chat_conversations WHERE id = :conversationId")
    fun observeConversation(conversationId: Long): Flow<ChatEntity?>

    @Query("SELECT * FROM chat_conversations WHERE id = :conversationId")
    suspend fun getConversationOnce(conversationId: Long): ChatEntity?

    @Query("SELECT * FROM chat_conversations ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getMostRecentConversation(): ChatEntity?

    @Query("SELECT * FROM chat_messages WHERE conversationId = :conversationId ORDER BY createdAt ASC, id ASC")
    fun observeMessages(conversationId: Long): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE conversationId = :conversationId ORDER BY createdAt ASC, id ASC")
    suspend fun getMessagesOnce(conversationId: Long): List<ChatMessageEntity>

    @Query("SELECT * FROM chat_messages WHERE conversationId = :conversationId ORDER BY createdAt DESC, id DESC LIMIT 1")
    suspend fun getLastMessageOnce(conversationId: Long): ChatMessageEntity?

    @Insert
    suspend fun insertConversation(conversation: ChatEntity): Long

    @Insert
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query("UPDATE chat_conversations SET title = :title, updatedAt = :updatedAt WHERE id = :conversationId")
    suspend fun renameConversation(conversationId: Long, title: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE chat_conversations SET model = :model, updatedAt = :updatedAt WHERE id = :conversationId")
    suspend fun updateModel(conversationId: Long, model: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE chat_conversations SET tone = :tone, updatedAt = :updatedAt WHERE id = :conversationId")
    suspend fun updateTone(conversationId: Long, tone: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE chat_messages SET feedback = :feedback WHERE id = :messageId")
    suspend fun setMessageFeedback(messageId: Long, feedback: Int)

    @Query("UPDATE chat_conversations SET updatedAt = :updatedAt WHERE id = :conversationId")
    suspend fun touchConversation(conversationId: Long, updatedAt: Long = System.currentTimeMillis())

    /**
     * Mise à jour 098 — score de pouces haut/bas par carte de suggestion :
     * sert à réordonner les suggestions de démarrage (affinage local).
     */
    @Query(
        "SELECT c.suggestionKey AS suggestionKey, COALESCE(SUM(m.feedback), 0) AS score " +
            "FROM chat_conversations c JOIN chat_messages m ON m.conversationId = c.id " +
            "WHERE c.suggestionKey != '' GROUP BY c.suggestionKey"
    )
    suspend fun suggestionScores(): List<ChatSuggestionScore>

    @Query("DELETE FROM chat_messages WHERE id = :messageId")
    suspend fun deleteMessageById(messageId: Long)

    @Query("DELETE FROM chat_conversations WHERE id = :conversationId")
    suspend fun deleteConversationById(conversationId: Long)

    /** Mise à jour 097 — « Tout effacer » dans Réglages › Confidentialité. */
    @Query("DELETE FROM chat_messages")
    suspend fun deleteAllMessages()

    @Query("DELETE FROM chat_conversations")
    suspend fun deleteAllConversations()
}
