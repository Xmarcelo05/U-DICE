package com.example.udice.data.local

import com.example.udice.data.model.Post
import com.example.udice.data.model.User
import kotlinx.coroutines.flow.Flow

interface LocalDataSource {
    fun observePosts(): Flow<List<Post>>
    fun observePostById(id: String): Flow<Post?>
    suspend fun getPostById(id: String): Post?
    suspend fun insertPost(post: Post)
    suspend fun insertPosts(posts: List<Post>)
    suspend fun updatePost(post: Post)
    suspend fun deletePostById(id: String)
    suspend fun clearPosts()

    fun observeCurrentUser(): Flow<User?>
    suspend fun getCurrentUser(): User?
    suspend fun insertUser(user: User, isCurrentSession: Boolean = false)
    suspend fun insertUsers(users: List<User>)
    suspend fun setCurrentSession(userId: String)
    suspend fun clearCurrentSession()

    fun observePreferences(userId: String): Flow<Map<String, Boolean>>
    suspend fun getPreferences(userId: String): Map<String, Boolean>
    suspend fun savePreferences(userId: String, preferences: Map<String, Boolean>)

    fun observeSavedPostIds(userId: String): Flow<Set<String>>
    fun observeSavedPosts(userId: String): Flow<List<Post>>
    fun isPostSaved(userId: String, postId: String): Flow<Boolean>
    suspend fun setPostSaved(userId: String, postId: String, saved: Boolean)
}
