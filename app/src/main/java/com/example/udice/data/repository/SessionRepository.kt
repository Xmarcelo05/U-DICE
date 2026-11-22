package com.example.udice.data.repository

import com.example.udice.data.model.Facultad
import com.example.udice.data.model.User
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    val currentUser: Flow<User?>
    suspend fun getCurrentUser(): User?
    suspend fun register(name: String, email: String, password: String, faculty: Facultad, career: String): Result<Unit>
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun logout(): Result<Unit>
}
