package com.mina.customerinsight.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mina.customerinsight.AIResultView
import com.mina.customerinsight.FeedbackEntity
import com.mina.customerinsight.viewmodel.FeedbackViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FeedbackCard(
    feedback: FeedbackEntity,
    viewModel: FeedbackViewModel,
    showActions: Boolean = true,
    modifier: Modifier = Modifier
) {
    // Local state to control analysis visibility - CHANGED to false
    var showAnalysis by remember { mutableStateOf(false) } // <-- This is now false

    // Convert database Long (0/1) to Boolean
    val isAnalyzed = feedback.isAnalyzed != 0L
    val aiAnalysisText = feedback.aiAnalysis
    val hasAnalysis = !aiAnalysisText.isNullOrBlank()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with name and time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = feedback.senderName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = feedback.senderEmail ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = formatTime(feedback.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Feedback content
            Text(
                text = feedback.content,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Actions section - Show analyze button if no analysis exists
            if (showActions && !hasAnalysis) {
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { viewModel.triggerAIAnalysis(feedback) },
                        enabled = viewModel.analyzingId != feedback.id
                    ) {
                        if (viewModel.analyzingId == feedback.id) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Analyzing...")
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = "Analyze",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Analyze with AI")
                        }
                    }
                }
            }

            // Show analysis toggle and content
            if (hasAnalysis) {
                if (showAnalysis) {
                    AIResultView(
                        analysis = aiAnalysisText!!,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { showAnalysis = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Outlined.Close, contentDescription = "Close", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Hide Analysis")
                    }
                } else {
                    TextButton(
                        onClick = { showAnalysis = true },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Outlined.Analytics, contentDescription = "Show", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Show AI Analysis")
                    }
                }
            }
        }
    }
}

private fun formatTime(timestamp: Long): String {
    return try {
        val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        dateFormat.format(Date(timestamp))
    } catch (e: Exception) {
        "Invalid time"
    }
}

