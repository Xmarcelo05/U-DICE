package com.example.udice.ui.detalle

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.udice.data.model.Post
import com.example.udice.data.model.ReactionCount
import com.example.udice.data.model.ReactionParticipant
import com.example.udice.data.repository.ReactionsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.asStateFlow
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

data class ReactionPanelState(
    val counts: List<ReactionCount> = emptyList(),
    val people: List<ReactionParticipant> = emptyList(),
    val open: Boolean = false,
    val selected: String? = null,
    val next: String? = null,
    val loading: Boolean = false,
    val failed: Boolean = false
)

@HiltViewModel
class DetallePublicacionStateHolder @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val postRepository: PostRepository,
    private val savedPostsRepository: SavedPostsRepository,
    private val sessionRepository: SessionRepository,
    private val reactions: ReactionsRepository
) : ViewModel() {

    private val postId: String = savedStateHandle.get<String>(NavRoutes.POST_ID_ARG).orEmpty()

    private val isUpdatingSave = MutableStateFlow(false)
    private val actionError = MutableStateFlow<String?>(null)
    private val fetching = MutableStateFlow(postId.isNotBlank())

    private val panel = MutableStateFlow(ReactionPanelState())
    val reactionPanel = panel.asStateFlow()
    private var peopleJob: Job? = null

    init {
        if (postId.isNotBlank()) viewModelScope.launch {
            actionError.value = postRepository.loadPostIfMissing(postId).exceptionOrNull()?.let {
                "No se pudo cargar el aviso."
            }
            fetching.value = false
        }
        if (postId.isNotBlank()) viewModelScope.launch {
            reactions.reactionDetails(postId, includePeople = false).onSuccess {
                if (!panel.value.open) panel.value = panel.value.copy(counts = it.counts)
            }
        }
    }

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
        actionError,
        fetching
    ) { post, isSaved, updating, error, loading ->
        if (post == null) {
            when {
                loading -> DetallePublicacionUiState.Loading
                error != null -> DetallePublicacionUiState.Error(error)
                else -> DetallePublicacionUiState.NotFound
            }
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

    fun openReactions() {
        panel.value = panel.value.copy(open = true)
        selectReaction(null)
    }

    fun closeReactions() {
        peopleJob?.cancel()
        panel.value = ReactionPanelState(counts = panel.value.counts)
    }

    fun selectReaction(type: String?) {
        peopleJob?.cancel()
        panel.value = panel.value.copy(selected = type, people = emptyList(), next = null)
        loadPeople(null)
    }

    fun loadMoreReactions() {
        if (panel.value.loading) return
        if (panel.value.next != null || panel.value.failed) loadPeople(panel.value.next)
    }

    private fun loadPeople(cursor: String?) {
        val current = panel.value
        panel.value = current.copy(loading = true, failed = false)
        peopleJob = viewModelScope.launch {
            reactions.reactionDetails(postId, true, current.selected, cursor).fold(
                onSuccess = { page ->
                    panel.value = current.copy(
                        counts = page.counts, people = (current.people + page.participants).distinctBy { it.id },
                        next = page.next, loading = false, failed = false
                    )
                },
                onFailure = { panel.value = current.copy(loading = false, failed = true) }
            )
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