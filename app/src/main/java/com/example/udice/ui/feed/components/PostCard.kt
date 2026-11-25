package com.example.udice.ui.feed.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import com.example.udice.ui.components.PublicationVideoThumbnail
import com.example.udice.ui.components.PublicationImageGrid
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.udice.data.model.Categoria
import com.example.udice.data.model.Post
import com.example.udice.data.model.ReaccionSummary
import com.example.udice.data.model.TipoReaccion
import com.example.udice.ui.theme.AzulInstitucional
import com.example.udice.ui.theme.AzulOscuro
import com.example.udice.ui.theme.AzulPrincipal
import com.example.udice.ui.theme.Blanco
import com.example.udice.ui.theme.ColorAviso
import com.example.udice.ui.theme.ColorConferencia
import com.example.udice.ui.theme.ColorConvocatoria
import com.example.udice.ui.theme.ColorEvento
import com.example.udice.ui.theme.ColorOtro
import com.example.udice.ui.theme.GrisAzuladoClaro
import com.example.udice.ui.theme.GrisBordes
import com.example.udice.ui.theme.GrisPizarra
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PostCard(
    post: Post,
    reactionSummary: ReaccionSummary,
    isSaved: Boolean,
    onReactionClick: (TipoReaccion) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
    onPostClick: () -> Unit = {},
    reactionTypes: List<TipoReaccion> = TipoReaccion.defaults,
    onEditClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
    authorAvatarUrl: String? = post.authorAvatarUrl,
    videoContent: (@Composable () -> Unit)? = null
) {
    val categoryColor = when (post.categoria) {
        Categoria.EVENTO -> ColorEvento
        Categoria.CONVOCATORIA -> ColorConvocatoria
        Categoria.CONFERENCIA -> ColorConferencia
        Categoria.AVISO -> ColorAviso
        Categoria.OTRO -> ColorOtro
    }

    val initials = post.autorNombre.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .map { it.first().uppercaseChar() }
        .joinToString("")
        .ifEmpty { "UD" }

    val formattedDate = SimpleDateFormat("dd MMM · HH:mm", Locale.getDefault())
        .format(Date(post.fecha))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onPostClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Blanco),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, GrisBordes)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Encabezado: Avatar, Autor, Fecha y Categoría
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AzulInstitucional),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        color = Blanco,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    authorAvatarUrl?.let { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (onEditClick != null) "Yo" else post.autorNombre.ifEmpty { post.facultad.displayName },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = AzulOscuro
                        ),
                        maxLines = 1
                    )
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = GrisPizarra,
                            fontSize = 11.sp
                        )
                    )
                }

                // Badge de Categoría
                Surface(
                    color = categoryColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, categoryColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = post.categoria.displayName,
                        color = categoryColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                if (onEditClick != null && onDeleteClick != null) {
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { expanded = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "Opciones de publicación",
                                tint = AzulPrincipal
                            )
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            DropdownMenuItem(text = { Text("Editar") }, onClick = {
                                expanded = false
                                onEditClick()
                            })
                            DropdownMenuItem(text = { Text("Eliminar") }, onClick = {
                                expanded = false
                                onDeleteClick()
                            })
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Título
            Text(
                text = post.titulo,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = AzulOscuro,
                    fontSize = 16.sp
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Descripción
            Text(
                text = post.descripcion,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = GrisPizarra,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (post.videoUrl != null) {
                if (videoContent != null) videoContent()
                else PublicationVideoThumbnail(post.imageUrl, "Abrir detalle del video", onPostClick)
                Spacer(modifier = Modifier.height(10.dp))
            } else if (post.imageUrls.isNotEmpty()) {
                PublicationImageGrid(post.imageUrls)
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Chips informativos: Facultad y Alcance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = GrisAzuladoClaro,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = AzulInstitucional,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = post.facultad.displayName,
                            fontSize = 11.sp,
                            color = AzulOscuro,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Fila de Reacciones Predefinidas (RF 9) y Guardado
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Reacciones predefinidas (RF 9)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    reactionTypes.filter { post.allowedReactionIds?.contains(it.id) != false }.forEach { tipo ->
                        val isSelected = reactionSummary.userReaction == tipo
                        val count = reactionSummary.counts[tipo] ?: 0

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) AzulPrincipal.copy(alpha = 0.15f) else GrisAzuladoClaro,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) AzulPrincipal else Color.Transparent
                            ),
                            modifier = Modifier
                                .semantics { contentDescription = tipo.displayName }
                                .clickable { onReactionClick(tipo) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = tipo.emoji, fontSize = 12.sp)
                                if (count > 0) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "$count",
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) AzulPrincipal else GrisPizarra
                                    )
                                }
                            }
                        }
                    }
                }

                // Botón Guardado (Bookmarks)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSaved) AzulPrincipal.copy(alpha = 0.1f) else Color.Transparent)
                        .clickable { onSaveClick() }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Guardar publicación",
                        tint = if (isSaved) AzulPrincipal else GrisPizarra,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
