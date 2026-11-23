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
    const val GUARDADOS = "guardados"
    const val POST_ID_ARG = "postId"
    const val DETALLE_PUBLICACION = "detalle_publicacion/{$POST_ID_ARG}"

    fun detallePublicacion(postId: String): String = "detalle_publicacion/$postId"
}