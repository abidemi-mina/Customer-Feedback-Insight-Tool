package com.mina.customerinsight

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AIResultView(analysis: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        modifier = Modifier.padding(top = 8.dp).fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                "Gemini Analysis",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            Text(analysis, style = MaterialTheme.typography.bodyMedium)
        }
    }
}