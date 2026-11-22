package com.example.udice.data.repository

import com.example.udice.data.local.dao.ReaccionDao
import com.example.udice.data.local.entity.ReaccionEntity
import com.example.udice.data.model.ReaccionSummary
import com.example.udice.data.model.TipoReaccion
import com.example.udice.data.remote.supabase.SupabaseHttpClient
import com.example.udice.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReactionsRepositoryImpl @Inject constructor(
    private val reaccionDao: ReaccionDao,
    private val supabaseClient: SupabaseHttpClient,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ReactionsRepository {

    override fun observeReactions(postId: String, currentUserId: String): Flow<ReaccionSummary> =
        reaccionDao.observeReactionsForPost(postId).map { entities ->
            buildSummary(entities, currentUserId)
        }.flowOn(ioDispatcher)

    override fun observeAllReactions(currentUserId: String): Flow<Map<String, ReaccionSummary>> =
        reaccionDao.observeAllReactions().map { allEntities ->
            val groupedByPost = allEntities.groupBy { it.postId }
            groupedByPost.mapValues { (_, entities) ->
                buildSummary(entities, currentUserId)
            }
        }.flowOn(ioDispatcher)

    private fun buildSummary(entities: List<ReaccionEntity>, currentUserId: String): ReaccionSummary {
        var userReaction: TipoReaccion? = null
        val counts = mutableMapOf<TipoReaccion, Int>()

        for (e in entities) {
            val tipo = runCatching { TipoReaccion.valueOf(e.tipo) }.getOrNull() ?: continue
            counts[tipo] = (counts[tipo] ?: 0) + 1
            if (e.userId == currentUserId) {
                userReaction = tipo
            }
        }

        return ReaccionSummary(
            userReaction = userReaction,
            counts = counts,
            totalCount = entities.size
        )
    }

    override suspend fun toggleReaction(
        postId: String,
        userId: String,
        tipo: TipoReaccion
    ): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            val currentReaction = reaccionDao.observeUserReaction(userId, postId).firstOrNull()

            if (currentReaction != null && currentReaction.tipo == tipo.name) {
                reaccionDao.deleteReaccion(userId, postId)
                runCatching {
                    supabaseClient.delete("reacciones", "publicacion_id=eq.$postId&usuario_id=eq.$userId")
                }
            } else {
                val newEntity = ReaccionEntity(
                    postId = postId,
                    userId = userId,
                    tipo = tipo.name,
                    createdAt = System.currentTimeMillis()
                )
                reaccionDao.insertReaccion(newEntity)

                runCatching {
                    val body = JSONObject().apply {
                        put("publicacion_id", postId)
                        put("usuario_id", userId)
                        put("tipo_reaccion", tipo.name)
                    }
                    supabaseClient.post("reacciones", body.toString())
                }
            }
            Unit
        }
    }

    override suspend fun syncReactions(): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            val result = supabaseClient.get("reacciones", "select=*")
            result.getOrNull()?.let { jsonStr ->
                val array = JSONArray(jsonStr)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val entity = ReaccionEntity(
                        postId = obj.getString("publicacion_id"),
                        userId = obj.getString("usuario_id"),
                        tipo = obj.getString("tipo_reaccion"),
                        createdAt = System.currentTimeMillis()
                    )
                    reaccionDao.insertReaccion(entity)
                }
            }
            Unit
        }
    }
}
