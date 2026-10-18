package com.example.udice.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
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
fun PreferencesScreen(
    isPublisher: Boolean,
    onNavigateToHome: () -> Unit,
    onNavigateToNewPost: () -> Unit
) {
    val faculties = listOf(
        "Ciencias de la Vida y Tecnologías" to true,
        "Ciencias de la Salud" to true,
        "Ingeniería, Industria y Construcción" to false,
        "Facultad de Ciencias Administrativas, Contables y Comercio" to false,
        "Facultad de Educación, Turismo, Artes y Humanidades" to false,
        "Facultad de Ciencias Sociales, Derecho y Bienestar" to false
    )

    val others = listOf(
        "Bienestar Universitario" to true,
        "Instituto de Idiomas" to false
    )

    val facultiesState = remember {
        mutableStateMapOf<String, Boolean>().apply {
            faculties.forEach { put(it.first, it.second) }
        }
    }

    val othersState = remember {
        mutableStateMapOf<String, Boolean>().apply {
            others.forEach { put(it.first, it.second) }
        }
    }

    Scaffold(
        topBar = {
            UniversityHeader(
                title = "Preferencias"
            )
        },
        bottomBar = {
            BottomNavigationBar(
                isPublisher = isPublisher,
                currentTab = "preferences",
                showProfileTab = false,
                onTabSelected = { tab ->
                    when (tab) {
                        "preferences" -> { /* Ya estamos aquí */ }
                        "home" -> onNavigateToHome()
                        "new_post" -> onNavigateToNewPost()
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
                // Sección FACULTADES
                Text(
                    text = "FACULTADES",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = SlateGray,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    ),
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderGray, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite)
                ) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        faculties.forEachIndexed { index, pair ->
                            val name = pair.first
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = DarkBlue,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp
                                    ),
                                    modifier = Modifier.weight(1f)
                                )

                                Switch(
                                    checked = facultiesState[name] ?: false,
                                    onCheckedChange = { facultiesState[name] = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = PureWhite,
                                        checkedTrackColor = PrimaryBlue,
                                        uncheckedThumbColor = SlateGray,
                                        uncheckedTrackColor = BorderGray
                                    )
                                )
                            }

                            if (index < faculties.size - 1) {
                                HorizontalDivider(
                                    color = BorderGray,
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Sección OTROS
                Text(
                    text = "OTROS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = SlateGray,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    ),
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderGray, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite)
                ) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        others.forEachIndexed { index, pair ->
                            val name = pair.first
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = DarkBlue,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp
                                    ),
                                    modifier = Modifier.weight(1f)
                                )

                                Switch(
                                    checked = othersState[name] ?: false,
                                    onCheckedChange = { othersState[name] = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = PureWhite,
                                        checkedTrackColor = PrimaryBlue,
                                        uncheckedThumbColor = SlateGray,
                                        uncheckedTrackColor = BorderGray
                                    )
                                )
                            }

                            if (index < others.size - 1) {
                                HorizontalDivider(
                                    color = BorderGray,
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botones Guardar y Cancelar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onNavigateToHome,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text(
                            text = "Guardar",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = PureWhite
                            )
                        )
                    }

                    OutlinedButton(
                        onClick = onNavigateToHome,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkBlue),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
                    ) {
                        Text(
                            text = "Cancelar",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = DarkBlue
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
