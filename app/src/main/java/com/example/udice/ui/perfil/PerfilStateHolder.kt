package com.example.udice.ui.perfil

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.udice.data.repository.SessionRepository
import com.example.udice.data.repository.FollowTotals
import com.example.udice.data.repository.Publisher
import com.example.udice.data.repository.PublisherRepository
import com.example.udice.BuildConfig
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.drop
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfilePhotoState(val saving: Boolean = false, val error: String? = null)

data class ConnectionsState(
    val followers: Boolean = false,
    val people: List<Publisher> = emptyList(),
    val next: String? = null,
    val loading: Boolean = false,
    val failed: Boolean = false
)

@HiltViewModel
class PerfilStateHolder @Inject constructor(
    private val session: SessionRepository,
    private val publishers: PublisherRepository
) : ViewModel() {
    private val totals = MutableStateFlow<FollowTotals?>(null)
    val followTotals = totals.asStateFlow()
    private val connections = MutableStateFlow<ConnectionsState?>(null)
    val connectionList = connections.asStateFlow()
    private var listJob: Job? = null
    private var totalsJob: Job? = null

    init {
        if (BuildConfig.UDICE_SUPABASE) viewModelScope.launch {
            publishers.followChanges.drop(1).collect { refreshTotals() }
        }
    }

    fun refreshTotals() {
        if (!BuildConfig.UDICE_SUPABASE) return
        totalsJob?.cancel()
        totalsJob = viewModelScope.launch {
            publishers.myFollowTotals().onSuccess { totals.value = it }
        }
    }

    fun openConnections(followers: Boolean) {
        listJob?.cancel()
        connections.value = ConnectionsState(followers = followers)
        loadConnections()
    }

    fun closeConnections() {
        listJob?.cancel()
        connections.value = null
    }

    fun loadConnections() {
        val current = connections.value ?: return
        if (current.loading) return
        connections.value = current.copy(loading = true, failed = false)
        listJob = viewModelScope.launch {
            publishers.myConnections(current.followers, current.next).fold(
                onSuccess = { page -> connections.value = current.copy(
                    people = (current.people + page.people).distinctBy(Publisher::id), next = page.next, loading = false, failed = false
                ) },
                onFailure = { connections.value = current.copy(loading = false, failed = true) }
            )
        }
    }

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
