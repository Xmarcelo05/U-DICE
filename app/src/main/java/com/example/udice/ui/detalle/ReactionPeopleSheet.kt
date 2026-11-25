package com.example.udice.ui.detalle

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.udice.data.model.ReactionCount

@Composable
internal fun ReactionSummaryButton(counts: List<ReactionCount>, onClick: () -> Unit) {
    val total = counts.sumOf { it.count }
    if (total == 0) return
    val emoji = counts.take(3).joinToString(" ") { it.emoji }
    TextButton(onClick = onClick) {
        Text("$emoji · $total ${if (total == 1) "reacción" else "reacciones"}")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReactionPeopleSheet(
    state: ReactionPanelState,
    onDismiss: () -> Unit,
    onSelect: (String?) -> Unit,
    onMore: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
            Text("Reacciones", style = MaterialTheme.typography.titleLarge)
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(state.selected == null, { onSelect(null) }, { Text("Todas") })
                state.counts.forEach { count ->
                    FilterChip(
                        state.selected == count.type, { onSelect(count.type) },
                        { Text("${count.emoji} ${count.count}") }
                    )
                }
            }
            LazyColumn(Modifier.fillMaxWidth().heightIn(max = 420.dp)) {
                items(state.people, key = { it.id }) { person ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(person.name.trim().take(1).uppercase())
                            person.avatarUrl?.let { url ->
                                AsyncImage(url, null, Modifier.size(36.dp), contentScale = ContentScale.Crop)
                            }
                        }
                        Text(person.name, Modifier.weight(1f))
                        Text(person.emoji)
                    }
                }
                item {
                    when {
                        state.loading -> CircularProgressIndicator(Modifier.padding(12.dp).size(24.dp))
                        state.failed -> TextButton(onClick = onMore) { Text("Reintentar") }
                        state.next != null -> TextButton(onClick = onMore) { Text("Cargar más") }
                        state.people.isEmpty() -> Text("Todavía no hay reacciones.")
                    }
                }
            }
        }
    }
}
