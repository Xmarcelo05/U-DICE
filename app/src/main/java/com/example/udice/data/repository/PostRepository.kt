package com.example.udice.data.repository

import android.net.Uri
import com.example.udice.data.model.Post
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    val posts: Flow<List<Post>>
    fun observePost(id: String): Flow<Post?>
    suspend fun getPostById(id: String): Post?
    suspend fun syncPosts(): Result<Unit>
    suspend fun createPost(
        post: Post,
        photos: List<Uri> = emptyList(),
        video: Uri? = null,
        onUploadProgress: (Int) -> Unit = {}
    ): Result<Post>
    suspend fun updatePost(post: Post): Result<Post>
    suspend fun deletePost(id: String): Result<Unit>
}
