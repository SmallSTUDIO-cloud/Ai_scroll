package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Dashboard : Screen("dashboard")
    object FeedSession : Screen("feed_session")
    object PersonalityEditor : Screen("personality_editor")
    object ModelManager : Screen("model_manager")
    object SessionReport : Screen("session_report/{sessionId}") {
        fun createRoute(sessionId: String) = "session_report/$sessionId"
    }
    object History : Screen("history")
    object Settings : Screen("settings")
    object RealDeviceSetup : Screen("real_device_setup")
}
