package com.example.udice.ui.mispublicaciones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.udice.data.model.Post
import com.example.udice.data.model.Rol
import com.example.udice.data.repository.PostRepository
import com.example.udice.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PostOperation(val running: Boolean = false, val error: String? = null)

@HiltViewModel
class MisPublicacionesStateHolder @Inject constructor(
    private val posts: PostRepository,
    session: SessionRepository
) : ViewModel() {
    val ownPosts = combine(session.currentUser, posts.posts) { user, allPosts ->
        if (user?.rol == Rol.ASO) allPosts.filter { it.autorId == user.id } else emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _operation = MutableStateFlow(PostOperation())
    val operation = _operation.asStateFlow()

    fun edit(post: Post) = perform {
        require(post.titulo.isNotBlank() && post.descripcion.isNotBlank()) { "Completa el título y la descripción." }
        posts.updatePost(post.copy(titulo = post.titulo.trim(), descripcion = post.descripcion.trim())).getOrThrow()
    }

    fun delete(id: String) = perform { posts.deletePost(id).getOrThrow() }

    private fun perform(action: suspend () -> Unit) {
        if (_operation.value.running) return
        _operation.value = PostOperation(running = true)
        viewModelScope.launch {
            val result = runCatching { action() }
            _operation.value = PostOperation(error = result.exceptionOrNull()?.message)
        }
    }
}
