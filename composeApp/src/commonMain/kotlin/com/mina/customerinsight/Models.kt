package com.mina.customerinsight


import kotlinx.serialization.Serializable
import kotlin.time.Clock

@Serializable
data class UserFeedback(
    val id: String = Clock.System.now().toEpochMilliseconds().toString(),
    val senderName: String,
    val senderEmail: String,
    val content: String,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds(),
    var aiAnalysis: String? = null,
    var isAnalyzed: Boolean = false
)

@Serializable
data class GeminiRequest(
    val contents: List<Content>,
    val safetySettings: List<SafetySetting>
)

@Serializable
data class Content(val parts: List<Part>)

@Serializable
data class Part(val text: String)


@Serializable
data class SafetySetting(
    val category: String,
    val threshold: String
)

@Serializable
data class SafetyRating(
    val category: String,
    val probability: String
)
@Serializable
data class GeminiResponse(
    val candidates: List<Candidate>? = emptyList(), // Default to empty list
    val promptFeedback: PromptFeedback? = null
)

@Serializable
data class Candidate(
    val content: Content? = null,
    val finishReason: String? = null
)

@Serializable
data class PromptFeedback(
    val blockReason: String? = null
)