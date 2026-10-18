package com.example.udice.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.udice.ui.theme.AzulPrincipal
import com.example.udice.ui.theme.Blanco
import com.example.udice.ui.theme.GrisAzuladoClaro
import com.example.udice.ui.theme.GrisBordes
import com.example.udice.ui.theme.GrisPizarra
import com.example.udice.ui.theme.UDICETheme

@Composable
fun PreferenciasScreen(
    onGuardarClick: () -> Unit,
    onCancelarClick: () -> Unit
) {
    // Mapa de preferencias iniciales (según prototipo)
    val initialPreferences = remember {
        mapOf(
            "Ciencias de la Vida y Tecnologías" to true,
            "Ciencias de la Salud" to true,
            "Ingeniería, Industria y Construcción" to false,
            "Facultad de Ciencias Administrativas, Contables y Comercio" to false,
            "Facultad de Educación, Turismo, Artes y Humanidades" to false,
            "Facultad de Ciencias Sociales, Derecho y Bienestar" to false,
            "Bienestar Universitario" to true,
            "Instituto de Idiomas" to false
        )
    }

    // Estado editable de switches
    val preferencesState = remember {
        mutableStateMapOf<String, Boolean>().apply {
            putAll(initialPreferences)
        }
    }

    // El botón Guardar se habilita SOLO si hay cambios
    val hasChanges by remember {
        derivedStateOf {
            preferencesState.any { (key, value) -> initialPreferences[key] != value }
        }
    }

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
                    text = "FACULTADES",
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
                        checked = preferencesState[facultad] ?: false,
                        onCheckedChange = { checked ->
                            preferencesState[facultad] = checked
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

                // SECCIÓN OTROS
                Text(
                    text = "OTROS",
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
                        checked = preferencesState[otro] ?: false,
                        onCheckedChange = { checked ->
                            preferencesState[otro] = checked
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
            // Botón Guardar (Deshabilitado si no hay cambios)
            Button(
                onClick = onGuardarClick,
                enabled = hasChanges,
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
                Text(
                    text = "Guardar",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            // Botón Cancelar
            OutlinedButton(
                onClick = onCancelarClick,
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
            onGuardarClick = {},
            onCancelarClick = {}
        )
    }
}
