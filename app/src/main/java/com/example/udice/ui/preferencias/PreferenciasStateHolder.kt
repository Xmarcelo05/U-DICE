package com.example.udice.ui.preferencias

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.udice.data.repository.PreferencesRepository
import com.example.udice.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface PreferenciasUiState {
    data object Loading : PreferenciasUiState
    data class Content(
        val preferences: Map<String, Boolean> = defaultPreferences,
        val hasChanges: Boolean = false,
        val isSaving: Boolean = false,
        val saveSuccess: Boolean = false,
        val errorMessage: String? = null
    ) : PreferenciasUiState
    data class Error(val message: String) : PreferenciasUiState
}

sealed interface PreferenciasEvent {
    data class Toggle(val key: String, val enabled: Boolean) : PreferenciasEvent
    data object Save : PreferenciasEvent
    data object Cancel : PreferenciasEvent
    data object DismissSuccess : PreferenciasEvent
}

val defaultPreferences = mapOf(
    "Ciencias de la Vida y Tecnologías" to true,
    "Ciencias de la Salud" to true,
    "Ingeniería, Industria y Construcción" to false,
    "Facultad de Ciencias Administrativas, Contables y Comercio" to false,
    "Facultad de Educación, Turismo, Artes y Humanidades" to false,
    "Facultad de Ciencias Sociales, Derecho y Bienestar" to false,
    "Bienestar Universitario" to true,
    "Instituto de Idiomas" to false
)

@HiltViewModel
class PreferenciasStateHolder @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PreferenciasUiState>(PreferenciasUiState.Loading)
    val uiState: StateFlow<PreferenciasUiState> = _uiState.asStateFlow()

    private var currentUserId: String = "user-lector-1"
    private var persistedPreferences: Map<String, Boolean> = defaultPreferences
    private var editablePreferences: MutableMap<String, Boolean> = defaultPreferences.toMutableMap()

    init {
        loadPreferences()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            val user = sessionRepository.currentUser.firstOrNull() ?: sessionRepository.getCurrentUser()
            currentUserId = user?.id ?: "user-lector-1"

            preferencesRepository.observePreferences(currentUserId).collect { storedMap ->
                val merged = if (storedMap.isEmpty()) {
                    defaultPreferences
                } else {
                    defaultPreferences + storedMap
                }
                persistedPreferences = merged
                editablePreferences = merged.toMutableMap()
                _uiState.update {
                    PreferenciasUiState.Content(
                        preferences = merged,
                        hasChanges = false
                    )
                }
            }
        }
    }

    fun onEvent(event: PreferenciasEvent) {
        when (event) {
            is PreferenciasEvent.Toggle -> {
                editablePreferences[event.key] = event.enabled
                val hasChanges = editablePreferences.any { (k, v) -> persistedPreferences[k] != v } ||
                        persistedPreferences.any { (k, v) -> editablePreferences[k] != v }
                _uiState.update { current ->
                    if (current is PreferenciasUiState.Content) {
                        current.copy(
                            preferences = editablePreferences.toMap(),
                            hasChanges = hasChanges
                        )
                    } else current
                }
            }
            PreferenciasEvent.Save -> {
                viewModelScope.launch {
                    _uiState.update { current ->
                        if (current is PreferenciasUiState.Content) current.copy(isSaving = true) else current
                    }
                    val result = preferencesRepository.updatePreferences(currentUserId, editablePreferences)
                    if (result.isSuccess) {
                        persistedPreferences = editablePreferences.toMap()
                        _uiState.update { current ->
                            if (current is PreferenciasUiState.Content) {
                                current.copy(
                                    preferences = persistedPreferences,
                                    hasChanges = false,
                                    isSaving = false,
                                    saveSuccess = true
                                )
                            } else current
                        }
                    } else {
                        _uiState.update { current ->
                            if (current is PreferenciasUiState.Content) {
                                current.copy(
                                    isSaving = false,
                                    errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Error al guardar preferencias"
                                )
                            } else current
                        }
                    }
                }
            }
            PreferenciasEvent.Cancel -> {
                editablePreferences = persistedPreferences.toMutableMap()
                _uiState.update { current ->
                    if (current is PreferenciasUiState.Content) {
                        current.copy(
                            preferences = persistedPreferences,
                            hasChanges = false
                        )
                    } else current
                }
            }
            PreferenciasEvent.DismissSuccess -> {
                _uiState.update { current ->
                    if (current is PreferenciasUiState.Content) current.copy(saveSuccess = false) else current
                }
            }
        }
    }
}
