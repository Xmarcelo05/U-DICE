package com.example.udice

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.udice.ui.components.UDiceBottomBar
import com.example.udice.ui.components.UDiceHeader
import com.example.udice.ui.navigation.NavRoutes
import com.example.udice.ui.navigation.UserRole
import com.example.udice.ui.screens.ForgotPasswordScreen
import com.example.udice.ui.screens.InicioFeedScreen
import com.example.udice.ui.screens.LoginScreen
import com.example.udice.ui.screens.NuevaPublicacionScreen
import com.example.udice.ui.screens.PerfilScreen
import com.example.udice.ui.screens.PreferenciasScreen
import com.example.udice.ui.screens.RegisterScreen
import com.example.udice.ui.theme.GrisAzuladoClaro
import com.example.udice.ui.theme.UDICETheme

@Composable
fun UDiceApp(
    initialRole: UserRole = UserRole.PUBLICADOR
) {
    UDICETheme {
        val navController = rememberNavController()
        var currentRole by remember { mutableStateOf(initialRole) }

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route ?: NavRoutes.LOGIN

        // Determinar si la pantalla actual es de autenticación
        val isAuthScreen = currentRoute == NavRoutes.LOGIN ||
                currentRoute == NavRoutes.REGISTRO ||
                currentRoute == NavRoutes.RECUPERAR_CONTRASENA

        // Título dinámico para la cabecera institucional
        val screenTitle = when (currentRoute) {
            NavRoutes.INICIO -> "Feed de la Facultad"
            NavRoutes.PREFERENCIAS -> "Preferencias"
            NavRoutes.NUEVA_PUBLICACION -> "Nueva Publicación"
            NavRoutes.PERFIL -> "Mi Perfil"
            else -> "U-DICE"
        }

        Scaffold(
            topBar = {
                if (!isAuthScreen) {
                    UDiceHeader(
                        title = screenTitle,
                        currentRole = currentRole,
                        onRoleToggle = {
                            val newRole = if (currentRole == UserRole.PUBLICADOR) UserRole.LECTOR else UserRole.PUBLICADOR
                            currentRole = newRole
                            // Si era publicador y estaba en Nueva Publicación pero cambió a Lector, redirigir a Inicio
                            if (newRole == UserRole.LECTOR && currentRoute == NavRoutes.NUEVA_PUBLICACION) {
                                navController.navigate(NavRoutes.INICIO) {
                                    popUpTo(NavRoutes.INICIO) { inclusive = true }
                                }
                            }
                        },
                        onProfileClick = {
                            navController.navigate(NavRoutes.PERFIL) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
            },
            bottomBar = {
                if (!isAuthScreen) {
                    UDiceBottomBar(
                        currentRoute = currentRoute,
                        currentRole = currentRole,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                popUpTo(NavRoutes.INICIO) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            },
            containerColor = GrisAzuladoClaro
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (isAuthScreen) androidx.compose.foundation.layout.PaddingValues() else innerPadding)
            ) {
                NavHost(
                    navController = navController,
                    startDestination = NavRoutes.LOGIN
                ) {
                    // PANTALLAS DE AUTENTICACIÓN
                    composable(NavRoutes.LOGIN) {
                        LoginScreen(
                            onLoginAsReader = {
                                currentRole = UserRole.LECTOR
                                navController.navigate(NavRoutes.INICIO) {
                                    popUpTo(NavRoutes.LOGIN) { inclusive = true }
                                }
                            },
                            onLoginAsPublisher = {
                                currentRole = UserRole.PUBLICADOR
                                navController.navigate(NavRoutes.INICIO) {
                                    popUpTo(NavRoutes.LOGIN) { inclusive = true }
                                }
                            },
                            onNavigateToRegister = {
                                navController.navigate(NavRoutes.REGISTRO)
                            },
                            onNavigateToForgotPassword = {
                                navController.navigate(NavRoutes.RECUPERAR_CONTRASENA)
                            }
                        )
                    }

                    composable(NavRoutes.REGISTRO) {
                        RegisterScreen(
                            onRegisterSuccess = {
                                navController.popBackStack()
                            },
                            onNavigateToLogin = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable(NavRoutes.RECUPERAR_CONTRASENA) {
                        ForgotPasswordScreen(
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            onSubmitSuccess = {
                                navController.popBackStack(NavRoutes.LOGIN, inclusive = false)
                            }
                        )
                    }

                    // PANTALLAS PRINCIPALES DE LA APLICACIÓN
                    composable(NavRoutes.INICIO) {
                        InicioFeedScreen()
                    }

                    composable(NavRoutes.PREFERENCIAS) {
                        PreferenciasScreen(
                            onGuardarClick = {
                                navController.navigate(NavRoutes.INICIO) {
                                    popUpTo(NavRoutes.INICIO) { inclusive = true }
                                }
                            },
                            onCancelarClick = {
                                navController.navigate(NavRoutes.INICIO) {
                                    popUpTo(NavRoutes.INICIO) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(NavRoutes.NUEVA_PUBLICACION) {
                        NuevaPublicacionScreen(
                            onPublicacionExitosa = {
                                navController.navigate(NavRoutes.INICIO) {
                                    popUpTo(NavRoutes.INICIO) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(NavRoutes.PERFIL) {
                        PerfilScreen(
                            currentRole = currentRole,
                            onLogout = {
                                navController.navigate(NavRoutes.LOGIN) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
