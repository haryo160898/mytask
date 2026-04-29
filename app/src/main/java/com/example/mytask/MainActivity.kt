package com.example.mytask

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mytask.data.local.AppDatabase
import com.example.mytask.data.repository.TaskRepository
import com.example.mytask.data.repository.UserRepository
import com.example.mytask.ui.screens.*
import com.example.mytask.ui.theme.MyTaskTheme
import com.example.mytask.ui.viewmodel.AuthViewModel
import com.example.mytask.ui.viewmodel.TaskViewModel

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val database = AppDatabase.getDatabase(this)
        val taskRepository = TaskRepository(database.taskDao(), database.statisticsDao(), database.settingsDao())
        val userRepository = UserRepository(database.userDao(), database.settingsDao(), applicationContext)
        
        enableEdgeToEdge()
        setContent {
            MyTaskTheme {
                val context = LocalContext.current
                val navController = rememberNavController()

                // Request notification permission for Android 13+
                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { isGranted ->
                        // Handle result if needed
                    }
                )

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }
                
                val authViewModel: AuthViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return AuthViewModel(userRepository) as T
                        }
                    }
                )
                val taskViewModel: TaskViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return TaskViewModel(taskRepository, userRepository, context.applicationContext) as T
                        }
                    }
                )

                NavHost(navController = navController, startDestination = if (userRepository.getSessionUserId() != -1L) "dashboard" else "login") {
                    composable("login") {
                        LoginScreen(
                            viewModel = authViewModel,
                            onLoginSuccess = {
                                navController.navigate("dashboard") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onSignUpClick = {
                                navController.navigate("register")
                            }
                        )
                    }
                    composable("register") {
                        RegisterScreen(
                            viewModel = authViewModel,
                            onBack = { navController.popBackStack() },
                            onRegisterSuccess = {
                                navController.navigate("dashboard") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("dashboard") {
                        DashboardScreen(
                            viewModel = taskViewModel,
                            authViewModel = authViewModel,
                            onAddTaskClick = {
                                navController.navigate("add_task")
                            },
                            onProfileClick = {
                                navController.navigate("profile")
                            },
                            onNotificationClick = {
                                navController.navigate("notifications")
                            },
                            onTaskClick = { taskId ->
                                navController.navigate("task_detail/$taskId")
                            }
                        )
                    }
                    composable("add_task") {
                        AddTaskScreen(
                            viewModel = taskViewModel,
                            authViewModel = authViewModel,
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                    composable("edit_task/{taskId}") { backStackEntry ->
                        val taskId = backStackEntry.arguments?.getString("taskId")?.toLongOrNull()
                        if (taskId != null) {
                            EditTaskScreen(
                                taskId = taskId,
                                viewModel = taskViewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                    composable("profile") {
                        ProfileScreen(
                            authViewModel = authViewModel,
                            taskViewModel = taskViewModel,
                            onBack = { navController.popBackStack() },
                            onEditProfile = { navController.navigate("edit_profile") },
                            onNotificationSettings = { navController.navigate("notification_settings") },
                            onSecurity = { navController.navigate("security") },
                            onLogout = {
                                authViewModel.logout()
                                navController.navigate("login") {
                                    popUpTo(0)
                                }
                            }
                        )
                    }
                    composable("edit_profile") {
                        EditProfileScreen(
                            authViewModel = authViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("notifications") {
                        NotificationScreen(
                            taskViewModel = taskViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("notification_settings") {
                        NotificationSettingsScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("security") {
                        SecurityScreen(
                            authViewModel = authViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("task_detail/{taskId}") { backStackEntry ->
                        val taskId = backStackEntry.arguments?.getString("taskId")?.toLongOrNull()
                        if (taskId != null) {
                            TaskDetailScreen(
                                taskId = taskId,
                                viewModel = taskViewModel,
                                onBack = { navController.popBackStack() },
                                onEditTask = { id ->
                                    navController.navigate("edit_task/$id")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
