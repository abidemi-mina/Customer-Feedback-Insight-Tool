package com.mina.customerinsight

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.mina.customerinsight.data.AIService
import com.mina.customerinsight.data.AuthService
import com.mina.customerinsight.ui.screens.*
import com.mina.customerinsight.ui.theme.CustomerInsightTheme
import com.mina.customerinsight.viewmodel.FeedbackViewModel
import kotlinx.coroutines.delay


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(driverFactory: DatabaseDriverFactory) {

    LaunchedEffect(Unit) {
        delay(1000)
        println("=== APP STARTED ===")
        println("Package: com.mina.customerinsight")
        println("Time: ${System.currentTimeMillis()}")
    }

    val scope = rememberCoroutineScope()

    val database = remember {
        FeedbackDB(driverFactory.createDriver())
    }

    val aiService = remember { AIService() }
    val authService = remember { AuthService(database) }

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
    // REMOVE showAdminLogin state - we'll handle login dialog differently
    var showAdminLogin by remember { mutableStateOf(false) }

    // Observe the login state from ViewModel - THIS IS THE ONLY LOGIN STATE
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsState()

    // Debug logging
    LaunchedEffect(isAdminLoggedIn) {
        println("=== APP: LOGIN STATE CHANGED ===")
        println("isAdminLoggedIn: $isAdminLoggedIn")
        println("showOnboarding: $showOnboarding")
    }

    CustomerInsightTheme {
        println("=== COMPOSING APP ===")
        println("showOnboarding: $showOnboarding")
        println("isAdminLoggedIn: $isAdminLoggedIn")

        // Onboarding screen
        if (showOnboarding) {
            println(">>> SHOWING ONBOARDING")
            OnboardingScreen(
                viewModel = viewModel,
                onComplete = {
                    println(">>> ONBOARDING COMPLETE")
                    showOnboarding = false
                }
            )
        } else {
            // Main decision point: Show either UserFeedbackScreen or AdminDashboard
            when {
                isAdminLoggedIn -> {
                    println(">>> SHOWING ADMIN DASHBOARD")
                    // Main app with admin dashboard
                    Scaffold(
                    ) { padding ->
                        Box(Modifier.padding(padding)) {
                            AdminDashboard(
                                viewModel = viewModel,
                                onLogout = {
                                    viewModel.logout()
                                    // No need to reset showAdminLogin - it's controlled by click
                                }
                            )
                        }
                    }
                }
                else -> {
                    println(">>> SHOWING USER FEEDBACK SCREEN")
                    // Show UserFeedbackScreen with optional login dialog
                    var showLoginDialog by remember { mutableStateOf(false) }

                    UserFeedbackScreen(
                        viewModel = viewModel,
                        onAdminLoginClick = {
                            println(">>> ADMIN LOGIN CLICKED")
                            showLoginDialog = true
                        }
                    )

                    // Show admin login dialog when requested
                    if (showLoginDialog) {
                        AdminLoginDialog(
                            viewModel = viewModel,
                            onLoginSuccess = {
                                println(">>> LOGIN SUCCESS CALLBACK")
                                showLoginDialog = false
                                // Login successful - app will automatically switch to admin dashboard
                                // because isAdminLoggedIn will become true
                            },
                            onDismiss = {
                                println(">>> LOGIN DISMISSED")
                                showLoginDialog = false
                                // User cancelled login - just close the dialog
                                // No state changes needed
                            }
                        )
                    }
                }
            }
        }

        // Show loading indicator
        if (viewModel.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        // Show error/success messages
        LaunchedEffect(viewModel.errorMessage, viewModel.successMessage) {
            if (viewModel.errorMessage != null || viewModel.successMessage != null) {
                delay(3000)
                viewModel.errorMessage = null
                viewModel.successMessage = null
            }
        }
    }
}

@Composable
fun AdminLoginDialog(
    viewModel: FeedbackViewModel,
    onLoginSuccess: () -> Unit,
    onDismiss: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showRegister by remember { mutableStateOf(false) }
    var confirmPassword by remember { mutableStateOf("") }
    var businessName by remember { mutableStateOf("") }
    var businessType by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }


    LaunchedEffect(viewModel.successMessage) {
        if (viewModel.successMessage != null && showRegister) {
            // Registration successful! Wait a brief moment then show login
            delay(1500)
            showRegister = false // This hides the registration form
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (showRegister) "Create Admin Account" else "Admin Login") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                // Show error message
                viewModel.errorMessage?.let { message ->
                    Text(
                        message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Show success message
                viewModel.successMessage?.let { message ->
                    Text(
                        message,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation()
                )

                if (showRegister) {
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Business Profile Setup",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = businessName,
                        onValueChange = { businessName = it },
                        label = { Text("Business Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = businessType,
                        onValueChange = { businessType = it },
                        label = { Text("Business Type") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            }
        },
        confirmButton = {
            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Button(
                    onClick = {
                        if (showRegister) {
                            if (password != confirmPassword) {
                                viewModel.errorMessage = "Passwords don't match"
                                return@Button
                            }
                            if (businessName.isBlank() || businessType.isBlank()) {
                                viewModel.errorMessage = "Business info required"
                                return@Button
                            }
                            viewModel.register(email, password, confirmPassword)
                            // After registration, save business profile
                            viewModel.saveBusinessProfile(
                                name = businessName,
                                type = businessType,
                                description = description,
                                categories = emptyList()
                            )
                        } else {
                            viewModel.login(email, password)
                        }
                    }
                ) {
                    Text(if (showRegister) "Register" else "Login")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { showRegister = !showRegister }) {
                Text(if (showRegister) "Already have account? Login" else "Need account? Register")
            }
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

