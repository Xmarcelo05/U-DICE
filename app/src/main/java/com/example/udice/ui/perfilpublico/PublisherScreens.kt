package com.example.udice.ui.perfilpublico

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.udice.data.model.Categoria
import com.example.udice.data.repository.Publisher
import java.text.DateFormat
import java.util.Date
import kotlinx.coroutines.flow.first

@Composable
fun PeopleSearch(onPublisher: (String) -> Unit, stateHolder: PeopleStateHolder = hiltViewModel()) {
    val query by stateHolder.query.collectAsStateWithLifecycle()
    val state by stateHolder.state.collectAsStateWithLifecycle()
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = stateHolder::updateQuery,
            label = { Text("Buscar publicadores por nombre") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        if (query.trim().length < 2) Text("Escribe al menos 2 caracteres.")
        if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.people, key = { it.id }) { person ->
                Card(Modifier.fillMaxWidth().clickable { onPublisher(person.id) }) {
                    PublisherIdentity(person, Modifier.padding(12.dp))
                }
            }
            item {
                when {
                    state.error != null -> {
                        Text(state.error.orEmpty(), color = MaterialTheme.colorScheme.error)
                        TextButton(onClick = stateHolder::loadMore) { Text("Reintentar") }
                    }
                    state.next != null && !state.loading -> TextButton(onClick = stateHolder::loadMore) { Text("Cargar más") }
                    state.people.isEmpty() && query.trim().length >= 2 && !state.loading -> Text("No se encontraron publicadores.")
                }
            }
        }
    }
}

@Composable
fun PublisherRoute(onBack: () -> Unit, onPost: (String) -> Unit, stateHolder: PublisherStateHolder = hiltViewModel()) {
    val state by stateHolder.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    LaunchedEffect(state.next, state.loading, state.postsFailed) {
        if (state.next != null && !state.loading && !state.postsFailed) {
            snapshotFlow {
                val layout = listState.layoutInfo
                val last = layout.visibleItemsInfo.lastOrNull()?.index ?: -1
                layout.totalItemsCount > 0 && last >= layout.totalItemsCount - 2
            }.first { it }
            stateHolder.loadMore()
        }
    }
    LazyColumn(
        Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { TextButton(onClick = onBack) { Text("Volver") } }
        state.profile?.let { profile ->
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PublisherIdentity(profile.publisher)
                        if (!profile.own) {
                            Button(onClick = stateHolder::toggleFollowing, enabled = !state.saving) {
                                Text(if (profile.following) "Dejar de seguir" else "Seguir")
                            }
                        }
                    }
                }
            }
            item { Text("Publicaciones", style = MaterialTheme.typography.titleMedium) }
        }
        state.error?.let { error ->
            item {
                Text(error, color = MaterialTheme.colorScheme.error)
                if (state.profile == null) TextButton(onClick = stateHolder::reload) { Text("Reintentar") }
            }
        }
        items(state.posts, key = { it.id }) { post ->
            val date = remember(post.date) { DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(post.date)) }
            val category = Categoria.entries.find { it.name == post.category }?.displayName ?: post.category
            Card(Modifier.fillMaxWidth().clickable { onPost(post.id) }) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(post.title, style = MaterialTheme.typography.titleSmall)
                    Text("$category · $date", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
        item {
            when {
                state.loading -> LinearProgressIndicator(Modifier.fillMaxWidth())
                state.postsFailed -> TextButton(onClick = stateHolder::loadMore) { Text("Reintentar carga") }
                state.next != null -> TextButton(onClick = stateHolder::loadMore) { Text("Cargar más publicaciones") }
                state.profile != null && state.posts.isEmpty() -> Text("Este publicador todavía no tiene avisos.")
            }
        }
    }
}

@Composable
internal fun PublisherIdentity(person: Publisher, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(52.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(person.name.trim().take(1).uppercase())
            person.avatar?.let { AsyncImage(it, null, Modifier.size(52.dp), contentScale = ContentScale.Crop) }
        }
        Column {
            Text(person.name, style = MaterialTheme.typography.titleMedium)
            person.faculty?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            person.followers?.let { count ->
                Text("$count ${if (count == 1L) "seguidor" else "seguidores"}", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}
