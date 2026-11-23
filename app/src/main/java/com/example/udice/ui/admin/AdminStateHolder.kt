package com.example.udice.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.udice.data.model.Facultad
import com.example.udice.data.repository.ReactionsRepository
import com.example.udice.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AdminOperation(
    val busy: Boolean = false,
    val error: String? = null,
    val success: String? = null
)

@HiltViewModel
class AdminStateHolder @Inject constructor(
    private val session: SessionRepository,
    private val reactions: ReactionsRepository
) : ViewModel() {
    val accounts = session.observeFacultyAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val reactionTypes = reactions.reactionTypes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _operation = MutableStateFlow(AdminOperation())
    val operation = _operation.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        if (_operation.value.busy) return
        _operation.value = AdminOperation(busy = true)
        viewModelScope.launch {
            val result = session.refreshFacultyAccounts().fold(
                onSuccess = { reactions.refreshReactionTypes() },
                onFailure = { Result.failure(it) }
            )
            _operation.value = AdminOperation(error = result.exceptionOrNull()?.let {
                it.message ?: "No se pudo cargar la información."
            })
        }
    }

    fun createAccount(name: String, email: String, password: String, faculty: Facultad) =
        submit("Cuenta de facultad creada.") { session.createFacultyAccount(name, email, password, faculty) }

    fun resetPassword(id: String, password: String) =
        submit("Contraseña actualizada.") { session.resetFacultyPassword(id, password) }

    fun deleteAccount(id: String) =
        submit("Cuenta eliminada.") { session.deleteFacultyAccount(id) }

    fun addReaction(name: String, emoji: String) =
        submit("Reacción añadida al feed.") { reactions.addReactionType(name, emoji) }

    fun clearResult() {
        if (!_operation.value.busy) _operation.value = AdminOperation()
    }

    private fun submit(message: String, action: suspend () -> Result<Unit>) {
        if (_operation.value.busy) return
        _operation.value = AdminOperation(busy = true)
        viewModelScope.launch {
            _operation.value = action().fold(
                onSuccess = { AdminOperation(success = message) },
                onFailure = { AdminOperation(error = it.message ?: "No se pudo completar la operación.") }
            )
        }
    }
}
