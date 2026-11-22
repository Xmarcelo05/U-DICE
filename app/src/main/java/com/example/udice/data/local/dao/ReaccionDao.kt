package com.example.udice.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.udice.data.local.entity.ReaccionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReaccionDao {
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
