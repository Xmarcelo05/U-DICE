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
import com.example.udice.ui.feed.InicioFeedRoute
import com.example.udice.ui.navigation.NavRoutes
import com.example.udice.ui.navigation.UserRole
import com.example.udice.ui.nuevapublicacion.NuevaPublicacionRoute
import com.example.udice.ui.perfil.PerfilScreen
import com.example.udice.ui.preferencias.PreferenciasRoute
import com.example.udice.ui.theme.UDICETheme

@Composable
fun UDiceApp(
    initialRole: UserRole = UserRole.PUBLICADOR
) {
    UDICETheme {
        val navController = rememberNavController()
        var currentRole by remember { mutableStateOf(initialRole) }
        // TODO(RF 8): Vincular navegación y permisos al usuario de SessionRepository.

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route ?: NavRoutes.INICIO_FEED

        // Título dinámico según la ruta
        val screenTitle = when (currentRoute) {
            NavRoutes.INICIO_FEED -> "U-DICE · Novedades"
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
                        // Si era publicador y estaba en Nueva Publicación pero cambió a Lector, redirigir al Feed
                        if (newRole == UserRole.LECTOR && currentRoute == NavRoutes.NUEVA_PUBLICACION) {
                            navController.navigate(NavRoutes.INICIO_FEED) {
                                popUpTo(NavRoutes.INICIO_FEED) { inclusive = true }
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
                            popUpTo(NavRoutes.INICIO_FEED) {
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
                    startDestination = NavRoutes.INICIO_FEED
                ) {
                    composable(NavRoutes.INICIO_FEED) {
                        InicioFeedRoute()
                    }
                    composable(NavRoutes.PREFERENCIAS) {
                        PreferenciasRoute(
                            onGuardarExitoso = {
                                navController.navigate(NavRoutes.INICIO_FEED) {
                                    popUpTo(NavRoutes.INICIO_FEED) { inclusive = true }
                                }
                            },
                            onCancelar = {
                                navController.navigate(NavRoutes.INICIO_FEED) {
                                    popUpTo(NavRoutes.INICIO_FEED) { inclusive = true }
                                }
                            }
                        )
                    }
                    composable(NavRoutes.NUEVA_PUBLICACION) {
                        NuevaPublicacionRoute(
                            onPublicacionExitosa = {
                                navController.navigate(NavRoutes.INICIO_FEED) {
                                    popUpTo(NavRoutes.INICIO_FEED) { inclusive = true }
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
