package com.example.udice.data.repository

import android.util.Patterns
import com.example.udice.BuildConfig
import com.example.udice.data.local.LocalDataSource
import com.example.udice.data.local.dao.UserDao
import com.example.udice.data.local.mapper.toDomain
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

internal const val DEMO_PUBLISHER_ID = "user-aso-1"
private const val DEMO_ADMIN_ID = "udice-local-admin"
private const val DEMO_ADMIN_EMAIL = "admin@udice.example"
private const val DEMO_ADMIN_PASSWORD = "UDiceAdmin2026!"

class SessionRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource,
    private val userDao: UserDao,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : SessionRepository {
    private val sessionMutex = Mutex()

    override val currentUser = localDataSource.observeCurrentUser().map { user ->
        user?.takeIf { BuildConfig.DEBUG || it.rol != Rol.ADMIN }
    }

    override suspend fun getCurrentUser(): User? =
        localDataSource.getCurrentUser()?.takeIf { BuildConfig.DEBUG || it.rol != Rol.ADMIN }

    override suspend fun register(
        name: String, email: String, password: String, faculty: Facultad, career: String
    ): Result<Unit> = withContext(ioDispatcher) {
        sessionMutex.withLock {
            dataResult {
                require(career.isNotBlank()) { "Completa la carrera." }
                insertAccount(name, email, password, faculty, career.trim(), Rol.LECTOR)
            }
        }
    }

    override suspend fun login(email: String, password: String): Result<Unit> = withContext(ioDispatcher) {
        sessionMutex.withLock {
            dataResult {
                val normalizedEmail = normalizeEmail(email)
                if (BuildConfig.DEBUG && normalizedEmail == DEMO_ADMIN_EMAIL && password == DEMO_ADMIN_PASSWORD &&
                    userDao.getUserById(DEMO_ADMIN_ID) == null
                ) {
                    val credentials = credentialsFor(password)
                    val admin = User(DEMO_ADMIN_ID, "Administrador U-DICE", DEMO_ADMIN_EMAIL, Rol.ADMIN)
                    userDao.insertUser(admin.toEntity().copy(passwordSalt = credentials.salt, passwordHash = credentials.hash))
                }
                val user = userDao.getUserByEmail(normalizedEmail)
                val demo = BuildConfig.DEBUG && user?.id == DEMO_PUBLISHER_ID &&
                    user.passwordHash == null && password == "UDiceDemo2026!"
                val salt = user?.passwordSalt
                val hash = user?.passwordHash
                val matches = salt != null && hash != null && password.length in 8..128 &&
                    MessageDigest.isEqual(hash, derivePassword(password, salt))
                require(user != null && (BuildConfig.DEBUG || user.rol != Rol.ADMIN.name) && (demo || matches)) { "Correo o contraseña incorrectos." }
                localDataSource.setCurrentSession(user.id)
            }
        }
    }

    override suspend fun logout(): Result<Unit> = withContext(ioDispatcher) {
        sessionMutex.withLock { dataResult { localDataSource.clearCurrentSession() } }
    }

    override fun observeFacultyAccounts() = combine(currentUser, userDao.observeFacultyAccounts()) { user, accounts ->
        if (user?.rol == Rol.ADMIN) accounts.map { it.toDomain() } else emptyList()
    }.flowOn(ioDispatcher)

    override suspend fun createFacultyAccount(name: String, email: String, password: String, faculty: Facultad): Result<Unit> =
        asAdmin { insertAccount(name, email, password, faculty, null, Rol.ASO) }

    override suspend fun resetFacultyPassword(id: String, password: String): Result<Unit> =
        asAdmin {
            val credentials = credentialsFor(password)
            check(userDao.resetFacultyPassword(id, credentials.salt, credentials.hash) == 1) {
                "La cuenta de facultad ya no existe."
            }
        }

    override suspend fun deleteFacultyAccount(id: String): Result<Unit> =
        asAdmin {
            check(userDao.deleteFacultyAccount(id) == 1) { "La cuenta de facultad ya no existe." }
        }

    private suspend fun asAdmin(action: suspend () -> Unit): Result<Unit> = withContext(ioDispatcher) {
        sessionMutex.withLock {
            dataResult {
                check(getCurrentUser()?.rol == Rol.ADMIN) { "Se requiere una sesión de administrador." }
                action()
            }
        }
    }

    private suspend fun insertAccount(
        name: String, email: String, password: String, faculty: Facultad, career: String?, role: Rol
    ) {
        val normalizedEmail = normalizeEmail(email)
        require(name.isNotBlank()) { "Completa el nombre." }
        require(Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches()) { "Ingresa un correo válido." }
        require(normalizedEmail != DEMO_ADMIN_EMAIL && userDao.getUserByEmail(normalizedEmail) == null) {
            "Ese correo ya está registrado o reservado."
        }
        val credentials = credentialsFor(password)
        val user = User(UUID.randomUUID().toString(), name.trim(), normalizedEmail, role, faculty, career)
        userDao.insertUser(user.toEntity().copy(passwordSalt = credentials.salt, passwordHash = credentials.hash))
    }

    private fun normalizeEmail(email: String): String = email.trim().lowercase(Locale.ROOT)

    private data class Credentials(val salt: ByteArray, val hash: ByteArray)

    private fun credentialsFor(password: String): Credentials {
        require(password.length in 8..128) { "La contraseña debe tener entre 8 y 128 caracteres." }
        val salt = ByteArray(16).also(SecureRandom()::nextBytes)
        return Credentials(salt, derivePassword(password, salt))
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
