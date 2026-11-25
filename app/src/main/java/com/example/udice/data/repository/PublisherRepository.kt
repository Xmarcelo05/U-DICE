package com.example.udice.data.repository

import com.example.udice.data.model.Facultad
import com.example.udice.data.model.FeedCursor
import com.example.udice.data.remote.supabase.CloudHttpClient
import com.example.udice.data.remote.supabase.SupabaseConfig
import com.example.udice.di.IoDispatcher
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class Publisher(val id: String, val name: String, val faculty: String?, val avatar: String?, val followers: Long? = null)
data class FollowTotals(val following: Long, val followers: Long)
data class PublisherProfile(val publisher: Publisher, val following: Boolean, val own: Boolean)
data class PublisherPost(val id: String, val title: String, val category: String, val date: Long)
data class PeoplePage(val people: List<Publisher>, val next: String?)
data class PublisherPostsPage(val posts: List<PublisherPost>, val next: FeedCursor?)

@Singleton
class PublisherRepository @Inject constructor(
    private val session: CloudSessionRepository,
    private val http: CloudHttpClient,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    private val changes = MutableStateFlow(0L)
    val followChanges = changes.asStateFlow()

    suspend fun search(text: String, after: String?): Result<PeoplePage> = query {
        val rows = JSONArray(call("udice_buscar_personas",
            JSONObject().put("texto", text).put("despues", after ?: JSONObject.NULL)))
        val people = List(rows.length()) { rows.getJSONObject(it).publisher() }
        PeoplePage(people, people.lastOrNull()?.id?.takeIf { people.size == 20 })
    }

    suspend fun profile(id: String): Result<PublisherProfile> = query {
        val rows = JSONArray(call("udice_perfil_publicador", JSONObject().put("destino", id)))
        val row = rows.optJSONObject(0) ?: error("El perfil del publicador no está disponible.")
        PublisherProfile(row.publisher(), row.getBoolean("siguiendo"), row.getBoolean("propia"))
    }

    suspend fun posts(id: String, after: FeedCursor?): Result<PublisherPostsPage> = query {
        val rows = JSONArray(call("udice_avisos_publicador", JSONObject().put("destino", id)
            .put("cursor_fecha", after?.date ?: JSONObject.NULL).put("cursor_id", after?.id ?: JSONObject.NULL)))
        val posts = List(rows.length()) { index ->
            val row = rows.getJSONObject(index)
            PublisherPost(row.getString("id"), row.getString("titulo"), row.getString("categoria"),
                Instant.parse(row.getString("fecha")).toEpochMilli())
        }
        val cursor = if (rows.length() == 20) rows.getJSONObject(19).let {
            FeedCursor(it.getString("fecha"), it.getString("id"))
        } else null
        PublisherPostsPage(posts, cursor)
    }

    suspend fun myFollowTotals(): Result<FollowTotals> = query {
        val result = JSONObject(call("udice_mis_totales_seguimiento", JSONObject()))
        FollowTotals(result.getLong("siguiendo"), result.getLong("seguidores"))
    }

    suspend fun myConnections(followers: Boolean, after: String?): Result<PeoplePage> = query {
        val rows = JSONArray(call("udice_mis_conexiones", JSONObject()
            .put("seguidores", followers).put("despues", after ?: JSONObject.NULL)))
        val people = List(rows.length()) { rows.getJSONObject(it).publisher() }
        PeoplePage(people, people.lastOrNull()?.id?.takeIf { people.size == 20 })
    }

    suspend fun setFollowing(id: String, following: Boolean): Result<Boolean> = query {
        val response = call("udice_seguir_publicador", JSONObject().put("destino", id).put("seguir", following))
        response.trim().toBooleanStrict().also { changes.update { it + 1 } }
    }

    private suspend fun call(function: String, body: JSONObject): String = session.signedRequest { token ->
        http.request("rest/v1/rpc/$function", "POST", token, body)
    }

    private suspend fun <T> query(action: suspend () -> T): Result<T> =
        withContext(ioDispatcher) { dataResult { action() } }

    private fun JSONObject.publisher(): Publisher {
        val faculty = if (isNull("facultad")) null else getString("facultad")
        return Publisher(getString("id"), getString("nombre"),
            Facultad.entries.find { it.name == faculty }?.displayName ?: faculty,
            if (isNull("avatar_path")) null else SupabaseConfig.avatarUrl(getString("avatar_path")),
            if (isNull("total_seguidores")) null else getLong("total_seguidores"))
    }
}
