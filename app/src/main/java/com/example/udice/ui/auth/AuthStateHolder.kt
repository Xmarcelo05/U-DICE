package com.example.udice.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.udice.data.model.Facultad
import com.example.udice.data.model.User
import com.example.udice.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface SessionState {
    data object Loading : SessionState
    data object SignedOut : SessionState
    data class SignedIn(val user: User) : SessionState
}

data class AuthFormState(
    val registering: Boolean = false,
    val busy: Boolean = false,
    val message: String? = null,
    val error: Boolean = false
)

@HiltViewModel
class AuthStateHolder @Inject constructor(private val repository: SessionRepository) : ViewModel() {
    val session = repository.currentUser
        .map<User?, SessionState> { user -> if (user == null) SessionState.SignedOut else SessionState.SignedIn(user) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SessionState.Loading)

    private val _form = MutableStateFlow(AuthFormState())
    val form = _form.asStateFlow()

    fun showRegistration(registering: Boolean) {
        if (!_form.value.busy) _form.value = AuthFormState(registering = registering)
    }

    fun login(email: String, password: String) = submit { repository.login(email, password) }

    fun register(name: String, email: String, password: String, faculty: Facultad, career: String) =
        submit("Cuenta creada. Inicia sesión con tu correo.") {
            repository.register(name, email, password, faculty, career)
        }

    fun logout() = submit { repository.logout() }

    private fun submit(successMessage: String? = null, action: suspend () -> Result<Unit>) {
        val current = _form.value
        if (current.busy) return
        _form.value = current.copy(busy = true, message = null)
        viewModelScope.launch {
            _form.value = action().fold(
                onSuccess = { AuthFormState(message = successMessage) },
                onFailure = { current.copy(busy = false, message = it.message ?: "Vuelve a intentarlo.", error = true) }
            )
        }
    }
}
