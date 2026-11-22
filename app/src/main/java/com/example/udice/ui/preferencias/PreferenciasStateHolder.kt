package com.example.udice.ui.preferencias

import androidx.lifecycle.ViewModel
import com.example.udice.data.repository.PreferencesRepository
import com.example.udice.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface PreferenciasUiState {
    data object Loading : PreferenciasUiState
    data class Content(val values: Map<String, Boolean> = emptyMap()) : PreferenciasUiState
    data class Error(val message: String) : PreferenciasUiState
}

sealed interface PreferenciasEvent {
    data class Toggle(val key: String, val enabled: Boolean) : PreferenciasEvent
    data object Save : PreferenciasEvent
    data object Cancel : PreferenciasEvent
}

@HiltViewModel
class PreferenciasStateHolder @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<PreferenciasUiState>(PreferenciasUiState.Content())
    val uiState: StateFlow<PreferenciasUiState> = _uiState.asStateFlow()

    fun onEvent(event: PreferenciasEvent) {
        // TODO(RF 7): Cargar, validar y persistir preferencias por usuario.
    }
}
