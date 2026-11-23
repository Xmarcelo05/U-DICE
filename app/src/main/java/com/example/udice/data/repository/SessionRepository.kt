package com.example.udice.data.repository

import com.example.udice.data.model.Facultad
import com.example.udice.data.model.User
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    val currentUser: Flow<User?>
    suspend fun getCurrentUser(): User?
    suspend fun register(name: String, email: String, password: String, faculty: Facultad, career: String): Result<Unit>
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun refreshFacultyAccounts(): Result<Unit> = Result.success(Unit)
    fun observeFacultyAccounts(): Flow<List<User>>
    suspend fun createFacultyAccount(name: String, email: String, password: String, faculty: Facultad): Result<Unit>
    suspend fun resetFacultyPassword(id: String, password: String): Result<Unit>
    suspend fun deleteFacultyAccount(id: String): Result<Unit>
    suspend fun logout(): Result<Unit>
}
