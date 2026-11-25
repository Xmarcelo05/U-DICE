package com.example.udice.data.repository

import android.net.Uri
import com.example.udice.data.local.LocalDataSource
import com.example.udice.data.model.MAX_PUBLICATION_IMAGES
import com.example.udice.data.model.Post
import com.example.udice.data.model.FeedCursor
import com.example.udice.data.model.PostPage
import com.example.udice.data.model.PostSearch
import com.example.udice.data.model.PostSearchPage
import com.example.udice.data.model.SEARCH_PAGE_SIZE
import com.example.udice.data.model.toSearchResult
import java.time.Instant
import java.text.Normalizer
import java.util.Locale
import kotlinx.coroutines.flow.first
import com.example.udice.data.model.Rol
import com.example.udice.data.remote.RemoteDataSource
import com.example.udice.data.remote.supabase.CloudRequestException
import com.example.udice.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.NonCancellable
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

    override suspend fun searchPosts(search: PostSearch, after: FeedCursor?): Result<PostSearchPage> = withContext(ioDispatcher) {
        if (usesCloud) return@withContext remoteDataSource.searchPosts(search, after)
        dataResult {
            val from = search.from?.let { Instant.parse(it).toEpochMilli() }
            val until = search.until?.let { Instant.parse(it).toEpochMilli() }
            val cursorTime = after?.let { Instant.parse(it.date).toEpochMilli() }
            val terms = search.query.normalizedForSearch().split(' ').filter(String::isNotEmpty)
            val matches = posts.first().asSequence()
                .filter { post ->
                    (search.category.isEmpty() || post.categoria.name == search.category) &&
                        (search.faculty.isEmpty() || post.facultad.name == search.faculty) &&
                        (from == null || post.fecha >= from) && (until == null || post.fecha < until) &&
                        (after == null || cursorTime == null || post.fecha < cursorTime ||
                            (post.fecha == cursorTime && post.id < after.id)) &&
                        terms.all { it in "${post.titulo} ${post.descripcion}".normalizedForSearch() }
                }
                .sortedWith(compareByDescending<Post> { it.fecha }.thenByDescending { it.id })
                .take(SEARCH_PAGE_SIZE)
                .toList()
            val next = matches.lastOrNull()?.takeIf { matches.size == SEARCH_PAGE_SIZE }?.let {
                FeedCursor(Instant.ofEpochMilli(it.fecha).toString(), it.id)
            }
            PostSearchPage(matches.map(Post::toSearchResult), next)
        }
    }

    override suspend fun loadPostIfMissing(id: String): Result<Unit> = withContext(ioDispatcher) {
        updateCache {
            if (usesCloud && localDataSource.getPostById(id) == null) {
                remoteDataSource.getPostById(id).getOrThrow()?.let { localDataSource.insertPost(it) }
            }
        }
    }

    override suspend fun syncPosts(): Result<Unit> = withContext(ioDispatcher) {
        updateCache {
            if (usesCloud) {
                localDataSource.replacePosts(remoteDataSource.getPosts().getOrThrow())
            }
        }
    }

    override suspend fun loadFeedPage(after: FeedCursor?): Result<PostPage> = withContext(ioDispatcher) {
        updateCache {
            if (!usesCloud) return@updateCache PostPage(localDataSource.observePosts().first(), null)
            val page = remoteDataSource.getFeedPage(after).getOrThrow()
            localDataSource.insertPosts(page.posts)
            page
        }
    }

    override suspend fun createPost(
        post: Post,
        photos: List<Uri>,
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

            require(photos.size <= MAX_PUBLICATION_IMAGES) { "Adjunta hasta cuatro imágenes." }
            require(photos.isEmpty() || video == null) { "Adjunta imágenes o un video." }
            val uploadedVideo = video?.let { images.saveVideo(it, currentUser.id, sessionRepository, onUploadProgress) }
            val uploadedPhotos = uploadPhotos(photos, currentUser.id)
            val completePost = postToCreate.copy(
                imageUrl = uploadedVideo?.thumbnailUrl ?: uploadedPhotos.firstOrNull() ?: postToCreate.imageUrl,
                additionalImageUrls = if (uploadedPhotos.isNotEmpty()) uploadedPhotos.drop(1) else postToCreate.additionalImageUrls,
                videoUrl = uploadedVideo?.videoUrl ?: postToCreate.videoUrl
            )
            val createdPost = if (usesCloud) {
                remoteDataSource.createPost(completePost).getOrElse { error ->
                    // Ante un error de transporte, el servidor podría haber guardado el aviso.
                    if (error is CloudRequestException && error.status in 400..499) {
                        removeMedia(uploadedPhotos + listOfNotNull(uploadedVideo?.thumbnailUrl, uploadedVideo?.videoUrl), currentUser.id)
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

    override suspend fun updateMedia(post: Post, edit: PostMediaEdit): Result<Post> = withContext(ioDispatcher) {
        updateCache {
            val user = sessionRepository.getCurrentUser()
                ?: error("Debes iniciar sesión para editar publicaciones.")
            check(user.rol == Rol.ASO) { "Solo una cuenta ASO puede editar publicaciones." }
            val existing = if (usesCloud) remoteDataSource.getPostById(post.id).getOrThrow()
                else localDataSource.getPostById(post.id)
            check(existing != null && existing.autorId == user.id) { "Solo el autor puede editar la multimedia." }

            val existingPhotos = if (existing.videoUrl == null) existing.imageUrls else emptyList()
            require(edit.retainedPhotos.all { it in existingPhotos } && (!edit.keepVideo || existing.videoUrl != null)) {
                "La multimedia cambió. Vuelve a abrir el editor."
            }
            require(edit.retainedPhotos.size + edit.newPhotos.size <= MAX_PUBLICATION_IMAGES) {
                "Adjunta hasta cuatro imágenes."
            }
            val hasPhotos = edit.retainedPhotos.isNotEmpty() || edit.newPhotos.isNotEmpty()
            require(!hasPhotos || (!edit.keepVideo && edit.newVideo == null)) { "Adjunta imágenes o un video." }
            require(!edit.keepVideo || edit.newVideo == null) { "Adjunta un solo video." }

            val video = edit.newVideo?.let { images.saveVideo(it, user.id, sessionRepository) {} }
            val uploaded = uploadPhotos(edit.newPhotos, user.id)
            val photos = edit.retainedPhotos + uploaded
            val changes = existing.copy(
                titulo = post.titulo,
                descripcion = post.descripcion,
                categoria = post.categoria,
                allowedReactionIds = post.allowedReactionIds,
                imageUrl = video?.thumbnailUrl ?: if (edit.keepVideo) existing.imageUrl else photos.firstOrNull(),
                additionalImageUrls = photos.drop(1),
                videoUrl = video?.videoUrl ?: existing.videoUrl.takeIf { edit.keepVideo }
            )
            val updated = if (usesCloud) {
                remoteDataSource.updatePostMedia(changes).getOrElse { error ->
                    if (error is CloudRequestException && error.status in 400..499) {
                        removeMedia(uploaded + listOfNotNull(video?.thumbnailUrl, video?.videoUrl), user.id)
                    }
                    throw error
                }
            } else changes

            localDataSource.insertPost(updated)
            val previous = existing.imageUrls + listOfNotNull(existing.videoUrl)
            val current = updated.imageUrls + listOfNotNull(updated.videoUrl)
            removeMedia(previous - current.toSet(), user.id)
            updated
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
            removeMedia(existing.imageUrls + listOfNotNull(existing.videoUrl), currentUser.id)
        }
    }

    private suspend fun uploadPhotos(photos: List<Uri>, userId: String): List<String> {
        val uploaded = mutableListOf<String>()
        try {
            for (photo in photos) {
                uploaded += images.save(photo, userId, sessionRepository)
            }
        } catch (error: Exception) {
            withContext(NonCancellable) { removeMedia(uploaded, userId) }
            throw error
        }
        return uploaded
    }

    private suspend fun removeMedia(urls: List<String>, userId: String) {
        for (url in urls) dataResult { images.remove(url, userId, sessionRepository) }
    }

    private suspend fun <T> updateCache(action: suspend () -> T): Result<T> =
        cacheMutex.withLock {
            dataResult { action() }
        }
}

private val searchAccents = Regex("\\p{M}+")
internal fun String.normalizedForSearch() =
    Normalizer.normalize(lowercase(Locale.ROOT), Normalizer.Form.NFD).replace(searchAccents, "")
