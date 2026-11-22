package com.example.udice.data.repository

import com.example.udice.data.model.Post
import kotlinx.coroutines.flow.Flow

interface SavedPostsRepository {
    fun observeSavedPostIds(userId: String): Flow<Set<String>>
    fun observeSavedPosts(userId: String): Flow<List<Post>>
    fun isPostSaved(userId: String, postId: String): Flow<Boolean>
    suspend fun toggleSavePost(userId: String, postId: String): Result<Boolean>
    suspend fun syncSavedPosts(userId: String): Result<Unit>
}
