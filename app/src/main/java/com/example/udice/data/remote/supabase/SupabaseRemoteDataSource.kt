package com.example.udice.data.remote.supabase

import com.example.udice.BuildConfig
import com.example.udice.data.repository.CloudSessionRepository
import com.example.udice.data.repository.dataResult
import com.example.udice.data.model.Alcance
import com.example.udice.data.model.Categoria
import com.example.udice.data.model.Facultad
import com.example.udice.data.model.Post
import com.example.udice.data.model.Rol
import com.example.udice.data.model.TipoReaccion
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
    private val cloud: CloudHttpClient,
    private val session: CloudSessionRepository,
    private val fallback: FakeRemoteDataSource
) : RemoteDataSource {

    private fun parseTimestamp(raw: String?): Long {
        if (raw.isNullOrBlank()) return System.currentTimeMillis()
        return runCatching {
            Instant.parse(raw).toEpochMilli()
        }.recoverCatching {
            if (BuildConfig.UDICE_SUPABASE) throw it
            raw.toLong()
        }.getOrDefault(System.currentTimeMillis())
    }

    private suspend fun cloudPosts(
        method: String, query: String = "", body: JSONObject? = null
    ): Result<String> = dataResult {
        session.signedRequest { token ->
            val path = "rest/v1/publicaciones" + if (query.isEmpty()) "" else "?$query"
            val rows = cloud.request(path, method, token, body, returnRow = method != "GET")
            if (method != "GET") check(JSONArray(rows).length() == 1) { "No se pudo modificar el aviso." }
            rows
        }
    }

    private suspend fun getCloudPostRows(): Result<String> = dataResult {
        val rows = JSONArray()
        var query = "select=*&order=id.asc"
        while (true) {
            val page = JSONArray(cloudPosts("GET", query).getOrThrow())
            if (page.length() == 0) break
            for (index in 0 until page.length()) {
                rows.put(page.getJSONObject(index))
            }
            val lastId = page.getJSONObject(page.length() - 1).getString("id")
            query = "select=*&order=id.asc&id=gt.$lastId"
        }
        rows.toString()
    }

    private fun JSONObject.reactionIds(): List<String>? =
        optJSONArray("reacciones_permitidas")?.let { ids ->
            List(ids.length()) { index ->
                val id = ids.getString(index)
                if (TipoReaccion.defaults.any { it.id == id }) id else "LOCAL_$id"
            }
        }

    private fun JSONObject.additionalImages(): List<String> =
        optJSONArray("additional_image_urls")?.let { urls ->
            List(urls.length()) { urls.getString(it) }
        }.orEmpty()

    override suspend fun getPosts(): Result<List<Post>> {
        val result = if (BuildConfig.UDICE_SUPABASE) getCloudPostRows()
            else client.get("publicaciones", "select=*&order=fecha.desc")
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
                        imageUrl = if (obj.has("image_url") && !obj.isNull("image_url")) obj.getString("image_url") else null,
                        allowedReactionIds = obj.reactionIds(),
                        videoUrl = if (obj.isNull("video_url")) null else obj.getString("video_url"),
                        additionalImageUrls = obj.additionalImages()
                    )
                )
            }
            if (posts.isEmpty() && !BuildConfig.UDICE_SUPABASE) {
                fallback.getPosts().getOrThrow()
            } else {
                posts
            }
        }.recoverCatching {
            if (BuildConfig.UDICE_SUPABASE) throw it
            fallback.getPosts().getOrThrow()
        }
    }

    override suspend fun getPostById(id: String): Result<Post?> {
        val result = if (BuildConfig.UDICE_SUPABASE) cloudPosts("GET", "id=eq.$id&select=*")
            else client.get("publicaciones", "id=eq.$id&select=*")
        return result.mapCatching { jsonStr ->
            val array = JSONArray(jsonStr)
            if (array.length() == 0) return@mapCatching if (BuildConfig.UDICE_SUPABASE) null else fallback.getPostById(id).getOrNull()
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
                imageUrl = if (obj.has("image_url") && !obj.isNull("image_url")) obj.getString("image_url") else null,
                allowedReactionIds = obj.reactionIds(),
                videoUrl = if (obj.isNull("video_url")) null else obj.getString("video_url"),
                additionalImageUrls = obj.additionalImages()
            )
        }.recoverCatching {
            if (BuildConfig.UDICE_SUPABASE) throw it
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
        if (post.videoUrl != null) json.put("video_url", post.videoUrl)
        if (post.additionalImageUrls.isNotEmpty()) json.put("additional_image_urls", JSONArray(post.additionalImageUrls))
        val result = if (BuildConfig.UDICE_SUPABASE) cloudPosts("POST", body = json.put("id", post.id))
            else client.post("publicaciones", json.toString())
        return result.mapCatching { jsonStr ->
            val array = JSONArray(jsonStr)
            val created = array.getJSONObject(0)
            post.copy(
                id = created.getString("id"),
                fecha = parseTimestamp(if (created.has("fecha") && !created.isNull("fecha")) created.getString("fecha") else null)
            )
        }.recoverCatching {
            if (BuildConfig.UDICE_SUPABASE) throw it
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
        val result = if (BuildConfig.UDICE_SUPABASE) {
            val allowed = post.allowedReactionIds?.map { it.removePrefix("LOCAL_") }
            json.put("reacciones_permitidas", allowed?.let { JSONArray(it) } ?: JSONObject.NULL)
            cloudPosts("PATCH", "id=eq.${post.id}", json)
        }
            else client.patch("publicaciones", "id=eq.${post.id}", json.toString())
        return result.mapCatching {
            post
        }.recoverCatching {
            if (BuildConfig.UDICE_SUPABASE) throw it
            fallback.updatePost(post).getOrThrow()
        }
    }

    override suspend fun deletePost(id: String): Result<Unit> {
        if (BuildConfig.UDICE_SUPABASE) return cloudPosts("DELETE", "id=eq.$id").map { Unit }
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
