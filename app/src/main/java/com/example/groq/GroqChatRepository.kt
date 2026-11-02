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
 */
object GroqChatRepository {
    fun stream(apiKey: String, messages: List<GroqChatMessage>): Flow<String> = flow {
        require(apiKey.isNotBlank()) { "Clé API Groq absente. Ajoutez-la dans Paramètres." }

        val response = GroqClient.apiService.streamChatCompletion(
            authorization = "Bearer ${apiKey.trim()}",
            request = GroqStreamingChatRequest(messages = messages)
        )

        if (!response.isSuccessful) {
            val detail = response.errorBody()?.string()?.take(240).orEmpty()
            throw IOException(
                "Groq a refusé la requête (HTTP ${response.code()}). " +
                    detail.ifBlank { "Vérifiez votre clé API et réessayez." }
            )
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
