package com.example.udice.data.repository

import com.example.udice.data.local.LocalDataSource
import com.example.udice.data.model.Post
import com.example.udice.data.model.Rol
import com.example.udice.data.remote.RemoteDataSource
import com.example.udice.di.IoDispatcher
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource,
    private val sessionRepository: SessionRepository,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : PostRepository {

    private val usesCloud = sessionRepository is CloudSessionRepository
    private val cacheMutex = Mutex()

    override val posts: Flow<List<Post>> = localDataSource.observePosts()

    override fun observePost(id: String): Flow<Post?> = localDataSource.observePostById(id)

    override suspend fun getPostById(id: String): Post? = withContext(ioDispatcher) {
        localDataSource.getPostById(id)
    }

    override suspend fun syncPosts(): Result<Unit> = withContext(ioDispatcher) {
        updateCache {
            if (usesCloud) {
                localDataSource.replacePosts(remoteDataSource.getPosts().getOrThrow())
            }
        }
    }

    override suspend fun createPost(post: Post): Result<Post> = withContext(ioDispatcher) {
        updateCache {
            val currentUser = sessionRepository.getCurrentUser()
                ?: throw IllegalStateException("Debes iniciar sesión para publicar")

            if (currentUser.rol != Rol.ASO) {
                throw SecurityException("Solo los usuarios con rol ASO pueden crear publicaciones")
            }

            val postToCreate = post.copy(
                autorId = currentUser.id,
                autorNombre = if (post.autorNombre.isNotBlank()) post.autorNombre else currentUser.nombre
            )

            val createdPost = if (!usesCloud) {
                postToCreate
            } else {
                remoteDataSource.createPost(postToCreate).getOrThrow()
            }

            // Persistir inmediatamente en Room (fuente única de verdad)
            localDataSource.insertPost(createdPost)
            createdPost
        }
    }

    override suspend fun updatePost(post: Post): Result<Post> = withContext(ioDispatcher) {
        updateCache {
            val currentUser = sessionRepository.getCurrentUser()
                ?: throw IllegalStateException("Debes iniciar sesión para editar publicaciones")

            if (currentUser.rol != Rol.ASO) {
                throw SecurityException("Solo los usuarios con rol ASO pueden editar publicaciones")
            }

            val existing = localDataSource.getPostById(post.id)
                ?: (if (usesCloud) remoteDataSource.getPostById(post.id).getOrThrow() else null)
                ?: throw NoSuchElementException("No se encontró la publicación con id ${post.id}")

            if (existing.autorId != currentUser.id) {
                throw SecurityException("Solo el autor puede modificar esta publicación")
            }

            val changes = existing.copy(
                titulo = post.titulo,
                descripcion = post.descripcion,
                categoria = post.categoria
            )
            val updatedPost = if (!usesCloud) {
                changes
            } else {
                remoteDataSource.updatePost(changes).getOrThrow()
            }

            localDataSource.insertPost(updatedPost)
            updatedPost
        }
    }

    override suspend fun deletePost(id: String): Result<Unit> = withContext(ioDispatcher) {
        updateCache {
            val currentUser = sessionRepository.getCurrentUser()
                ?: throw IllegalStateException("Debes iniciar sesión para eliminar publicaciones")

            if (currentUser.rol != Rol.ASO) {
                throw SecurityException("Solo los usuarios con rol ASO pueden eliminar publicaciones")
            }

            val existing = localDataSource.getPostById(id)
                ?: (if (usesCloud) remoteDataSource.getPostById(id).getOrThrow() else null)
                ?: throw NoSuchElementException("No se encontró la publicación con id $id")

            if (existing.autorId != currentUser.id) {
                throw SecurityException("Solo el autor puede eliminar esta publicación")
            }

            if (usesCloud) {
                remoteDataSource.deletePost(id).getOrThrow()
            }
            localDataSource.deletePostById(id)
        }
    }

    private suspend fun <T> updateCache(action: suspend () -> T): Result<T> =
        cacheMutex.withLock {
            runCatching { action() }.onFailure {
                if (it is CancellationException) throw it
            }
        }
}
