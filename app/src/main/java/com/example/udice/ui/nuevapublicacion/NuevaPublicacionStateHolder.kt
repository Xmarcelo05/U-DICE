package com.example.udice.ui.nuevapublicacion

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.udice.data.model.Alcance
import com.example.udice.data.model.Categoria
import com.example.udice.data.model.Facultad
import com.example.udice.data.model.Post
import com.example.udice.data.model.Rol
import com.example.udice.data.repository.PostRepository
import com.example.udice.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface NuevaPublicacionUiState {
    data object Loading : NuevaPublicacionUiState
    data class Content(
        val titulo: String = "", val descripcion: String = "", val categoria: String = "Evento",
        val image: Uri? = null, val isSubmitting: Boolean = false,
        val error: String? = null, val published: Boolean = false
    ) : NuevaPublicacionUiState
    data class Error(val message: String) : NuevaPublicacionUiState
}

sealed interface NuevaPublicacionEvent {
    data class TitleChanged(val value: String) : NuevaPublicacionEvent
    data class DescriptionChanged(val value: String) : NuevaPublicacionEvent
    data class CategorySelected(val value: String) : NuevaPublicacionEvent
    data class ImageSelected(val uri: Uri?) : NuevaPublicacionEvent
    data object Submit : NuevaPublicacionEvent
    data object DismissSuccess : NuevaPublicacionEvent
}

@HiltViewModel
class NuevaPublicacionStateHolder @Inject constructor(
    private val postRepository: PostRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<NuevaPublicacionUiState>(NuevaPublicacionUiState.Content())
    val uiState: StateFlow<NuevaPublicacionUiState> = _uiState.asStateFlow()

    fun onEvent(event: NuevaPublicacionEvent) {
        val current = _uiState.value as? NuevaPublicacionUiState.Content ?: return
        if (current.isSubmitting) return
        when (event) {
            is NuevaPublicacionEvent.TitleChanged -> _uiState.value = current.copy(titulo = event.value, error = null)
            is NuevaPublicacionEvent.DescriptionChanged -> _uiState.value = current.copy(descripcion = event.value, error = null)
            is NuevaPublicacionEvent.CategorySelected -> _uiState.value = current.copy(categoria = event.value, error = null)
            is NuevaPublicacionEvent.ImageSelected -> _uiState.value = current.copy(image = event.uri, error = null)
            NuevaPublicacionEvent.DismissSuccess -> _uiState.value = current.copy(
                titulo = "", descripcion = "", categoria = "Evento", image = null, published = false, error = null
            )
            NuevaPublicacionEvent.Submit -> submit(current)
        }
    }

    private fun submit(form: NuevaPublicacionUiState.Content) {
        // TODO(RF 6): Emitir navegación al detalle de la publicación creada.
        if (form.titulo.isBlank() || form.descripcion.isBlank()) {
            _uiState.value = form.copy(error = "Completa el título y la descripción.")
            return
        }
        viewModelScope.launch {
            _uiState.value = form.copy(isSubmitting = true, error = null)
            val user = sessionRepository.getCurrentUser()
            if (user == null || user.rol != Rol.ASO) {
                _uiState.value = form.copy(error = "Solo una cuenta ASO puede publicar.")
                return@launch
            }
            val post = Post(
                id = java.util.UUID.randomUUID().toString(),
                autorId = user.id,
                autorNombre = user.nombre,
                titulo = form.titulo.trim(),
                descripcion = form.descripcion.trim(),
                categoria = Categoria.fromDisplayName(form.categoria),
                facultad = user.facultad ?: Facultad.CIENCIAS_VIDA_TECNOLOGIAS,
                alcance = Alcance.GENERAL
            )
            postRepository.createPost(post, form.image)
                .onSuccess { _uiState.value = form.copy(published = true) }
                .onFailure { error -> _uiState.value = form.copy(error = error.message ?: "No se pudo publicar.") }
        }
    }
}
