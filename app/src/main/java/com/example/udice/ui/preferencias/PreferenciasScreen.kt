package com.example.udice.ui.preferencias

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.udice.ui.theme.AzulPrincipal
import com.example.udice.ui.theme.Blanco
import com.example.udice.ui.theme.GrisAzuladoClaro
import com.example.udice.ui.theme.GrisBordes
import com.example.udice.ui.theme.GrisPizarra
import com.example.udice.ui.theme.UDICETheme

@Composable
fun PreferenciasRoute(
    onGuardarExitoso: () -> Unit,
    onCancelar: () -> Unit,
    stateHolder: PreferenciasStateHolder = hiltViewModel()
) {
    val state by stateHolder.uiState.collectAsStateWithLifecycle()

    when (val current = state) {
        is PreferenciasUiState.Content -> {
            PreferenciasScreen(
                state = current,
                onToggle = { key, enabled ->
                    stateHolder.onEvent(PreferenciasEvent.Toggle(key, enabled))
                },
                onGuardarClick = {
                    stateHolder.onEvent(PreferenciasEvent.Save)
                },
                onCancelarClick = {
                    stateHolder.onEvent(PreferenciasEvent.Cancel)
                    onCancelar()
                },
                onDismissSuccess = {
                    stateHolder.onEvent(PreferenciasEvent.DismissSuccess)
                    onGuardarExitoso()
                }
            )
        }
        PreferenciasUiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(GrisAzuladoClaro),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AzulPrincipal)
            }
        }
        is PreferenciasUiState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(GrisAzuladoClaro)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = current.message, color = Color.Red)
            }
        }
    }
}

@Composable
fun PreferenciasScreen(
    state: PreferenciasUiState.Content,
    onToggle: (String, Boolean) -> Unit,
    onGuardarClick: () -> Unit,
    onCancelarClick: () -> Unit,
    onDismissSuccess: () -> Unit
) {
    val scrollState = rememberScrollState()

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
                // SECCIÓN FACULTADES
                Text(
                    text = "FACULTADES DE INTERÉS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GrisPizarra,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                listOf(
                    "Ciencias de la Vida y Tecnologías",
                    "Ciencias de la Salud",
                    "Ingeniería, Industria y Construcción",
                    "Facultad de Ciencias Administrativas, Contables y Comercio",
                    "Facultad de Educación, Turismo, Artes y Humanidades",
                    "Facultad de Ciencias Sociales, Derecho y Bienestar"
                ).forEachIndexed { index, facultad ->
                    PreferenceSwitchRow(
                        title = facultad,
                        checked = state.preferences[facultad] ?: false,
                        onCheckedChange = { checked ->
                            onToggle(facultad, checked)
                        }
                    )
                    if (index < 5) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = GrisBordes.copy(alpha = 0.5f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "DEPENDENCIAS Y SERVICIOS CAMPUS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GrisPizarra,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                listOf(
                    "Bienestar Universitario",
                    "Instituto de Idiomas"
                ).forEachIndexed { index, otro ->
                    PreferenceSwitchRow(
                        title = otro,
                        checked = state.preferences[otro] ?: false,
                        onCheckedChange = { checked ->
                            onToggle(otro, checked)
                        }
                    )
                    if (index < 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = GrisBordes.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // BOTONES GUARDAR Y CANCELAR
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Botón Guardar (Deshabilitado si no hay cambios o si está guardando)
            Button(
                onClick = onGuardarClick,
                enabled = state.hasChanges && !state.isSaving,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AzulPrincipal,
                    disabledContainerColor = GrisBordes,
                    disabledContentColor = GrisPizarra
                )
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Blanco,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Guardar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            // Botón Cancelar
            OutlinedButton(
                onClick = onCancelarClick,
                enabled = !state.isSaving,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = GrisPizarra
                )
            ) {
                Text(
                    text = "Cancelar",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Diálogo de éxito al guardar
        if (state.saveSuccess) {
            AlertDialog(
                onDismissRequest = onDismissSuccess,
                icon = {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Éxito",
                        tint = AzulPrincipal,
                        modifier = Modifier.size(48.dp)
                    )
                },
                title = {
                    Text(
                        text = "Preferencias Guardadas",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = AzulPrincipal
                    )
                },
                text = {
                    Text(
                        text = "Tus facultades y dependencias de interés se han actualizado y sincronizado correctamente.",
                        fontSize = 14.sp,
                        color = GrisPizarra
                    )
                },
                confirmButton = {
                    TextButton(onClick = onDismissSuccess) {
                        Text("Aceptar", fontWeight = FontWeight.Bold, color = AzulPrincipal)
                    }
                },
                containerColor = Blanco,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
private fun PreferenceSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1E293B),
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Blanco,
                checkedTrackColor = AzulPrincipal,
                uncheckedThumbColor = Blanco,
                uncheckedTrackColor = GrisBordes,
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreferenciasScreenPreview() {
    UDICETheme {
        PreferenciasScreen(
            state = PreferenciasUiState.Content(),
            onToggle = { _, _ -> },
            onGuardarClick = {},
            onCancelarClick = {},
            onDismissSuccess = {}
        )
    }
}
