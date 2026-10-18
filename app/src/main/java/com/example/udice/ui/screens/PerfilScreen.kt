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
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.udice.ui.navigation.UserRole
import com.example.udice.ui.theme.AzulOscuro
import com.example.udice.ui.theme.AzulPrincipal
import com.example.udice.ui.theme.Blanco
import com.example.udice.ui.theme.ColorEvento
import com.example.udice.ui.theme.GrisAzuladoClaro
import com.example.udice.ui.theme.GrisBordes
import com.example.udice.ui.theme.GrisPizarra

@Composable
fun PerfilScreen(
    currentRole: UserRole = UserRole.PUBLICADOR,
    onLogout: () -> Unit = {}
) {
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TARJETA PRINCIPAL DE PERFIL
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
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(AzulPrincipal),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ES",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Blanco
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Facultad",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrisPizarra
                    )
                    Text(
                        text = "Ciencias de la Vida y Tecnologías",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = AzulOscuro
                    )

                    // Rol actual en la universidad
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AzulPrincipal.copy(alpha = 0.1f),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(
                            text = if (currentRole == UserRole.PUBLICADOR) "Rol: Publicador Universitario" else "Rol: Estudiante Lector",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AzulPrincipal,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Estadísticas adaptadas según el rol:
                    // Para Publicador: Publicaciones | Guardados | Me gusta
                    // Para Lector: Guardados | Me gusta (el lector no publica)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (currentRole == UserRole.PUBLICADOR) {
                            StatItem(number = "2", label = "Publicaciones")
                            StatDivider()
                        }
                        StatItem(number = "1", label = "Guardados")
                        StatDivider()
                        StatItem(number = "94", label = "Me gusta")
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // TARJETA DE OPCIONES DE PERFIL
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    // "Mis publicaciones" -> SOLO para el usuario PUBLICADOR
                    if (currentRole == UserRole.PUBLICADOR) {
                        PerfilOptionRow(
                            icon = Icons.Default.Description,
                            iconTint = AzulPrincipal,
                            iconBg = Color(0xFFEFF6FF),
                            title = "Mis publicaciones",
                            subtitle = "Ver todas tus publicaciones creadas",
                            badgeCount = "2",
                            onClick = { /* Abrir lista de publicaciones */ }
                        )

                        HorizontalDivider(
                            color = GrisBordes,
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    // "Publicaciones guardadas" / "Mis guardados" -> Visible para ambos (única para lector)
                    PerfilOptionRow(
                        icon = Icons.Default.Bookmark,
                        iconTint = AzulPrincipal,
                        iconBg = Color(0xFFEFF6FF),
                        title = if (currentRole == UserRole.LECTOR) "Mis guardados" else "Publicaciones guardadas",
                        subtitle = "Ver publicaciones que guardaste",
                        badgeCount = "1",
                        onClick = { /* Abrir guardados */ }
                    )

                    HorizontalDivider(
                        color = GrisBordes,
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // "Cambiar contraseña"
                    PerfilOptionRow(
                        icon = Icons.Default.Lock,
                        iconTint = AzulPrincipal,
                        iconBg = Color(0xFFEFF6FF),
                        title = "Cambiar contraseña",
                        subtitle = "Actualizar clave y seguridad",
                        onClick = { /* Cambiar contraseña */ }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // TARJETA CERRAR SESIÓN
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onLogout),
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
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFEF2F2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Cerrar sesión",
                                tint = ColorEvento,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "Cerrar sesión",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorEvento
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
                        tint = ColorEvento,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
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
            color = AzulOscuro
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = GrisPizarra
        )
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(28.dp)
            .background(GrisBordes)
    )
}

@Composable
private fun PerfilOptionRow(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    subtitle: String,
    badgeCount: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
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

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AzulOscuro
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
                    color = AzulPrincipal.copy(alpha = 0.12f),
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
