package com.example.udice.data.repository

import android.net.Uri
import com.example.udice.data.local.LocalDataSource
import com.example.udice.data.model.Post
import com.example.udice.data.model.Rol
import com.example.udice.data.remote.RemoteDataSource
import com.example.udice.data.remote.supabase.CloudRequestException
import com.example.udice.di.IoDispatcher
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
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val images: PublicationImageStorage
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

    override suspend fun createPost(
        post: Post,
        image: Uri?,
        video: Uri?,
        onUploadProgress: (Int) -> Unit
    ): Result<Post> = withContext(ioDispatcher) {
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

            require(image == null || video == null) { "Adjunta una imagen o un video." }
            val uploadedVideo = video?.let { images.saveVideo(it, currentUser.id, sessionRepository, onUploadProgress) }
            val imageUrl = uploadedVideo?.thumbnailUrl ?: image?.let { images.save(it, currentUser.id, sessionRepository) }
            val completePost = postToCreate.copy(
                imageUrl = imageUrl ?: postToCreate.imageUrl,
                videoUrl = uploadedVideo?.videoUrl ?: postToCreate.videoUrl
            )
            val createdPost = if (usesCloud) {
                remoteDataSource.createPost(completePost).getOrElse { error ->
                    // Ante un error de transporte, el servidor podría haber guardado el aviso.
                    if (error is CloudRequestException && error.status in 400..499) {
                        removeMedia(listOfNotNull(imageUrl, uploadedVideo?.videoUrl), currentUser.id)
                    }
                    throw error
                }
            } else {
                completePost
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
                categoria = post.categoria,
                allowedReactionIds = post.allowedReactionIds
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
            removeMedia(listOfNotNull(existing.imageUrl, existing.videoUrl), currentUser.id)
        }
    }

    private suspend fun removeMedia(urls: List<String>, userId: String) {
        for (url in urls) dataResult { images.remove(url, userId, sessionRepository) }
    }

    private suspend fun <T> updateCache(action: suspend () -> T): Result<T> =
        cacheMutex.withLock {
            dataResult { action() }
        }
}
