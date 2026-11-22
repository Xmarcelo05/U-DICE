package com.example.udice.data.repository

import com.example.udice.data.local.LocalDataSource
import com.example.udice.data.model.Rol
import com.example.udice.data.model.User
import com.example.udice.data.remote.RemoteDataSource
import com.example.udice.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SessionRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : SessionRepository {

    override val currentUser: Flow<User?> = localDataSource.observeCurrentUser()

    override suspend fun getCurrentUser(): User? = withContext(ioDispatcher) {
        localDataSource.getCurrentUser()
    }

    override suspend fun switchUser(userId: String): Result<User> = withContext(ioDispatcher) {
        runCatching {
            localDataSource.setCurrentSession(userId)
            val user = localDataSource.getCurrentUser()
                ?: throw NoSuchElementException("Usuario con id $userId no encontrado")
            user
        }
    }

    override suspend fun updateRole(newRole: Rol): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            val current = localDataSource.getCurrentUser()
                ?: throw IllegalStateException("No hay una sesión activa para modificar el rol")
            val updatedUser = current.copy(rol = newRole)
            localDataSource.insertUser(updatedUser, isCurrentSession = true)
        }
    }

    override suspend fun syncUsers(): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            val remoteUsersResult = remoteDataSource.getUsers()
            val remoteUsers = remoteUsersResult.getOrThrow()
            localDataSource.insertUsers(remoteUsers)

            // Si no hay usuario activo en sesión local, establecer el primero
            if (localDataSource.getCurrentUser() == null && remoteUsers.isNotEmpty()) {
                localDataSource.setCurrentSession(remoteUsers.first().id)
            }
        }
    }

    override suspend fun logout(): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            localDataSource.clearCurrentSession()
        }
    }
}
