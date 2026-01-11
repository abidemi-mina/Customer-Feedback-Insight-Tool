package com.mina.customerinsight.viewmodel

import androidx.compose.runtime.*
import com.mina.customerinsight.data.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.mina.customerinsight.FeedbackDB
import com.mina.customerinsight.FeedbackEntity

class FeedbackViewModel(
    private val database: FeedbackDB,
    private val aiService: AIService,
    private val authService: AuthService,
    private val scope: CoroutineScope
) {
    // Use feedbackDBQueries for ALL queries
    private val feedbackDBQueries = database.feedbackDBQueries

    // Feedback list
    val feedbacks: StateFlow<List<FeedbackEntity>> = feedbackDBQueries.selectAll()
        .asFlow()
        .mapToList(Dispatchers.IO)
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Authentication state
    var currentAdmin by mutableStateOf<AdminEntity?>(null)
    var businessProfile by mutableStateOf<BusinessProfileEntity?>(null)

    // Registration state
    var registrationStep by mutableStateOf(0) // 0: login, 1: register, 2: setup profile

    // UI State
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)

    // Keep existing analyzing state
    var analyzingId by mutableStateOf<String?>(null)
    var analysisError by mutableStateOf<String?>(null)

    // Login function - SIMPLIFIED FOR NOW
    fun login(email: String, password: String) {
        scope.launch {
            isLoading = true
            errorMessage = null

            // Temporary: Always succeed for demo
            successMessage = "Login successful (demo mode)"

            // Real implementation (uncomment later):
            // val admin = authService.loginAdmin(email, password)
            // if (admin != null) {
            //     currentAdmin = admin
            //     successMessage = "Welcome back!"
            // } else {
            //     errorMessage = "Invalid credentials"
            // }

            isLoading = false
        }
    }

    // Register function - SIMPLIFIED FOR NOW
    fun register(email: String, password: String, confirmPassword: String) {
        scope.launch {
            if (password != confirmPassword) {
                errorMessage = "Passwords don't match"
                return@launch
            }

            isLoading = true

            // Temporary: Always succeed for demo
            successMessage = "Account created! You can now login."
            registrationStep = 0

            // Real implementation (uncomment later):
            // val success = authService.registerAdmin(email, password)
            // if (success) {
            //     registrationStep = 2
            //     successMessage = "Account created! Now set up your business profile."
            // } else {
            //     errorMessage = "Registration failed. Email might be taken."
            // }

            isLoading = false
        }
    }

    // Submit feedback function
    fun submitFeedback(name: String, email: String, message: String) {
        if (name.isNotBlank() && message.isNotBlank()) {
            val id = System.currentTimeMillis().toString()
            feedbackDBQueries.insertFeedback(
                id = id,
                senderName = name,
                senderEmail = email,
                content = message,
                timestamp = System.currentTimeMillis()
            )
        }
    }

    fun triggerAIAnalysis(feedback: FeedbackEntity) {
        scope.launch {
            analyzingId = feedback.id
            analysisError = null

            try {
                val result = aiService.analyzeFeedback(feedback.content)
                feedbackDBQueries.updateAnalysis(
                    aiAnalysis = result,
                    id = feedback.id
                )
            } catch (e: Exception) {
                analysisError = "AI Analysis failed: ${e.message}"
            } finally {
                analyzingId = null
            }
        }
    }

    // Add reply to feedback
    fun addReply(feedbackId: String, message: String) {
        scope.launch {
            // currentAdmin?.let { admin ->  // Comment out for now
            val id = System.currentTimeMillis().toString()
            // TODO: Add reply queries when you create them
            // feedbackDBQueries.insertReply(...)
            feedbackDBQueries.markAsReplied(feedbackId)
            // }
        }
    }

    // Logout
    fun logout() {
        currentAdmin = null
        businessProfile = null
        successMessage = "Logged out successfully"
    }
}