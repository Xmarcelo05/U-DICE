package com.example.udice.ui.detalle

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import com.example.udice.ui.components.PublicationImage
import com.example.udice.ui.components.PublicationVideo
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.udice.ui.theme.AzulPrincipal
import com.example.udice.ui.theme.GrisAzuladoClaro
import com.example.udice.ui.theme.GrisPizarra
import java.text.DateFormat
import java.util.Date

@Composable
fun DetallePublicacionRoute(
    onBack: () -> Unit,
    stateHolder: DetallePublicacionStateHolder = hiltViewModel()
) {
    val state by stateHolder.uiState.collectAsStateWithLifecycle()

    DetallePublicacionScreen(
        state = state,
        onToggleSave = { stateHolder.onEvent(DetallePublicacionEvent.ToggleSave) },
        onBack = onBack
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetallePublicacionScreen(
    state: DetallePublicacionUiState,
    onToggleSave: () -> Unit,
    onBack: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Volver")
                }
            }

            when (state) {
                DetallePublicacionUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AzulPrincipal)
                    }
                }
                DetallePublicacionUiState.NotFound -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            color = colors.surface,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, colors.outline)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = GrisPizarra,
                                    modifier = Modifier.size(44.dp)
                                )
                                Text(
                                    text = "Publicación no disponible",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Este aviso fue eliminado o ya no existe.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.onSurfaceVariant
                                )
                                Button(onClick = onBack, shape = RoundedCornerShape(12.dp)) {
                                    Text("Regresar")
                                }
                            }
                        }
                    }
                }
                is DetallePublicacionUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = state.message, color = colors.error)
                    }
                }
                is DetallePublicacionUiState.Content -> {
                    val post = state.post
                    val formattedDate = remember(post.fecha) {
                        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                            .format(Date(post.fecha))
                    }
                    val scopeLabel = when (post.alcance.name) {
                        "GENERAL" -> "Alcance general"
                        "CARRERA" -> post.carrera?.let { "Carrera: $it" } ?: "Específico de carrera"
                        else -> post.alcance.name
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        state.errorMessage?.let { error ->
                            Surface(
                                color = colors.errorContainer,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = error,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    color = colors.onErrorContainer
                                )
                            }
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = colors.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            border = BorderStroke(1.dp, colors.outline)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        color = colors.surfaceVariant,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = post.categoria.displayName,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.primary
                                        )
                                    }
                                    Surface(
                                        color = GrisAzuladoClaro,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = scopeLabel,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = colors.onSurfaceVariant
                                        )
                                    }
                                }

                                Text(
                                    text = post.titulo,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.onSurface
                                )

                                val videoUrl = post.videoUrl
                                if (videoUrl != null) {
                                    PublicationVideo(videoUrl, post.imageUrl)
                                } else {
                                    post.imageUrl?.takeIf(String::isNotBlank)?.let { url ->
                                        PublicationImage(url, "Imagen de ${post.titulo}")
                                    }
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    DetailMetaRow(Icons.Default.School, post.facultad.displayName)
                                    if (post.autorNombre.isNotBlank()) {
                                        DetailMetaRow(Icons.Default.Person, post.autorNombre)
                                    }
                                    DetailMetaRow(Icons.Default.CalendarToday, formattedDate)
                                    DetailMetaRow(Icons.Default.Public, scopeLabel)
                                }

                                HorizontalDivider(color = colors.outline)

                                Text(
                                    text = post.descripcion,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = colors.onSurface
                                )

                                Spacer(Modifier.height(4.dp))

                                if (state.isSaved) {
                                    OutlinedButton(
                                        onClick = onToggleSave,
                                        enabled = !state.isUpdatingSave,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text(if (state.isUpdatingSave) "Actualizando..." else "Guardado en tu bóveda (Quitar)")
                                    }
                                } else {
                                    Button(
                                        onClick = onToggleSave,
                                        enabled = !state.isUpdatingSave,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = AzulPrincipal)
                                    ) {
                                        Icon(Icons.Default.BookmarkBorder, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text(if (state.isUpdatingSave) "Guardando..." else "Guardar en mi bóveda")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailMetaRow(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = GrisPizarra,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}