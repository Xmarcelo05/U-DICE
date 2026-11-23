package com.example.udice.data.local.dao

import androidx.room.Transaction
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.udice.data.local.entity.ReaccionEntity
import com.example.udice.data.local.entity.ReactionTypeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReaccionDao {
    @Query("SELECT * FROM reaction_types ORDER BY label COLLATE NOCASE")
    fun observeCustomTypes(): Flow<List<ReactionTypeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cacheReactionTypes(types: List<ReactionTypeEntity>)

    @Insert
    suspend fun insertReactionType(type: ReactionTypeEntity)

    @Query("""
        INSERT INTO reactions (postId, userId, tipo, createdAt) VALUES (:postId, :userId, :type, :createdAt)
        ON CONFLICT(postId, userId) DO UPDATE SET tipo = excluded.tipo, createdAt = excluded.createdAt
        WHERE substr(reactions.tipo, 1, 6) != 'LOCAL_'
    """)
    suspend fun insertSyncedReaction(postId: String, userId: String, type: String, createdAt: Long)

    @Query("DELETE FROM reactions")
    suspend fun clearVotes()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVotes(rows: List<ReaccionEntity>)

    @Transaction
    suspend fun replaceCloudVotes(rows: List<ReaccionEntity>) {
        clearVotes()
        insertVotes(rows)
    }

    @Query("SELECT * FROM reactions WHERE postId = :postId")
    fun observeReactionsForPost(postId: String): Flow<List<ReaccionEntity>>

    @Query("SELECT * FROM reactions")
    fun observeAllReactions(): Flow<List<ReaccionEntity>>

    @Query("SELECT * FROM reactions WHERE userId = :userId AND postId = :postId")
    fun observeUserReaction(userId: String, postId: String): Flow<ReaccionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReaccion(reaccion: ReaccionEntity)

    @Query("DELETE FROM reactions WHERE userId = :userId AND postId = :postId")
    suspend fun deleteReaccion(userId: String, postId: String)
}
