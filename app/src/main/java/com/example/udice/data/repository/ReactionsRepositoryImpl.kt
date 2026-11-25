package com.example.udice.data.repository

import android.icu.lang.UCharacter
import android.icu.lang.UProperty
import android.icu.text.BreakIterator
import com.example.udice.data.remote.supabase.CloudHttpClient
import com.example.udice.data.local.dao.ReaccionDao
import com.example.udice.data.local.entity.ReaccionEntity
import com.example.udice.data.local.entity.ReactionTypeEntity
import com.example.udice.data.model.ReaccionSummary
import com.example.udice.data.model.ReactionCount
import com.example.udice.data.model.ReactionDetails
import com.example.udice.data.model.ReactionParticipant
import com.example.udice.data.remote.supabase.SupabaseConfig
import com.example.udice.data.model.Rol
import com.example.udice.data.model.TipoReaccion
import com.example.udice.data.remote.supabase.SupabaseHttpClient
import com.example.udice.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReactionsRepositoryImpl @Inject constructor(
    private val reaccionDao: ReaccionDao,
    private val supabaseClient: SupabaseHttpClient,
    private val sessionRepository: SessionRepository,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val cloudHttp: CloudHttpClient
) : ReactionsRepository {

    private val catalogMutex = Mutex()

    override val reactionTypes: Flow<List<TipoReaccion>> = reaccionDao.observeCustomTypes()
        .map { rows -> TipoReaccion.defaults + rows.map { TipoReaccion(it.id, it.label, it.emoji) } }
        .flowOn(ioDispatcher)

    override suspend fun addReactionType(label: String, emoji: String): Result<Unit> = withContext(ioDispatcher) {
        catalogMutex.withLock {
            dataResult {
                check(sessionRepository.getCurrentUser()?.rol == Rol.ADMIN) {
                    "Se requiere una sesión de administrador."
                }
                val name = label.trim()
                val symbol = emoji.trim()
                require(name.length in 1..30) { "El nombre debe tener entre 1 y 30 caracteres." }
                val graphemes = BreakIterator.getCharacterInstance().apply { setText(symbol) }
                require(symbol.length in 1..20 && graphemes.next() == symbol.length && symbol.codePoints().anyMatch {
                    UCharacter.hasBinaryProperty(it, UProperty.EMOJI_PRESENTATION) ||
                        UCharacter.hasBinaryProperty(it, UProperty.EXTENDED_PICTOGRAPHIC) || it == 0x20E3
                }) { "Ingresa un emoji válido." }
                require(reactionTypes.first().none { it.displayName.equals(name, ignoreCase = true) || it.emoji == symbol }) {
                    "Ya existe una reacción con ese nombre o emoji."
                }
                if (sessionRepository is CloudSessionRepository) {
                    sessionRepository.signedRequest { token ->
                        cloudHttp.request("rest/v1/tipos_reaccion_udice", "POST", token, JSONObject()
                            .put("label", name).put("emoji", symbol))
                    }
                    refreshReactionTypes()
                    return@dataResult
                }
                val id = "LOCAL_" + UUID.randomUUID().toString().replace("-", "").take(24)
                reaccionDao.insertReactionType(ReactionTypeEntity(id, name, symbol))
            }
        }
    }

    override suspend fun refreshReactionTypes(): Result<Unit> = withContext(ioDispatcher) {
        dataResult {
            if (sessionRepository is CloudSessionRepository) {
                sessionRepository.signedRequest { token ->
                    val rows = JSONArray(cloudHttp.request("rest/v1/tipos_reaccion_udice?select=id,label,emoji", token = token))
                    reaccionDao.cacheReactionTypes((0 until rows.length()).map { index ->
                        val row = rows.getJSONObject(index)
                        ReactionTypeEntity("LOCAL_" + row.getString("id"), row.getString("label"), row.getString("emoji"))
                    })
                }
            }
        }
    }

    override fun observeReactions(postId: String, currentUserId: String): Flow<ReaccionSummary> =
        reaccionDao.observeReactionsForPost(postId).combine(reactionTypes) { entities, types ->
            buildSummary(entities, currentUserId, types.associateBy { it.id })
        }.flowOn(ioDispatcher)

    override fun observeAllReactions(currentUserId: String): Flow<Map<String, ReaccionSummary>> =
        reaccionDao.observeAllReactions().combine(reactionTypes) { entities, types ->
            val typesById = types.associateBy { it.id }
            entities.groupBy { it.postId }.mapValues { (_, reactions) ->
                buildSummary(reactions, currentUserId, typesById)
            }
        }.flowOn(ioDispatcher)

    private fun buildSummary(
        entities: List<ReaccionEntity>,
        currentUserId: String,
        types: Map<String, TipoReaccion>
    ): ReaccionSummary {
        var userReaction: TipoReaccion? = null
        val counts = mutableMapOf<TipoReaccion, Int>()

        for (e in entities) {
            val tipo = types[e.tipo] ?: continue
            counts[tipo] = (counts[tipo] ?: 0) + 1
            if (e.userId == currentUserId) {
                userReaction = tipo
            }
        }

        return ReaccionSummary(
            userReaction = userReaction,
            counts = counts,
            totalCount = counts.values.sum()
        )
    }

    private val voteMutex = Mutex()

    private suspend fun saveCloudVote(
        session: CloudSessionRepository, postId: String, userId: String, type: TipoReaccion
    ) = voteMutex.withLock {
        require(runCatching { UUID.fromString(postId) }.isSuccess) { "Este aviso de demostración solo existe en el dispositivo." }
        val previous = reaccionDao.observeUserReaction(userId, postId).firstOrNull()
        val remove = previous?.tipo == type.id
        val body = JSONObject().put("aviso", postId)
            .put("tipo", if (remove) JSONObject.NULL else type.id.removePrefix("LOCAL_"))
        session.signedRequest { token ->
            cloudHttp.request("rest/v1/rpc/udice_guardar_reaccion", "POST", token, body)
        }
        if (remove) reaccionDao.deleteReaccion(userId, postId)
        else reaccionDao.insertReaccion(ReaccionEntity(postId, userId, type.id, System.currentTimeMillis()))
    }

    private suspend fun loadCloudVotes(session: CloudSessionRepository) = voteMutex.withLock {
        val rows = mutableListOf<ReaccionEntity>()
        session.signedRequest { token ->
            while (true) {
                val path = "rest/v1/reacciones?select=publicacion_id,usuario_id,tipo_reaccion" +
                    "&order=publicacion_id,usuario_id&limit=500&offset=${rows.size}"
                val page = JSONArray(cloudHttp.request(path, token = token))
                if (page.length() == 0) break
                for (index in 0 until page.length()) {
                    val row = page.getJSONObject(index)
                    val type = row.getString("tipo_reaccion")
                    rows.add(ReaccionEntity(row.getString("publicacion_id"), row.getString("usuario_id"),
                        if (TipoReaccion.defaults.any { it.id == type }) type else "LOCAL_$type",
                        System.currentTimeMillis()))
                }
            }
        }
        reaccionDao.replaceCloudVotes(rows)
    }

    override suspend fun toggleReaction(
        postId: String,
        userId: String,
        tipo: TipoReaccion
    ): Result<Unit> = withContext(ioDispatcher) {
        dataResult {
            require(reactionTypes.first().any { it.id == tipo.id }) { "La reacción ya no está disponible." }
            if (sessionRepository is CloudSessionRepository) {
                saveCloudVote(sessionRepository, postId, userId, tipo)
                return@dataResult
            }
            val currentReaction = reaccionDao.observeUserReaction(userId, postId).firstOrNull()

            if (currentReaction != null && currentReaction.tipo == tipo.id) {
                reaccionDao.deleteReaccion(userId, postId)
                if (!tipo.id.startsWith("LOCAL_")) runCatching {
                    supabaseClient.delete("reacciones", "publicacion_id=eq.$postId&usuario_id=eq.$userId")
                }
            } else {
                val newEntity = ReaccionEntity(
                    postId = postId,
                    userId = userId,
                    tipo = tipo.id,
                    createdAt = System.currentTimeMillis()
                )
                reaccionDao.insertReaccion(newEntity)

                if (tipo.id.startsWith("LOCAL_")) {
                    if (currentReaction != null && !currentReaction.tipo.startsWith("LOCAL_")) {
                        supabaseClient.delete("reacciones", "publicacion_id=eq.$postId&usuario_id=eq.$userId")
                    }
                } else runCatching {
                    val body = JSONObject().apply {
                        put("publicacion_id", postId)
                        put("usuario_id", userId)
                        put("tipo_reaccion", tipo.id)
                    }
                    supabaseClient.post("reacciones", body.toString())
                }
            }
            Unit
        }
    }

    override suspend fun reactionDetails(
        postId: String, includePeople: Boolean, type: String?, after: String?
    ): Result<ReactionDetails> = withContext(ioDispatcher) {
        dataResult {
            val session = sessionRepository as? CloudSessionRepository
                ?: return@dataResult ReactionDetails(emptyList(), emptyList(), null)
            val body = JSONObject().put("aviso", postId).put("incluir_personas", includePeople)
                .put("tipo_filtro", type ?: JSONObject.NULL).put("cursor_usuario", after ?: JSONObject.NULL)
            val result = session.signedRequest { token ->
                JSONObject(cloudHttp.request("rest/v1/rpc/udice_detalle_reacciones", "POST", token, body))
            }
            val totals = result.getJSONArray("totales")
            val counts = List(totals.length()) { index ->
                val row = totals.getJSONObject(index)
                ReactionCount(row.getString("tipo"), row.getString("emoji"), row.getString("label"), row.getInt("total"))
            }
            val people = result.getJSONArray("personas")
            val participants = List(people.length()) { index ->
                val row = people.getJSONObject(index)
                ReactionParticipant(
                    row.getString("id"), row.getString("nombre"),
                    if (row.isNull("avatar_path")) null else SupabaseConfig.avatarUrl(row.getString("avatar_path")),
                    row.getString("emoji")
                )
            }
            ReactionDetails(counts, participants, if (result.isNull("siguiente")) null else result.getString("siguiente"))
        }
    }

    override suspend fun syncReactions(): Result<Unit> = withContext(ioDispatcher) {
        refreshReactionTypes().getOrElse { return@withContext Result.failure(it) }
        dataResult {
            if (sessionRepository is CloudSessionRepository) {
                loadCloudVotes(sessionRepository)
                return@dataResult
            }
            val result = supabaseClient.get("reacciones", "select=*")
            result.getOrNull()?.let { jsonStr ->
                val array = JSONArray(jsonStr)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val type = obj.getString("tipo_reaccion")
                    if (TipoReaccion.defaults.any { it.id == type }) {
                        reaccionDao.insertSyncedReaction(
                            obj.getString("publicacion_id"),
                            obj.getString("usuario_id"),
                            type,
                            System.currentTimeMillis()
                        )
                    }
                }
            }
            Unit
        }
    }
}
