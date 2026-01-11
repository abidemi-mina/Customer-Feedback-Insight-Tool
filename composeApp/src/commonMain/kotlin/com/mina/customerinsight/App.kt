package com.mina.customerinsight
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mina.customerinsight.data.AIService
import com.mina.customerinsight.data.AuthService
import com.mina.customerinsight.ui.screens.*
import com.mina.customerinsight.ui.theme.CustomerInsightTheme
import com.mina.customerinsight.viewmodel.FeedbackViewModel
import app.cash.sqldelight.ColumnAdapter
import kotlinx.coroutines.delay  // For delay()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(driverFactory: DatabaseDriverFactory) {
    val scope = rememberCoroutineScope()

    val database = remember {
        FeedbackDB(driverFactory.createDriver())
        // NO adapter parameter!
    }

    // Create services
    val aiService = remember { AIService() }
    val authService = remember { AuthService(database) }

    // Create view model
    val viewModel = remember {
        FeedbackViewModel(
            database = database,
            aiService = aiService,
            authService = authService,
            scope = scope
        )
    }

    // App state
    var showOnboarding by remember { mutableStateOf(true) }
    var isAdminLoggedIn by remember { mutableStateOf(false) }
    var currentTab by remember { mutableStateOf(0) }

    CustomerInsightTheme {
        // Onboarding screen
        if (showOnboarding) {
            OnboardingScreen(
                viewModel = viewModel,
                onComplete = { showOnboarding = false }
            )
        } else {
            // Check authentication
            if (!isAdminLoggedIn) {
                AuthFlow(
                    viewModel = viewModel,
                    onLoginSuccess = { isAdminLoggedIn = true }
                )
            } else {
                // Main app with admin dashboard
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Customer Insight Pro") },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            actions = {
                                IconButton(
                                    onClick = {
                                        viewModel.logout()
                                        isAdminLoggedIn = false
                                    }
                                ) {
                                    Icon(Icons.Outlined.Logout, "Logout")
                                }
                            }
                        )
                    },
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
                            0 -> UserFeedbackScreen(viewModel)
                            1 -> AdminDashboard(
                                viewModel = viewModel,
                                onLogout = {
                                    viewModel.logout()
                                    isAdminLoggedIn = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Simple UserFeedbackScreen if you haven't created it yet
@Composable
fun UserFeedbackScreen(viewModel: FeedbackViewModel) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf("") }
    var showSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(3000)
        showSuccess = false
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Submit Feedback",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Your Name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Your Email (optional)") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = feedback,
            onValueChange = { feedback = it },
            label = { Text("Your Feedback") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            minLines = 5
        )

        if (showSuccess) {
            Text(
                "✓ Thank you for your feedback!",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Button(onClick = {
                if (name.isNotBlank() && feedback.isNotBlank()) {
                    viewModel.submitFeedback(name, email, feedback)
                    showSuccess = true
                    name = ""
                    email = ""
                    feedback = ""

                }
            }, modifier = Modifier.fillMaxWidth(), enabled = name.isNotBlank() && feedback.isNotBlank()) {
            Text("Submit Feedback")
        }
    }
}

//// If you have UserSide function, rename it or keep it
//@Composable
//fun UserSide(viewModel: FeedbackViewModel) {
//    UserFeedbackScreen(viewModel)
//}
//
//// If you have AdminSide function
//@Composable
//fun AdminSide(viewModel: FeedbackViewModel) {
//    AdminDashboard(
//        viewModel = viewModel,
//        onLogout = { /* handled in App */ }
//    )
//}