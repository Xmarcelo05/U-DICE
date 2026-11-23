package com.example.udice.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.udice.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE isCurrentSession = 1 LIMIT 1")
    fun observeCurrentUser(): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE isCurrentSession = 1 LIMIT 1")
    suspend fun getCurrentUser(): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email COLLATE NOCASE LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE rol = 'ASO' ORDER BY nombre COLLATE NOCASE")
    fun observeFacultyAccounts(): Flow<List<UserEntity>>

    @Query("UPDATE users SET passwordSalt = :salt, passwordHash = :hash WHERE id = :id AND rol = 'ASO'")
    suspend fun resetFacultyPassword(id: String, salt: ByteArray, hash: ByteArray): Int

    @Query("DELETE FROM users WHERE id = :id AND rol = 'ASO'")
    suspend fun deleteFacultyAccount(id: String): Int

    @Query("SELECT * FROM users")
    fun observeAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Query("UPDATE users SET isCurrentSession = 0")
    suspend fun clearCurrentSession()

    @Query("UPDATE users SET isCurrentSession = CASE WHEN id = :userId THEN 1 ELSE 0 END")
    suspend fun setCurrentSession(userId: String)
}
