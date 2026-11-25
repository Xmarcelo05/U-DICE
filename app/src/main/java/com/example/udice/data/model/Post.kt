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
    val videoUrl: String? = null,
    val additionalImageUrls: List<String> = emptyList()
) {
    val imageUrls: List<String> get() = listOfNotNull(imageUrl) + additionalImageUrls
}

const val MAX_PUBLICATION_IMAGES = 4
const val FEED_PAGE_SIZE = 50

data class FeedCursor(val date: String, val id: String)
data class PostPage(val posts: List<Post>, val next: FeedCursor?)
