package com.example.udice.ui.perfilpublico

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.udice.data.model.FeedCursor
import com.example.udice.data.repository.Publisher
import com.example.udice.data.repository.PublisherPost
import com.example.udice.data.repository.PublisherProfile
import com.example.udice.data.repository.PublisherRepository
import com.example.udice.ui.navigation.NavRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PeopleState(
    val people: List<Publisher> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
    val next: String? = null
)

@HiltViewModel
class PeopleStateHolder @Inject constructor(private val repository: PublisherRepository) : ViewModel() {
    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()
    private val _state = MutableStateFlow(PeopleState())
    val state = _state.asStateFlow()
    private var request: Job? = null

    fun updateQuery(value: String) {
        _query.value = value.take(120)
        request?.cancel()
        val query = _query.value.trim()
        _state.value = PeopleState(loading = query.length >= 2)
        if (query.length >= 2) request = viewModelScope.launch {
            delay(400)
            load(query, null)
        }
    }

    fun loadMore() {
        if (_state.value.loading || request?.isActive == true) return
        val next = _state.value.next
        if (next == null && _state.value.error == null) return
        request = viewModelScope.launch { load(_query.value.trim(), next) }
    }

    private suspend fun load(query: String, after: String?) {
        _state.update { it.copy(loading = true, error = null) }
        repository.search(query, after).fold(
            onSuccess = { page ->
                _state.update { it.copy(people = (it.people + page.people).distinctBy(Publisher::id), next = page.next, loading = false) }
            },
            onFailure = { _state.update { it.copy(loading = false, error = "No se pudo buscar publicadores.") } }
        )
    }
}

data class PublisherState(
    val profile: PublisherProfile? = null,
    val posts: List<PublisherPost> = emptyList(),
    val next: FeedCursor? = null,
    val loading: Boolean = false,
    val saving: Boolean = false,
    val error: String? = null,
    val postsFailed: Boolean = false
)

@HiltViewModel
class PublisherStateHolder @Inject constructor(
    savedState: SavedStateHandle,
    private val repository: PublisherRepository
) : ViewModel() {
    private val id = savedState.get<String>(NavRoutes.PUBLISHER_ID_ARG).orEmpty()
    private val _state = MutableStateFlow(PublisherState())
    val state = _state.asStateFlow()

    init { reload() }

    fun reload() {
        if (_state.value.loading || id.isBlank()) return
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            repository.profile(id).fold(
                onSuccess = { profile ->
                    _state.value = PublisherState(profile = profile, loading = true)
                    loadPosts(null)
                },
                onFailure = { _state.update { it.copy(loading = false, error = it.error ?: "No se pudo abrir el perfil.") } }
            )
        }
    }

    fun toggleFollowing() {
        val profile = _state.value.profile ?: return
        if (_state.value.saving || profile.own) return
        _state.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            repository.setFollowing(id, !profile.following).fold(
                onSuccess = { following ->
                    val count = profile.publisher.followers?.let {
                        (it + if (following) 1 else -1).coerceAtLeast(0)
                    }
                    _state.update { it.copy(
                        profile = profile.copy(following = following, publisher = profile.publisher.copy(followers = count)),
                        saving = false
                    ) }
                },
                onFailure = { _state.update { it.copy(saving = false, error = "No se pudo actualizar el seguimiento.") } }
            )
        }
    }

    fun loadMore() {
        val current = _state.value
        if (current.loading || (current.next == null && !current.postsFailed)) return
        _state.update { it.copy(loading = true, postsFailed = false) }
        viewModelScope.launch { loadPosts(current.next) }
    }

    private suspend fun loadPosts(cursor: FeedCursor?) {
        repository.posts(id, cursor).fold(
            onSuccess = { page -> _state.update {
                it.copy(posts = (it.posts + page.posts).distinctBy(PublisherPost::id), next = page.next, loading = false)
            } },
            onFailure = { _state.update { it.copy(loading = false, postsFailed = true) } }
        )
    }
}
