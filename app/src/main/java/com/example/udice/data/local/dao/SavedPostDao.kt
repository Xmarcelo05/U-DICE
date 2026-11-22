package com.example.udice.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.udice.data.local.entity.PostEntity
import com.example.udice.data.local.entity.SavedPostEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedPostDao {
    @Query("SELECT postId FROM saved_posts WHERE userId = :userId")
    fun observeSavedPostIds(userId: String): Flow<List<String>>

    @Query("SELECT p.* FROM posts p INNER JOIN saved_posts sp ON p.id = sp.postId WHERE sp.userId = :userId ORDER BY sp.savedAt DESC")
    fun observeSavedPosts(userId: String): Flow<List<PostEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_posts WHERE userId = :userId AND postId = :postId)")
    fun isPostSaved(userId: String, postId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedPost(savedPost: SavedPostEntity)

    @Query("DELETE FROM saved_posts WHERE userId = :userId AND postId = :postId")
    suspend fun deleteSavedPost(userId: String, postId: String)
}
