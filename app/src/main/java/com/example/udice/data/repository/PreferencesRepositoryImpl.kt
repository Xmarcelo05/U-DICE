package com.example.udice.data.repository

import com.example.udice.data.local.LocalDataSource
import com.example.udice.data.remote.RemoteDataSource
import com.example.udice.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PreferencesRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : PreferencesRepository {

    override fun observePreferences(userId: String): Flow<Map<String, Boolean>> =
        localDataSource.observePreferences(userId)

    override suspend fun getPreferences(userId: String): Map<String, Boolean> = withContext(ioDispatcher) {
        localDataSource.getPreferences(userId)
    }

    override suspend fun updatePreferences(
        userId: String,
        preferences: Map<String, Boolean>
    ): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            // Guardar localmente primero (fuente de verdad offline)
            localDataSource.savePreferences(userId, preferences)
            // Sincronizar con el backend / remoteDataSource
            remoteDataSource.savePreferences(userId, preferences).getOrThrow()
        }
    }

    override suspend fun syncPreferences(userId: String): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            val remotePrefs = remoteDataSource.getPreferences(userId).getOrThrow()
            if (remotePrefs.isNotEmpty()) {
                localDataSource.savePreferences(userId, remotePrefs)
            }
        }
    }
}
