package com.example.udice.ui.screens

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NuevaPublicacionScreen(
    onPublicacionExitosa: () -> Unit = {}
) {
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    val categorias = listOf(
        "Evento" to ColorEvento,
        "Convocatoria" to ColorConvocatoria,
        "Conferencia" to ColorConferencia,
        "Aviso" to ColorAviso,
        "Otro" to ColorOtro
    )
    var selectedCategoria by remember { mutableStateOf("Convocatoria") }
    var hasImage by remember { mutableStateOf(false) }
    var showDialogSuccess by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GrisAzuladoClaro),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
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
                        value = titulo,
                        onValueChange = { titulo = it },
                        placeholder = { Text("Escribe el título de la publicación...", color = GrisPizarra.copy(alpha = 0.6f)) },
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
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        placeholder = { Text("Detalla la información importante, fechas, requisitos...", color = GrisPizarra.copy(alpha = 0.6f)) },
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
                            val isSelected = selectedCategoria == nombre
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
                                modifier = Modifier.clickable { selectedCategoria = nombre }
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
                            .clickable { hasImage = !hasImage }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 16.dp, horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (hasImage) Icons.Default.CheckCircle else Icons.Default.AddPhotoAlternate,
                                contentDescription = "Agregar imagen",
                                tint = if (hasImage) AzulPrincipal else GrisPizarra,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (hasImage) "Imagen adjuntada correctamente ✓" else "Agregar imagen",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (hasImage) AzulPrincipal else GrisPizarra
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // BOTÓN PUBLICAR
                    Button(
                        onClick = {
                            if (titulo.isNotBlank() && descripcion.isNotBlank()) {
                                showDialogSuccess = true
                            }
                        },
                        enabled = titulo.isNotBlank() && descripcion.isNotBlank(),
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
                            text = "Publicar",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Blanco
                        )
                    }
                }
            }
        }
    }

    // Diálogo de confirmación de publicación exitosa
    if (showDialogSuccess) {
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
                    text = "Tu publicación sobre '$titulo' ha sido difundida correctamente a la comunidad universitaria.",
                    fontSize = 14.sp,
                    color = GrisPizarra
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        titulo = ""
                        descripcion = ""
                        selectedCategoria = "Convocatoria"
                        hasImage = false
                        showDialogSuccess = false
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
