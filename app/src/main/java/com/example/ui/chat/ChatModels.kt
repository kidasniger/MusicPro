package com.example.ui.chat

import com.example.groq.GroqException

/**
 * Logique pure du chat (lot 091–100) : choix du modèle et du ton (099),
 * titre auto-généré en 5 mots (097), garde-fous d'entrée (100) et
 * classification des erreurs en langage simple. Tout est testable sans
 * Android ni réseau.
 */

/** Mise à jour 100 — longueur maximale d'une requête, avec compteur discret. */
const val CHAT_MAX_INPUT_CHARS = 4000

/** Seuil d'affichage du compteur « x / 4 000 » (80 % de la limite). */
const val CHAT_COUNTER_VISIBLE_FROM = 3200

/** Mise à jour 097 — nombre de mots du titre auto-généré d'une conversation. */
const val CHAT_TITLE_WORDS = 5

/**
 * Mise à jour 099 — modèle Groq mémorisé par conversation.
 * « rapide » = gpt-oss-20b (latence minimale), « précis » = gpt-oss-120b (raisonnement).
 */
enum class ChatModel(val id: String, val groqModel: String) {
    FAST("fast", "openai/gpt-oss-20b"),
    PRECISE("precis", "openai/gpt-oss-120b");

    companion object {
        fun fromId(id: String?): ChatModel = entries.firstOrNull { it.id == id } ?: FAST
    }
}

/** Mise à jour 099 — ton de la réponse, mémorisé par conversation. */
enum class ChatTone(val id: String, val promptSuffix: String) {
    NEUTRAL("neutral", ""),
    WARM("warm", "Adopte un ton chaleureux, encourageant et humain."),
    CONCISE("concise", "Réponds en trois phrases maximum, sans détour ni préambule.");

    companion object {
        fun fromId(id: String?): ChatTone = entries.firstOrNull { it.id == id } ?: NEUTRAL
    }
}

/** Clé des cartes de suggestion (093), retenue avec la conversation pour l'affinage (098). */
enum class ChatSuggestion(val id: String) {
    PLAYLIST("playlist"),
    LYRICS("lyrics"),
    TONIGHT("tonight"),
    EXPLAIN("explain")
}

/**
 * Mise à jour 097 — titre auto-généré : les 5 premiers mots du premier message,
 * capitalisés, sans retour à la ligne, tronqués à 64 caractères.
 */
fun chatTitleFrom(prompt: String): String {
    val words = prompt.trim()
        .replace(Regex("\\s+"), " ")
        .split(' ')
        .filter { it.isNotBlank() }
        .take(CHAT_TITLE_WORDS)
    if (words.isEmpty()) return "Nouvelle conversation"
    val title = words.joinToString(" ")
    val capitalized = title.replaceFirstChar { it.uppercase() }
    return if (capitalized.length <= 64) capitalized else capitalized.take(61) + "…"
}

/**
 * Mise à jour 100 — consigne système : rôle de l'assistant, ton choisi, et règle
 * de minimisation des données (jamais de chemin de fichier ni de donnée
 * personnelle ; uniquement titre, artiste, album et genre du contexte joint).
 */
fun buildSystemPrompt(tone: ChatTone, hasAttachedContext: Boolean): String = buildString {
    append("Tu es l'assistant musical de MusicPro. Réponds en français, de façon claire ")
    append("et concise. Aide à découvrir la musique, composer des playlists et comprendre ")
    append("les morceaux. Ne prétends pas avoir recherché des paroles ou créé une playlist ")
    append("dans l'application si tu ne l'as pas fait. Pour des paroles complètes protégées, ")
    append("propose plutôt un résumé ou un extrait bref.")
    if (tone.promptSuffix.isNotBlank()) {
        append(' ')
        append(tone.promptSuffix)
    }
    if (hasAttachedContext) {
        append(' ')
        append(
            "Le contexte musical fourni par l'utilisateur est limité au titre, à l'artiste, " +
                "à l'album et au genre : ne demande jamais de fichier, de chemin ou de donnée " +
                "personnelle."
        )
    }
}

/** Mise à jour 100 — type d'erreur traduit en message clair par l'interface. */
enum class ChatErrorKind { NO_KEY, RATE_LIMIT, NETWORK, SERVER, GENERAL }

/**
 * Mise à jour 100 — classe une exception en type d'erreur compréhensible :
 * clé absente/invalide, limite de débit (429), réseau coupé, panne serveur, autre.
 */
fun classifyChatError(error: Throwable?): ChatErrorKind = when (error) {
    is GroqException.InvalidApiKeyException -> ChatErrorKind.NO_KEY
    is GroqException.RateLimitExceededException -> ChatErrorKind.RATE_LIMIT
    is GroqException.ServerException -> ChatErrorKind.SERVER
    is GroqException.NetworkException -> ChatErrorKind.NETWORK
    is java.io.IOException -> ChatErrorKind.NETWORK
    else -> ChatErrorKind.GENERAL
}

/**
 * Mise à jour 100 — le bouton « + » n'insère un contexte musical que si
 * l'utilisateur a consenti (Réglages › Confidentialité) : uniquement
 * titre, artiste, album et genre — jamais de chemin de fichier.
 */
fun buildTrackAttachment(title: String, artist: String, album: String, genre: String): String =
    "— Morceau joint : ${title.trim()} — ${artist.trim()} — ${album.trim()} — " +
        genre.trim().ifBlank { "?" }
