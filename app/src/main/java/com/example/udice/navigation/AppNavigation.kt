package com.example.udice.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.udice.ui.screens.FeedScreen
import com.example.udice.ui.screens.ForgotPasswordScreen
import com.example.udice.ui.screens.LoginScreen
import com.example.udice.ui.screens.NewPostScreen
import com.example.udice.ui.screens.PreferencesScreen
import com.example.udice.ui.screens.RegisterScreen

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        // Pantalla de Inicio de Sesión
        composable(route = Screen.Login.route) {
            LoginScreen(
                onLoginAsReader = {
                    navController.navigate(Screen.Feed.createRoute("reader")) {
                        popUpTo(Screen.Login.route) { inclusive = false }
                    }
                },
                onLoginAsPublisher = {
                    navController.navigate(Screen.Feed.createRoute("publisher")) {
                        popUpTo(Screen.Login.route) { inclusive = false }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                }
            )
        }

        // Pantalla de Registro
        composable(route = Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.popBackStack()
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // Pantalla de Recuperar Contraseña
        composable(route = Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSubmitSuccess = {
                    navController.popBackStack(Screen.Login.route, inclusive = false)
                }
            )
        }

        // Pantalla de Inicio (Feed de la Facultad) - Lector o Publicador
        composable(
            route = Screen.Feed.route,
            arguments = listOf(
                navArgument("userType") {
                    type = NavType.StringType
                    defaultValue = "reader"
                }
            )
        ) { backStackEntry ->
            val userType = backStackEntry.arguments?.getString("userType") ?: "reader"
            val isPublisher = userType == "publisher"

            FeedScreen(
                isPublisher = isPublisher,
                onNavigateToPreferences = {
                    navController.navigate(Screen.Preferences.createRoute(userType)) {
                        launchSingleTop = true
                    }
                },
                onNavigateToNewPost = {
                    if (isPublisher) {
                        navController.navigate(Screen.NewPost.route) {
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        // Pantalla de Preferencias
        composable(
            route = Screen.Preferences.route,
            arguments = listOf(
                navArgument("userType") {
                    type = NavType.StringType
                    defaultValue = "reader"
                }
            )
        ) { backStackEntry ->
            val userType = backStackEntry.arguments?.getString("userType") ?: "reader"
            val isPublisher = userType == "publisher"

            PreferencesScreen(
                isPublisher = isPublisher,
                onNavigateToHome = {
                    navController.navigate(Screen.Feed.createRoute(userType)) {
                        popUpTo(Screen.Feed.createRoute(userType)) { inclusive = true }
                    }
                },
                onNavigateToNewPost = {
                    if (isPublisher) {
                        navController.navigate(Screen.NewPost.route) {
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        // Pantalla de Nueva Publicación (Solo para publicadores)
        composable(route = Screen.NewPost.route) {
            NewPostScreen(
                onPostCreated = {
                    navController.navigate(Screen.Feed.createRoute("publisher")) {
                        popUpTo(Screen.Feed.createRoute("publisher")) { inclusive = true }
                    }
                },
                onNavigateToPreferences = {
                    navController.navigate(Screen.Preferences.createRoute("publisher")) {
                        launchSingleTop = true
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Feed.createRoute("publisher")) {
                        popUpTo(Screen.Feed.createRoute("publisher")) { inclusive = true }
                    }
                }
            )
        }
    }
}
