package com.example.udice.data.repository

import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    fun observePreferences(userId: String): Flow<Map<String, Boolean>>
    suspend fun getPreferences(userId: String): Map<String, Boolean>
    suspend fun updatePreferences(userId: String, preferences: Map<String, Boolean>): Result<Unit>
    suspend fun syncPreferences(userId: String): Result<Unit>
}
