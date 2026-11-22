package com.example.udice.ui.perfil

import androidx.lifecycle.ViewModel
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
class PerfilStateHolder @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow<PerfilUiState>(PerfilUiState.Content())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    fun onEvent(event: PerfilEvent) {
        // TODO(RF 1): Conectar el perfil con sesión, guardados y navegación.
    }
}
