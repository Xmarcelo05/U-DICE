package com.example.udice.data.model

data class Post(
    val id: String,
    val autorId: String,
    val autorNombre: String = "",
    val titulo: String,
    val descripcion: String,
    val categoria: Categoria,
    val facultad: Facultad,
    val alcance: Alcance,
    val carrera: String? = null,
    val fecha: Long = System.currentTimeMillis(),
    val imageUrl: String? = null,
    val allowedReactionIds: List<String>? = null,
    val videoUrl: String? = null
)
