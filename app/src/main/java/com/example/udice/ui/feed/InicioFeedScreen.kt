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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
                onRefresh = { stateHolder.onEvent(InicioFeedEvent.Refresh) },
                onPostClick = onNavigateToPostDetail
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

@Composable
fun InicioFeedScreen(
    state: InicioFeedUiState.Content,
    onCategorySelect: (String) -> Unit,
    onReactionClick: (String, TipoReaccion) -> Unit,
    onSaveClick: (String) -> Unit,
    onPostClick: (String) -> Unit = {},
    onRefresh: () -> Unit = {}
) {
    val listState = rememberLazyListState()
    var lastAppliedRefresh by rememberSaveable { mutableIntStateOf(0) }
    LaunchedEffect(state.refreshVersion) {
        if (state.refreshVersion > lastAppliedRefresh) {
            listState.requestScrollToItem(0)
        }
        lastAppliedRefresh = state.refreshVersion
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
            // Fila de chips de filtro por categoría (RF 13)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onRefresh, enabled = !state.isRefreshing) {
                    if (state.isRefreshing) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar novedades", tint = AzulPrincipal)
                    }
                }
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

            // Lista de publicaciones en el feed (RF 6: orden cronológico y preferencias)
            if (state.posts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
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
                            text = "No hay avisos para los filtros seleccionados o tus facultades de interés.",
                            fontSize = 14.sp,
                            color = GrisPizarra,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.posts, key = { it.post.id }) { item ->
                        PostCard(
                            post = item.post,
                            reactionSummary = item.reactions,
                            isSaved = item.isSaved,
                            reactionTypes = state.reactionTypes,
                            onReactionClick = { tipo -> onReactionClick(item.post.id, tipo) },
                            onSaveClick = { onSaveClick(item.post.id) },
                            onPostClick = { onPostClick(item.post.id) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}
