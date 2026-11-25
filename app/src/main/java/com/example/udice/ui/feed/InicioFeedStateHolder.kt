package com.example.udice.ui.feed

import android.os.SystemClock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.udice.data.model.Categoria
import com.example.udice.data.model.FEED_PAGE_SIZE
import com.example.udice.data.model.FeedCursor
import com.example.udice.data.model.Post
import com.example.udice.data.model.ReaccionSummary
import com.example.udice.data.model.TipoReaccion
import com.example.udice.data.repository.PostRepository
import com.example.udice.data.repository.PublisherRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.drop
import com.example.udice.data.repository.PreferencesRepository
import com.example.udice.data.repository.ReactionsRepository
import com.example.udice.data.repository.SavedPostsRepository
import com.example.udice.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PostFeedItem(
    val post: Post,
    val reactions: ReaccionSummary,
    val isSaved: Boolean
)

sealed interface InicioFeedUiState {
    data object Loading : InicioFeedUiState
    data class Content(
        val posts: List<PostFeedItem>,
        val categories: List<String>,
        val selectedCategory: String = "Todos",
        val isRefreshing: Boolean = false,
        val errorMessage: String? = null,
        val reactionTypes: List<TipoReaccion> = TipoReaccion.defaults,
        val refreshVersion: Int = 0,
        val hasMore: Boolean = false,
        val loadingMore: Boolean = false,
        val pageFailed: Boolean = false,
        val following: Boolean = false,
        val followingLoaded: Boolean = false
    ) : InicioFeedUiState
    data class Error(val message: String) : InicioFeedUiState
}

sealed interface InicioFeedEvent {
    data class SelectCategory(val category: String) : InicioFeedEvent
    data class ToggleReaction(val postId: String, val tipo: TipoReaccion) : InicioFeedEvent
    data class ToggleSave(val postId: String) : InicioFeedEvent
    data class DeletePost(val postId: String) : InicioFeedEvent
    data object Refresh : InicioFeedEvent
    data object LoadMore : InicioFeedEvent
    data class SelectFollowing(val value: Boolean) : InicioFeedEvent
}

val FEED_CATEGORIES = listOf("Todos", "Eventos", "Convocatorias", "Conferencias", "Avisos", "Otros")

private const val BACKGROUND_REFRESH_AFTER_MS = 60_000L

