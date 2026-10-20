package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.db.AppDatabase
import com.example.data.repository.AgentRepository
import com.example.engine.ModelRepository
import com.example.service.RealDeviceController
import com.example.ui.navigation.Screen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FeedSessionScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.ModelManagerScreen
import com.example.ui.screens.PersonalityScreen
import com.example.ui.screens.RealDeviceSetupScreen
import com.example.ui.screens.SessionReportScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.AiBreakTheme
import com.example.ui.viewmodel.FeedSessionViewModel
import com.example.ui.viewmodel.ModelManagerViewModel
import com.example.ui.viewmodel.PersonalityViewModel
import com.example.ui.viewmodel.ReportHistoryViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val agentRepository = AgentRepository(database.agentDao())
        val modelRepository = ModelRepository(applicationContext)
        val realDeviceController = RealDeviceController(applicationContext)

        setContent {
            AiBreakTheme {
                AiBreakApp(
                    agentRepository = agentRepository,
                    modelRepository = modelRepository,
                    realDeviceController = realDeviceController
                )
            }
        }
    }
}

@Composable
fun AiBreakApp(
    agentRepository: AgentRepository,
    modelRepository: ModelRepository,
    realDeviceController: RealDeviceController
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    val personalityViewModel = remember { PersonalityViewModel(agentRepository) }
    val modelManagerViewModel = remember { ModelManagerViewModel(modelRepository) }
    val reportHistoryViewModel = remember { ReportHistoryViewModel(agentRepository) }

    val currentProfile by personalityViewModel.selectedProfile.collectAsStateWithLifecycle()
    val currentModel by modelManagerViewModel.currentModel.collectAsStateWithLifecycle()
    val pastSessions by reportHistoryViewModel.allSessions.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = Screen.Welcome.route,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onEnterDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                profile = currentProfile,
                currentModel = currentModel,
                recentSessions = pastSessions,
                onStartBreak = { navController.navigate(Screen.FeedSession.route) },
                onChoosePersonality = { navController.navigate(Screen.PersonalityEditor.route) },
                onOpenModelManager = { navController.navigate(Screen.ModelManager.route) },
                onOpenRealDeviceSetup = { navController.navigate(Screen.RealDeviceSetup.route) },
                onOpenHistory = { navController.navigate(Screen.History.route) },
                onOpenSettings = { navController.navigate(Screen.Settings.route) },
                onSelectPastSession = { sessionId ->
                    navController.navigate(Screen.SessionReport.createRoute(sessionId))
                }
            )
        }

        composable(Screen.FeedSession.route) {
            val feedViewModel = remember {
                FeedSessionViewModel(agentRepository, modelRepository)
            }
            FeedSessionScreen(
                viewModel = feedViewModel,
                onSessionFinished = { sessionId ->
                    navController.navigate(Screen.SessionReport.createRoute(sessionId)) {
                        popUpTo(Screen.FeedSession.route) { inclusive = true }
                    }
                },
                onExitToDashboard = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.PersonalityEditor.route) {
            PersonalityScreen(
                viewModel = personalityViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ModelManager.route) {
            ModelManagerScreen(
                viewModel = modelManagerViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.SessionReport.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            SessionReportScreen(
                sessionId = sessionId,
                viewModel = reportHistoryViewModel,
                onBackToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                },
                onStartNewBreak = {
                    navController.navigate(Screen.FeedSession.route) {
                        popUpTo(Screen.Dashboard.route)
                    }
                }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                viewModel = reportHistoryViewModel,
                onSelectSession = { sessionId ->
                    navController.navigate(Screen.SessionReport.createRoute(sessionId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onOpenPersonality = { navController.navigate(Screen.PersonalityEditor.route) },
                onOpenModelManager = { navController.navigate(Screen.ModelManager.route) },
                onClearAllData = {
                    scope.launch {
                        agentRepository.resetAllData()
                    }
                }
            )
        }

        composable(Screen.RealDeviceSetup.route) {
            RealDeviceSetupScreen(
                controller = realDeviceController,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
