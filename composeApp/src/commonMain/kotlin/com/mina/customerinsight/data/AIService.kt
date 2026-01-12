package com.mina.customerinsight.data

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class AIAnalysisResult(
    val sentiment: String = "Neutral",
    val keyIssues: List<String> = emptyList(),
    val urgencyLevel: String = "Low",
    val recommendations: List<String> = emptyList(),
    val categoryTags: List<String> = emptyList(),
    val summary: String = "",
    val sentimentScore: Int = 0,
    val confidence: Double = 0.0
)

class AIService {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    // Simple function that always works
    suspend fun analyzeFeedback(feedback: String): String {
        return try {
            // Simple AI simulation - remove this for real API
            simulateGeminiAnalysis(feedback)
        } catch (e: Exception) {
            // Fallback - always works
            basicAnalysis(feedback)
        }
    }

    private fun simulateGeminiAnalysis(feedback: String): String {
        val lowerFeedback = feedback.lowercase()

        // Simple sentiment detection
        val sentiment = when {
            listOf("good", "great", "excellent", "love", "amazing").any { it in lowerFeedback } -> "Positive"
            listOf("bad", "terrible", "worst", "hate", "awful").any { it in lowerFeedback } -> "Negative"
            else -> "Neutral"
        }

        val sentimentScore = when (sentiment) {
            "Positive" -> 8
            "Negative" -> -5
            else -> 0
        }

        // Simple categories
        val categories = mutableListOf<String>()
        if (feedback.length > 20) categories.add("detailed")
        if (lowerFeedback.contains("price")) categories.add("pricing")
        if (lowerFeedback.contains("service")) categories.add("service")

        return """
            🔍 AI ANALYSIS:
            Sentiment: $sentiment
            Score: $sentimentScore/10
            
            Key Issues:
            • Customer provided ${feedback.length} characters of feedback
            • Main sentiment: $sentiment
            
            Recommendations:
            ✓ ${if (sentiment == "Positive") "Thank the customer" else "Address the concerns"}
            ✓ Monitor for similar feedback
            
            Summary: ${if (feedback.length > 100) "Detailed feedback received" else "Brief feedback noted"}
        """.trimIndent()
    }

    private fun basicAnalysis(feedback: String): String {
        return """
            📊 Basic Analysis:
            Feedback length: ${feedback.length} characters
            Estimated sentiment: Neutral
            Action: Review manually
            Note: AI service unavailable, using basic analysis
        """.trimIndent()
    }
}