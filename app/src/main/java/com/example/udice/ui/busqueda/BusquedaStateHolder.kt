package com.example.udice.ui.busqueda

import android.os.SystemClock
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.udice.data.model.FeedCursor
import com.example.udice.data.model.PostSearch
import com.example.udice.data.model.PostSearchResult
import com.example.udice.data.model.SEARCH_PAGE_SIZE
import com.example.udice.data.model.toSearchResult
import com.example.udice.data.repository.PostRepository
import com.example.udice.data.repository.normalizedForSearch
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SearchResults(
    val posts: List<PostSearchResult> = emptyList(),
    val loading: Boolean = false,
    val failed: Boolean = false,
    val tooShort: Boolean = false,
    val hasMore: Boolean = false
)

@HiltViewModel
class BusquedaStateHolder @Inject constructor(
    private val repository: PostRepository,
    private val savedState: SavedStateHandle
) : ViewModel() {
    val query = savedState.getStateFlow("query", "")
    val category = savedState.getStateFlow("category", "")
    val faculty = savedState.getStateFlow("faculty", "")
    private val start = savedState.getStateFlow("startDate", "")
    private val end = savedState.getStateFlow("endDate", "")
    val dates = combine(start, end, ::Pair).stateIn(viewModelScope, SharingStarted.Eagerly, start.value to end.value)
    private val state = MutableStateFlow(SearchResults())
    val results = state.asStateFlow()
    private val retries = MutableStateFlow(0)
    private var activeSearch: PostSearch? = null
    private var next: FeedCursor? = null
    private var pageJob: Job? = null
    private val cache = LinkedHashMap<PostSearch, CachedSearch>()
    private data class CachedSearch(val result: SearchResults, val next: FeedCursor?, val timestamp: Long)

    init {
        viewModelScope.launch {
            combine(query, category, faculty, dates) { query, category, faculty, dates ->
                PostSearch(query.trim().lowercase(), category, faculty, boundary(dates.first, false), boundary(dates.second, true))
            }.distinctUntilChanged().combine(retries) { search, _ -> search }.collectLatest { search ->
                pageJob?.cancel()
                activeSearch = null
                next = null
                when {
                    search.query.length in 1..2 -> state.value = SearchResults(tooShort = true)
                    search.query.isEmpty() && search.category.isEmpty() && search.faculty.isEmpty() &&
                        search.from == null && search.until == null -> {
                        repository.posts.collect { posts ->
                            state.value = SearchResults(posts = posts.take(SEARCH_PAGE_SIZE).map { it.toSearchResult() })
                        }
                    }
                    else -> {
                        state.value = SearchResults(loading = true)
                        delay(400)
                        activeSearch = search
                        val cached = cache[search]?.takeIf { SystemClock.elapsedRealtime() - it.timestamp < 60_000L }
                        if (cached != null) {
                            next = cached.next
                            state.value = cached.result
                        } else requestPage(search, null)
                    }
                }
            }
        }
    }

    fun updateQuery(value: String) { savedState["query"] = value.take(120) }
    fun updateCategory(value: String) { savedState["category"] = value }
    fun updateFaculty(value: String) { savedState["faculty"] = value }
    fun updateDate(isStart: Boolean, value: String) { savedState[if (isStart) "startDate" else "endDate"] = value }

    fun retry() {
        if (next != null) loadMore() else retries.value++
    }

    fun loadMore() {
        val search = activeSearch ?: return
        val cursor = next ?: return
        if (state.value.loading) return
        state.value = state.value.copy(loading = true, failed = false)
        pageJob = viewModelScope.launch { requestPage(search, cursor) }
    }

    private suspend fun requestPage(search: PostSearch, cursor: FeedCursor?) {
        repository.searchPosts(search, cursor).fold(
            onSuccess = { page ->
                next = page.next
                val previous = if (cursor == null) emptyList() else state.value.posts
                val result = SearchResults((previous + page.results).distinctBy { it.id }, hasMore = next != null)
                state.value = result
                cache.remove(search)
                cache[search] = CachedSearch(result, next, SystemClock.elapsedRealtime())
                if (cache.size > 16) cache.remove(cache.keys.first())
            },
            onFailure = { state.value = state.value.copy(loading = false, failed = true) }
        )
    }

    private fun boundary(value: String, endExclusive: Boolean): String? {
        if (value.isEmpty()) return null
        val date = LocalDate.parse(value)
        return (if (endExclusive) date.plusDays(1) else date)
            .atStartOfDay(ZoneId.of("America/Guayaquil")).toInstant().toString()
    }
}

internal fun String.searchable() = normalizedForSearch()
