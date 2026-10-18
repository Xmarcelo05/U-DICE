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
import com.example.udice.ui.screens.NuevaPublicacionScreen
import com.example.udice.ui.screens.PerfilScreen
import com.example.udice.ui.screens.PreferenciasScreen
import com.example.udice.ui.theme.UDICETheme

@Composable
fun UDiceApp(
    initialRole: UserRole = UserRole.PUBLICADOR
) {
    UDICETheme {
        val navController = rememberNavController()
        var currentRole by remember { mutableStateOf(initialRole) }

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route ?: NavRoutes.PREFERENCIAS

        // Título dinámico según la ruta
        val screenTitle = when (currentRoute) {
            NavRoutes.PREFERENCIAS -> "Preferencias"
            NavRoutes.NUEVA_PUBLICACION -> "Nueva Publicación"
            NavRoutes.PERFIL -> "Mi Perfil"
            else -> "U-DICE"
        }

        Scaffold(
            topBar = {
                UDiceHeader(
                    title = screenTitle,
                    currentRole = currentRole,
                    onRoleToggle = {
                        val newRole = if (currentRole == UserRole.PUBLICADOR) UserRole.LECTOR else UserRole.PUBLICADOR
                        currentRole = newRole
                        // Si era publicador y estaba en Nueva Publicación pero cambió a Lector, redirigir a Preferencias
                        if (newRole == UserRole.LECTOR && currentRoute == NavRoutes.NUEVA_PUBLICACION) {
                            navController.navigate(NavRoutes.PREFERENCIAS) {
                                popUpTo(NavRoutes.PREFERENCIAS) { inclusive = true }
                            }
                        }
                    }
                )
            },
            bottomBar = {
                UDiceBottomBar(
                    currentRoute = currentRoute,
                    currentRole = currentRole,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(NavRoutes.PREFERENCIAS) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                NavHost(
                    navController = navController,
                    startDestination = NavRoutes.PREFERENCIAS
                ) {
                    composable(NavRoutes.PREFERENCIAS) {
                        PreferenciasScreen(
                            onGuardarClick = {
                                navController.navigate(NavRoutes.PREFERENCIAS) {
                                    popUpTo(NavRoutes.PREFERENCIAS) { inclusive = true }
                                }
                            },
                            onCancelarClick = {
                                navController.navigate(NavRoutes.PREFERENCIAS) {
                                    popUpTo(NavRoutes.PREFERENCIAS) { inclusive = true }
                                }
                            }
                        )
                    }
                    composable(NavRoutes.NUEVA_PUBLICACION) {
                        NuevaPublicacionScreen(
                            onPublicacionExitosa = {
                                navController.navigate(NavRoutes.PREFERENCIAS) {
                                    popUpTo(NavRoutes.PREFERENCIAS) { inclusive = true }
                                }
                            }
                        )
                    }
                    composable(NavRoutes.PERFIL) {
                        PerfilScreen()
                    }
                }
            }
        }
    }
}
