package com.example.groq

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GroqStreamingChatRequest(
    /**
     * Mise à jour 099 — modèle Groq choisi par conversation :
     * « openai/gpt-oss-20b » (rapide) ou « openai/gpt-oss-120b » (précis).
     */
    @Json(name = "model") val model: String = "openai/gpt-oss-20b",
    @Json(name = "messages") val messages: List<GroqChatMessage>,
    @Json(name = "temperature") val temperature: Double = 0.6,
    @Json(name = "max_completion_tokens") val maxCompletionTokens: Int = 900,
    @Json(name = "stream") val stream: Boolean = true
)
