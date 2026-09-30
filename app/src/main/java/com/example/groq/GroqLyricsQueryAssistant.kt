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

    private const val MODEL = "llama-3.1-8b-instant"

    data class Suggestion(
        val title: String,
        val artist: String
    )

    suspend fun suggest(
        apiKeyStore: GroqApiKeyStore,
        track: AudioTrackEntity,
        currentTitle: String,
        currentArtist: String
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
            appendLine("Tu aides à rechercher une chanson dans LRCLIB.")
            appendLine("Nettoie et normalise uniquement le titre et l'artiste fournis.")
            appendLine("Retire les suffixes de fichier et les détails parasites comme [Official Video],")
            appendLine("[Lyrics], (Official Audio), qualité audio, année et tags évidents de téléchargement.")
            appendLine("Conserve les versions musicales utiles comme Remix, Live, Acoustic, Edit ou Radio Edit.")
            appendLine("Conserve les accents et la langue originale.")
            appendLine("N'invente jamais un artiste ou un titre absent : améliore seulement ce qui est fourni.")
            appendLine("Retourne exactement deux lignes, sans markdown :")
            appendLine("TITLE=<titre normalisé>")
            appendLine("ARTIST=<artiste normalisé>")
            appendLine()
            appendLine("TITLE_INPUT=$title")
            appendLine("ARTIST_INPUT=$artist")
        }

        try {
            val response = GroqClient.apiService.suggestLyricsSearchQuery(
                authorization = "Bearer ${apiKey.trim()}",
                request = GroqChatCompletionRequest(
                    model = MODEL,
                    messages = listOf(
                        GroqChatMessage(
                            role = "system",
                            content = "Tu es un assistant de métadonnées musicales précis et minimaliste."
                        ),
                        GroqChatMessage(
                            role = "user",
                            content = prompt
                        )
                    ),
                    temperature = 0.0,
                    maxCompletionTokens = 80
                )
            )

            if (!response.isSuccessful) {
                val code = response.code()
                return@withContext Result.failure(
                    Exception("Aide IA indisponible (HTTP $code).")
                )
            }

            val content = response.body()
                ?.choices
                ?.firstOrNull()
                ?.message
                ?.content
                ?.trim()
                .orEmpty()

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
