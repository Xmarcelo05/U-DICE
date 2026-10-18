package com.example.udice.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.udice.ui.components.UdiceLogo
import com.example.udice.ui.theme.BorderGray
import com.example.udice.ui.theme.DarkBlue
import com.example.udice.ui.theme.LightBlueGray
import com.example.udice.ui.theme.PrimaryBlue
import com.example.udice.ui.theme.PureWhite
import com.example.udice.ui.theme.SlateGray

@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    onSubmitSuccess: () -> Unit
) {
    var identifier by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBlueGray)
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.widthIn(max = 440.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Fila superior con botón volver
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Volver",
                                    tint = DarkBlue
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            UdiceLogo(size = 48.dp, showText = false)
                            Spacer(modifier = Modifier.weight(1f))
                            Spacer(modifier = Modifier.weight(0.2f))
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Recuperar Contraseña",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = DarkBlue,
                                textAlign = TextAlign.Center
                            )
                        )

                        Text(
                            text = "Ingresa tu información para restablecer el acceso a tu cuenta",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SlateGray,
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier.padding(top = 6.dp, bottom = 24.dp)
                        )

                        if (!submitted) {
                            // Etiqueta del wireframe: "Nombre de usuario o correo electrónico"
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Nombre de usuario o correo electrónico",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = DarkBlue
                                    ),
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )

                                OutlinedTextField(
                                    value = identifier,
                                    onValueChange = { identifier = it },
                                    placeholder = { Text("usuario o correo institucional") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Email,
                                            contentDescription = null,
                                            tint = PrimaryBlue
                                        )
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PrimaryBlue,
                                        unfocusedBorderColor = BorderGray,
                                        focusedLabelColor = PrimaryBlue,
                                        cursorColor = PrimaryBlue
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Botón SIGUIENTE según el wireframe
                            Button(
                                onClick = {
                                    if (identifier.isNotBlank()) {
                                        submitted = true
                                    } else {
                                        onSubmitSuccess()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                            ) {
                                Text(
                                    text = "SIGUIENTE",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        fontSize = 15.sp,
                                        color = PureWhite
                                    )
                                )
                            }
                        } else {
                            // Mensaje de éxito tras presionar SIGUIENTE
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(PrimaryBlue.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Se ha enviado un correo con instrucciones para restablecer tu contraseña.",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = DarkBlue,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Center
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = onSubmitSuccess,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                            ) {
                                Text(
                                    text = "Volver al inicio de sesión",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = PureWhite
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        HorizontalDivider(color = BorderGray, thickness = 1.dp)

                        Spacer(modifier = Modifier.height(16.dp))

                        // Enlace para volver
                        Text(
                            text = "Cancelar y volver",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SlateGray,
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.clickable { onNavigateBack() }
                        )
                    }
                }
            }
        }
    }
}
