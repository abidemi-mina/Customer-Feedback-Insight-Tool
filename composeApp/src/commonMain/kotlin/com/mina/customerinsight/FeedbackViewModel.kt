package com.mina.customerinsight

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import app.cash.sqldelight.coroutines.asFlow
import kotlin.time.Clock

class FeedbackViewModel(
    private val database: FeedbackDB,
    private val aiService: AIService,
    private val scope: CoroutineScope
) {
    // ADD THIS LINE - Get the queries from database
    private val queries = database.feedbackDBQueries

    // State properties
    val feedbacks: StateFlow<List<FeedbackEntity>> = queries.selectAll()
        .asFlow()
        .mapToList(Dispatchers.IO)
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    var isAdminLoggedIn by mutableStateOf(false)
    var analyzingId by mutableStateOf<String?>(null)
    var analysisError by mutableStateOf<String?>(null)

    // ADD THIS FUNCTION - Submit feedback to database
    fun submitFeedback(name: String, email: String, message: String) {
        if (name.isNotBlank() && message.isNotBlank()) {
            val id = Clock.System.now().toEpochMilliseconds().toString()
            val timestamp = Clock.System.now().toEpochMilliseconds()

            // Insert into database
            queries.insertFeedback(
                id = id,
                senderName = name,
                senderEmail = email,
                content = message,
                timestamp = timestamp
            )
        }
    }

    fun triggerAIAnalysis(feedback: FeedbackEntity) {
        scope.launch {
            analyzingId = feedback.id
            analysisError = null

            try {
                val result = aiService.analyzeFeedback(feedback.content)

                // Check for errors in result
                if (result.startsWith("Analysis Error") ||
                    result.startsWith("Blocked") ||
                    result.contains("API Error")) {
                    analysisError = "Analysis failed: $result"
                } else {
                    queries.updateAnalysis(
                        aiAnalysis = result,
                        id = feedback.id
                    )
                }
            } catch (e: Exception) {
                analysisError = "Network error: ${e.message}"
            } finally {
                analyzingId = null
            }
        }
    }
}