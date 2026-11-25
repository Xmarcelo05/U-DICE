package com.example.udice.data.remote

import com.example.udice.data.model.Post
import com.example.udice.data.model.FeedCursor
import com.example.udice.data.model.PostPage
import com.example.udice.data.model.User

interface RemoteDataSource {
    suspend fun getPosts(): Result<List<Post>>
    suspend fun searchPosts(search: com.example.udice.data.model.PostSearch, after: FeedCursor?): Result<com.example.udice.data.model.PostSearchPage>
    suspend fun getFeedPage(after: FeedCursor?): Result<PostPage>
    suspend fun getPostById(id: String): Result<Post?>
    suspend fun createPost(post: Post): Result<Post>
    suspend fun updatePost(post: Post): Result<Post>
    suspend fun updatePostMedia(post: Post): Result<Post>
    suspend fun deletePost(id: String): Result<Unit>

    suspend fun getUsers(): Result<List<User>>
    suspend fun getUser(userId: String): Result<User?>

    suspend fun getPreferences(userId: String): Result<Map<String, Boolean>>
    suspend fun savePreferences(userId: String, preferences: Map<String, Boolean>): Result<Unit>

    suspend fun getSavedPostIds(userId: String): Result<Set<String>>
    suspend fun savePost(userId: String, postId: String): Result<Unit>
    suspend fun unsavePost(userId: String, postId: String): Result<Unit>
}
