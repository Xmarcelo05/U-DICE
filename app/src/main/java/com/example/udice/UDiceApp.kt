package com.example.udice

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.udice.data.model.Rol
import com.example.udice.data.model.User
import com.example.udice.ui.admin.AdminRoute
import com.example.udice.ui.auth.AuthScreen
import com.example.udice.ui.auth.AuthStateHolder
import com.example.udice.ui.auth.SessionState
import com.example.udice.ui.busqueda.BusquedaRoute
import com.example.udice.ui.components.UDiceBottomBar
import com.example.udice.ui.components.UDiceHeader
import com.example.udice.ui.detalle.DetallePublicacionRoute
import com.example.udice.ui.feed.InicioFeedRoute
import com.example.udice.ui.guardados.GuardadosRoute
import com.example.udice.ui.mispublicaciones.MisPublicacionesRoute
import com.example.udice.ui.navigation.NavRoutes
import com.example.udice.ui.navigation.UserRole
import com.example.udice.ui.nuevapublicacion.NuevaPublicacionRoute
import com.example.udice.ui.perfil.PerfilScreen
import com.example.udice.ui.preferencias.PreferenciasRoute
import com.example.udice.ui.theme.UDICETheme

@Composable
fun UDiceApp(auth: AuthStateHolder) {
    UDICETheme {
        val session by auth.session.collectAsStateWithLifecycle()
        val form by auth.form.collectAsStateWithLifecycle()
        when (val current = session) {
            SessionState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            SessionState.SignedOut -> AuthScreen(form, auth::showRegistration, auth::dismissVerification, auth::login, auth::register)
            is SessionState.SignedIn -> key(current.user.id) {
                SignedInApp(current.user, !form.busy, form.message.takeIf { form.error }, auth::logout)
            }
        }
    }
}

private const val FEED_SCROLL_REQUEST = "feedScrollRequest"

@Composable
private fun SignedInApp(user: User, logoutEnabled: Boolean, error: String?, onLogout: () -> Unit) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route
    val role = when (user.rol) {
        Rol.LECTOR -> UserRole.LECTOR
        Rol.ASO -> UserRole.PUBLICADOR
        Rol.ADMIN -> UserRole.ADMIN
    }
    val isAdmin = role == UserRole.ADMIN
    val startRoute = if (isAdmin) NavRoutes.ADMIN else NavRoutes.INICIO_FEED
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(error) { if (error != null) snackbar.showSnackbar(error) }

    val currentRoute = route ?: startRoute
    val title = when {
        currentRoute == NavRoutes.ADMIN -> "Administración"
        currentRoute == NavRoutes.BUSQUEDA -> "Buscar publicaciones"
        currentRoute == NavRoutes.PREFERENCIAS -> "Preferencias"
        currentRoute == NavRoutes.NUEVA_PUBLICACION -> "Nueva publicación"
        currentRoute == NavRoutes.PERFIL -> "Mi perfil"
        currentRoute == NavRoutes.MIS_PUBLICACIONES || currentRoute == NavRoutes.EDITAR_PUBLICACION -> "Mis publicaciones"
        currentRoute == NavRoutes.GUARDADOS -> "Publicaciones guardadas"
        currentRoute.startsWith("detalle_publicacion/") -> "Detalle del aviso"
        else -> "U-DICE · Novedades"
    }
    val goToFeed: () -> Unit = {
        navController.navigate(NavRoutes.INICIO_FEED) {
            popUpTo(NavRoutes.INICIO_FEED)
            launchSingleTop = true
        }
    }
    val goToDetail: (String) -> Unit = { postId ->
        navController.navigate(NavRoutes.detallePublicacion(postId)) {
            launchSingleTop = true
        }
    }

    Scaffold(
        topBar = { UDiceHeader(title, role, onLogout, logoutEnabled) },
        bottomBar = {
            if (!isAdmin) {
                fun navigateToTab(destination: String) {
                    navController.navigate(destination) {
                        popUpTo(NavRoutes.INICIO_FEED) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
                UDiceBottomBar(currentRoute, role) { destination ->
                    if (currentRoute == NavRoutes.INICIO_FEED && destination == NavRoutes.INICIO_FEED) {
                        navController.getBackStackEntry(NavRoutes.INICIO_FEED).savedStateHandle[FEED_SCROLL_REQUEST] = true
                    } else {
                        navigateToTab(destination)
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        NavHost(
            navController,
            startDestination = startRoute,
            modifier = Modifier.padding(padding).consumeWindowInsets(padding)
        ) {
            if (isAdmin) {
                composable(NavRoutes.ADMIN) { AdminRoute() }
            } else {
                composable(NavRoutes.INICIO_FEED) { entry ->
                    val scrollRequested by entry.savedStateHandle
                        .getStateFlow(FEED_SCROLL_REQUEST, false).collectAsStateWithLifecycle()
                    InicioFeedRoute(
                        scrollRequested = scrollRequested,
                        onScrollHandled = { entry.savedStateHandle[FEED_SCROLL_REQUEST] = false },
                        onNavigateToPostDetail = goToDetail,
                        editableAuthorId = user.id.takeIf { user.rol == Rol.ASO },
                        onEditPost = { navController.navigate(NavRoutes.editarPublicacion(it)) { launchSingleTop = true } }
                    )
                }
                composable(NavRoutes.BUSQUEDA) { BusquedaRoute(onPostClick = goToDetail) }
                composable(NavRoutes.PREFERENCIAS) {
                    PreferenciasRoute(onGuardarExitoso = goToFeed, onCancelar = goToFeed)
                }
                composable(NavRoutes.NUEVA_PUBLICACION) {
                    if (user.rol == Rol.ASO) NuevaPublicacionRoute(onPublicacionExitosa = goToFeed)
                }
                composable(NavRoutes.PERFIL) {
                    PerfilScreen(
                        user = user,
                        logoutEnabled = logoutEnabled,
                        onLogout = onLogout,
                        onSavedPosts = {
                            navController.navigate(NavRoutes.GUARDADOS) { launchSingleTop = true }
                        },
                        onMyPosts = {
                            navController.navigate(NavRoutes.MIS_PUBLICACIONES) { launchSingleTop = true }
                        }
                    )
                }
                composable(NavRoutes.MIS_PUBLICACIONES) {
                    if (user.rol == Rol.ASO) MisPublicacionesRoute(onPostClick = goToDetail)
                }
                composable(
                    NavRoutes.EDITAR_PUBLICACION,
                    arguments = listOf(navArgument(NavRoutes.POST_ID_ARG) { type = NavType.StringType })
                ) { entry ->
                    if (user.rol == Rol.ASO) MisPublicacionesRoute(
                        onPostClick = goToDetail,
                        initialEditId = entry.arguments?.getString(NavRoutes.POST_ID_ARG),
                        onEditorClosed = { navController.popBackStack() }
                    )
                }
                composable(NavRoutes.GUARDADOS) {
                    GuardadosRoute(
                        onPostClick = goToDetail,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(
                    route = NavRoutes.DETALLE_PUBLICACION,
                    arguments = listOf(navArgument(NavRoutes.POST_ID_ARG) { type = NavType.StringType })
                ) {
                    DetallePublicacionRoute(
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}