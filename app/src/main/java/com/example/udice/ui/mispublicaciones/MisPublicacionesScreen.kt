package com.example.udice.ui.mispublicaciones

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.udice.data.model.Categoria
import com.example.udice.data.model.Post
import java.text.DateFormat
import java.util.Date

@Composable
fun MisPublicacionesRoute(
    onPostClick: (String) -> Unit = {},
    stateHolder: MisPublicacionesStateHolder = hiltViewModel()
) {
    val posts by stateHolder.ownPosts.collectAsStateWithLifecycle()
    val operation by stateHolder.operation.collectAsStateWithLifecycle()
    MisPublicacionesScreen(posts, operation, onPostClick, stateHolder::edit, stateHolder::delete)
}

@Composable
private fun MisPublicacionesScreen(
    posts: List<Post>,
    operation: PostOperation,
    onPostClick: (String) -> Unit,
    onEdit: (Post) -> Unit,
    onDelete: (String) -> Unit
) {
    var editing by remember { mutableStateOf<Post?>(null) }
    var deleting by remember { mutableStateOf<Post?>(null) }

    val colors = MaterialTheme.colorScheme
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(colors.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (operation.running) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        operation.error?.let { error ->
            item {
                Surface(color = colors.errorContainer, shape = RoundedCornerShape(12.dp)) {
                    Text(error, Modifier.fillMaxWidth().padding(12.dp), color = colors.onErrorContainer)
                }
            }
        }
        if (posts.isEmpty()) item {
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
                    Text("Todavía no tienes publicaciones.", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        items(posts, key = { it.id }) { post ->
            OwnPostCard(
                post = post,
                enabled = !operation.running,
                onPostClick = { onPostClick(post.id) },
                onEdit = { editing = post },
                onDelete = { deleting = post }
            )
        }
    }

    editing?.let { post ->
        EditPostDialog(post, onDismiss = { editing = null }) {
            editing = null
            onEdit(it)
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

@Composable
private fun EditPostDialog(post: Post, onDismiss: () -> Unit, onSave: (Post) -> Unit) {
    var title by remember(post.id) { mutableStateOf(post.titulo) }
    var description by remember(post.id) { mutableStateOf(post.descripcion) }
    var category by remember(post.id) { mutableStateOf(post.categoria) }
    var categoryMenu by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary) },
        title = { Text("Editar publicación") },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(post.copy(titulo = title, descripcion = description, categoria = category)) },
                shape = RoundedCornerShape(12.dp),
                enabled = title.isNotBlank() && description.isNotBlank()
            ) { Text("Guardar cambios") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}