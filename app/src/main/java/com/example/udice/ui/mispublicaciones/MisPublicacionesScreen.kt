package com.example.udice.ui.mispublicaciones

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.udice.data.model.Categoria
import com.example.udice.data.model.Post

@Composable
fun MisPublicacionesRoute(stateHolder: MisPublicacionesStateHolder = hiltViewModel()) {
    val posts by stateHolder.ownPosts.collectAsStateWithLifecycle()
    val operation by stateHolder.operation.collectAsStateWithLifecycle()
    MisPublicacionesScreen(posts, operation, stateHolder::edit, stateHolder::delete)
}

@Composable
private fun MisPublicacionesScreen(
    posts: List<Post>,
    operation: PostOperation,
    onEdit: (Post) -> Unit,
    onDelete: (String) -> Unit
) {
    var editing by remember { mutableStateOf<Post?>(null) }
    var deleting by remember { mutableStateOf<Post?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (operation.running) item { Text("Guardando cambios…") }
        operation.error?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
        if (posts.isEmpty()) item { Text("Todavía no tienes publicaciones.") }
        items(posts, key = { it.id }) { post ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(post.titulo, style = MaterialTheme.typography.titleMedium)
                    Text(post.categoria.displayName)
                    Text(post.descripcion)
                    Row {
                        TextButton(onClick = { editing = post }, enabled = !operation.running) { Text("Editar") }
                        TextButton(onClick = { deleting = post }, enabled = !operation.running) { Text("Eliminar") }
                    }
                }
            }
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
            title = { Text("Eliminar publicación") },
            text = { Text("¿Eliminar «${post.titulo}» del feed?") },
            confirmButton = {
                TextButton(onClick = { deleting = null; onDelete(post.id) }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { deleting = null }) { Text("Cancelar") }
            }
        )
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
        title = { Text("Editar publicación") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(title, { title = it }, label = { Text("Título") }, singleLine = true)
                OutlinedTextField(description, { description = it }, label = { Text("Descripción") })
                Box {
                    TextButton(onClick = { categoryMenu = true }) { Text(category.displayName) }
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
            TextButton(
                onClick = { onSave(post.copy(titulo = title, descripcion = description, categoria = category)) },
                enabled = title.isNotBlank() && description.isNotBlank()
            ) { Text("Guardar cambios") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
