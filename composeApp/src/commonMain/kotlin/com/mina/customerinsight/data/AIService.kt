package com.mina.customerinsight.data

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class AIService {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(Logging) {
            level = LogLevel.ALL
        }
    }

    suspend fun analyzeFeedback(feedback: String): String {
        return try {
            // Mock response for now (since your API token expired)
            "AI Analysis (Mock): This feedback appears to be ${if (feedback.length > 20) "positive" else "neutral"}. Key insight: Customer ${if (feedback.contains("good", ignoreCase = true)) "likes" else "mentioned"} the service."

            // Uncomment when you get new API key:
            /*
            val prompt = """
                Analyze this customer feedback.
                1. Determine Sentiment.
                2. List 2 Key Insights or Action Items.

                Feedback: "$feedback"
            """.trimIndent()

            val safetySettings = listOf(
                SafetySetting("HARM_CATEGORY_HARASSMENT", "BLOCK_NONE"),
                SafetySetting("HARM_CATEGORY_HATE_SPEECH", "BLOCK_NONE"),
                SafetySetting("HARM_CATEGORY_SEXUALLY_EXPLICIT", "BLOCK_NONE"),
                SafetySetting("HARM_CATEGORY_DANGEROUS_CONTENT", "BLOCK_NONE")
            )

            val requestBody = GeminiRequest(
                contents = listOf(Content(listOf(Part(prompt)))),
                safetySettings = safetySettings
            )

            val httpResponse = client.post(SecretConfig.BASE_URL) {
                header("x-goog-api-key", SecretConfig.GEMINI_API_KEY)
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            val rawJson = httpResponse.bodyAsText()
            println("DEBUG GOOGLE RESPONSE: $rawJson")

            val geminiResponse: GeminiResponse = httpResponse.body()
            val candidate = geminiResponse.candidates?.firstOrNull()

            when (candidate?.finishReason) {
                "STOP" -> candidate.content?.parts?.firstOrNull()?.text ?: "AI returned no text."
                "SAFETY" -> "Blocked: Content triggered safety filters."
                null -> {
                    if (rawJson.contains("error")) "API Error: Check Console"
                    else "Error: No response from AI."
                }
                else -> "AI stopped: ${candidate.finishReason}"
            }
            */
        } catch (e: Exception) {
            "Analysis Error: ${e.message}"
        }
    }
}

