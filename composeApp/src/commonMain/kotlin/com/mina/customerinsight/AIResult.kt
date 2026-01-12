package com.mina.customerinsight

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mina.customerinsight.data.AIAnalysisResult
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

@Composable
fun AIResultView(
    analysis: AIAnalysisResult,
    modifier: Modifier = Modifier,
    showFullDetails: Boolean = true
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.05f),
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header with sentiment
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = getSentimentIcon(analysis.sentiment),
                        contentDescription = "Sentiment",
                        tint = getSentimentColor(analysis.sentiment),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Gemini AI Analysis",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Confidence badge
                Badge(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                ) {
                    Text(
                        "${(analysis.confidence * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sentiment and urgency summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sentiment with score
                AnalysisMetric(
                    label = "Sentiment",
                    value = analysis.sentiment,
                    score = analysis.sentimentScore,
                    icon = Icons.Outlined.Mood,
                    color = getSentimentColor(analysis.sentiment)
                )

                // Urgency level
                AnalysisMetric(
                    label = "Urgency",
                    value = analysis.urgencyLevel,
                    score = when (analysis.urgencyLevel) {
                        "High" -> 10
                        "Medium" -> 5
                        else -> 0
                    },
                    icon = when (analysis.urgencyLevel) {
                        "High" -> Icons.Outlined.PriorityHigh
                        "Medium" -> Icons.Outlined.Schedule
                        else -> Icons.Outlined.LowPriority
                    },
                    color = getUrgencyColor(analysis.urgencyLevel)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (showFullDetails) {
                // Key Issues Section
                if (analysis.keyIssues.isNotEmpty()) {
                    Text(
                        "Key Issues Identified",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        analysis.keyIssues.forEachIndexed { index, issue ->
                            KeyIssueItem(issue = issue, index = index + 1)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Recommendations Section
                if (analysis.recommendations.isNotEmpty()) {
                    Text(
                        "Recommendations",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        analysis.recommendations.forEachIndexed { index, recommendation ->
                            RecommendationItem(recommendation = recommendation, index = index + 1)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Category Tags
                if (analysis.categoryTags.isNotEmpty()) {
                    Text(
                        "Categories",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        analysis.categoryTags.forEach { tag ->
                            FilterChip(
                                selected = false,
                                onClick = {
                                    // Add functionality: Filter by category, show snackbar, etc.
                                    // Example: onCategorySelected?.invoke(tag)
                                },
                                label = {
                                    Text(
                                        tag.replace("_", " ").replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                ),
                                shape = MaterialTheme.shapes.small,
                                modifier = Modifier.height(28.dp)
                            )                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Summary (always shown)
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                ),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "Summary",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        analysis.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun AnalysisMetric(
    label: String,
    value: String,
    score: Int,
    icon: ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon with background
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f))
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            value,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (score != 0) {
            Text(
                "(${if (score > 0) "+" else ""}$score)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun KeyIssueItem(issue: String, index: Int) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Number badge
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.errorContainer)
        ) {
            Text(
                index.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            issue,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun RecommendationItem(recommendation: String, index: Int) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(0.dp),
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            // Check icon with number
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Check,
                        contentDescription = "Recommendation",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                recommendation,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// Helper functions
@Composable
private fun getSentimentIcon(sentiment: String): ImageVector {
    return when (sentiment.lowercase()) {
        "positive" -> Icons.Outlined.SentimentVerySatisfied
        "negative" -> Icons.Outlined.SentimentVeryDissatisfied
        else -> Icons.Outlined.SentimentNeutral
    }
}

@Composable
private fun getSentimentColor(sentiment: String): Color {
    return when (sentiment.lowercase()) {
        "positive" -> MaterialTheme.colorScheme.primary
        "negative" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

@Composable
private fun getUrgencyColor(urgency: String): Color {
    return when (urgency.lowercase()) {
        "high" -> MaterialTheme.colorScheme.error
        "medium" -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

// Backward compatibility version for string analysis
@Composable
fun AIResultView(
    analysis: String,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        modifier = modifier.padding(top = 8.dp).fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                "Gemini Analysis",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                analysis,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Function to parse analysis string
fun parseAnalysisString(analysisString: String): AIAnalysisResult? {
    return try {
        // Try to parse as JSON
        val json = Json.parseToJsonElement(analysisString)

        // Check if it's a JSON object with the expected structure
        if (json.jsonObject.containsKey("sentiment")) {
            Json.decodeFromString<AIAnalysisResult>(analysisString)
        } else {
            // Check if it's the formatted text version
            extractAnalysisFromText(analysisString)
        }
    } catch (e: Exception) {
        null
    }
}

private fun extractAnalysisFromText(text: String): AIAnalysisResult {
    val lowerText = text.lowercase()

    // Simple sentiment detection
    val sentiment = when {
        lowerText.contains("positive") -> "Positive"
        lowerText.contains("negative") -> "Negative"
        else -> "Neutral"
    }

    val sentimentScore = when (sentiment) {
        "Positive" -> 7
        "Negative" -> -5
        else -> 0
    }

    // Simple urgency based on keywords
    val urgency = when {
        lowerText.contains("urgent") || lowerText.contains("high") -> "High"
        lowerText.contains("medium") -> "Medium"
        else -> "Low"
    }

    return AIAnalysisResult(
        sentiment = sentiment,
        keyIssues = listOf("Feedback analysis"),
        urgencyLevel = urgency,
        recommendations = listOf("Review feedback for details"),
        categoryTags = listOf("feedback"),
        summary = "AI analysis: $sentiment sentiment detected",
        sentimentScore = sentimentScore,
        confidence = 0.8
    )
}
// Helper extraction functions
private fun extractFromLine(
    lines: List<String>,
    prefix: String,
    vararg possibleValues: String
): String {
    val line = lines.firstOrNull { it.contains(prefix) } ?: return "Neutral"
    val value = line.substringAfter(prefix).trim()
    return possibleValues.firstOrNull { value.contains(it, ignoreCase = true) } ?: "Neutral"
}

private fun extractScore(lines: List<String>): Int {
    val line = lines.firstOrNull { it.contains("Score:") } ?: return 0
    val scoreText = line.substringAfter("Score:").substringBefore("/").trim()
    return try {
        scoreText.toInt()
    } catch (e: Exception) {
        0
    }
}

private fun extractConfidence(lines: List<String>): Double {
    val line = lines.firstOrNull { it.contains("CONFIDENCE:") } ?: return 0.0
    val confidenceText = line.substringAfter("CONFIDENCE:").substringBefore("%").trim()
    return try {
        confidenceText.toDouble() / 100
    } catch (e: Exception) {
        0.0
    }
}

private fun extractSection(lines: List<String>, startMarker: String, endMarker: String?): List<String> {
    val startIndex = lines.indexOfFirst { it.contains(startMarker) }
    if (startIndex == -1) return emptyList()

    val endIndex = if (endMarker != null) {
        lines.indexOfFirst { it.contains(endMarker) && lines.indexOf(it) > startIndex }
    } else {
        lines.size
    }

    val sectionEnd = if (endIndex > startIndex) endIndex else lines.size

    return lines.subList(startIndex + 1, sectionEnd)
        .filter { it.isNotBlank() && !it.contains("══") } // Filter out separator lines
        .map { it.replace("•", "").replace("✓", "").trim() }
        .filter { it.isNotBlank() }
}

private fun extractCategories(lines: List<String>): List<String> {
    val line = lines.firstOrNull { it.contains("CATEGORIES:") } ?: return emptyList()
    val categoriesText = line.substringAfter("CATEGORIES:").trim()
    return categoriesText.split(",").map { it.trim() }.filter { it.isNotBlank() }
}

// Smart function that handles both String and AIAnalysisResult
@Composable
fun SmartAIResultView(
    analysisData: Any, // Can be String or AIAnalysisResult
    modifier: Modifier = Modifier,
    showFullDetails: Boolean = true
) {
    when (analysisData) {
        is AIAnalysisResult -> {
            AIResultView(
                analysis = analysisData,
                modifier = modifier,
                showFullDetails = showFullDetails
            )
        }
        is String -> {
            val parsedResult = parseAnalysisString(analysisData)

            if (parsedResult != null) {
                AIResultView(
                    analysis = parsedResult,
                    modifier = modifier,
                    showFullDetails = showFullDetails
                )
            } else {
                AIResultView(
                    analysis = analysisData,
                    modifier = modifier
                )
            }
        }
        else -> {
            // Fallback
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                ),
                modifier = modifier
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Outlined.Error,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Unable to display analysis",
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}