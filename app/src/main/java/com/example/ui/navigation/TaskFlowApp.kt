package com.example.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.TaskFlowViewModel
import com.example.ui.addtask.AddTaskScreen
import com.example.ui.alltasks.AllTasksScreen
import com.example.ui.components.BottomNavBar
import com.example.ui.home.HomeScreen
import com.example.ui.onboarding.OnboardingScreen
import com.example.ui.completed.CompletedTasksScreen
import com.example.ui.favorites.FavoritesTasksScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.taskdetail.TaskDetailScreen

@Composable
fun TaskFlowApp(
    viewModel: TaskFlowViewModel,
    startDestination: String,
    onOnboardingFinished: (String) -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute != "onboarding" && currentRoute != "add_task") {
                BottomNavBar(
                    currentRoute = currentRoute ?: "home",
                    onNavigateToHome = { navController.navigate("home") { launchSingleTop = true } },
                    onNavigateToCompleted = { navController.navigate("completed") { launchSingleTop = true } },
                    onNavigateToFavorites = { navController.navigate("favorites") { launchSingleTop = true } },
                    onNavigateToSettings = { navController.navigate("settings") { launchSingleTop = true } },
                    onNavigateToAdd = { navController.navigate("add_task") }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("onboarding") {
                OnboardingScreen(onFinished = { name ->
                    onOnboardingFinished(name)
                    navController.navigate("home") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                })
            }
            composable("home") {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToDetail = { taskId -> navController.navigate("task_detail/$taskId") },
                    onNavigateToAllTasks = { navController.navigate("all_tasks") }
                )
            }
            composable("all_tasks") {
                AllTasksScreen(
                    viewModel = viewModel,
                    onNavigateToDetail = { taskId -> navController.navigate("task_detail/$taskId") }
                )
            }
            composable("completed") {
                CompletedTasksScreen(viewModel = viewModel)
            }
            composable("favorites") {
                FavoritesTasksScreen(viewModel = viewModel)
            }
            composable("settings") {
                SettingsScreen(viewModel = viewModel)
            }
            composable("add_task") {
                AddTaskScreen(
                    onBack = { navController.popBackStack() },
                    onTaskAdded = { title, desc, subs, priority, colorIndex, notes ->
                        viewModel.addTask(title, desc, subs, priority, colorIndex, notes)
                        navController.popBackStack()
                    }
                )
            }
            composable(
                route = "task_detail/{taskId}",
                arguments = listOf(navArgument("taskId") { type = NavType.LongType })
            ) { backStackEntry ->
                val taskId = backStackEntry.arguments?.getLong("taskId") ?: 0L
                TaskDetailScreen(
                    taskId = taskId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
