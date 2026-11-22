package com.example.udice.data.repository

import com.example.udice.data.model.ReaccionSummary
import com.example.udice.data.model.TipoReaccion
import kotlinx.coroutines.flow.Flow

interface ReactionsRepository {
    fun observeReactions(postId: String, currentUserId: String): Flow<ReaccionSummary>
    fun observeAllReactions(currentUserId: String): Flow<Map<String, ReaccionSummary>>
    suspend fun toggleReaction(postId: String, userId: String, tipo: TipoReaccion): Result<Unit>
    suspend fun syncReactions(): Result<Unit>
}
