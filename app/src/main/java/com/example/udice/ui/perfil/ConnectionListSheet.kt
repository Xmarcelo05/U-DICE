package com.example.udice.ui.perfil

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.udice.ui.perfilpublico.PublisherIdentity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ConnectionListSheet(state: ConnectionsState, onClose: () -> Unit, onMore: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onClose) {
        Column(Modifier.fillMaxWidth().padding(20.dp)) {
            Text(if (state.followers) "Seguidores" else "Siguiendo", style = MaterialTheme.typography.titleLarge)
            LazyColumn(Modifier.fillMaxWidth().heightIn(max = 420.dp)) {
                items(state.people, key = { it.id }) {
                    PublisherIdentity(it, Modifier.fillMaxWidth().padding(vertical = 8.dp))
                }
                item {
                    when {
                        state.loading -> LinearProgressIndicator(Modifier.fillMaxWidth())
                        state.failed -> TextButton(onClick = onMore) { Text("Reintentar") }
                        state.next != null -> TextButton(onClick = onMore) { Text("Ver más") }
                        state.people.isEmpty() -> Text(
                            if (state.followers) "Todavía no tienes seguidores." else "Todavía no sigues a ningún publicador.",
                            Modifier.padding(vertical = 12.dp)
                        )
                    }
                }
            }
        }
    }
}
