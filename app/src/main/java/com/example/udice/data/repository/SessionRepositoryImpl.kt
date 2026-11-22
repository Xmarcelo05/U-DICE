package com.example.udice.data.repository

import android.util.Patterns
import com.example.udice.BuildConfig
import com.example.udice.data.local.LocalDataSource
import com.example.udice.data.local.dao.UserDao
import com.example.udice.data.local.mapper.toEntity
import com.example.udice.data.model.Facultad
import com.example.udice.data.model.Rol
import com.example.udice.data.model.User
import com.example.udice.di.IoDispatcher
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Locale
import java.util.UUID
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

internal const val DEMO_PUBLISHER_ID = "user-aso-1"

class SessionRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource,
    private val userDao: UserDao,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : SessionRepository {
    private val sessionMutex = Mutex()

    override val currentUser = localDataSource.observeCurrentUser()
    override suspend fun getCurrentUser(): User? = localDataSource.getCurrentUser()

    override suspend fun register(
        name: String, email: String, password: String, faculty: Facultad, career: String
    ): Result<Unit> = withContext(ioDispatcher) {
        sessionMutex.withLock {
            runCatching {
                val normalizedEmail = email.trim().lowercase(Locale.ROOT)
                require(name.isNotBlank() && career.isNotBlank()) { "Completa el nombre y la carrera." }
                require(Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches()) { "Ingresa un correo válido." }
                require(password.length in 8..128) { "La contraseña debe tener entre 8 y 128 caracteres." }
                require(userDao.getUserByEmail(normalizedEmail) == null) { "Ese correo ya est? registrado." }
                val salt = ByteArray(16).also(SecureRandom()::nextBytes)
                val user = User(
                    UUID.randomUUID().toString(), name.trim(), normalizedEmail,
                    Rol.LECTOR, faculty, career.trim()
                )
                userDao.insertUser(user.toEntity().copy(passwordSalt = salt, passwordHash = derivePassword(password, salt)))
            }
        }
    }

    override suspend fun login(email: String, password: String): Result<Unit> = withContext(ioDispatcher) {
        sessionMutex.withLock {
            runCatching {
                val user = userDao.getUserByEmail(email.trim())
                val demo = BuildConfig.DEBUG && user?.id == DEMO_PUBLISHER_ID && password == "UDiceDemo2026!"
                val salt = user?.passwordSalt
                val hash = user?.passwordHash
                val matches = salt != null && hash != null && password.length in 8..128 &&
                    MessageDigest.isEqual(hash, derivePassword(password, salt))
                require(user != null && (demo || matches)) { "Correo o contraseña incorrectos." }
                localDataSource.setCurrentSession(user.id)
            }
        }
    }

    override suspend fun logout(): Result<Unit> = withContext(ioDispatcher) {
        sessionMutex.withLock { runCatching { localDataSource.clearCurrentSession() } }
    }

    private fun derivePassword(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, 600_000, 256)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }
}
