package com.example.udice.ui.perfil

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.layout.ContentScale
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.udice.BuildConfig
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.udice.data.model.Rol
import com.example.udice.data.model.User
import com.example.udice.ui.theme.AzulPrincipal
import com.example.udice.ui.theme.Blanco
import com.example.udice.ui.theme.GrisAzuladoClaro
import com.example.udice.ui.theme.GrisPizarra

@Composable
fun PerfilScreen(
    user: User,
    logoutEnabled: Boolean,
    onLogout: () -> Unit,
    onSavedPosts: () -> Unit,
    onMyPosts: () -> Unit,
    stateHolder: PerfilStateHolder = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val photoState by stateHolder.photoState.collectAsStateWithLifecycle()
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { photo: Uri? ->
        if (photo != null) stateHolder.updatePhoto(photo)
    }

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
                // Iniciales de la cuenta activa
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(AzulPrincipal),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.nombre.split(" ").filter(String::isNotBlank).take(2).joinToString("") { it.take(1).uppercase() },
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Blanco
                    )
                    user.avatarUrl?.let { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = "Foto de perfil",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(72.dp)
                        )
                    }
                }
                if (BuildConfig.UDICE_SUPABASE) {
                    TextButton(
                        enabled = !photoState.saving,
                        onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                    ) { Text(if (user.avatarUrl == null) "Agregar foto de perfil" else "Cambiar foto de perfil") }
                    if (user.avatarUrl != null) {
                        TextButton(enabled = !photoState.saving, onClick = { stateHolder.updatePhoto(null) }) {
                            Text("Quitar foto")
                        }
                    }
                    if (photoState.saving) LinearProgressIndicator(Modifier.fillMaxWidth())
                    photoState.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = user.nombre,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = GrisPizarra
                )
                Text(
                    text = user.facultad?.displayName ?: user.email,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0F172A)
                )

                user.carrera?.let { Text(it, color = GrisPizarra) }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // OPCIONES DE PERFIL
        PerfilOptionCard(
            icon = Icons.Default.Bookmark,
            iconTint = AzulPrincipal,
            iconBg = Color(0xFFEFF6FF),
            title = "Publicaciones guardadas",
            subtitle = "Consulta tu bóveda personal de avisos",
            onClick = onSavedPosts
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (user.rol == Rol.ASO) {
            PerfilOptionCard(
                icon = Icons.Default.Description,
                iconTint = AzulPrincipal,
                iconBg = Color(0xFFEFF6FF),
                title = "Mis publicaciones",
                subtitle = "Edita o elimina tus avisos",
                onClick = onMyPosts
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // CERRAR SESIÓN
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = logoutEnabled, onClick = onLogout),
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
private fun PerfilOptionCard(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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