@HiltViewModel
class InicioFeedStateHolder @Inject constructor(
    private val postRepository: PostRepository,
    private val preferencesRepository: PreferencesRepository,
    private val sessionRepository: SessionRepository,
    private val reactionsRepository: ReactionsRepository,
    private val savedPostsRepository: SavedPostsRepository,
    private val publishers: PublisherRepository
) : ViewModel() {

    private val selectedCategory = MutableStateFlow("Todos")
    private val feedError = MutableStateFlow<String?>(null)
    private data class FeedControls(
        val category: String,
        val refreshing: Boolean,
        val error: String?,
        val refreshVersion: Int,
        val pages: FeedPages
    )

    private data class FeedPages(
        val ids: Set<String>? = null,
        val following: Boolean = false,
        val newest: Long = Long.MIN_VALUE,
        val cachedBeforeRefresh: Set<String> = emptySet(),
        val next: FeedCursor? = null,
        val loading: Boolean = false,
        val failed: Boolean = false
    )

    private val pages = MutableStateFlow(FeedPages())
    private val isRefreshing = MutableStateFlow(false)
    private val refreshVersion = MutableStateFlow(0)
    private var pageRequest: Job? = null
    private var requestVersion = 0
    private var backgroundSince: Long? = null

    init {
        refreshFeed(resetScroll = false)
        viewModelScope.launch {
            publishers.followChanges.drop(1).collect {
                if (pages.value.following) refreshFeed(force = true)
            }
        }
    }

    fun onBackground() {
        backgroundSince = SystemClock.elapsedRealtime()
    }

    fun onForeground() {
        val since = backgroundSince ?: return
        backgroundSince = null
        if (SystemClock.elapsedRealtime() - since >= BACKGROUND_REFRESH_AFTER_MS) {
            refreshFeed()
        }
    }

    private fun refreshFeed(resetScroll: Boolean = true, force: Boolean = false) {
        if (!force && (isRefreshing.value || pages.value.loading)) return
        val following = pages.value.following
        val version = ++requestVersion
        pageRequest?.cancel()
        if (force) pages.value = FeedPages(following = following)
        isRefreshing.value = true
        pageRequest = viewModelScope.launch {
            try {
                sessionRepository.getCurrentUser() ?: return@launch
                val cached = postRepository.posts.first()
                val postsError = postRepository.loadFeedPage(null, following).onSuccess { page ->
                    pages.value = FeedPages(
                        ids = page.posts.map { it.id }.toSet(),
                        following = following,
                        newest = page.posts.maxOfOrNull { it.fecha } ?: cached.maxOfOrNull { it.fecha } ?: Long.MIN_VALUE,
                        cachedBeforeRefresh = cached.map { it.id }.toSet(),
                        next = page.next
                    )
                    if (resetScroll) refreshVersion.value++
                }.exceptionOrNull()
                val reactionsError = if (force) null else reactionsRepository.syncReactions().exceptionOrNull()
                feedError.value = postsError?.let { "No se pudieron sincronizar los avisos." }
                    ?: reactionsError?.message
            } finally {
                if (requestVersion == version) isRefreshing.value = false
            }
        }
    }

    private fun loadMore() {
        val current = pages.value
        val next = current.next ?: return
        if (isRefreshing.value || current.loading) return
        pages.value = current.copy(loading = true, failed = false)
        pageRequest = viewModelScope.launch {
            postRepository.loadFeedPage(next, current.following).fold(
                onSuccess = { page ->
                    pages.value = current.copy(
                        ids = current.ids.orEmpty() + page.posts.map { it.id }, next = page.next, loading = false, failed = false
                    )
                },
                onFailure = { pages.value = current.copy(loading = false, failed = true) }
            )
        }
    }

    private val filterFlow = combine(selectedCategory, isRefreshing, feedError, refreshVersion, pages, ::FeedControls)

    private fun feedFor(userId: String): Flow<InicioFeedUiState> = combine(
        postRepository.posts,
        preferencesRepository.observePreferences(userId),
        reactionsRepository.observeAllReactions(userId).combine(reactionsRepository.reactionTypes) { summaries, types ->
            summaries to types
        },
        savedPostsRepository.observeSavedPostIds(userId),
        filterFlow
    ) { posts, preferences, reactionData, savedIds, filter ->
        val (reactionsMap, types) = reactionData
        val category = filter.category
        val loaded = filter.pages.ids?.let { ids ->
            posts.filter { it.id in ids || (!filter.pages.following &&
                it.id !in filter.pages.cachedBeforeRefresh && it.fecha > filter.pages.newest) }
        } ?: if (filter.pages.following) emptyList() else posts.take(FEED_PAGE_SIZE)

        // 1. Filtrar por preferencias de facultad (RF 3 y RF 6)
        val filteredByPrefs = if (filter.pages.following || preferences.isEmpty()) {
            loaded
        } else {
            loaded.filter { post ->
                val isFacultyEnabled = preferences[post.facultad.displayName]
                isFacultyEnabled ?: true
            }
        }

        // 2. Filtrar por categoría seleccionada (RF 13)
        val filteredByCategory = if (category == "Todos") {
            filteredByPrefs
        } else {
            val mappedCategory = when (category) {
                "Eventos" -> Categoria.EVENTO
                "Convocatorias" -> Categoria.CONVOCATORIA
                "Conferencias" -> Categoria.CONFERENCIA
                "Avisos" -> Categoria.AVISO
                "Otros" -> Categoria.OTRO
                else -> null
            }
            if (mappedCategory != null) {
                filteredByPrefs.filter { it.categoria == mappedCategory }
            } else {
                filteredByPrefs
            }
        }

        // 3. Ordenar cronológicamente descendente (RF 6: del más reciente al más antiguo)
        val sortedPosts = filteredByCategory.sortedWith(compareByDescending<Post> { it.fecha }.thenByDescending { it.id })

        // 4. Mapear con reacciones (RF 9) y estado de guardado
        val items = sortedPosts.map { post ->
            PostFeedItem(
                post = post,
                reactions = reactionsMap[post.id] ?: ReaccionSummary(),
                isSaved = savedIds.contains(post.id)
            )
        }

        InicioFeedUiState.Content(
            posts = items,
            categories = FEED_CATEGORIES,
            selectedCategory = category,
            isRefreshing = filter.refreshing,
            errorMessage = filter.error,
            reactionTypes = types,
            refreshVersion = filter.refreshVersion,
            hasMore = filter.pages.next != null,
            loadingMore = filter.pages.loading,
            pageFailed = filter.pages.failed,
            following = filter.pages.following,
            followingLoaded = filter.pages.ids != null
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<InicioFeedUiState> = sessionRepository.currentUser
        .flatMapLatest { user -> if (user == null) flowOf(InicioFeedUiState.Loading) else feedFor(user.id) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = InicioFeedUiState.Loading
        )

    fun onEvent(event: InicioFeedEvent) {
        when (event) {
            is InicioFeedEvent.SelectCategory -> {
                selectedCategory.value = event.category
            }
            is InicioFeedEvent.ToggleReaction -> {
                viewModelScope.launch {
                    val user = sessionRepository.getCurrentUser() ?: return@launch
                    feedError.value = reactionsRepository.toggleReaction(event.postId, user.id, event.tipo).exceptionOrNull()?.message
                }
            }
            is InicioFeedEvent.ToggleSave -> {
                viewModelScope.launch {
                    val user = sessionRepository.getCurrentUser() ?: return@launch
                    savedPostsRepository.toggleSavePost(user.id, event.postId)
                }
            }
            is InicioFeedEvent.DeletePost -> {
                viewModelScope.launch {
                    feedError.value = postRepository.deletePost(event.postId).exceptionOrNull()?.let {
                        it.message ?: "No se pudo eliminar la publicación."
                    }
                }
            }
            InicioFeedEvent.Refresh -> refreshFeed()
            InicioFeedEvent.LoadMore -> loadMore()
            is InicioFeedEvent.SelectFollowing -> if (pages.value.following != event.value) {
                pages.value = FeedPages(following = event.value)
                refreshFeed(force = true)
            }
        }
    }
}
