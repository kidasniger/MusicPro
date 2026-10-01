package com.example.groq

import com.example.data.local.AudioTrackEntity
import com.example.data.security.GroqApiKeyStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

/**
 * Utilise le modèle Groq rapide pour nettoyer les métadonnées d'un morceau
 * avant une recherche LRCLIB. L'IA ne choisit jamais les paroles : elle prépare
 * seulement une requête de recherche plus propre.
 */
object GroqLyricsQueryAssistant {

    private const val MODEL = "openai/gpt-oss-20b"

    data class Suggestion(
        val title: String,
        val artist: String
    )

    suspend fun suggest(
        apiKeyStore: GroqApiKeyStore,
        track: AudioTrackEntity,
        currentTitle: String,
        currentArtist: String,
        durationSec: Int? = null
    ): Result<Suggestion> = withContext(Dispatchers.IO) {
        val apiKey = apiKeyStore.getApiKey()
        if (apiKey.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("Clé API Groq absente. Ajoutez-la dans Paramètres.")
            )
        }

        val title = currentTitle.trim().ifBlank { track.title.trim() }
        val artist = currentArtist.trim().ifBlank { track.artist.trim() }

        if (title.isBlank() && artist.isBlank()) {
            return@withContext Result.failure(
                IllegalArgumentException("Aucun titre ou artiste à analyser.")
            )
        }

        val prompt = buildString {
            appendLine("Tu dois identifier précisément cette chanson pour une recherche de paroles LRCLIB.")
            appendLine("UTILISE OBLIGATOIREMENT la recherche Web avant de répondre. Ne te limite pas à ta mémoire.")
            appendLine("Cherche plusieurs sources musicales crédibles et recoupe les résultats.")
            appendLine("Privilégie les bases musicales et les pages officielles quand elles existent.")
            appendLine("Trouve le titre exact ET l'artiste exact, y compris la bonne version musicale (Remix, Live, Acoustic, Edit, etc.) lorsqu'elle est confirmée.")
            appendLine("Utilise aussi la durée du fichier pour départager les homonymes lorsque la durée est disponible.")
            appendLine("Ne traduis pas le titre, ne remplace pas un titre par une autre chanson ressemblante et n'invente jamais de métadonnée.")
            appendLine("Retourne exactement deux lignes, sans explication ni markdown :")
            appendLine("TITLE=<titre exact vérifié sur le Web>")
            appendLine("ARTIST=<artiste exact vérifié sur le Web>")
            appendLine("Si une information ne peut pas être vérifiée, conserve la valeur locale correspondante.")
            appendLine()
            appendLine("TITLE_INPUT=$title")
            appendLine("ARTIST_INPUT=$artist")
            appendLine("DURATION_INPUT=" + (durationSec?.takeIf { it > 0 }?.toString() ?: "inconnue") + " secondes")
        }

        try {
            val response = GroqClient.apiService.suggestLyricsSearchQueryWithWeb(
                authorization = "Bearer " + apiKey.trim(),
                request = GroqResponsesRequest(
                    model = MODEL,
                    input = prompt,
                    toolChoice = "required",
                    tools = listOf(GroqResponseTool(type = "browser_search")),
                    reasoning = GroqReasoning(effort = "low"),
                    maxOutputTokens = 120
                )
            )

            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    Exception("Recherche Web IA indisponible (HTTP " + response.code() + ").")
                )
            }

            val content = response.body()
                ?.output
                .orEmpty()
                .asSequence()
                .flatMap { it.content.asSequence() }
                .filter { it.type.equals("output_text", ignoreCase = true) }
                .mapNotNull { it.text }
                .joinToString("\n")
                .trim()

            val suggestion = parseSuggestion(content, title, artist)
                ?: return@withContext Result.failure(
                    Exception("L'IA n'a pas produit une requête exploitable.")
                )

            Result.success(suggestion)
        } catch (e: HttpException) {
            Result.failure(Exception("Service IA indisponible (HTTP ${e.code()})."))
        } catch (e: IOException) {
            Result.failure(Exception("Impossible de joindre Groq. Vérifiez votre connexion Internet."))
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Erreur pendant l'aide IA."))
        }
    }

    internal fun parseSuggestion(
        raw: String,
        fallbackTitle: String,
        fallbackArtist: String
    ): Suggestion? {
        val cleaned = raw
            .replace("```text", "", ignoreCase = true)
            .replace("```", "", ignoreCase = true)
            .trim()

        var title = ""
        var artist = ""

        cleaned.lineSequence().forEach { line ->
            val normalized = line.trim()
            when {
                normalized.startsWith("TITLE=", ignoreCase = true) ->
                    title = normalized.substringAfter('=').trim()
                normalized.startsWith("TITLE:", ignoreCase = true) ->
                    title = normalized.substringAfter(':').trim()
                normalized.startsWith("ARTIST=", ignoreCase = true) ->
                    artist = normalized.substringAfter('=').trim()
                normalized.startsWith("ARTIST:", ignoreCase = true) ->
                    artist = normalized.substringAfter(':').trim()
            }
        }

        if (title.isBlank() && artist.isBlank()) {
            val parts = cleaned.split('\n')
                .map { it.trim() }
                .filter { it.isNotBlank() }
            if (parts.size >= 2) {
                title = parts[0].substringAfter(':', parts[0]).trim()
                artist = parts[1].substringAfter(':', parts[1]).trim()
            }
        }

        title = title.trim().trim('"', '\\', '*')
        artist = artist.trim().trim('"', '\\', '*')

        if (title.isBlank()) title = fallbackTitle.trim()
        if (artist.isBlank()) artist = fallbackArtist.trim()

        return if (title.isBlank() && artist.isBlank()) null else Suggestion(title, artist)
    }
}
