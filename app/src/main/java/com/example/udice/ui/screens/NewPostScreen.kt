package com.example.udice.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.udice.ui.components.BottomNavigationBar
import com.example.udice.ui.components.UniversityHeader
import com.example.udice.ui.theme.BorderGray
import com.example.udice.ui.theme.DarkBlue
import com.example.udice.ui.theme.LightBlueGray
import com.example.udice.ui.theme.PrimaryBlue
import com.example.udice.ui.theme.PureWhite
import com.example.udice.ui.theme.SlateGray

@Composable
fun NewPostScreen(
    onPostCreated: () -> Unit,
    onNavigateToPreferences: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val classifications = listOf("Evento", "Convocatoria", "Conferencia", "Aviso", "Otro")
    var selectedClassification by remember { mutableStateOf("Convocatoria") }
    var imageSelected by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            UniversityHeader(
                title = "Nueva Publicación"
            )
        },
        bottomBar = {
            BottomNavigationBar(
                isPublisher = true,
                currentTab = "new_post",
                showProfileTab = false,
                onTabSelected = { tab ->
                    when (tab) {
                        "preferences" -> onNavigateToPreferences()
                        "home" -> onNavigateToHome()
                        "new_post" -> { /* Ya estamos aquí */ }
                    }
                }
            )
        },
        containerColor = LightBlueGray
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderGray, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        // TÍTULO
                        Text(
                            text = "TÍTULO",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = SlateGray,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            ),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            placeholder = { Text("Escribe el título de la publicación") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = BorderGray,
                                cursorColor = PrimaryBlue
                            )
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // DESCRIPCIÓN
                        Text(
                            text = "DESCRIPCIÓN",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = SlateGray,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            ),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            placeholder = { Text("Detalla la información importante, fechas, requisitos...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp),
                            maxLines = 6,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = BorderGray,
                                cursorColor = PrimaryBlue
                            )
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // CLASIFICACIÓN
                        Text(
                            text = "CLASIFICACIÓN",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = SlateGray,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            ),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            classifications.forEach { cat ->
                                val isSelected = selectedClassification == cat
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedClassification = cat },
                                    label = {
                                        Text(
                                            text = cat,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 12.sp
                                        )
                                    },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryBlue,
                                        selectedLabelColor = PureWhite,
                                        containerColor = LightBlueGray,
                                        labelColor = DarkBlue
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSelected,
                                        borderColor = if (isSelected) PrimaryBlue else BorderGray
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Botón Agregar Imagen
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .border(1.dp, BorderGray, RoundedCornerShape(12.dp))
                                .background(if (imageSelected) PrimaryBlue.copy(alpha = 0.08f) else LightBlueGray)
                                .clickable { imageSelected = !imageSelected }
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = "Agregar imagen",
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = if (imageSelected) "Imagen adjunta correctamente" else "Agregar imagen",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = if (imageSelected) PrimaryBlue else SlateGray,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Botón Publicar
                        Button(
                            onClick = onPostCreated,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Text(
                                text = "Publicar",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = PureWhite
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
