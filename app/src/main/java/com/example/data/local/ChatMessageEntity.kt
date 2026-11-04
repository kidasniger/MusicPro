package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Mise à jour 097 — message d'une conversation de chat, stocké localement.
 *
 * [role] vaut « user » ou « assistant » ; [feedback] mémorise le pouce levé (+1) ou
 * baissé (−1) posé sous une réponse terminée (mise à jour 098) ; 0 = pas d'avis.
 * Les fragments de streaming ne sont PAS écrits ici : le message assistant est
 * inséré une seule fois, à la fin de la génération.
 */
@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("conversationId")]
)
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val conversationId: Long,
    val role: String,
    val content: String,
    val feedback: Int = 0,
    val createdAt: Long
)
