package com.example.udice.ui.nuevapublicacion

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface NuevaPublicacionUiState {
    data object Loading : NuevaPublicacionUiState
    data class Content(
        val titulo: String = "", val descripcion: String = "", val categoria: String = "Evento",
        val hasImage: Boolean = false, val isSubmitting: Boolean = false,
        val error: String? = null, val published: Boolean = false
    ) : NuevaPublicacionUiState
    data class Error(val message: String) : NuevaPublicacionUiState
}

sealed interface NuevaPublicacionEvent {
    data class TitleChanged(val value: String) : NuevaPublicacionEvent
    data class DescriptionChanged(val value: String) : NuevaPublicacionEvent
    data class CategorySelected(val value: String) : NuevaPublicacionEvent
    data object ToggleImage : NuevaPublicacionEvent
    data object Submit : NuevaPublicacionEvent
    data object DismissSuccess : NuevaPublicacionEvent
}

@HiltViewModel
class NuevaPublicacionStateHolder @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow<NuevaPublicacionUiState>(NuevaPublicacionUiState.Content())
    val uiState: StateFlow<NuevaPublicacionUiState> = _uiState.asStateFlow()

    fun onEvent(event: NuevaPublicacionEvent) {
        // TODO(RF 8): Completar la publicación y autorización con PostRepository.
    }
}
