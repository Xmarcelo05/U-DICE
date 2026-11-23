package com.example.udice.ui.detalle

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.udice.data.model.Post
import com.example.udice.data.repository.PostRepository
import com.example.udice.data.repository.SavedPostsRepository
import com.example.udice.data.repository.SessionRepository
import com.example.udice.ui.navigation.NavRoutes
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

sealed interface DetallePublicacionUiState {
    data object Loading : DetallePublicacionUiState
    data class Content(
        val post: Post,
        val isSaved: Boolean,
        val isUpdatingSave: Boolean = false,
        val errorMessage: String? = null
    ) : DetallePublicacionUiState
    data object NotFound : DetallePublicacionUiState
    data class Error(val message: String) : DetallePublicacionUiState
}

sealed interface DetallePublicacionEvent {
    data object ToggleSave : DetallePublicacionEvent
}

@HiltViewModel
class DetallePublicacionStateHolder @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val postRepository: PostRepository,
    private val savedPostsRepository: SavedPostsRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val postId: String = savedStateHandle.get<String>(NavRoutes.POST_ID_ARG).orEmpty()

    private val isUpdatingSave = MutableStateFlow(false)
    private val actionError = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val isSavedFlow = sessionRepository.currentUser.flatMapLatest { user ->
        if (user == null || postId.isBlank()) {
            flowOf(false)
        } else {
            savedPostsRepository.isPostSaved(user.id, postId)
        }
    }

    private val postFlow = if (postId.isBlank()) {
        flowOf(null)
    } else {
        postRepository.observePost(postId)
    }

    val uiState: StateFlow<DetallePublicacionUiState> = combine(
        postFlow,
        isSavedFlow,
        isUpdatingSave,
        actionError
    ) { post, isSaved, updating, error ->
        if (post == null) {
            DetallePublicacionUiState.NotFound
        } else {
            DetallePublicacionUiState.Content(
                post = post,
                isSaved = isSaved,
                isUpdatingSave = updating,
                errorMessage = error
            )
        }
    }.catch { throwable ->
        emit(DetallePublicacionUiState.Error(throwable.message ?: "Error al cargar el detalle."))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DetallePublicacionUiState.Loading
    )

    fun onEvent(event: DetallePublicacionEvent) {
        when (event) {
            DetallePublicacionEvent.ToggleSave -> toggleSave()
        }
    }

    private fun toggleSave() {
        val current = uiState.value as? DetallePublicacionUiState.Content ?: return
        if (isUpdatingSave.value) return

        viewModelScope.launch {
            val user = sessionRepository.getCurrentUser()
            if (user == null) {
                actionError.value = "Debes iniciar sesión para guardar publicaciones."
                return@launch
            }
            isUpdatingSave.value = true
            actionError.value = null
            savedPostsRepository.toggleSavePost(user.id, current.post.id)
                .onFailure {
                    actionError.value = it.message ?: "No se pudo actualizar el estado de guardado."
                }
            isUpdatingSave.value = false
        }
    }
}