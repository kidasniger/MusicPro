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


@JsonClass(generateAdapter = true)
data class GroqResponsesRequest(
    @Json(name = "model") val model: String,
    @Json(name = "input") val input: String,
    @Json(name = "tool_choice") val toolChoice: String = "required",
    @Json(name = "tools") val tools: List<GroqResponseTool> = listOf(GroqResponseTool()),
    @Json(name = "reasoning") val reasoning: GroqReasoning? = GroqReasoning(),
    @Json(name = "max_output_tokens") val maxOutputTokens: Int = 120
)

@JsonClass(generateAdapter = true)
data class GroqResponseTool(
    @Json(name = "type") val type: String = "browser_search"
)

@JsonClass(generateAdapter = true)
data class GroqReasoning(
    @Json(name = "effort") val effort: String = "low"
)

@JsonClass(generateAdapter = true)
data class GroqResponsesResponse(
    @Json(name = "output") val output: List<GroqResponseOutputItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class GroqResponseOutputItem(
    @Json(name = "type") val type: String? = null,
    @Json(name = "content") val content: List<GroqResponseContentItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class GroqResponseContentItem(
    @Json(name = "type") val type: String? = null,
    @Json(name = "text") val text: String? = null
)
