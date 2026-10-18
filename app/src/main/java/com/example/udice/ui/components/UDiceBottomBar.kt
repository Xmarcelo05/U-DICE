package com.example.udice.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
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
import com.example.udice.ui.navigation.NavRoutes
import com.example.udice.ui.navigation.UserRole
import com.example.udice.ui.theme.AzulPrincipal
import com.example.udice.ui.theme.Blanco
import com.example.udice.ui.theme.GrisBordes
import com.example.udice.ui.theme.GrisPizarra

@Composable
fun UDiceBottomBar(
    currentRoute: String,
    currentRole: UserRole,
    onNavigate: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Blanco,
        shadowElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, GrisBordes)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tab 1: Preferencias
            BottomNavItem(
                label = "Preferencias",
                icon = Icons.Default.Settings,
                isSelected = currentRoute == NavRoutes.PREFERENCIAS,
                onClick = { onNavigate(NavRoutes.PREFERENCIAS) }
            )

            // Tab 2: Inicio
            BottomNavItem(
                label = "Inicio",
                icon = Icons.Default.Home,
                isSelected = currentRoute == NavRoutes.INICIO,
                onClick = { onNavigate(NavRoutes.INICIO) }
            )

            // Tab 3: Nueva Publicación (Solo si el rol es PUBLICADOR)
            if (currentRole == UserRole.PUBLICADOR) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(AzulPrincipal)
                        .clickable { onNavigate(NavRoutes.NUEVA_PUBLICACION) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Nueva Publicación",
                        tint = Blanco,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Tab 4: Perfil
            BottomNavItem(
                label = "Perfil",
                icon = Icons.Default.Person,
                isSelected = currentRoute == NavRoutes.PERFIL,
                onClick = { onNavigate(NavRoutes.PERFIL) }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) AzulPrincipal else GrisPizarra,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) AzulPrincipal else GrisPizarra,
            modifier = Modifier.padding(top = 2.dp)
        )
        if (isSelected) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(AzulPrincipal)
            )
        }
    }
}
