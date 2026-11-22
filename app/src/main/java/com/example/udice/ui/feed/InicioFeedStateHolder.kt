package com.example.udice.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.udice.data.model.Categoria
import com.example.udice.data.model.Post
import com.example.udice.data.model.ReaccionSummary
import com.example.udice.data.model.TipoReaccion
import com.example.udice.data.repository.PostRepository
import com.example.udice.data.repository.PreferencesRepository
import com.example.udice.data.repository.ReactionsRepository
import com.example.udice.data.repository.SavedPostsRepository
import com.example.udice.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PostFeedItem(
    val post: Post,
    val reactions: ReaccionSummary,
    val isSaved: Boolean
)

sealed interface InicioFeedUiState {
    data object Loading : InicioFeedUiState
    data class Content(
        val posts: List<PostFeedItem>,
        val categories: List<String>,
        val selectedCategory: String = "Todos",
        val isRefreshing: Boolean = false,
        val errorMessage: String? = null
    ) : InicioFeedUiState
    data class Error(val message: String) : InicioFeedUiState
}

sealed interface InicioFeedEvent {
    data class SelectCategory(val category: String) : InicioFeedEvent
    data class ToggleReaction(val postId: String, val tipo: TipoReaccion) : InicioFeedEvent
    data class ToggleSave(val postId: String) : InicioFeedEvent
    data object Refresh : InicioFeedEvent
}

val FEED_CATEGORIES = listOf("Todos", "Eventos", "Convocatorias", "Conferencias", "Avisos", "Otros")

@HiltViewModel
class InicioFeedStateHolder @Inject constructor(
    private val postRepository: PostRepository,
    private val preferencesRepository: PreferencesRepository,
    private val sessionRepository: SessionRepository,
    private val reactionsRepository: ReactionsRepository,
    private val savedPostsRepository: SavedPostsRepository
) : ViewModel() {

    private val selectedCategory = MutableStateFlow("Todos")
    private val isRefreshing = MutableStateFlow(false)
    private var currentUserId: String = "user-lector-1"

    init {
        viewModelScope.launch {
            val user = sessionRepository.getCurrentUser()
            currentUserId = user?.id ?: "user-lector-1"
            // Sincronizar en frío
            postRepository.syncPosts()
            reactionsRepository.syncReactions()
            preferencesRepository.syncPreferences(currentUserId)
        }
    }

    private val filterFlow = combine(selectedCategory, isRefreshing) { category, refreshing ->
        category to refreshing
    }

    val uiState: StateFlow<InicioFeedUiState> = combine(
        postRepository.posts,
        preferencesRepository.observePreferences(currentUserId),
        reactionsRepository.observeAllReactions(currentUserId),
        savedPostsRepository.observeSavedPostIds(currentUserId),
        filterFlow
    ) { posts, preferences, reactionsMap, savedIds, filter ->
        val category = filter.first
        val refreshing = filter.second

        // 1. Filtrar por preferencias de facultad (RF 3 y RF 6)
        val filteredByPrefs = if (preferences.isEmpty()) {
            posts
        } else {
            posts.filter { post ->
                val isFacultyEnabled = preferences[post.facultad.displayName]
                isFacultyEnabled ?: true
            }
        }

        // 2. Filtrar por categoría seleccionada (RF 13)
        val filteredByCategory = if (category == "Todos") {
            filteredByPrefs
        } else {
            val mappedCategory = when (category) {
                "Eventos" -> Categoria.EVENTO
                "Convocatorias" -> Categoria.CONVOCATORIA
                "Conferencias" -> Categoria.CONFERENCIA
                "Avisos" -> Categoria.AVISO
                "Otros" -> Categoria.OTRO
                else -> null
            }
            if (mappedCategory != null) {
                filteredByPrefs.filter { it.categoria == mappedCategory }
            } else {
                filteredByPrefs
            }
        }

        // 3. Ordenar cronológicamente descendente (RF 6: del más reciente al más antiguo)
        val sortedPosts = filteredByCategory.sortedByDescending { it.fecha }

        // 4. Mapear con reacciones (RF 9) y estado de guardado
        val items = sortedPosts.map { post ->
            PostFeedItem(
                post = post,
                reactions = reactionsMap[post.id] ?: ReaccionSummary(),
                isSaved = savedIds.contains(post.id)
            )
        }

        InicioFeedUiState.Content(
            posts = items,
            categories = FEED_CATEGORIES,
            selectedCategory = category,
            isRefreshing = refreshing
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = InicioFeedUiState.Loading
    )

    fun onEvent(event: InicioFeedEvent) {
        when (event) {
            is InicioFeedEvent.SelectCategory -> {
                selectedCategory.value = event.category
            }
            is InicioFeedEvent.ToggleReaction -> {
                viewModelScope.launch {
                    reactionsRepository.toggleReaction(event.postId, currentUserId, event.tipo)
                }
            }
            is InicioFeedEvent.ToggleSave -> {
                viewModelScope.launch {
                    savedPostsRepository.toggleSavePost(currentUserId, event.postId)
                }
            }
            InicioFeedEvent.Refresh -> {
                viewModelScope.launch {
                    isRefreshing.value = true
                    postRepository.syncPosts()
                    reactionsRepository.syncReactions()
                    isRefreshing.value = false
                }
            }
        }
    }
}
