package com.example.udice.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.udice.data.local.entity.PostEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {
    @Query("SELECT * FROM posts ORDER BY fecha DESC")
    fun observePosts(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE id = :id")
    fun observePostById(id: String): Flow<PostEntity?>

    @Query("SELECT * FROM posts WHERE id = :id")
    suspend fun getPostById(id: String): PostEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<PostEntity>)

    @Update
    suspend fun updatePost(post: PostEntity)

    @Query("DELETE FROM posts WHERE id = :id")
    suspend fun deletePostById(id: String)

    @Query("DELETE FROM posts")
    suspend fun clearPosts()
}
