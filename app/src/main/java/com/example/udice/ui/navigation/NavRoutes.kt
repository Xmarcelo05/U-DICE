package com.example.udice.ui.navigation

enum class UserRole {
    LECTOR,
    PUBLICADOR,
    ADMIN
}

object NavRoutes {
    const val ADMIN = "administracion"
    const val INICIO_FEED = "inicio_feed"
    const val BUSQUEDA = "busqueda"
    const val PREFERENCIAS = "preferencias"
    const val NUEVA_PUBLICACION = "nueva_publicacion"
    const val PERFIL = "perfil"
    const val MIS_PUBLICACIONES = "mis_publicaciones"
    const val GUARDADOS = "guardados"
    const val PUBLISHER_ID_ARG = "publisherId"
    const val PERFIL_PUBLICADOR = "publicador/{$PUBLISHER_ID_ARG}"
    const val POST_ID_ARG = "postId"
    const val EDITAR_PUBLICACION = "mis_publicaciones/editar/{$POST_ID_ARG}"
    const val DETALLE_PUBLICACION = "detalle_publicacion/{$POST_ID_ARG}"

    fun editarPublicacion(postId: String): String = "mis_publicaciones/editar/$postId"

    fun perfilPublicador(id: String): String = "publicador/$id"

    fun detallePublicacion(postId: String): String = "detalle_publicacion/$postId"
}