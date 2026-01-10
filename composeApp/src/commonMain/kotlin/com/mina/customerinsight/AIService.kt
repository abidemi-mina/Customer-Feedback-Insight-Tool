package com.mina.customerinsight

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.client.request.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.statement.* // This fixes the "shouting" bodyAsText
import io.ktor.http.*
import kotlinx.serialization.json.Json



class AIService {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(Logging) {
            level = LogLevel.ALL // This will show the Gemini error in the Run tab
        }
    }
    suspend fun analyzeFeedback(feedback: String): String {
        return try {
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

            // 1. Get the RAW HTTP response first
            val httpResponse = client.post(SecretConfig.BASE_URL) {
                header("x-goog-api-key", SecretConfig.GEMINI_API_KEY)
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            // 2. Debug: Print exactly what Google sent back to the console
            val rawJson = httpResponse.bodyAsText()
            println("DEBUG GOOGLE RESPONSE: $rawJson")

            // 3. Now convert it to our Kotlin object
            val geminiResponse: GeminiResponse = httpResponse.body()

            val candidate = geminiResponse.candidates?.firstOrNull()

            return when (candidate?.finishReason) {
                "STOP" -> candidate.content?.parts?.firstOrNull()?.text ?: "AI returned no text."
                "SAFETY" -> "Blocked: Content triggered safety filters."
                null -> {
                    // If candidates are null, check if there was a block reason
                    if (rawJson.contains("error")) "API Error: Check Console"
                    else "Error: No response from AI."
                }
                else -> "AI stopped: ${candidate.finishReason}"
            }

        } catch (e: Exception) {
            "Analysis Error: ${e.message}"
        }
    }
}