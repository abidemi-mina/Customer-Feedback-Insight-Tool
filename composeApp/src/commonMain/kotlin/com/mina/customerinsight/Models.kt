package com.mina.customerinsight

import kotlinx.serialization.Serializable

@Serializable
data class GeminiRequest(
    val contents: List<Content>,
    val safetySettings: List<SafetySetting>
)

@Serializable
data class Content(
    val parts: List<Part>
)

@Serializable
data class Part(
    val text: String
)


@Serializable
data class SafetySetting(
    val category: String,
    val threshold: String
)
@Serializable
data class GeminiResponse(
    val candidates: List<Candidate>? = null,
    val promptFeedback: PromptFeedback? = null // Capture blocks at the input level
)

@Serializable
data class Candidate(
    val content: Content? = null,
    val finishReason: String? = null, // STOP, SAFETY, OTHER, etc.
    val safetyRatings: List<SafetyRating>? = null
)

@Serializable
data class PromptFeedback(
    val blockReason: String? = null // SAFETY, OTHER, etc.
)

@Serializable
data class SafetyRating(
    val category: String,
    val probability: String
)