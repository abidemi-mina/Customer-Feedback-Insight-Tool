package com.mina.customerinsight

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.Json

class AIService {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true // Prevents crashing if Google adds new fields
                prettyPrint = true
            })
        }
    }

    suspend fun analyzeFeedback(userInput: String): String {
        return try {
            // 1. Prepare the Prompt
            val prompt = """
                Analyze the following customer feedback. 
                Provide a summary of the sentiment (Positive/Negative/Neutral) 
                and list the top 3 key insights or complaints.
                
                Feedback: "$userInput"
            """.trimIndent()

            val requestBody = GeminiRequest(
                contents = listOf(Content(parts = listOf(Part(text = prompt))))
            )

            // 2. Make the POST request
            val response: GeminiResponse = client.post(SecretConfig.BASE_URL) {
                url { parameters.append("key", SecretConfig.GEMINI_API_KEY) }
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }.body()

            // 3. Extract the text from the response
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "AI could not generate a response. Please try again."

        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}