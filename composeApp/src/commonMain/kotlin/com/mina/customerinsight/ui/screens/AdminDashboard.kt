package com.mina.customerinsight.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mina.customerinsight.ui.component.ActionButton
import com.mina.customerinsight.ui.component.FeedbackCard
import com.mina.customerinsight.viewmodel.FeedbackViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(
    viewModel: FeedbackViewModel,
    onLogout: () -> Unit
) {
    val feedbacks by viewModel.feedbacks.collectAsState()
    var showAllFeedbacks by remember { mutableStateOf(false) } // Navigation state

    val displayedFeedbacks = if (showAllFeedbacks) {
        feedbacks // Show all feedbacks
    } else {
        feedbacks.take(5) // Show only recent 5
    }

    val analyzedCount = feedbacks.count { !it.aiAnalysis.isNullOrBlank() }
    val pendingCount = feedbacks.count { it.aiAnalysis.isNullOrBlank() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Dashboard,
                            contentDescription = "Dashboard"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Admin Dashboard")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Outlined.Logout, "Logout")
                    }
                }
            )
        },
        bottomBar = {
            // Footer with navigation
            AdminFooter(
                showAllFeedbacks = showAllFeedbacks,
                totalCount = feedbacks.size,
                analyzedCount = analyzedCount,
                onToggleView = { showAllFeedbacks = !showAllFeedbacks }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Stats Cards
            item {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatCard(
                        title = "Total",
                        value = feedbacks.size.toString(),
                        icon = Icons.AutoMirrored.Outlined.Message,
                        color = MaterialTheme.colorScheme.primary
                    )

                    StatCard(
                        title = "Analyzed",
                        value = analyzedCount.toString(),
                        icon = Icons.Outlined.CheckCircle,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    StatCard(
                        title = "Pending",
                        value = pendingCount.toString(),
                        icon = Icons.Outlined.Psychology,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            // Section Title
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (showAllFeedbacks) "All Feedbacks" else "Recent Feedbacks",
                        style = MaterialTheme.typography.titleLarge
                    )

                    // View Toggle Button
                    FilterChip(
                        selected = showAllFeedbacks,
                        onClick = { showAllFeedbacks = !showAllFeedbacks },
                        label = {
                            Text(
                                if (showAllFeedbacks) "Show Recent" else "Show All",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        leadingIcon = if (showAllFeedbacks) {
                            { Icon(Icons.Outlined.List, null, Modifier.size(16.dp)) }
                        } else {
                            { Icon(Icons.Outlined.Schedule, null, Modifier.size(16.dp)) }
                        }
                    )
                }
            }

            // Feedback List
            items(displayedFeedbacks) { feedback ->
                FeedbackCard(
                    feedback = feedback,
                    viewModel = viewModel,
                    showActions = true
                )
            }

            // Show message if no feedbacks
            if (displayedFeedbacks.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Outlined.Feedback,
                            contentDescription = "No feedback",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No feedback yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Customers will appear here when they submit feedback",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Quick Actions (only in recent view)
            if (!showAllFeedbacks) {
                item {
                    Column {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Quick Actions",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )

                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            ActionButton(
                                text = "Analyze All",
                                icon = Icons.Outlined.AutoAwesome,
                                onClick = {
                                    feedbacks.filter { it.aiAnalysis.isNullOrBlank() }
                                        .forEach { feedback ->
                                            viewModel.triggerAIAnalysis(feedback)
                                        }
                                }
                            )

                            ActionButton(
                                text = "View All",
                                icon = Icons.Outlined.List,
                                onClick = { showAllFeedbacks = true }
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

// Footer Component
@Composable
fun AdminFooter(
    showAllFeedbacks: Boolean,
    totalCount: Int,
    analyzedCount: Int,
    onToggleView: () -> Unit
) {
    Surface(
        tonalElevation = 8.dp,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Stats
            Column {
                Text(
                    "${analyzedCount}/${totalCount} Analyzed",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LinearProgressIndicator(
                    progress = if (totalCount > 0) analyzedCount.toFloat() / totalCount.toFloat() else 0f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Toggle Button
            FilledTonalButton(
                onClick = onToggleView,
                modifier = Modifier.height(36.dp)
            ) {
                Icon(
                    if (showAllFeedbacks) Icons.Outlined.Schedule else Icons.Outlined.List,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (showAllFeedbacks) "Recent" else "All",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, icon: ImageVector, color: Color) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .height(120.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(title, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}