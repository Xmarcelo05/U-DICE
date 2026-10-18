package com.example.udice.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
fun PerfilScreen() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GrisAzuladoClaro)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // TARJETA DE PERFIL
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Blanco),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar Iniciales ES
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(AzulPrincipal),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ES",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Blanco
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Facultad",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = GrisPizarra
                )
                Text(
                    text = "Ciencias de la Vida y Tecnologías",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0F172A)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Estadísticas (Publicaciones | Guardados | Me gusta)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(number = "2", label = "Publicaciones")
                    StatItem(number = "1", label = "Guardados")
                    StatItem(number = "94", label = "Me gusta")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // OPCIONES DE PERFIL
        PerfilOptionCard(
            icon = Icons.Default.Description,
            iconTint = AzulPrincipal,
            iconBg = Color(0xFFEFF6FF),
            title = "Mis publicaciones",
            subtitle = "Ver todas tus publicaciones",
            badgeCount = "2"
        )

        Spacer(modifier = Modifier.height(12.dp))

        PerfilOptionCard(
            icon = Icons.Default.Bookmark,
            iconTint = AzulPrincipal,
            iconBg = Color(0xFFEFF6FF),
            title = "Publicaciones guardadas",
            subtitle = "Ver publicaciones que guardaste",
            badgeCount = "1"
        )

        Spacer(modifier = Modifier.height(12.dp))

        PerfilOptionCard(
            icon = Icons.Default.Lock,
            iconTint = AzulPrincipal,
            iconBg = Color(0xFFEFF6FF),
            title = "Cambiar contraseña",
            subtitle = "Actualizar clave y seguridad"
        )

        Spacer(modifier = Modifier.height(12.dp))

        // CERRAR SESIÓN
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Blanco),
            border = BorderStroke(1.dp, Color(0xFFFEE2E2)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFEF2F2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar sesión",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Cerrar sesión",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
                        )
                        Text(
                            text = "Salir de tu cuenta universitaria",
                            fontSize = 12.sp,
                            color = GrisPizarra
                        )
                    }
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun StatItem(number: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = number,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = GrisPizarra
        )
    }
}

@Composable
private fun PerfilOptionCard(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    subtitle: String,
    badgeCount: String? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Blanco),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = GrisPizarra
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (badgeCount != null) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFEFF6FF),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = badgeCount,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AzulPrincipal,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = GrisPizarra,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PerfilScreenPreview() {
    UDICETheme {
        PerfilScreen()
    }
}
