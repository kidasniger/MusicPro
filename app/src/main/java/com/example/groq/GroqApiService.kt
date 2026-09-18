package com.example.groq

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface GroqApiService {

    /**
     * Endpoint OpenAI-compatible de Groq pour la transcription audio via Whisper large-v3.
     * https://api.groq.com/openai/v1/audio/transcriptions
     */
    @Multipart
    @POST("openai/v1/audio/transcriptions")
    suspend fun transcribeAudio(
        @Header("Authorization") authorization: String,
        @Part file: MultipartBody.Part,
        @Part("model") model: RequestBody,
        @Part("response_format") responseFormat: RequestBody,
        @Part("timestamp_granularities[]") timestampGranularities: RequestBody? = null,
        @Part("language") language: RequestBody? = null
    ): Response<GroqTranscriptionResponse>
}
