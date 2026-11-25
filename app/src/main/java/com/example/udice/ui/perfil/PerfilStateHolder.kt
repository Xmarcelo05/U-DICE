package com.example.udice.ui.perfil

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.udice.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfilePhotoState(val saving: Boolean = false, val error: String? = null)

@HiltViewModel
class PerfilStateHolder @Inject constructor(private val session: SessionRepository) : ViewModel() {
    private val state = MutableStateFlow(ProfilePhotoState())
    val photoState = state.asStateFlow()

    fun updatePhoto(photo: Uri?) {
        if (state.value.saving) return
        state.value = ProfilePhotoState(saving = true)
        viewModelScope.launch {
            state.value = session.updateProfilePhoto(photo).fold(
                onSuccess = { ProfilePhotoState() },
                onFailure = { ProfilePhotoState(error = it.message ?: "No se pudo actualizar la foto.") }
            )
        }
    }
}
