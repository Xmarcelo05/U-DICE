package com.example.udice.ui.navigation

enum class UserRole {
    LECTOR,
    PUBLICADOR,
    ADMIN
}

object NavRoutes {
    const val ADMIN = "administracion"
    const val INICIO_FEED = "inicio_feed"
    const val PREFERENCIAS = "preferencias"
    const val NUEVA_PUBLICACION = "nueva_publicacion"
    const val PERFIL = "perfil"
    const val MIS_PUBLICACIONES = "mis_publicaciones"
}
