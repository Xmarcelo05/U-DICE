package com.example.udice.data.repository

import com.example.udice.data.model.ReaccionSummary
import com.example.udice.data.model.ReactionDetails
import com.example.udice.data.model.TipoReaccion
import kotlinx.coroutines.flow.Flow

interface ReactionsRepository {
    val reactionTypes: Flow<List<TipoReaccion>>
    suspend fun refreshReactionTypes(): Result<Unit> = Result.success(Unit)
    suspend fun addReactionType(label: String, emoji: String): Result<Unit>
    fun observeReactions(postId: String, currentUserId: String): Flow<ReaccionSummary>
    fun observeAllReactions(currentUserId: String): Flow<Map<String, ReaccionSummary>>
    suspend fun toggleReaction(postId: String, userId: String, tipo: TipoReaccion): Result<Unit>
    suspend fun reactionDetails(postId: String, includePeople: Boolean, type: String? = null, after: String? = null): Result<ReactionDetails>
    suspend fun syncReactions(): Result<Unit>
}
