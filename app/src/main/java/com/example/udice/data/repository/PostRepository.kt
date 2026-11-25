package com.example.udice.data.repository

import android.net.Uri
import com.example.udice.data.model.Post
import com.example.udice.data.model.FeedCursor
import com.example.udice.data.model.PostPage
import com.example.udice.data.model.PostSearch
import com.example.udice.data.model.PostSearchPage
import kotlinx.coroutines.flow.Flow

data class PostMediaEdit(
    val retainedPhotos: List<String>,
    val newPhotos: List<Uri>,
    val keepVideo: Boolean,
    val newVideo: Uri?
)

interface PostRepository {
    val posts: Flow<List<Post>>
    fun observePost(id: String): Flow<Post?>
    suspend fun getPostById(id: String): Post?
    suspend fun searchPosts(search: PostSearch, after: FeedCursor?): Result<PostSearchPage>
    suspend fun loadPostIfMissing(id: String): Result<Unit>
    suspend fun syncPosts(): Result<Unit>
    suspend fun loadFeedPage(after: FeedCursor?, following: Boolean = false): Result<PostPage>
    suspend fun createPost(
        post: Post,
        photos: List<Uri> = emptyList(),
        video: Uri? = null,
        onUploadProgress: (Int) -> Unit = {}
    ): Result<Post>
    suspend fun updatePost(post: Post): Result<Post>
    suspend fun updateMedia(post: Post, edit: PostMediaEdit): Result<Post>
    suspend fun deletePost(id: String): Result<Unit>
}
