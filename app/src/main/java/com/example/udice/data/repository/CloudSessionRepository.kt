package com.example.udice.data.repository

import com.example.udice.data.model.Facultad
import com.example.udice.data.model.Rol
import com.example.udice.data.model.User
import com.example.udice.data.remote.supabase.CloudHttpClient
import com.example.udice.data.remote.supabase.CloudRequestException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.json.JSONObject

@Singleton
class CloudSessionRepository @Inject constructor(private val http: CloudHttpClient) : SessionRepository {
    private data class Tokens(val access: String, val refresh: String, val expiresAt: Long)

    private val mutex = Mutex()
    private val user = MutableStateFlow<User?>(null)
    private val facultyAccounts = MutableStateFlow<List<User>>(emptyList())
    private var tokens: Tokens? = null

    override val currentUser = user.asStateFlow()
    override suspend fun getCurrentUser() = user.value
    override fun observeFacultyAccounts() = facultyAccounts.asStateFlow()

    override suspend fun register(
        name: String, email: String, password: String, faculty: Facultad, career: String
    ): Result<Unit> = cloudResult {
        require(name.isNotBlank() && career.isNotBlank()) { "Completa el nombre y la carrera." }
        require(password.length in 8..128) { "La contraseña debe tener entre 8 y 128 caracteres." }
        http.request("auth/v1/signup", "POST", body = JSONObject()
            .put("email", email.trim())
            .put("password", password)
            .put("data", JSONObject().put("nombre", name.trim()).put("facultad", faculty.name).put("carrera", career.trim())))
    }

    override suspend fun login(email: String, password: String): Result<Unit> = cloudResult {
        mutex.withLock {
            val newTokens = requestTokens("password", JSONObject().put("email", email.trim()).put("password", password))
            val identity = JSONObject(http.request("auth/v1/user", token = newTokens.access)).getString("id")
            val rows = JSONArray(http.request(
                "rest/v1/perfiles_udice?id=eq.$identity&select=id,email,nombre,rol,facultad,carrera",
                token = newTokens.access
            ))
            val profile = rows.optJSONObject(0)?.toUser()
                ?: error("La cuenta no tiene un perfil de U-DICE. Revisa el despliegue de la migración.")
            tokens = newTokens
            user.value = profile
        }
    }

    override suspend fun logout(): Result<Unit> {
        mutex.withLock {
            try {
                tokens?.let { http.request("auth/v1/logout?scope=local", "POST", it.access) }
            } catch (_: java.io.IOException) {
                // The local session must close even when the device is offline.
            } finally {
                clearSession()
            }
        }
        return Result.success(Unit)
    }

    override suspend fun refreshFacultyAccounts(): Result<Unit> = cloudResult {
        loadFacultyAccounts()
    }

    override suspend fun createFacultyAccount(name: String, email: String, password: String, faculty: Facultad) =
        manage("create", JSONObject().put("nombre", name.trim()).put("email", email.trim())
            .put("password", password).put("facultad", faculty.name))

    override suspend fun resetFacultyPassword(id: String, password: String) =
        manage("reset", JSONObject().put("id", id).put("password", password))

    override suspend fun deleteFacultyAccount(id: String) = manage("delete", JSONObject().put("id", id))

    private suspend fun loadFacultyAccounts() {
        signedRequest { token ->
            val response = http.request(
                "rest/v1/perfiles_udice?rol=eq.ASO&select=id,email,nombre,rol,facultad,carrera&order=nombre",
                token = token
            )
            val rows = JSONArray(response)
            facultyAccounts.value = (0 until rows.length()).map { rows.getJSONObject(it).toUser() }
        }
    }

    private suspend fun manage(action: String, body: JSONObject): Result<Unit> = cloudResult {
        signedRequest { token ->
            http.request("functions/v1/udice-faculty-admin", "POST", token, body.put("action", action))
            if (action == "delete") facultyAccounts.value = facultyAccounts.value.filterNot { it.id == body.getString("id") }
        }
        // A read failure must not report a successfully completed mutation as failed.
        refreshFacultyAccounts()
    }

    suspend fun <T> signedRequest(request: suspend (String) -> T): T = mutex.withLock {
        val current = tokens ?: error("Vuelve a iniciar sesión.")
        val active = if (System.currentTimeMillis() >= current.expiresAt - 30_000) {
            try {
                requestTokens("refresh_token", JSONObject().put("refresh_token", current.refresh)).also { tokens = it }
            } catch (e: CloudRequestException) {
                if (e.status == 400 || e.status == 401 || e.status == 403) clearSession()
                throw e
            }
        } else current
        request(active.access)
    }

    private suspend fun requestTokens(grant: String, body: JSONObject): Tokens {
        val result = JSONObject(http.request("auth/v1/token?grant_type=$grant", "POST", body = body))
        return Tokens(result.getString("access_token"), result.getString("refresh_token"),
            System.currentTimeMillis() + result.getLong("expires_in") * 1_000)
    }

    private fun clearSession() {
        tokens = null
        facultyAccounts.value = emptyList()
        user.value = null
    }

    private fun JSONObject.toUser() = User(
        getString("id"), getString("nombre"), getString("email"), Rol.valueOf(getString("rol")),
        if (isNull("facultad")) null else Facultad.valueOf(getString("facultad")),
        if (isNull("carrera")) null else getString("carrera")
    )
}

internal suspend fun cloudResult(action: suspend () -> Unit): Result<Unit> =
    try {
        action()
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
