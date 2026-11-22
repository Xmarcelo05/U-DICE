package com.example.udice.data.repository

import com.example.udice.data.local.LocalDataSource
import com.example.udice.data.model.Post
import com.example.udice.data.model.Rol
import com.example.udice.data.remote.RemoteDataSource
import com.example.udice.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource,
    private val sessionRepository: SessionRepository,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : PostRepository {

    override val posts: Flow<List<Post>> = localDataSource.observePosts()

    override fun observePost(id: String): Flow<Post?> = localDataSource.observePostById(id)

    override suspend fun getPostById(id: String): Post? = withContext(ioDispatcher) {
        localDataSource.getPostById(id)
    }

    override suspend fun syncPosts(): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            val remoteResult = remoteDataSource.getPosts()
            val remotePosts = remoteResult.getOrThrow()
            localDataSource.insertPosts(remotePosts)
        }
    }

    override suspend fun createPost(post: Post): Result<Post> = withContext(ioDispatcher) {
        runCatching {
            val currentUser = sessionRepository.getCurrentUser()
                ?: throw IllegalStateException("Debes iniciar sesión para publicar")

            if (currentUser.rol != Rol.ASO) {
                throw SecurityException("Solo los usuarios con rol ASO pueden crear publicaciones")
            }

            val postToCreate = post.copy(
                autorId = currentUser.id,
                autorNombre = if (post.autorNombre.isNotBlank()) post.autorNombre else currentUser.nombre
            )

            val remoteResult = remoteDataSource.createPost(postToCreate)
            val createdPost = remoteResult.getOrThrow()

            // Persistir inmediatamente en Room (fuente única de verdad)
            localDataSource.insertPost(createdPost)
            createdPost
        }
    }

    override suspend fun updatePost(post: Post): Result<Post> = withContext(ioDispatcher) {
        runCatching {
            val currentUser = sessionRepository.getCurrentUser()
                ?: throw IllegalStateException("Debes iniciar sesión para editar publicaciones")

            if (currentUser.rol != Rol.ASO) {
                throw SecurityException("Solo los usuarios con rol ASO pueden editar publicaciones")
            }

            val existing = localDataSource.getPostById(post.id)
                ?: remoteDataSource.getPostById(post.id).getOrNull()
                ?: throw NoSuchElementException("No se encontró la publicación con id ${post.id}")

            if (existing.autorId != currentUser.id) {
                throw SecurityException("Solo el autor puede modificar esta publicación")
            }

            val remoteResult = remoteDataSource.updatePost(post)
            val updatedPost = remoteResult.getOrThrow()

            localDataSource.updatePost(updatedPost)
            updatedPost
        }
    }

    override suspend fun deletePost(id: String): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            val currentUser = sessionRepository.getCurrentUser()
                ?: throw IllegalStateException("Debes iniciar sesión para eliminar publicaciones")

            if (currentUser.rol != Rol.ASO) {
                throw SecurityException("Solo los usuarios con rol ASO pueden eliminar publicaciones")
            }

            val existing = localDataSource.getPostById(id)
                ?: remoteDataSource.getPostById(id).getOrNull()
                ?: throw NoSuchElementException("No se encontró la publicación con id $id")

            if (existing.autorId != currentUser.id) {
                throw SecurityException("Solo el autor puede eliminar esta publicación")
            }

            remoteDataSource.deletePost(id).getOrThrow()
            localDataSource.deletePostById(id)
        }
    }
}
