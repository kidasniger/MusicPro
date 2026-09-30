package com.example.groq

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GroqChatCompletionRequest(
    @Json(name = "model") val model: String,
    @Json(name = "messages") val messages: List<GroqChatMessage>,
    @Json(name = "temperature") val temperature: Double = 0.0,
    @Json(name = "max_completion_tokens") val maxCompletionTokens: Int = 80
)

@JsonClass(generateAdapter = true)
data class GroqChatMessage(
    @Json(name = "role") val role: String,
    @Json(name = "content") val content: String
)

@JsonClass(generateAdapter = true)
data class GroqChatCompletionResponse(
    @Json(name = "choices") val choices: List<GroqChatChoice> = emptyList()
)

@JsonClass(generateAdapter = true)
data class GroqChatChoice(
    @Json(name = "message") val message: GroqChatMessage? = null
)
