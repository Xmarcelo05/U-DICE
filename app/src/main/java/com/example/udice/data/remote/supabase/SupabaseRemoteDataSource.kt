package com.example.udice.data.remote.supabase

import com.example.udice.data.model.Alcance
import com.example.udice.data.model.Categoria
import com.example.udice.data.model.Facultad
import com.example.udice.data.model.Post
import com.example.udice.data.model.Rol
import com.example.udice.data.model.User
import com.example.udice.data.remote.FakeRemoteDataSource
import com.example.udice.data.remote.RemoteDataSource
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseRemoteDataSource @Inject constructor(
    private val client: SupabaseHttpClient,
    private val fallback: FakeRemoteDataSource
) : RemoteDataSource {

    private fun parseTimestamp(raw: String?): Long {
        if (raw.isNullOrBlank()) return System.currentTimeMillis()
        return runCatching {
            Instant.parse(raw).toEpochMilli()
        }.recoverCatching {
            raw.toLong()
        }.getOrDefault(System.currentTimeMillis())
    }

    override suspend fun getPosts(): Result<List<Post>> {
        val result = client.get("publicaciones", "select=*&order=fecha.desc")
        return result.mapCatching { jsonStr ->
            val array = JSONArray(jsonStr)
            val posts = mutableListOf<Post>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                posts.add(
                    Post(
                        id = obj.getString("id"),
                        autorId = obj.getString("autor_id"),
                        autorNombre = obj.getString("autor_nombre"),
                        titulo = obj.getString("titulo"),
                        descripcion = obj.getString("descripcion"),
                        categoria = runCatching { Categoria.valueOf(obj.getString("categoria")) }.getOrDefault(Categoria.AVISO),
                        facultad = Facultad.fromDisplayName(obj.getString("facultad")) ?: Facultad.CIENCIAS_VIDA_TECNOLOGIAS,
                        alcance = runCatching { Alcance.valueOf(obj.getString("alcance")) }.getOrDefault(Alcance.GENERAL),
                        carrera = if (obj.has("carrera") && !obj.isNull("carrera")) obj.getString("carrera") else null,
                        fecha = parseTimestamp(if (obj.has("fecha") && !obj.isNull("fecha")) obj.getString("fecha") else null),
                        imageUrl = if (obj.has("image_url") && !obj.isNull("image_url")) obj.getString("image_url") else null
                    )
                )
            }
            if (posts.isEmpty()) {
                fallback.getPosts().getOrThrow()
            } else {
                posts
            }
        }.recoverCatching {
            fallback.getPosts().getOrThrow()
        }
    }

    override suspend fun getPostById(id: String): Result<Post?> {
        val result = client.get("publicaciones", "id=eq.$id&select=*")
        return result.mapCatching { jsonStr ->
            val array = JSONArray(jsonStr)
            if (array.length() == 0) return@mapCatching fallback.getPostById(id).getOrNull()
            val obj = array.getJSONObject(0)
            Post(
                id = obj.getString("id"),
                autorId = obj.getString("autor_id"),
                autorNombre = obj.getString("autor_nombre"),
                titulo = obj.getString("titulo"),
                descripcion = obj.getString("descripcion"),
                categoria = runCatching { Categoria.valueOf(obj.getString("categoria")) }.getOrDefault(Categoria.AVISO),
                facultad = Facultad.fromDisplayName(obj.getString("facultad")) ?: Facultad.CIENCIAS_VIDA_TECNOLOGIAS,
                alcance = runCatching { Alcance.valueOf(obj.getString("alcance")) }.getOrDefault(Alcance.GENERAL),
                carrera = if (obj.has("carrera") && !obj.isNull("carrera")) obj.getString("carrera") else null,
                fecha = parseTimestamp(if (obj.has("fecha") && !obj.isNull("fecha")) obj.getString("fecha") else null),
                imageUrl = if (obj.has("image_url") && !obj.isNull("image_url")) obj.getString("image_url") else null
            )
        }.recoverCatching {
            fallback.getPostById(id).getOrNull()
        }
    }

    override suspend fun createPost(post: Post): Result<Post> {
        val json = JSONObject().apply {
            put("autor_id", post.autorId)
            put("autor_nombre", post.autorNombre)
            put("titulo", post.titulo)
            put("descripcion", post.descripcion)
            put("categoria", post.categoria.name)
            put("facultad", post.facultad.displayName)
            put("alcance", post.alcance.name)
            if (post.imageUrl != null) put("image_url", post.imageUrl)
        }
        val result = client.post("publicaciones", json.toString())
        return result.mapCatching { jsonStr ->
            val array = JSONArray(jsonStr)
            val created = array.getJSONObject(0)
            post.copy(
                id = created.getString("id"),
                fecha = parseTimestamp(if (created.has("fecha") && !created.isNull("fecha")) created.getString("fecha") else null)
            )
        }.recoverCatching {
            fallback.createPost(post).getOrThrow()
        }
    }

    override suspend fun updatePost(post: Post): Result<Post> {
        val json = JSONObject().apply {
            put("titulo", post.titulo)
            put("descripcion", post.descripcion)
            put("categoria", post.categoria.name)
            put("facultad", post.facultad.displayName)
            put("alcance", post.alcance.name)
            if (post.imageUrl != null) put("image_url", post.imageUrl)
        }
        val result = client.patch("publicaciones", "id=eq.${post.id}", json.toString())
        return result.mapCatching {
            post
        }.recoverCatching {
            fallback.updatePost(post).getOrThrow()
        }
    }

    override suspend fun deletePost(id: String): Result<Unit> {
        val result = client.delete("publicaciones", "id=eq.$id")
        return result.recoverCatching {
            fallback.deletePost(id).getOrThrow()
        }
    }

    override suspend fun getUsers(): Result<List<User>> {
        val result = client.get("facultades_cuentas", "select=*")
        return result.mapCatching { jsonStr ->
            val array = JSONArray(jsonStr)
            val users = mutableListOf<User>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                users.add(
                    User(
                        id = obj.getString("id"),
                        nombre = obj.getString("nombre_contacto"),
                        email = obj.getString("email"),
                        rol = Rol.ASO,
                        facultad = Facultad.fromDisplayName(obj.optString("facultad", "")) ?: Facultad.CIENCIAS_VIDA_TECNOLOGIAS,
                        carrera = if (obj.has("carrera") && !obj.isNull("carrera")) obj.getString("carrera") else null
                    )
                )
            }
            if (users.isEmpty()) fallback.getUsers().getOrThrow() else users
        }.recoverCatching {
            fallback.getUsers().getOrThrow()
        }
    }

    override suspend fun getUser(userId: String): Result<User?> {
        val result = client.get("facultades_cuentas", "id=eq.$userId&select=*")
        return result.mapCatching { jsonStr ->
            val array = JSONArray(jsonStr)
            if (array.length() == 0) return@mapCatching fallback.getUser(userId).getOrNull()
            val obj = array.getJSONObject(0)
            User(
                id = obj.getString("id"),
                nombre = obj.getString("nombre_contacto"),
                email = obj.getString("email"),
                rol = Rol.ASO,
                facultad = Facultad.fromDisplayName(obj.optString("facultad", "")) ?: Facultad.CIENCIAS_VIDA_TECNOLOGIAS,
                carrera = if (obj.has("carrera") && !obj.isNull("carrera")) obj.getString("carrera") else null
            )
        }.recoverCatching {
            fallback.getUser(userId).getOrNull()
        }
    }

    override suspend fun getPreferences(userId: String): Result<Map<String, Boolean>> {
        val result = client.get("preferencias_usuarios", "usuario_id=eq.$userId&select=*")
        return result.mapCatching { jsonStr ->
            val array = JSONArray(jsonStr)
            val map = mutableMapOf<String, Boolean>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                map[obj.getString("facultad")] = obj.getBoolean("interesado")
            }
            if (map.isEmpty()) fallback.getPreferences(userId).getOrThrow() else map
        }.recoverCatching {
            fallback.getPreferences(userId).getOrThrow()
        }
    }

    override suspend fun savePreferences(userId: String, preferences: Map<String, Boolean>): Result<Unit> {
        val jsonArray = JSONArray()
        preferences.forEach { (facultad, interesado) ->
            jsonArray.put(
                JSONObject().apply {
                    put("usuario_id", userId)
                    put("facultad", facultad)
                    put("interesado", interesado)
                }
            )
        }
        val result = client.post("preferencias_usuarios", jsonArray.toString(), preferReturn = false)
        return result.map { }.recoverCatching {
            fallback.savePreferences(userId, preferences).getOrThrow()
        }
    }

    override suspend fun getSavedPostIds(userId: String): Result<Set<String>> =
        fallback.getSavedPostIds(userId)

    override suspend fun savePost(userId: String, postId: String): Result<Unit> =
        fallback.savePost(userId, postId)

    override suspend fun unsavePost(userId: String, postId: String): Result<Unit> =
        fallback.unsavePost(userId, postId)
}
