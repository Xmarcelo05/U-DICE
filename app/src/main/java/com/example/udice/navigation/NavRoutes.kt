package com.example.udice.navigation

/**
 * Rutas de navegación de la aplicación UDICE.
 */
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    
    // Pantalla de inicio (Feed de la Facultad) parametrizada por tipo de usuario ("reader" o "publisher")
    object Feed : Screen("feed/{userType}") {
        fun createRoute(userType: String): String = "feed/$userType"
    }
    
    // Pantallas complementarias del prototipo
    object Preferences : Screen("preferences/{userType}") {
        fun createRoute(userType: String): String = "preferences/$userType"
    }
    
    object NewPost : Screen("new_post")
}
