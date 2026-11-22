package com.example.udice.data.repository

import com.example.udice.data.model.Rol
import com.example.udice.data.model.User
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    val currentUser: Flow<User?>
    suspend fun getCurrentUser(): User?
    suspend fun switchUser(userId: String): Result<User>
    suspend fun updateRole(newRole: Rol): Result<Unit>
    suspend fun syncUsers(): Result<Unit>
    suspend fun logout(): Result<Unit>
}
