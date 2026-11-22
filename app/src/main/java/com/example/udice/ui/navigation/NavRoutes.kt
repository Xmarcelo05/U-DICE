package com.example.udice.ui.navigation

enum class UserRole {
    LECTOR,
    PUBLICADOR
}

object NavRoutes {
    // TODO(RF 1): Agregar la ruta de inicio de sesión y conectar su state holder.
    // TODO(RF 2): Agregar la ruta de registro y su flujo de creación de cuenta.
    const val INICIO_FEED = "inicio_feed"
    const val PREFERENCIAS = "preferencias"
    const val NUEVA_PUBLICACION = "nueva_publicacion"
    const val PERFIL = "perfil"
}
