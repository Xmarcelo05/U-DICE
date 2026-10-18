package com.example.udice.ui.navigation

enum class UserRole {
    LECTOR,
    PUBLICADOR
}

object NavRoutes {
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val RECUPERAR_CONTRASENA = "recuperar_contrasena"
    const val INICIO = "inicio"
    const val PREFERENCIAS = "preferencias"
    const val NUEVA_PUBLICACION = "nueva_publicacion"
    const val PERFIL = "perfil"
    const val GUARDADOS = "guardados"
    const val MIS_PUBLICACIONES = "mis_publicaciones"
}
