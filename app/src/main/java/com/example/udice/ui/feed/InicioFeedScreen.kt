package com.example.udice.ui.feed

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.TextButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.remember
import com.example.udice.ui.components.FeedPublicationVideo
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.udice.data.model.TipoReaccion
import com.example.udice.ui.feed.components.PostCard
import com.example.udice.ui.theme.AzulOscuro
import com.example.udice.ui.theme.AzulPrincipal
import com.example.udice.ui.theme.Blanco
import com.example.udice.ui.theme.GrisAzuladoClaro
import com.example.udice.ui.theme.GrisBordes
import com.example.udice.ui.theme.GrisPizarra

@Composable
fun InicioFeedRoute(
    onNavigateToPostDetail: (String) -> Unit = {},
    editableAuthorId: String? = null,
    onPublisherClick: (String) -> Unit = {},
    ownAvatarUrl: String? = null,
    onEditPost: (String) -> Unit = {},
    scrollRequested: Boolean = false,
    onScrollHandled: () -> Unit = {},
    stateHolder: InicioFeedStateHolder = hiltViewModel()
) {
    val state by stateHolder.uiState.collectAsStateWithLifecycle()
    val activityLifecycle = (LocalActivity.current as? LifecycleOwner)?.lifecycle
    DisposableEffect(activityLifecycle, stateHolder) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> stateHolder.onBackground()
                Lifecycle.Event.ON_START -> stateHolder.onForeground()
                else -> Unit
            }
        }
        activityLifecycle?.addObserver(observer)
        onDispose { activityLifecycle?.removeObserver(observer) }
    }

    when (val current = state) {
        is InicioFeedUiState.Content -> {
            InicioFeedScreen(
                state = current,
                onCategorySelect = { category ->
                    stateHolder.onEvent(InicioFeedEvent.SelectCategory(category))
                },
                onReactionClick = { postId, tipo ->
                    stateHolder.onEvent(InicioFeedEvent.ToggleReaction(postId, tipo))
                },
                onSaveClick = { postId ->
                    stateHolder.onEvent(InicioFeedEvent.ToggleSave(postId))
                },
                onPostClick = onNavigateToPostDetail,
                editableAuthorId = editableAuthorId,
                onPublisherClick = onPublisherClick,
                onFollowingChange = { stateHolder.onEvent(InicioFeedEvent.SelectFollowing(it)) },
                ownAvatarUrl = ownAvatarUrl,
                onEditPost = onEditPost,
                scrollRequested = scrollRequested,
                onScrollHandled = onScrollHandled,
                onRefresh = { stateHolder.onEvent(InicioFeedEvent.Refresh) },
                onDeletePost = { stateHolder.onEvent(InicioFeedEvent.DeletePost(it)) },
                onLoadMore = { stateHolder.onEvent(InicioFeedEvent.LoadMore) }
            )
        }
        InicioFeedUiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(GrisAzuladoClaro),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AzulPrincipal)
            }
        }
        is InicioFeedUiState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(GrisAzuladoClaro)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = current.message, color = GrisPizarra)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioFeedScreen(
    state: InicioFeedUiState.Content,
    onCategorySelect: (String) -> Unit,
    onReactionClick: (String, TipoReaccion) -> Unit,
    onSaveClick: (String) -> Unit,
    onPostClick: (String) -> Unit = {},
    editableAuthorId: String? = null,
    onPublisherClick: (String) -> Unit = {},
    ownAvatarUrl: String? = null,
    onEditPost: (String) -> Unit = {},
    onDeletePost: (String) -> Unit = {},
    onLoadMore: () -> Unit = {},
    scrollRequested: Boolean = false,
    onScrollHandled: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onFollowingChange: (Boolean) -> Unit = {}
) {
    var deletingId by rememberSaveable { mutableStateOf<String?>(null) }
    state.posts.firstOrNull { it.post.id == deletingId && it.post.autorId == editableAuthorId }?.let { item ->
        AlertDialog(
            onDismissRequest = { deletingId = null },
            title = { Text("Eliminar publicación") },
            text = { Text("¿Eliminar «${item.post.titulo}» del feed?") },
            confirmButton = {
                TextButton(onClick = {
                    deletingId = null
                    onDeletePost(item.post.id)
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { deletingId = null }) { Text("Cancelar") }
            }
        )
    }

    val listState = rememberLazyListState()
    var activeVideoId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(activeVideoId) {
        val id = activeVideoId ?: return@LaunchedEffect
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.any { it.key == id } }.first { visible -> !visible }
        if (activeVideoId == id) activeVideoId = null
    }
    LaunchedEffect(scrollRequested) {
        if (scrollRequested) {
            listState.scrollToItem(0)
            onScrollHandled()
        }
    }
    LaunchedEffect(state.posts.size, state.hasMore, state.loadingMore, state.pageFailed, state.isRefreshing) {
        if (state.posts.isNotEmpty() && state.hasMore && !state.loadingMore && !state.pageFailed && !state.isRefreshing) {
            snapshotFlow { listState.layoutInfo.visibleItemsInfo.any { it.index >= state.posts.lastIndex } }
                .filter { it }.first()
            onLoadMore()
        }
    }
    var lastAppliedRefresh by rememberSaveable { mutableIntStateOf(0) }
    var lastCategory by rememberSaveable { mutableStateOf(state.selectedCategory) }
    LaunchedEffect(state.following) { activeVideoId = null }
    LaunchedEffect(state.refreshVersion, state.selectedCategory) {
        if (state.refreshVersion > lastAppliedRefresh || state.selectedCategory != lastCategory) {
            listState.requestScrollToItem(0)
        }
        lastAppliedRefresh = state.refreshVersion
        lastCategory = state.selectedCategory
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GrisAzuladoClaro),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp)
        ) {
            if (com.example.udice.BuildConfig.UDICE_SUPABASE) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(selected = !state.following, onClick = { onFollowingChange(false) }, label = { Text("Novedades") })
                    FilterChip(selected = state.following, onClick = { onFollowingChange(true) }, label = { Text("Siguiendo") })
                }
            }
            // Fila de chips de filtro por categoría (RF 13)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                state.categories.forEach { category ->
                    val isSelected = category == state.selectedCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = { onCategorySelect(category) },
                        label = {
                            Text(
                                text = category,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AzulPrincipal,
                            selectedLabelColor = Blanco,
                            containerColor = Blanco,
                            labelColor = AzulOscuro
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) AzulPrincipal else GrisBordes
                        )
                    )
                }
            }

            state.errorMessage?.let { message ->
                Text(message, Modifier.padding(horizontal = 16.dp), color = androidx.compose.material3.MaterialTheme.colorScheme.error)
            }

            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val showEmpty = !state.following || (state.followingLoaded && !state.isRefreshing)
                    if (state.posts.isEmpty() && showEmpty) item {
                        Box(
                            modifier = Modifier
                                .fillParentMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = GrisPizarra,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (state.hasMore) "No hay coincidencias en los avisos cargados."
                                        else if (state.following) "No hay avisos visibles de los publicadores que sigues."
                                        else "No hay avisos para los filtros seleccionados o tus facultades de interés.",
                                    fontSize = 14.sp,
                                    color = GrisPizarra,
                                    fontWeight = FontWeight.Medium
                                )
                                if (state.hasMore) FeedPageFooter(state, onLoadMore)
                            }
                        }
                    }
                    items(state.posts, key = { it.post.id }) { item ->
                        PostCard(
                            post = item.post,
                            onPublisherClick = if (com.example.udice.BuildConfig.UDICE_SUPABASE) {
                                { activeVideoId = null; onPublisherClick(item.post.autorId) }
                            } else null,
                            authorAvatarUrl = if (item.post.autorId == editableAuthorId) ownAvatarUrl else item.post.authorAvatarUrl,
                            reactionSummary = item.reactions,
                            isSaved = item.isSaved,
                            reactionTypes = state.reactionTypes,
                            onReactionClick = { tipo -> onReactionClick(item.post.id, tipo) },
                            onSaveClick = { onSaveClick(item.post.id) },
                            onPostClick = {
                                activeVideoId = null
                                onPostClick(item.post.id)
                            },
                            videoContent = item.post.videoUrl?.let { url ->
                                {
                                    FeedPublicationVideo(
                                        url,
                                        item.post.imageUrl,
                                        active = activeVideoId == item.post.id,
                                        onPlay = { activeVideoId = item.post.id },
                                        onStop = { if (activeVideoId == item.post.id) activeVideoId = null }
                                    )
                                }
                            },
                            onEditClick = if (item.post.autorId == editableAuthorId) {
                                { onEditPost(item.post.id) }
                            } else null,
                            onDeleteClick = if (item.post.autorId == editableAuthorId) {
                                { deletingId = item.post.id }
                            } else null
                        )
                    }

                    if (state.posts.isNotEmpty()) item {
                        FeedPageFooter(state, onLoadMore)
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedPageFooter(state: InicioFeedUiState.Content, onLoadMore: () -> Unit) {
    Box(Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
        when {
            state.loadingMore -> CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
            state.pageFailed -> TextButton(onClick = onLoadMore) { Text("Reintentar carga") }
            state.hasMore -> TextButton(onClick = onLoadMore, enabled = !state.isRefreshing) { Text("Cargar más") }
        }
    }
}
