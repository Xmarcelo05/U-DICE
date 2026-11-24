package com.example.udice.ui.nuevapublicacion

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.udice.ui.components.PublicationImage
import com.example.udice.ui.theme.AzulPrincipal
import com.example.udice.ui.theme.Blanco
import com.example.udice.ui.theme.ColorAviso
import com.example.udice.ui.theme.ColorConferencia
import com.example.udice.ui.theme.ColorConvocatoria
import com.example.udice.ui.theme.ColorConvocatoriaTexto
import com.example.udice.ui.theme.ColorEvento
import com.example.udice.ui.theme.ColorOtro
import com.example.udice.ui.theme.GrisAzuladoClaro
import com.example.udice.ui.theme.GrisBordes
import com.example.udice.ui.theme.GrisPizarra
import com.example.udice.ui.theme.UDICETheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NuevaPublicacionScreen(
    state: NuevaPublicacionUiState.Content,
    onTituloChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onCategoriaChange: (String) -> Unit,
    onImageSelected: (Uri?) -> Unit,
    onVideoSelected: (Uri?) -> Unit,
    onSubmit: () -> Unit,
    onDismissSuccess: () -> Unit,
    onPublicacionExitosa: () -> Unit
) {
    val scrollState = rememberScrollState()
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) onImageSelected(uri)
    }
    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) onVideoSelected(uri)
    }
    val hasImage = state.image != null

    val categorias = listOf(
        "Evento" to ColorEvento,
        "Convocatoria" to ColorConvocatoria,
        "Conferencia" to ColorConferencia,
        "Aviso" to ColorAviso,
        "Otro" to ColorOtro
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GrisAzuladoClaro)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Blanco),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // TÍTULO
                Text(
                    text = "TÍTULO",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GrisPizarra,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.titulo,
                    onValueChange = onTituloChange,
                    placeholder = { Text("Escribe un título para la publicación...", color = GrisPizarra.copy(alpha = 0.6f)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AzulPrincipal,
                        unfocusedBorderColor = GrisBordes,
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedContainerColor = Color(0xFFF8FAFC)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                // DESCRIPCIÓN
                Text(
                    text = "DESCRIPCIÓN",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GrisPizarra,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.descripcion,
                    onValueChange = onDescripcionChange,
                    placeholder = { Text("Detalla la información extracurricular...", color = GrisPizarra.copy(alpha = 0.6f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AzulPrincipal,
                        unfocusedBorderColor = GrisBordes,
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedContainerColor = Color(0xFFF8FAFC)
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // CLASIFICACIÓN
                Text(
                    text = "CLASIFICACIÓN",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GrisPizarra,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categorias.forEach { (nombre, colorBase) ->
                        val isSelected = state.categoria == nombre
                        val backgroundColor = if (isSelected) colorBase else Color(0xFFF1F5F9)
                        val textColor = when {
                            isSelected && nombre == "Convocatoria" -> ColorConvocatoriaTexto
                            isSelected -> Blanco
                            else -> GrisPizarra
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = backgroundColor,
                            border = if (isSelected) null else BorderStroke(1.dp, GrisBordes),
                            modifier = Modifier.clickable { onCategoriaChange(nombre) }
                        ) {
                            Text(
                                text = nombre,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = textColor,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // AGREGAR IMAGEN
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, if (hasImage) AzulPrincipal else GrisBordes),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !state.isSubmitting) {
                            picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 16.dp, horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (hasImage) Icons.Default.CheckCircle else Icons.Default.Image,
                            contentDescription = "Agregar imagen",
                            tint = if (hasImage) AzulPrincipal else GrisPizarra,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (hasImage) "Cambiar imagen" else "Agregar imagen",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (hasImage) AzulPrincipal else GrisPizarra
                        )
                    }
                }

                state.image?.let { uri ->
                    Spacer(modifier = Modifier.height(12.dp))
                    PublicationImage(uri, "Vista previa de la imagen")
                    TextButton(onClick = { onImageSelected(null) }, enabled = !state.isSubmitting) {
                        Text("Quitar imagen")
                    }
                }

                OutlinedButton(
                    onClick = {
                        videoPicker.launch(PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.SingleMimeType("video/mp4")
                        ))
                    },
                    enabled = !state.isSubmitting,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (state.video == null) "Agregar video MP4" else "Cambiar video MP4")
                }
                Text("Video opcional, hasta 20 MB", color = GrisPizarra, fontSize = 12.sp)
                if (state.video != null) {
                    TextButton(onClick = { onVideoSelected(null) }, enabled = !state.isSubmitting) {
                        Text("Quitar video")
                    }
                }
                state.uploadProgress?.let { percent ->
                    LinearProgressIndicator(progress = { percent / 100f }, modifier = Modifier.fillMaxWidth())
                    Text(if (percent == 100) "Guardando publicación…" else "Subiendo video: $percent%")
                }

                state.error?.let { message ->
                    Text(text = message, color = Color(0xFFB91C1C), modifier = Modifier.padding(top = 12.dp))
                }
                Spacer(modifier = Modifier.height(28.dp))

                // BOTÓN PUBLICAR
                Button(
                    onClick = onSubmit,
                    enabled = !state.isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AzulPrincipal,
                        disabledContainerColor = GrisBordes,
                        disabledContentColor = GrisPizarra
                    )
                ) {
                    Text(
                        text = if (state.isSubmitting) "Publicando…" else "Publicar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }

    // Diálogo de confirmación de publicación exitosa
    if (state.published) {
        AlertDialog(
            onDismissRequest = { },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = AzulPrincipal,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = "¡Publicación realizada!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Tu publicación sobre '${state.titulo}' ha sido difundida correctamente a la comunidad universitaria.",
                    fontSize = 14.sp,
                    color = GrisPizarra
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDismissSuccess()
                        onPublicacionExitosa()
                    }
                ) {
                    Text("Aceptar", fontWeight = FontWeight.Bold, color = AzulPrincipal)
                }
            },
            containerColor = Blanco,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun NuevaPublicacionRoute(
    onPublicacionExitosa: () -> Unit,
    stateHolder: NuevaPublicacionStateHolder = hiltViewModel()
) {
    val state by stateHolder.uiState.collectAsStateWithLifecycle()
    when (val current = state) {
        is NuevaPublicacionUiState.Content -> NuevaPublicacionScreen(
            state = current,
            onTituloChange = { stateHolder.onEvent(NuevaPublicacionEvent.TitleChanged(it)) },
            onDescripcionChange = { stateHolder.onEvent(NuevaPublicacionEvent.DescriptionChanged(it)) },
            onCategoriaChange = { stateHolder.onEvent(NuevaPublicacionEvent.CategorySelected(it)) },
            onImageSelected = { stateHolder.onEvent(NuevaPublicacionEvent.ImageSelected(it)) },
            onVideoSelected = { stateHolder.onEvent(NuevaPublicacionEvent.VideoSelected(it)) },
            onSubmit = { stateHolder.onEvent(NuevaPublicacionEvent.Submit) },
            onDismissSuccess = { stateHolder.onEvent(NuevaPublicacionEvent.DismissSuccess) },
            onPublicacionExitosa = onPublicacionExitosa
        )
        NuevaPublicacionUiState.Loading -> Unit
        is NuevaPublicacionUiState.Error -> Text(text = current.message)
    }
}

@Preview(showBackground = true)
@Composable
fun NuevaPublicacionScreenPreview() {
    UDICETheme {
        NuevaPublicacionScreen(
            state = NuevaPublicacionUiState.Content(), onTituloChange = {}, onDescripcionChange = {},
            onCategoriaChange = {}, onImageSelected = {}, onVideoSelected = {}, onSubmit = {}, onDismissSuccess = {}, onPublicacionExitosa = {}
        )
    }
}
