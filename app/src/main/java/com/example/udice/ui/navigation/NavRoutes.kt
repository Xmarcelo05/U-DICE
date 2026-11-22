package com.example.udice.ui.navigation

enum class UserRole {
    LECTOR,
    PUBLICADOR
}

object NavRoutes {
    // TODO(RF 1): Agregar la ruta de inicio de sesión y conectar su state holder.
    // TODO(RF 2): Agregar la ruta de registro y su flujo de creación de cuenta.
    // TODO(RF 3): Crear la ruta del feed con filtros basados en preferencias.
    // TODO(RF 4): Incorporar reacciones y estado de Me gusta en publicaciones.
    // TODO(RF 5): Incorporar la ruta de publicaciones guardadas con SavedPostsRepository.
    // TODO(RF 6): Agregar la ruta de detalle de publicación con argumento postId.
    const val PREFERENCIAS = "preferencias"
    const val NUEVA_PUBLICACION = "nueva_publicacion"
    const val PERFIL = "perfil"
}
