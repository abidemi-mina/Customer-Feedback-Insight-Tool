package com.mina.customerinsight.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
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
    val feedbacks by viewModel.feedbacks.collectAsState()  // Now this should work

    // Convert Long to Boolean for calculations
    val unanalyzedCount by remember(feedbacks) {
        derivedStateOf {
            feedbacks.count { it.isAnalyzed == 0L }  // 0L = false, 1L = true
        }
    }

    val repliedCount by remember(feedbacks) {
        derivedStateOf {
            feedbacks.count { it.hasReply == 1L }  // 1L = has reply
        }
    }

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
                        Text("Dashboard")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Outlined.Logout, "Logout")
                    }
                }
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
                        title = "Total Feedback",
                        value = feedbacks.size.toString(),
                        icon = Icons.Outlined.Message,
                        color = MaterialTheme.colorScheme.primary
                    )

                    StatCard(
                        title = "To Analyze",
                        value = unanalyzedCount.toString(),
                        icon = Icons.Outlined.Psychology,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    StatCard(
                        title = "Replied",
                        value = repliedCount.toString(),
                        icon = Icons.Outlined.Reply,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            // Recent Feedback
            item {
                Text(
                    "Recent Feedback",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            items(feedbacks.take(5)) { feedback ->
                FeedbackCard(
                    feedback = feedback,
                    viewModel = viewModel,
                    showActions = true
                )
            }

            // Quick Actions
            item {
                Text(
                    "Quick Actions",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                )

                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ActionButton(
                        text = "Analyze All",
                        icon = Icons.Outlined.AutoAwesome,
                        onClick = {
                            // Analyze unanalyzed feedbacks
                            feedbacks.filter { it.isAnalyzed == 0L }
                                .forEach { feedback ->
                                    viewModel.triggerAIAnalysis(feedback)
                                }
                        }
                    )

                    ActionButton(
                        text = "Export",
                        icon = Icons.Outlined.Download,
                        onClick = { /* Export data */ }
                    )

                    ActionButton(
                        text = "Settings",
                        icon = Icons.Outlined.Settings,
                        onClick = { /* Open settings */ }
                    )
                }
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