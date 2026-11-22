package com.example.udice.ui.perfil

import androidx.lifecycle.ViewModel
import com.example.udice.data.repository.SavedPostsRepository
import com.example.udice.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface PerfilUiState {
    data object Loading : PerfilUiState
    data class Content(val nombre: String = "", val facultad: String = "", val savedCount: Int = 0) : PerfilUiState
    data class Error(val message: String) : PerfilUiState
}

sealed interface PerfilEvent {
    data object Logout : PerfilEvent
    data object OpenSavedPosts : PerfilEvent
    data object OpenMyPosts : PerfilEvent
}

@HiltViewModel
class PerfilStateHolder @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val savedPostsRepository: SavedPostsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<PerfilUiState>(PerfilUiState.Content())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    fun onEvent(event: PerfilEvent) {
        // TODO(RF 5): Conectar el conteo y acceso a guardados con SavedPostsRepository.
        // TODO(RF 8): Ejecutar cierre de sesión mediante SessionRepository.
    }
}
