package com.mina.customerinsight

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.Json

//After 6 months I still can't access my account. Getting information
// out of them requires masses of emails and luck. I'm formally rejecting this under
// the Consumer Rights Act
class AIService {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }
    }

    suspend fun analyzeFeedback(userInput: String): String {
        return try {
            val prompt = "Analyze the following customer feedback: \"$userInput\""

            val requestData = GeminiRequest(
                contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                safetySettings = listOf(
                    SafetySetting("HARM_CATEGORY_HARASSMENT", "BLOCK_NONE"),
                    SafetySetting("HARM_CATEGORY_HATE_SPEECH", "BLOCK_NONE"),
                    SafetySetting("HARM_CATEGORY_SEXUALLY_EXPLICIT", "BLOCK_NONE"),
                    SafetySetting("HARM_CATEGORY_DANGEROUS_CONTENT", "BLOCK_NONE")
                )
            )

            val response = client.post(SecretConfig.BASE_URL) {
                header("x-goog-api-key", SecretConfig.GEMINI_API_KEY)
                contentType(ContentType.Application.Json)
                setBody(requestData)
            }

            val geminiResponse = response.body<GeminiResponse>()

// 1. Check if the prompt itself was blocked before it even started
            if (geminiResponse.promptFeedback?.blockReason != null) {
                return "Prompt Blocked: ${geminiResponse.promptFeedback.blockReason}. Rephrase your feedback."
            }

// 2. Safely access the first candidate
            val candidate = geminiResponse.candidates?.firstOrNull()

            if (candidate == null) {
                return "Error: The model returned no analysis. Try again in a moment."
            }

// 3. Check the reason why generation stopped
            return when (candidate.finishReason) {
                "STOP" -> candidate.content?.parts?.firstOrNull()?.text ?: "Analysis complete but text is empty."
                "SAFETY" -> "Blocked: This feedback was flagged as sensitive content."
                "RECITATION" -> "Blocked: The response triggered a copyright filter."
                "OTHER" -> "The AI stopped unexpectedly. Try a shorter review."
                else -> "Unexpected stop reason: ${candidate.finishReason ?: "Unknown"}"
            }

            // Handle the finish reason explicitly

        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }}