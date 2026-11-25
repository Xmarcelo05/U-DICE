package com.example.udice.ui.mispublicaciones

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.key
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.udice.ui.busqueda.searchable
import com.example.udice.data.model.Categoria
import com.example.udice.data.model.Post
import com.example.udice.data.repository.PostMediaEdit
import com.example.udice.data.model.TipoReaccion
import java.text.DateFormat
import java.util.Date

@Composable
fun MisPublicacionesRoute(
    onPostClick: (String) -> Unit = {},
    initialEditId: String? = null,
    onEditorClosed: () -> Unit = {},
    stateHolder: MisPublicacionesStateHolder = hiltViewModel()
) {
    val posts by stateHolder.ownPosts.collectAsStateWithLifecycle()
    val operation by stateHolder.operation.collectAsStateWithLifecycle()
    val reactionTypes by stateHolder.reactionTypes.collectAsStateWithLifecycle(TipoReaccion.defaults)
    MisPublicacionesScreen(posts, operation, reactionTypes, initialEditId, onPostClick, stateHolder::edit, stateHolder::delete, stateHolder::clearError, onEditorClosed)
}

@Composable
private fun MisPublicacionesScreen(
    posts: List<Post>,
    operation: PostOperation,
    reactionTypes: List<TipoReaccion>,
    initialEditId: String?,
    onPostClick: (String) -> Unit,
    onEdit: (Post, PostMediaEdit?, () -> Unit) -> Unit,
    onDelete: (String) -> Unit,
    onClearError: () -> Unit,
    onEditorClosed: () -> Unit
) {
    var editingId by rememberSaveable(initialEditId) { mutableStateOf(initialEditId) }
    var query by rememberSaveable { mutableStateOf("") }
    val search = query.trim().searchable()
    val filtered = posts.filter { "${it.titulo} ${it.descripcion}".searchable().contains(search) }
    var deleting by remember { mutableStateOf<Post?>(null) }

    val colors = MaterialTheme.colorScheme
    LazyColumn(
        modifier = Modifier.fillMaxSize().imePadding().background(colors.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Buscar en mis publicaciones") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
        if (operation.running) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        operation.error?.takeIf { editingId == null }?.let { error ->
            item {
                Surface(color = colors.errorContainer, shape = RoundedCornerShape(12.dp)) {
                    Text(error, Modifier.fillMaxWidth().padding(12.dp), color = colors.onErrorContainer)
                }
            }
        }
        if (filtered.isEmpty()) item {
            Surface(
                color = colors.surface,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, colors.outline)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Article, contentDescription = null, tint = colors.primary)
                    Text(if (posts.isEmpty()) "Todavía no tienes publicaciones." else "No se encontraron publicaciones.",
                        style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        items(filtered, key = { it.id }) { post ->
            OwnPostCard(
                post = post,
                enabled = !operation.running,
                onPostClick = { onPostClick(post.id) },
                onEdit = { onClearError(); editingId = post.id },
                onDelete = { deleting = post }
            )
        }
    }

    posts.firstOrNull { it.id == editingId }?.let { post ->
        key(post.id) {
            EditPostDialog(post, reactionTypes, operation, onDismiss = {
                editingId = null
                onClearError()
                onEditorClosed()
            }) { changes, media ->
                onEdit(changes, media) {
                    editingId = null
                    onEditorClosed()
                }
            }
        }
    }
    deleting?.let { post ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            icon = { Icon(Icons.Default.DeleteOutline, null, tint = colors.error) },
            title = { Text("Eliminar publicación") },
            shape = RoundedCornerShape(24.dp),
            containerColor = colors.surface,
            text = { Text("¿Eliminar «${post.titulo}» del feed?") },
            confirmButton = {
                Button(
                    onClick = {
                        deleting = null
                        onDelete(post.id)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.error, contentColor = colors.onError)
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { deleting = null }) { Text("Cancelar") }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OwnPostCard(
    post: Post,
    enabled: Boolean,
    onPostClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val date = remember(post.fecha) { DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(post.fecha)) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onPostClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, colors.outline)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                itemVerticalAlignment = Alignment.CenterVertically
            ) {
                Surface(color = colors.surfaceVariant, shape = RoundedCornerShape(8.dp)) {
                    Text(
                        post.categoria.displayName,
                        Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.primary
                    )
                }
                Text(date, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
            Text(post.titulo, style = MaterialTheme.typography.titleMedium)
            Text(post.descripcion, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onEdit, enabled = enabled, shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Default.Edit, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Editar")
                }
                TextButton(
                    onClick = onDelete,
                    enabled = enabled,
                    colors = ButtonDefaults.textButtonColors(contentColor = colors.error)
                ) {
                    Icon(Icons.Default.DeleteOutline, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Eliminar")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditPostDialog(
    post: Post,
    reactionTypes: List<TipoReaccion>,
    operation: PostOperation,
    onDismiss: () -> Unit,
    onSave: (Post, PostMediaEdit?) -> Unit
) {
    var title by remember(post.id) { mutableStateOf(post.titulo) }
    var description by remember(post.id) { mutableStateOf(post.descripcion) }
    var category by remember(post.id) { mutableStateOf(post.categoria) }
    var categoryMenu by remember { mutableStateOf(false) }
    var selection by remember(post.id) { mutableStateOf(post.allowedReactionIds) }
    var tab by rememberSaveable { mutableStateOf(0) }
    val originalMedia = remember(post.id) { initialMedia(post) }
    var media by remember(post.id) { mutableStateOf(originalMedia) }

    AlertDialog(
        onDismissRequest = { if (!operation.running) onDismiss() },
        icon = { Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary) },
        title = { Text("Editar publicación") },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PrimaryTabRow(selectedTabIndex = tab) {
                    Tab(selected = tab == 0, enabled = !operation.running, onClick = { tab = 0 }, text = { Text("Publicación") })
                    Tab(selected = tab == 1, enabled = !operation.running, onClick = { tab = 1 }, text = { Text("Multimedia") })
                }
                operation.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                if (operation.running) LinearProgressIndicator(Modifier.fillMaxWidth())
                if (tab == 1) {
                    EditMediaFields(post, media, operation.running) { media = it }
                } else Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        title, { title = it }, Modifier.fillMaxWidth(), label = { Text("Título") },
                        singleLine = true, shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        description, { description = it }, Modifier.fillMaxWidth(), label = { Text("Descripción") },
                        minLines = 3, shape = RoundedCornerShape(12.dp)
                    )
                    Box {
                        OutlinedButton(onClick = { categoryMenu = true }, shape = RoundedCornerShape(12.dp)) {
                            Text("Categoría: ${category.displayName}")
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(expanded = categoryMenu, onDismissRequest = { categoryMenu = false }) {
                            Categoria.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.displayName) },
                                    onClick = { category = option; categoryMenu = false }
                                )
                            }
                        }
                    }
                    Text("Emojis para los lectores", style = MaterialTheme.typography.titleSmall)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        reactionTypes.forEach { type ->
                            val selected = selection?.contains(type.id) ?: true
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    val current = selection ?: reactionTypes.map { it.id }
                                    selection = if (selected) current - type.id else current + type.id
                                },
                                label = { Text("${type.emoji} ${type.displayName}") }
                            )
                        }
                    }
                    if (selection != null) {
                        TextButton(onClick = { selection = null }) { Text("Usar catálogo completo") }
                    }
                    if (selection?.isEmpty() == true) {
                        Text("Elige al menos un emoji.", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(post.copy(titulo = title, descripcion = description, categoria = category,
                        allowedReactionIds = selection), media.takeUnless { it == originalMedia })
                },
                shape = RoundedCornerShape(12.dp),
                enabled = !operation.running && title.isNotBlank() && description.isNotBlank() && selection?.isEmpty() != true
            ) { Text(if (operation.running) "Guardando…" else "Guardar cambios") }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !operation.running) { Text("Cancelar") } }
    )
}