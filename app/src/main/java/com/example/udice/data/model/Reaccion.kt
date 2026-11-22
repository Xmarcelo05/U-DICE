package com.example.udice.data.model

enum class TipoReaccion(val displayName: String, val emoji: String) {
    ME_GUSTA("Me gusta", "❤️"),
    UTIL("Útil", "💡"),
    IMPORTANTE("Importante", "⚠️"),
    INTERESANTE("Interesante", "👏")
}

data class Reaccion(
    val id: String,
    val postId: String,
    val userId: String,
    val tipo: TipoReaccion,
    val createdAt: Long = System.currentTimeMillis()
)

data class ReaccionSummary(
    val userReaction: TipoReaccion? = null,
    val counts: Map<TipoReaccion, Int> = emptyMap(),
    val totalCount: Int = 0
)
