package com.example.udice.data.repository

import com.example.udice.data.local.LocalDataSource
import com.example.udice.data.remote.RemoteDataSource
import com.example.udice.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SavedPostsRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource,
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
            val remoteResult = if (newSavedState) remoteDataSource.savePost(userId, postId)
                else remoteDataSource.unsavePost(userId, postId)
            remoteResult.getOrThrow()
            newSavedState
        }
    }

    override suspend fun syncSavedPosts(userId: String): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            val remoteIds = remoteDataSource.getSavedPostIds(userId).getOrThrow()
            val localIds = localDataSource.observeSavedPostIds(userId).first()
            (localIds - remoteIds).forEach { localDataSource.setPostSaved(userId, it, false) }
            (remoteIds - localIds).forEach { localDataSource.setPostSaved(userId, it, true) }
        }
    }
}
