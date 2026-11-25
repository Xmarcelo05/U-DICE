package com.example.udice.ui.busqueda

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.udice.BuildConfig
import com.example.udice.ui.perfilpublico.PeopleSearch
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.udice.data.model.Categoria
import com.example.udice.data.model.Facultad

@Composable
fun BusquedaRoute(onPostClick: (String) -> Unit, onPublisher: (String) -> Unit) {
    var tab by rememberSaveable { mutableStateOf(0) }
    Column(Modifier.fillMaxSize()) {
        if (BuildConfig.UDICE_SUPABASE) {
            PrimaryTabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Publicaciones") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Personas") })
            }
        }
        if (tab == 0) PublicationSearch(onPostClick)
        else Box(Modifier.fillMaxSize().imePadding().padding(16.dp)) { PeopleSearch(onPublisher) }
    }
}

@Composable
private fun PublicationSearch(onPostClick: (String) -> Unit, stateHolder: BusquedaStateHolder = hiltViewModel()) {
    val query by stateHolder.query.collectAsStateWithLifecycle()
    val category by stateHolder.category.collectAsStateWithLifecycle()
    val faculty by stateHolder.faculty.collectAsStateWithLifecycle()
    val dates by stateHolder.dates.collectAsStateWithLifecycle()
    val results by stateHolder.results.collectAsStateWithLifecycle()
    val focus = LocalFocusManager.current
    val listState = rememberLazyListState()
    LaunchedEffect(query, category, faculty, dates) { listState.scrollToItem(0) }
    LaunchedEffect(results.posts.size, results.hasMore, results.loading, results.failed) {
        if (results.hasMore && !results.loading && !results.failed && results.posts.isNotEmpty()) {
            snapshotFlow { listState.layoutInfo.visibleItemsInfo.any { it.index >= results.posts.lastIndex } }.first { it }
            stateHolder.loadMore()
        }
    }

    Column(Modifier.fillMaxSize().imePadding().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = stateHolder::updateQuery,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Busca por título o descripción") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { stateHolder.updateQuery("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Borrar búsqueda")
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() })
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SearchFilter("Categoría", category, categories, Modifier.weight(1f), stateHolder::updateCategory)
            SearchFilter("Facultad", faculty, faculties, Modifier.weight(1f), stateHolder::updateFaculty)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SearchDate("Desde", dates.first, dates.second, true, Modifier.weight(1f)) { stateHolder.updateDate(true, it) }
            SearchDate("Hasta", dates.second, dates.first, false, Modifier.weight(1f)) { stateHolder.updateDate(false, it) }
        }
        if (results.tooShort) Text("Escribe al menos 3 caracteres.")
        if (results.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
        if (results.failed) {
            Text("No se pudieron actualizar los avisos.", color = MaterialTheme.colorScheme.error)
            TextButton(onClick = stateHolder::retry) { Text("Reintentar") }
        }
        if (results.posts.isEmpty() && !results.loading && !results.failed && !results.tooShort) Text("No se encontraron publicaciones.")
        LazyColumn(state = listState, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(results.posts, key = { it.id }) { post ->
                Card(Modifier.fillMaxWidth().clickable {
                    focus.clearFocus()
                    onPostClick(post.id)
                }) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(post.title, style = MaterialTheme.typography.titleMedium)
                        Text(post.excerpt, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(
                            "${post.category.displayName} · ${post.faculty.displayName}",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchFilter(
    label: String,
    value: String,
    options: List<Pair<String, String>>,
    modifier: Modifier,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        FilterChip(
            selected = value.isNotEmpty(),
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(options.firstOrNull { it.first == value }?.second ?: label, maxLines = 1, overflow = TextOverflow.Ellipsis) }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("Todas") }, onClick = { onSelect(""); expanded = false })
            options.forEach { (id, name) ->
                DropdownMenuItem(text = { Text(name) }, onClick = { onSelect(id); expanded = false })
            }
        }
    }
}

@Composable
private fun SearchDate(
    label: String,
    value: String,
    other: String,
    isStart: Boolean,
    modifier: Modifier,
    onChange: (String) -> Unit
) {
    val context = LocalContext.current
    Box(modifier) {
        FilterChip(
            selected = value.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                val initial = LocalDate.parse(value.ifEmpty { other.ifEmpty { LocalDate.now().toString() } })
                val dialog = DatePickerDialog(context, { _, year, month, day ->
                    onChange(LocalDate.of(year, month + 1, day).toString())
                }, initial.year, initial.monthValue - 1, initial.dayOfMonth)
                if (other.isNotEmpty()) {
                    val bound = LocalDate.parse(other).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                    if (isStart) dialog.datePicker.maxDate = bound else dialog.datePicker.minDate = bound
                }
                dialog.show()
            },
            label = { Text(value.ifEmpty { label }) },
            trailingIcon = if (value.isEmpty()) null else {
                { Icon( Icons.Default.Close, contentDescription = "Quitar fecha", modifier = Modifier.clickable { onChange("") }) }
            }
        )
    }
}

private val categories = Categoria.entries.map { it.name to it.displayName }
private val faculties = Facultad.entries.map { it.name to it.displayName }
