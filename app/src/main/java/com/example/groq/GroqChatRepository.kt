package com.example.groq

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.ResponseBody
import org.json.JSONObject
import java.io.IOException

/**
 * Client de conversation Groq en Server-Sent Events.
 * Le flux est lu sur Dispatchers.IO et chaque fragment est transmis à l'interface.
 *
 * Mise à jour 099 — le modèle Groq est choisi par conversation (rapide / précis).
 * Mise à jour 100 — les refus HTTP sont convertis en exceptions parlantes : 401
 * → clé absente ou invalide, 429 → limite de débit, 5xx → panne du service,
 * le reste → erreur générale ; les coupures réseau remontent telles quelles et
 * sont classées par [com.example.ui.chat.classifyChatError].
 */
object GroqChatRepository {
    fun stream(
        apiKey: String,
        messages: List<GroqChatMessage>,
        model: String = "openai/gpt-oss-20b"
    ): Flow<String> = flow {
        if (apiKey.isBlank()) throw GroqException.InvalidApiKeyException()

        val response = GroqClient.apiService.streamChatCompletion(
            authorization = "Bearer ${apiKey.trim()}",
            request = GroqStreamingChatRequest(model = model, messages = messages)
        )

        if (!response.isSuccessful) {
            throw when (response.code()) {
                401, 403 -> GroqException.InvalidApiKeyException()
                429 -> GroqException.RateLimitExceededException()
                in 500..599 -> GroqException.ServerException(response.code())
                else -> GroqException.GeneralException(
                    "Groq a refusé la requête (HTTP ${response.code()})."
                )
            }
        }

        val body: ResponseBody = response.body()
            ?: throw IOException("Groq a renvoyé une réponse vide.")

        body.source().use { source ->
            while (!source.exhausted()) {
                val line = source.readUtf8Line() ?: break
                if (!line.startsWith("data:")) continue

                val payload = line.removePrefix("data:").trim()
                if (payload == "[DONE]") break
                if (payload.isBlank()) continue

                val token = runCatching {
                    JSONObject(payload)
                        .optJSONArray("choices")
                        ?.optJSONObject(0)
                        ?.optJSONObject("delta")
                        ?.optString("content")
                        .orEmpty()
                }.getOrDefault("")

                if (token.isNotEmpty() && token != "null") emit(token)
            }
        }
    }.flowOn(Dispatchers.IO)
}
