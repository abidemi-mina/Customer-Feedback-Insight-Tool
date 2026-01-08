package com.mina.customerinsight
import io.ktor.client.*


// This will automatically use OkHttp on Android and CIO on Desktop

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp

import kotlinx.coroutines.launch

@Composable
fun App() {
    val aiService = remember { AIService() }
    val scope = rememberCoroutineScope() // Needed for async calls

    var feedbackText by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("Results will appear here...") }
    var isLoading by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        TextField(
            value = feedbackText,
            onValueChange = { feedbackText = it },
            modifier = Modifier.fillMaxWidth().height(150.dp)
        )

        Button(
            onClick = {
                isLoading = true
                scope.launch {
                    val response = aiService.analyzeFeedback(feedbackText)
                    resultText = response
                    isLoading = false
                }
            },
            enabled = !isLoading && feedbackText.isNotBlank()
        ) {
            Text(if (isLoading) "Analyzing..." else "Analyze Feedback")
        }

        // Display the result
        SelectionContainer { // Allows user to copy text on Desktop/Mobile
            Text(resultText, modifier = Modifier.padding(top = 10.dp))
        }
    }
}