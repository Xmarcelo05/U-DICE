package com.example.udice.ui.guardados

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.udice.data.model.Post
import com.example.udice.data.repository.SavedPostsRepository
import com.example.udice.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface GuardadosUiState {
    data object Loading : GuardadosUiState
    data class Content(
        val posts: List<Post>,
        val removingPostIds: Set<String> = emptySet(),
        val errorMessage: String? = null
    ) : GuardadosUiState
    data class Error(val message: String) : GuardadosUiState
}

sealed interface GuardadosEvent {
    data class RemoveSaved(val postId: String) : GuardadosEvent
    data object ClearError : GuardadosEvent
}

@HiltViewModel
class GuardadosStateHolder @Inject constructor(
    private val savedPostsRepository: SavedPostsRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val removingIds = MutableStateFlow<Set<String>>(emptySet())
    private val actionError = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val savedPostsFlow = sessionRepository.currentUser.flatMapLatest { user ->
        if (user == null) {
            flowOf(emptyList())
        } else {
            savedPostsRepository.observeSavedPosts(user.id)
        }
    }

    val uiState: StateFlow<GuardadosUiState> = combine(
        savedPostsFlow,
        removingIds,
        actionError
    ) { posts, removing, error ->
        GuardadosUiState.Content(
            posts = posts.sortedByDescending { it.fecha },
            removingPostIds = removing,
            errorMessage = error
        ) as GuardadosUiState
    }.catch { throwable ->
        emit(GuardadosUiState.Error(throwable.message ?: "No se pudieron cargar tus guardados."))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GuardadosUiState.Loading
    )

    fun onEvent(event: GuardadosEvent) {
        when (event) {
            is GuardadosEvent.RemoveSaved -> removeSavedPost(event.postId)
            GuardadosEvent.ClearError -> actionError.value = null
        }
    }

    private fun removeSavedPost(postId: String) {
        if (postId in removingIds.value) return
        viewModelScope.launch {
            val user = sessionRepository.getCurrentUser()
            if (user == null) {
                actionError.value = "Debes iniciar sesión para modificar tus guardados."
                return@launch
            }
            removingIds.value = removingIds.value + postId
            actionError.value = null
            savedPostsRepository.toggleSavePost(user.id, postId)
                .onFailure {
                    actionError.value = it.message ?: "No se pudo quitar la publicación de tu bóveda."
                }
            removingIds.value = removingIds.value - postId
        }
    }
}