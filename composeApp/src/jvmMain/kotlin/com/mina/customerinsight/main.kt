package com.mina.customerinsight

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.unit.dp

fun main() = application {
    val windowState = rememberWindowState(
        width = 1200.dp,
        height = 800.dp
    )

    val driverFactory = JvmDatabaseDriverFactory()

    Window(
        onCloseRequest = ::exitApplication,
        title = "Customer Insight",
        state = windowState
    ) {
        App(driverFactory)
    }
}