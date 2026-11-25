package com.example.udice.data.model

const val SEARCH_PAGE_SIZE = 20

data class PostSearch(
    val query: String,
    val category: String,
    val faculty: String,
    val from: String?,
    val until: String?
)

data class PostSearchResult(
    val id: String,
    val title: String,
    val excerpt: String,
    val category: Categoria,
    val faculty: Facultad,
    val date: Long
)

data class PostSearchPage(val results: List<PostSearchResult>, val next: FeedCursor?)

fun Post.toSearchResult() = PostSearchResult(id, titulo, descripcion.take(240), categoria, facultad, fecha)
