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
    @Suppress("ModifierParameter") modifier: Modifier = Modifier
) {
    var showAnalysis by remember { mutableStateOf(false) }

    val aiAnalysisText = feedback.aiAnalysis
    val hasAnalysis = !aiAnalysisText.isNullOrBlank()
    val isAnalyzing = viewModel.analyzingId == feedback.id

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // ... Header and Content remain the same ...
            Text(text = feedback.senderName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = feedback.content, style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(12.dp))

            // COMBINED ACTIONS ROW
            if (showActions) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left side: Show/Hide toggle (only if analysis exists)
                    if (hasAnalysis) {
                        TextButton(onClick = { showAnalysis = !showAnalysis }) {
                            Icon(
                                imageVector = if (showAnalysis) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (showAnalysis) "Hide Analysis" else "View Analysis")
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    // Right side: Analyze / Reanalyze Button
                    Button(
                        onClick = {
                            // This will overwrite the old analysis in the DB
                            viewModel.triggerAIAnalysis(feedback)
                        },
                        enabled = !isAnalyzing,
                        colors = if (hasAnalysis) {
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        } else {
                            ButtonDefaults.buttonColors()
                        }
                    ) {
                        if (isAnalyzing) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(
                                imageVector = if (hasAnalysis) Icons.Outlined.Refresh else Icons.Outlined.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (hasAnalysis) "Reanalyze" else "Analyze")
                        }
                    }
                }
            }

            // Expanded Analysis View
            if (hasAnalysis && showAnalysis) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                AIResultView(
                    analysis = aiAnalysisText,
                    modifier = Modifier.fillMaxWidth()
                )
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

