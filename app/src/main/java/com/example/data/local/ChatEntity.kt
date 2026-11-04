package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Mise à jour 097 — conversation de l'assistant IA, stockée localement dans Room.
 *
 * Le titre est auto-généré en 5 mots à partir du premier message (voir
 * [com.example.ui.chat.chatTitleFrom]) ; modèle et ton sont mémorisés PAR conversation
 * (mise à jour 099) ; [suggestionKey] retient la carte de suggestion qui a lancé la
 * conversation pour affiner l'ordre des suggestions (mise à jour 098).
 */
@Entity(tableName = "chat_conversations")
data class ChatEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    /** « fast » ou « precis » (voir ChatModel). */
    val model: String,
    /** « neutral », « warm » ou « concise » (voir ChatTone). */
    val tone: String,
    val suggestionKey: String = "",
    val createdAt: Long,
    val updatedAt: Long
)
