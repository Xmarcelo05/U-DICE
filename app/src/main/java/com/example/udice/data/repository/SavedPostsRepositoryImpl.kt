package com.example.udice.data.repository

import com.example.udice.data.local.LocalDataSource
import com.example.udice.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SavedPostsRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : SavedPostsRepository {
    override fun observeSavedPostIds(userId: String): Flow<Set<String>> = localDataSource.observeSavedPostIds(userId)
    override fun observeSavedPosts(userId: String) = localDataSource.observeSavedPosts(userId)
    override fun isPostSaved(userId: String, postId: String): Flow<Boolean> = localDataSource.isPostSaved(userId, postId)

    override suspend fun toggleSavePost(userId: String, postId: String): Result<Boolean> = withContext(ioDispatcher) {
        runCatching {
            val saved = localDataSource.isPostSaved(userId, postId).first()
            val newSavedState = !saved
            localDataSource.setPostSaved(userId, postId, newSavedState)
            newSavedState
        }
    }

}
