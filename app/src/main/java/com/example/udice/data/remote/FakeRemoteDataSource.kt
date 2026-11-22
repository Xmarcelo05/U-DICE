package com.example.udice.data.remote

import com.example.udice.data.model.Alcance
import com.example.udice.data.model.Categoria
import com.example.udice.data.model.Facultad
import com.example.udice.data.model.Post
import com.example.udice.data.model.Rol
import com.example.udice.data.model.User
import com.example.udice.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FakeRemoteDataSource @Inject constructor(
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : RemoteDataSource {

    private val mutex = Mutex()
    private val networkDelayMs = 500L

    private val users = mutableListOf(
        User(
            id = "user-aso-1",
            nombre = "Asociación Estudiantil Sistemas",
            email = "aso.sistemas@universidad.edu",
            rol = Rol.ASO,
            facultad = Facultad.CIENCIAS_VIDA_TECNOLOGIAS,
            carrera = "Ingeniería en Computación"
        ),
        User(
            id = "user-lector-1",
            nombre = "Estudiante Lector",
            email = "estudiante@universidad.edu",
            rol = Rol.LECTOR,
            facultad = Facultad.CIENCIAS_VIDA_TECNOLOGIAS,
            carrera = "Ingeniería en Computación"
        )
    )

    private val posts = mutableListOf(
        Post(
            id = "post-1",
            autorId = "user-aso-1",
            autorNombre = "Asociación Estudiantil Sistemas",
            titulo = "Hackathon Universitaria 2026",
            descripcion = "Participa en la 5ta edición de la hackathon interdisciplinaria. Premios para los 3 primeros lugares y mentoría de la industria tecnológica.",
            categoria = Categoria.EVENTO,
            facultad = Facultad.CIENCIAS_VIDA_TECNOLOGIAS,
            alcance = Alcance.GENERAL,
            carrera = null,
            fecha = System.currentTimeMillis() - 1000 * 60 * 60 * 4,
            imageUrl = null
        ),
        Post(
            id = "post-2",
            autorId = "user-aso-1",
            autorNombre = "Asociación Estudiantil Sistemas",
            titulo = "Convocatoria a Ayudantías de Cátedra",
            descripcion = "Abierta la recepción de carpetas para ayudantías en asignaturas de programación, física y matemáticas. Revisa los requisitos en secretaría.",
            categoria = Categoria.CONVOCATORIA,
            facultad = Facultad.CIENCIAS_VIDA_TECNOLOGIAS,
            alcance = Alcance.CARRERA,
            carrera = "Ingeniería en Computación",
            fecha = System.currentTimeMillis() - 1000 * 60 * 60 * 12,
            imageUrl = null
        ),
        Post(
            id = "post-3",
            autorId = "user-aso-1",
            autorNombre = "Asociación Estudiantil Sistemas",
            titulo = "Congreso Internacional de Salud y Biotecnología",
            descripcion = "Conferencias magistrales con ponentes de 8 países sobre innovaciones biomédicas, bioinformática y salud pública universitaria.",
            categoria = Categoria.CONFERENCIA,
            facultad = Facultad.CIENCIAS_SALUD,
            alcance = Alcance.GENERAL,
            carrera = null,
            fecha = System.currentTimeMillis() - 1000 * 60 * 60 * 24,
            imageUrl = null
        ),
        Post(
            id = "post-4",
            autorId = "user-aso-1",
            autorNombre = "Asociación Estudiantil Sistemas",
            titulo = "Actualización de Horarios y Aulas",
            descripcion = "Se informa a la comunidad estudiantil sobre los cambios de aulas para los laboratorios de cómputo a partir del próximo lunes.",
            categoria = Categoria.AVISO,
            facultad = Facultad.CIENCIAS_VIDA_TECNOLOGIAS,
            alcance = Alcance.GENERAL,
            carrera = null,
            fecha = System.currentTimeMillis() - 1000 * 60 * 60 * 36,
            imageUrl = null
        ),
        Post(
            id = "post-5",
            autorId = "user-aso-1",
            autorNombre = "Bienestar Universitario",
            titulo = "Campaña de Donación Solidaria de Sangre",
            descripcion = "Bienestar Universitario invita a toda la comunidad a participar en la jornada de donación solidaria en el campus central de 9:00 a 16:00.",
            categoria = Categoria.OTRO,
            facultad = Facultad.BIENESTAR_UNIVERSITARIO,
            alcance = Alcance.GENERAL,
            carrera = null,
            fecha = System.currentTimeMillis() - 1000 * 60 * 60 * 48,
            imageUrl = null
        )
    )

    private val preferences = mutableMapOf<String, MutableMap<String, Boolean>>(
        "user-aso-1" to mutableMapOf(
            "Ciencias de la Vida y Tecnologías" to true,
            "Ciencias de la Salud" to true,
            "Ingeniería, Industria y Construcción" to false,
            "Facultad de Ciencias Administrativas, Contables y Comercio" to false,
            "Facultad de Educación, Turismo, Artes y Humanidades" to false,
            "Facultad de Ciencias Sociales, Derecho y Bienestar" to false,
            "Bienestar Universitario" to true,
            "Instituto de Idiomas" to false
        ),
        "user-lector-1" to mutableMapOf(
            "Ciencias de la Vida y Tecnologías" to true,
            "Ciencias de la Salud" to false,
            "Ingeniería, Industria y Construcción" to false,
            "Facultad de Ciencias Administrativas, Contables y Comercio" to false,
            "Facultad de Educación, Turismo, Artes y Humanidades" to false,
            "Facultad de Ciencias Sociales, Derecho y Bienestar" to false,
            "Bienestar Universitario" to true,
            "Instituto de Idiomas" to false
        )
    )

    private val savedPosts = mutableMapOf<String, MutableSet<String>>(
        "user-aso-1" to mutableSetOf("post-1"),
        "user-lector-1" to mutableSetOf("post-1", "post-2")
    )

    override suspend fun getPosts(): Result<List<Post>> = withContext(ioDispatcher) {
        delay(networkDelayMs)
        mutex.withLock {
            Result.success(posts.toList())
        }
    }

    override suspend fun getPostById(id: String): Result<Post?> = withContext(ioDispatcher) {
        delay(networkDelayMs)
        mutex.withLock {
            Result.success(posts.find { it.id == id })
        }
    }

    override suspend fun createPost(post: Post): Result<Post> = withContext(ioDispatcher) {
        delay(networkDelayMs)
        mutex.withLock {
            posts.removeAll { it.id == post.id }
            posts.add(0, post)
            Result.success(post)
        }
    }

    override suspend fun updatePost(post: Post): Result<Post> = withContext(ioDispatcher) {
        delay(networkDelayMs)
        mutex.withLock {
            val index = posts.indexOfFirst { it.id == post.id }
            if (index != -1) {
                posts[index] = post
                Result.success(post)
            } else {
                Result.failure(NoSuchElementException("No se encontró la publicación con id ${post.id}"))
            }
        }
    }

    override suspend fun deletePost(id: String): Result<Unit> = withContext(ioDispatcher) {
        delay(networkDelayMs)
        mutex.withLock {
            val removed = posts.removeAll { it.id == id }
            if (removed) {
                Result.success(Unit)
            } else {
                Result.failure(NoSuchElementException("No se encontró la publicación con id $id"))
            }
        }
    }

    override suspend fun getUsers(): Result<List<User>> = withContext(ioDispatcher) {
        delay(networkDelayMs)
        mutex.withLock {
            Result.success(users.toList())
        }
    }

    override suspend fun getUser(userId: String): Result<User?> = withContext(ioDispatcher) {
        delay(networkDelayMs)
        mutex.withLock {
            Result.success(users.find { it.id == userId })
        }
    }

    override suspend fun getPreferences(userId: String): Result<Map<String, Boolean>> = withContext(ioDispatcher) {
        delay(networkDelayMs)
        mutex.withLock {
            val userPrefs = preferences[userId] ?: emptyMap()
            Result.success(userPrefs.toMap())
        }
    }

    override suspend fun savePreferences(userId: String, preferences: Map<String, Boolean>): Result<Unit> = withContext(ioDispatcher) {
        delay(networkDelayMs)
        mutex.withLock {
            this@FakeRemoteDataSource.preferences[userId] = preferences.toMutableMap()
            Result.success(Unit)
        }
    }

    override suspend fun getSavedPostIds(userId: String): Result<Set<String>> = withContext(ioDispatcher) {
        delay(networkDelayMs)
        mutex.withLock {
            val set = savedPosts[userId] ?: emptySet()
            Result.success(set.toSet())
        }
    }

    override suspend fun savePost(userId: String, postId: String): Result<Unit> = withContext(ioDispatcher) {
        delay(networkDelayMs)
        mutex.withLock {
            savedPosts.getOrPut(userId) { mutableSetOf() }.add(postId)
            Result.success(Unit)
        }
    }

    override suspend fun unsavePost(userId: String, postId: String): Result<Unit> = withContext(ioDispatcher) {
        delay(networkDelayMs)
        mutex.withLock {
            savedPosts[userId]?.remove(postId)
            Result.success(Unit)
        }
    }
}
