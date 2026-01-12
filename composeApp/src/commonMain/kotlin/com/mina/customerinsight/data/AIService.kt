package com.mina.customerinsight.data

import com.mina.customerinsight.SecretConfig
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
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

@Serializable
private data class GeminiRequest(
    val contents: List<Content>
)

@Serializable
private data class Content(
    val parts: List<Part>
)

@Serializable
private data class Part(
    val text: String
)

@Serializable
private data class GeminiResponse(
    val candidates: List<Candidate>
)

@Serializable
private data class Candidate(
    val content: Content
)

class AIService {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    suspend fun analyzeFeedback(
        feedback: String,
        businessType: String? = null,
        businessName: String? = null
    ): String {
        return try {
            callGeminiAPI(feedback, businessType, businessName)
        } catch (e: Exception) {
            println("Gemini API Error: ${e.message}")
            e.printStackTrace()
            // Fallback to basic analysis if API fails
            basicAnalysis(feedback)
        }
    }

    private suspend fun callGeminiAPI(
        feedback: String,
        businessType: String?,
        businessName: String?
    ): String {
        val businessContext = if (businessType != null && businessName != null) {
            "Business: $businessName (Type: $businessType)"
        } else {
            "Business type: General"
        }

        val prompt = """
You are an expert customer feedback analyst. Analyze this customer feedback and provide actionable insights.

$businessContext

Customer Feedback:
"$feedback"

Provide a detailed analysis in the following EXACT format:

📊 SENTIMENT: [Positive/Negative/Neutral]
Score: [number from -10 to +10]

🔍 KEY ISSUES:
- [Issue 1]
- [Issue 2]
- [Issue 3]

⚠️ URGENCY: [Low/Medium/High]

💡 RECOMMENDATIONS FOR ${businessName ?: "THE BUSINESS"}:
${if (businessType != null) "Based on being a $businessType business:" else ""}
✓ [Specific actionable recommendation 1]
✓ [Specific actionable recommendation 2]
✓ [Specific actionable recommendation 3]
✓ [How to improve customer service based on this feedback]

📑 CATEGORIES: [comma-separated tags like: service_quality, pricing, product_issue, staff_behavior]

📝 SUMMARY:
[2-3 sentence summary of the feedback and its implications for the business]

CONFIDENCE: [percentage like 85%]

IMPORTANT: 
- Make recommendations SPECIFIC to ${businessType ?: "this business type"}
- Focus on ACTIONABLE steps the business can take
- Consider how this feedback affects customer retention
- Suggest concrete improvements to customer service
        """.trimIndent()

        val requestBody = GeminiRequest(
            contents = listOf(
                Content(
                    parts = listOf(Part(text = prompt))
                )
            )
        )

        val url = "${SecretConfig.BASE_URL}?key=${SecretConfig.GEMINI_API_KEY}"

        val response: HttpResponse = client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        val geminiResponse: GeminiResponse = response.body()
        val analysisText = geminiResponse.candidates.firstOrNull()
            ?.content?.parts?.firstOrNull()?.text
            ?: throw Exception("No response from Gemini")

        return analysisText
    }

    private fun basicAnalysis(feedback: String): String {
        val lowerFeedback = feedback.lowercase()

        val sentiment = when {
            listOf("good", "great", "excellent", "love", "amazing", "wonderful", "perfect").any { it in lowerFeedback } -> "Positive"
            listOf("bad", "terrible", "worst", "hate", "awful", "poor", "disappointed").any { it in lowerFeedback } -> "Negative"
            else -> "Neutral"
        }

        val sentimentScore = when (sentiment) {
            "Positive" -> 7
            "Negative" -> -6
            else -> 0
        }

        return """
📊 SENTIMENT: $sentiment
Score: $sentimentScore/10

🔍 KEY ISSUES:
- Customer feedback received (${feedback.length} characters)
- Sentiment: $sentiment
- Requires manual review

⚠️ URGENCY: Medium

💡 RECOMMENDATIONS:
✓ Review this feedback in detail
✓ Consider following up with the customer
✓ Look for patterns in similar feedback
✓ Address concerns promptly to improve service

📑 CATEGORIES: general_feedback, needs_review

📝 SUMMARY:
Customer provided $sentiment feedback. Manual review recommended to extract detailed insights and take appropriate action.

CONFIDENCE: 60%

Note: Using fallback analysis - Gemini API unavailable
        """.trimIndent()
    }
}