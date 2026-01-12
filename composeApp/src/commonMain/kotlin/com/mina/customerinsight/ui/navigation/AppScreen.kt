package com.mina.customerinsight.ui.navigation


sealed class AppScreen {
    object Onboarding : AppScreen()
    object UserFeedback : AppScreen()
    object Auth : AppScreen()
    object AdminDashboard : AppScreen()
    object Analytics : AppScreen()
    object Settings : AppScreen()
}