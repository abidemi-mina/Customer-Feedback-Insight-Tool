package com.mina.customerinsight.viewmodel

import androidx.compose.runtime.*
import com.mina.customerinsight.data.*
import com.mina.customerinsight.BusinessProfileEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val feedbackDBQueries = database.feedbackDBQueries

    // Feedback list
    val feedbacks: StateFlow<List<FeedbackEntity>> = feedbackDBQueries.selectAll()
        .asFlow()
        .mapToList(Dispatchers.IO)
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Authentication state
    var currentAdmin by mutableStateOf<com.mina.customerinsight.AdminEntity?>(null)
    var businessProfile by mutableStateOf<BusinessProfileEntity?>(null)

    // Login state as StateFlow for proper reactivity
    private val _isAdminLoggedIn = MutableStateFlow(false)
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    // Registration state
    var registrationStep by mutableStateOf(0)

    // UI State
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)

    var analyzingId by mutableStateOf<String?>(null)
    var analysisError by mutableStateOf<String?>(null)

    // Login function
    fun login(email: String, password: String) {
        scope.launch {
            println("=== LOGIN ATTEMPT ===")
            println("Email: $email")
            isLoading = true
            errorMessage = null
            successMessage = null

            try {
                val admin = authService.loginAdmin(email, password)
                println("Admin result: $admin")

                if (admin != null) {
                    currentAdmin = admin
                    _isAdminLoggedIn.value = true
                    println("Login successful! isAdminLoggedIn set to: ${_isAdminLoggedIn.value}")
                    successMessage = "Login successful!"

                    // Load business profile if exists
                    if (admin.businessProfileId != null) {
                        businessProfile = database.feedbackDBQueries
                            .getProfileById(admin.businessProfileId)
                            .executeAsOneOrNull()
                    }
                } else {
                    println("Login failed - admin is null")
                    errorMessage = "Invalid email or password"
                }
            } catch (e: Exception) {
                println("Login exception: ${e.message}")
                e.printStackTrace()
                errorMessage = "Login error: ${e.message}"
            } finally {
                isLoading = false
                println("=== LOGIN COMPLETE ===")
            }
        }
    }

    // Register function
    fun register(email: String, password: String, confirmPassword: String) {
        scope.launch {
            if (password != confirmPassword) {
                errorMessage = "Passwords don't match"
                return@launch
            }

            isLoading = true
            errorMessage = null
            successMessage = null

            val success = authService.registerAdmin(email, password)
            if (success) {
                successMessage = "Registration successful! Please login."
                registrationStep = 0
            } else {
                errorMessage = "Registration failed. Email might be taken."
            }
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

    // ==== KEEP THIS SIMPLE FUNCTION ====
    fun triggerAIAnalysis(feedback: FeedbackEntity) {
        scope.launch {
            analyzingId = feedback.id
            analysisError = null

            try {
                // Get business profile for context
                val businessName = businessProfile?.businessName
                val businessType = businessProfile?.businessType

                val result = aiService.analyzeFeedback(
                    feedback = feedback.content,
                    businessType = businessType,
                    businessName = businessName
                )

                feedbackDBQueries.updateAnalysis(
                    aiAnalysis = result,
                    id = feedback.id
                )
                successMessage = "Analysis complete!"
            } catch (e: Exception) {
                analysisError = "AI Analysis failed: ${e.message}"
                e.printStackTrace()
            } finally {
                analyzingId = null
            }
        }
    }    // ====================================

    fun saveBusinessProfile(
        name: String,
        type: String,
        description: String,
        categories: List<String>
    ) {
        scope.launch {
            currentAdmin?.let { admin ->
                isLoading = true
                val success = authService.saveBusinessProfile(
                    adminId = admin.id,
                    businessName = name,
                    businessType = type,
                    description = description,
                    categories = categories
                )

                if (success) {
                    businessProfile = database.feedbackDBQueries
                        .getProfileByAdminId(admin.id)
                        .executeAsOneOrNull()
                    successMessage = "Business profile saved!"
                } else {
                    errorMessage = "Failed to save profile"
                }
                isLoading = false
            }
        }
    }

    fun addReply(feedbackId: String, message: String) {
        scope.launch {
            currentAdmin?.let { admin ->
                val id = System.currentTimeMillis().toString()
                database.feedbackDBQueries.insertReply(
                    id = id,
                    feedbackId = feedbackId,
                    adminId = admin.id,
                    message = message,
                    timestamp = System.currentTimeMillis()
                )
                successMessage = "Reply added!"
            }
        }
    }

    // Logout
    fun logout() {
        currentAdmin = null
        businessProfile = null
        _isAdminLoggedIn.value = false
        successMessage = "Logged out successfully"
    }

    fun deleteFeedback(feedbackId: String) {
        scope.launch {
            try {
                feedbackDBQueries.deleteFeedback(feedbackId)
                successMessage = "Feedback deleted"
            } catch (e: Exception) {
                errorMessage = "Failed to delete: ${e.message}"
            }
        }
    }
}