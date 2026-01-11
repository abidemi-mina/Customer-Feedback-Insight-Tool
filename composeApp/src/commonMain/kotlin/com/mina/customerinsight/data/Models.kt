package com.mina.customerinsight.data

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

@Serializable
data class BusinessProfile(
    val id: String,
    val adminId: String,
    val businessName: String,
    val businessType: String, // e.g., "Restaurant", "SaaS", "Retail"
    val description: String,
    val categories: List<String>, // e.g., ["Food", "Service", "Ambiance"]
    val createdAt: Long
)

@Serializable
data class Admin(
    val id: String,
    val email: String,
    val hashedPassword: String,
    val businessProfileId: String? = null,
    val createdAt: Long
)

@Serializable
data class FeedbackReply(
    val id: String,
    val feedbackId: String,
    val adminId: String,
    val message: String,
    val timestamp: Long
)

@Serializable
data class EnhancedAnalysis(
    val sentiment: String, // "positive", "negative", "neutral"
    val score: Float, // 0.0 to 1.0
    val insights: List<String>,
    val recommendations: List<String>,
    val businessContext: String
)

@Serializable
data class AdminEntity(
    val id: String,
    val email: String,
    val hashedPassword: String,
    val businessProfileId: String?,
    val createdAt: Long
)

@Serializable
data class BusinessProfileEntity(
    val id: String,
    val adminId: String,
    val businessName: String,
    val businessType: String,
    val description: String?,
    val categories: String, // JSON string
    val createdAt: Long
)