package com.mina.customerinsight

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        val driverFactory = JvmDatabaseDriverFactory()
        App(driverFactory) // Passing the Desktop factory
    }
}