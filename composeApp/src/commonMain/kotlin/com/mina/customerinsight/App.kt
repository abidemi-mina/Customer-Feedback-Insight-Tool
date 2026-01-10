package com.mina.customerinsight

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import app.cash.sqldelight.db.SqlDriver
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun App(driverFactory: DatabaseDriverFactory) {
    val scope = rememberCoroutineScope()
    val database = remember {
        val driver = driverFactory.createDriver()
        FeedbackDB(driver)
    }
    val viewModel = remember {
        FeedbackViewModel(
            database = database,
            aiService = AIService(),
            scope = scope
        )
    }
    var currentTab by remember { mutableStateOf(0) }

    MaterialTheme {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentTab == 0,
                        onClick = { currentTab = 0 },
                        icon = { Text("✍️") },
                        label = { Text("Submit") }
                    )
                    NavigationBarItem(
                        selected = currentTab == 1,
                        onClick = { currentTab = 1 },
                        icon = { Text("📊") },
                        label = { Text("Admin") }
                    )
                }
            }
        ) { padding ->
            Box(Modifier.padding(padding)) {
                when (currentTab) {
                    0 -> UserSide(viewModel)
                    1 -> AdminSide(viewModel)
                }
            }
        }
    }
}

@Composable
fun UserSide(vm: FeedbackViewModel) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }
    var showValidationError by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    // Reset success message after 2 seconds
    LaunchedEffect(showSuccess) {
        if (showSuccess) {
            delay(2000)
            showSuccess = false
        }
    }

    Column(Modifier.padding(16.dp)) {
        Text("Give Us Feedback", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(16.dp))

        // Name field
        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                showValidationError = false
            },
            label = { Text("Your Name *") },
            modifier = Modifier.fillMaxWidth(),
            isError = showValidationError && name.isBlank()
        )

        Spacer(Modifier.height(12.dp))

        // Email field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email (optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        // Feedback field
        OutlinedTextField(
            value = text,
            onValueChange = {
                text = it
                showValidationError = false
            },
            label = { Text("Your Feedback *") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 5,
            maxLines = 10,
            isError = showValidationError && text.isBlank()
        )

        // Validation error message
        if (showValidationError) {
            Text(
                "Please provide both name and feedback",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Success message
        if (showSuccess) {
            Text(
                "✓ Feedback submitted successfully!",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        // Submit button
        Button(
            onClick = {
                if (name.isBlank() || text.isBlank()) {
                    showValidationError = true
                    showSuccess = false
                } else {
                    vm.submitFeedback(name, email, text)
                    showSuccess = true
                    showValidationError = false
                    name = ""
                    email = ""
                    text = ""
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !showSuccess
        ) {
            if (showSuccess) {
                // Use text icon instead
                Text("✓ ")
                Spacer(Modifier.width(8.dp))
                Text("Submitted!")
            } else {
                Text("Submit Feedback")
            }
        }
    }
}

@Composable
fun AdminSide(vm: FeedbackViewModel) {
    val feedbacks by vm.feedbacks.collectAsState(initial = emptyList())

    if (!vm.isAdminLoggedIn) {
        AdminLogin { vm.isAdminLoggedIn = true }
    } else {
        Column(modifier = Modifier.padding(16.dp)) {
            // Show error if any
            vm.analysisError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            LazyColumn {
                items(feedbacks) { item ->
                    FeedbackCard(item = item, viewModel = vm)
                }
            }
        }
    }
}

@Composable
fun FeedbackCard(item: FeedbackEntity, viewModel: FeedbackViewModel) {
    Card(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with name and time
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "From: ${item.senderName}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = formatTime(item.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Text(
                text = "Email: ${item.senderEmail}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Feedback content
            Text(
                text = item.content,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Analysis section
            when {
                item.isAnalyzed == true && !item.aiAnalysis.isNullOrEmpty() -> {
                    AIResultView(analysis = item.aiAnalysis!!)
                }
                viewModel.analyzingId == item.id -> {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Analyzing...")
                    }
                }
                else -> {
                    Button(
                        onClick = { viewModel.triggerAIAnalysis(item) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Analyze with Gemini")
                    }
                }
            }
        }
    }
}

// Helper function for formatting timestamp
private fun formatTime(timestamp: Long): String {
    return try {
        val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        dateFormat.format(Date(timestamp))
    } catch (e: Exception) {
        "Invalid time"
    }
}

@Composable
fun AdminLogin(onSuccess: () -> Unit) {
    var pass by remember { mutableStateOf("") }
    Column(Modifier.padding(32.dp)) {
        Text("Admin Login", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(
            value = pass,
            onValueChange = { pass = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )
        Button(
            onClick = { if (pass == "admin123") onSuccess() },
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
        ) {
            Text("Login")
        }
    }
}