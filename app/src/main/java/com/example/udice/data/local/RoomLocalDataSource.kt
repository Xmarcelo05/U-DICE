package com.example.udice.data.local

import com.example.udice.data.local.dao.PostDao
import com.example.udice.data.local.dao.PreferenceDao
import com.example.udice.data.local.dao.SavedPostDao
import com.example.udice.data.local.dao.UserDao
import com.example.udice.data.local.entity.PreferenceEntity
import com.example.udice.data.local.entity.SavedPostEntity
import com.example.udice.data.local.mapper.toDomain
import com.example.udice.data.local.mapper.toEntity
import com.example.udice.data.model.Post
import com.example.udice.data.model.User
import com.example.udice.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RoomLocalDataSource @Inject constructor(
    private val postDao: PostDao,
    private val userDao: UserDao,
    private val preferenceDao: PreferenceDao,
    private val savedPostDao: SavedPostDao,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : LocalDataSource {

    override fun observePosts(): Flow<List<Post>> =
        postDao.observePosts()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(ioDispatcher)

    override fun observePostById(id: String): Flow<Post?> =
        postDao.observePostById(id)
            .map { it?.toDomain() }
            .flowOn(ioDispatcher)

    override suspend fun getPostById(id: String): Post? = withContext(ioDispatcher) {
        postDao.getPostById(id)?.toDomain()
    }

    override suspend fun insertPost(post: Post): Unit = withContext(ioDispatcher) {
        postDao.insertPost(post.toEntity())
    }

    override suspend fun insertPosts(posts: List<Post>): Unit = withContext(ioDispatcher) {
        postDao.insertPosts(posts.map { it.toEntity() })
    }

    override suspend fun replacePosts(posts: List<Post>): Unit = withContext(ioDispatcher) {
        postDao.replacePosts(posts.map { it.toEntity() })
    }

    override suspend fun updatePost(post: Post): Unit = withContext(ioDispatcher) {
        postDao.updatePost(post.toEntity())
    }

    override suspend fun deletePostById(id: String): Unit = withContext(ioDispatcher) {
        postDao.deletePostById(id)
    }

    override suspend fun clearPosts(): Unit = withContext(ioDispatcher) {
        postDao.clearPosts()
    }

    override fun observeCurrentUser(): Flow<User?> =
        userDao.observeCurrentUser()
            .map { it?.toDomain() }
            .flowOn(ioDispatcher)

    override suspend fun getCurrentUser(): User? = withContext(ioDispatcher) {
        userDao.getCurrentUser()?.toDomain()
    }

    override suspend fun insertUser(user: User, isCurrentSession: Boolean): Unit = withContext(ioDispatcher) {
        userDao.insertUser(user.toEntity(isCurrentSession))
    }

    override suspend fun insertUsers(users: List<User>): Unit = withContext(ioDispatcher) {
        userDao.insertUsers(users.map { it.toEntity() })
    }

    override suspend fun setCurrentSession(userId: String): Unit = withContext(ioDispatcher) {
        userDao.setCurrentSession(userId)
    }

    override suspend fun clearCurrentSession(): Unit = withContext(ioDispatcher) {
        userDao.clearCurrentSession()
    }

    override fun observePreferences(userId: String): Flow<Map<String, Boolean>> =
        preferenceDao.observePreferences(userId)
            .map { list -> list.associate { it.preferenceKey to it.isEnabled } }
            .flowOn(ioDispatcher)

    override suspend fun getPreferences(userId: String): Map<String, Boolean> = withContext(ioDispatcher) {
        preferenceDao.getPreferences(userId).associate { it.preferenceKey to it.isEnabled }
    }

    override suspend fun savePreferences(userId: String, preferences: Map<String, Boolean>): Unit = withContext(ioDispatcher) {
        val entities = preferences.map { (key, isEnabled) ->
            PreferenceEntity(userId = userId, preferenceKey = key, isEnabled = isEnabled)
        }
        preferenceDao.replacePreferences(userId, entities)
    }

    override fun observeSavedPostIds(userId: String): Flow<Set<String>> =
        savedPostDao.observeSavedPostIds(userId)
            .map { it.toSet() }
            .flowOn(ioDispatcher)

    override fun observeSavedPosts(userId: String): Flow<List<Post>> =
        savedPostDao.observeSavedPosts(userId)
            .map { list -> list.map { it.toDomain() } }
            .flowOn(ioDispatcher)

    override fun isPostSaved(userId: String, postId: String): Flow<Boolean> =
        savedPostDao.isPostSaved(userId, postId)
            .flowOn(ioDispatcher)

    override suspend fun setPostSaved(userId: String, postId: String, saved: Boolean): Unit = withContext(ioDispatcher) {
        if (saved) {
            savedPostDao.insertSavedPost(SavedPostEntity(userId = userId, postId = postId))
        } else {
            savedPostDao.deleteSavedPost(userId = userId, postId = postId)
        }
    }
}
