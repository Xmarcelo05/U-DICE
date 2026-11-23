package com.example.udice.data.model

data class TipoReaccion(val id: String, val displayName: String, val emoji: String) {
    companion object {
        val defaults = listOf(
            TipoReaccion("ME_GUSTA", "Me gusta", "❤️"),
            TipoReaccion("UTIL", "Útil", "💡"),
            TipoReaccion("IMPORTANTE", "Importante", "⚠️"),
            TipoReaccion("INTERESANTE", "Interesante", "👏")
        )
    }
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
