package com.example.udice.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.udice.data.local.entity.PreferenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PreferenceDao {
    @Query("SELECT * FROM user_preferences WHERE userId = :userId")
    fun observePreferences(userId: String): Flow<List<PreferenceEntity>>

    @Query("SELECT * FROM user_preferences WHERE userId = :userId")
    suspend fun getPreferences(userId: String): List<PreferenceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreferences(preferences: List<PreferenceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreference(preference: PreferenceEntity)

    @Transaction
    suspend fun replacePreferences(userId: String, preferences: List<PreferenceEntity>) {
        clearPreferences(userId)
        insertPreferences(preferences)
    }

    @Query("DELETE FROM user_preferences WHERE userId = :userId")
    suspend fun clearPreferences(userId: String)
}
