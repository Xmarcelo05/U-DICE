package com.example.udice.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
        border = BorderStroke(1.dp, GrisBordes)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem("Inicio", Icons.Default.Home, currentRoute == NavRoutes.INICIO_FEED, Modifier.weight(1f)) {
                onNavigate(NavRoutes.INICIO_FEED)
            }
            BottomNavItem("Buscar", Icons.Default.Search, currentRoute == NavRoutes.BUSQUEDA, Modifier.weight(1f)) {
                onNavigate(NavRoutes.BUSQUEDA)
            }
            if (currentRole == UserRole.PUBLICADOR) {
                Box(Modifier.weight(1f).height(60.dp), contentAlignment = Alignment.TopCenter) {
                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(AzulPrincipal)
                            .clickable { onNavigate(NavRoutes.NUEVA_PUBLICACION) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Nueva publicación",
                            tint = Blanco,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
            BottomNavItem("Preferencias", Icons.Default.Tune, currentRoute == NavRoutes.PREFERENCIAS, Modifier.weight(1f)) {
                onNavigate(NavRoutes.PREFERENCIAS)
            }
            BottomNavItem("Perfil", Icons.Default.Person, currentRoute == NavRoutes.PERFIL, Modifier.weight(1f)) {
                onNavigate(NavRoutes.PERFIL)
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .height(60.dp)
            .clickable(onClick = onClick)
            .padding(top = 14.dp),
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
            maxLines = 1,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) AzulPrincipal else GrisPizarra,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
