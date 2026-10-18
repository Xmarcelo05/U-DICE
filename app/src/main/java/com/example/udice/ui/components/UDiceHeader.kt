package com.example.udice.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.udice.R
import com.example.udice.ui.navigation.UserRole
import com.example.udice.ui.theme.AzulInstitucional
import com.example.udice.ui.theme.AzulOscuro
import com.example.udice.ui.theme.AzulPrincipal
import com.example.udice.ui.theme.Blanco

@Composable
fun UDiceHeader(
    title: String,
    currentRole: UserRole,
    onRoleToggle: () -> Unit
) {
    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(AzulOscuro, AzulInstitucional, AzulPrincipal)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(gradientBrush)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Logo de la App
                Image(
                    painter = painterResource(id = R.drawable.logoudicenegativo),
                    contentDescription = "Logo U-DICE",
                    modifier = Modifier.size(36.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "UNIVERSIDAD",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.8f),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Blanco
                    )
                }
            }

            // Selector de Rol (Lector / Publicador) y Avatar
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onRoleToggle,
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = if (currentRole == UserRole.PUBLICADOR) "Publicador" else "Lector",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Blanco,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                // Avatar con punto rojo de notificación
                Box {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Perfil",
                            tint = Blanco,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    // Punto rojo
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444))
                            .align(Alignment.TopEnd)
                    )
                }
            }
        }
    }
}
