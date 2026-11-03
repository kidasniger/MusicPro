package com.example.ui.playlist

import com.example.data.local.AudioTrackEntity
import com.example.data.security.GroqApiKeyStore
import com.example.groq.GroqChatCompletionRequest
import com.example.groq.GroqChatMessage
import com.example.groq.GroqClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Mise à jour 069 — création de playlist assistée par IA.
 *
 * L'utilisateur décrit une ambiance ; le modèle Groq choisit des morceaux PARMI la
 * bibliothèque locale uniquement (jamais de morceau externe inventé) : l'invite contient
 * la liste des morceaux disponibles et la réponse n'est acceptée que si chaque identifiant
 * renvoyé existe réellement dans la bibliothèque.
 */
object AiPlaylistAssistant {

    private const val MODEL = "openai/gpt-oss-20b"

    /** Nombre maximal de morceaux proposés au modèle (évite les invites géantes). */
    internal const val LIBRARY_SAMPLE_LIMIT = 250

    /** Nombre maximal de morceaux demandés à l'IA pour une playlist. */
    internal const val PICK_LIMIT = 15

    /**
     * Extrait les identifiants numériques d'une réponse (tolérant : JSON, puces,
     * lignes « ID=12 »…). Seuls les identifiants retrouvés dans la bibliothèque
     * sont conservés par [generate].
     */
    internal fun parseTrackIds(response: String): List<Long> =
        Regex("""\d+""").findAll(response).mapNotNull { it.value.toLongOrNull() }.distinct().toList()

    /** Catalogue compact présenté au modèle : « id | titre | artiste | album | genre ». */
    internal fun buildCatalogue(tracks: List<AudioTrackEntity>): String =
        tracks.take(LIBRARY_SAMPLE_LIMIT).joinToString("\n") { track ->
            buildString {
                append(track.id).append(" | ").append(track.title)
                append(" | ").append(track.artist)
                append(" | ").append(track.album)
                append(" | ").append(track.genre.ifBlank { "?" })
            }
        }

    internal fun buildPrompt(ambiance: String, catalogue: String): String = buildString {
        appendLine("Tu es l'assistant musical de MusicPro. L'utilisateur décrit une ambiance,")
        appendLine("et tu choisis des morceaux pour une playlist.")
        appendLine()
        appendLine("RÈGLES ABSOLUES :")
        appendLine("- Choisis UNIQUEMENT parmi les morceaux du catalogue ci-dessous (colonne id en premier).")
        appendLine("- N'invente JAMAIS de morceau, d'artiste ou d'identifiant externe.")
        appendLine("- Renvoie au maximum $PICK_LIMIT identifiants, les plus adaptés à l'ambiance d'abord.")
        appendLine("- Réponds UNIQUEMENT par une liste d'identifiants séparés par des virgules, sans texte.")
        appendLine()
        appendLine("AMBIANCE DEMANDÉE : ${ambiance.trim()}")
        appendLine()
        appendLine("CATALOGUE (id | titre | artiste | album | genre) :")
        append(catalogue)
    }

    /**
     * Demande au modèle une sélection pour [ambiance] et renvoie les morceaux de
     * [libraryTracks] correspondants, dans l'ordre proposé. `Result.failure` si la
     * clé est absente, la requête échoue ou aucun identifiant valide n'est renvoyé.
     */
    suspend fun generate(
        apiKeyStore: GroqApiKeyStore,
        ambiance: String,
        libraryTracks: List<AudioTrackEntity>
    ): Result<List<AudioTrackEntity>> = withContext(Dispatchers.IO) {
        val apiKey = apiKeyStore.getApiKey()
        if (apiKey.isBlank()) {
            return@withContext Result.failure(IllegalStateException("API key missing"))
        }
        val trimmed = ambiance.trim()
        if (trimmed.isBlank() || libraryTracks.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Empty ambiance or library"))
        }

        try {
            val response = GroqClient.apiService.chatCompletion(
                authorization = "Bearer " + apiKey.trim(),
                request = GroqChatCompletionRequest(
                    model = MODEL,
                    messages = listOf(
                        GroqChatMessage(role = "system", content = "Tu es un DJ précis qui ne choisit que dans la liste fournie."),
                        GroqChatMessage(role = "user", content = buildPrompt(trimmed, buildCatalogue(libraryTracks)))
                    ),
                    temperature = 0.4,
                    maxCompletionTokens = 300
                )
            )
            val content = response.body()?.choices?.firstOrNull()?.message?.content.orEmpty()
            val byId = libraryTracks.associateBy { it.id }
            val picked = parseTrackIds(content).mapNotNull { byId[it] }.distinctBy { it.id }
            if (picked.isEmpty()) {
                Result.failure(IllegalStateException("No valid track in response"))
            } else {
                Result.success(picked)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
