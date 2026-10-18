package com.example.udice.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.udice.ui.theme.BorderGray
import com.example.udice.ui.theme.DarkBlue
import com.example.udice.ui.theme.PrimaryBlue
import com.example.udice.ui.theme.PureWhite
import com.example.udice.ui.theme.SlateGray

@Composable
fun BottomNavigationBar(
    isPublisher: Boolean,
    currentTab: String, // "preferences", "home", "new_post", "profile"
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    showProfileTab: Boolean = false
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp),
        color = PureWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            HorizontalDivider(color = BorderGray, thickness = 1.dp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Tab Preferencias
                BottomNavItem(
                    label = "Preferencias",
                    selectedIcon = Icons.Filled.Settings,
                    unselectedIcon = Icons.Outlined.Settings,
                    isSelected = currentTab == "preferences",
                    onClick = { onTabSelected("preferences") }
                )

                // Tab Inicio (Feed)
                BottomNavItem(
                    label = "Inicio",
                    selectedIcon = Icons.Filled.Home,
                    unselectedIcon = Icons.Outlined.Home,
                    isSelected = currentTab == "home",
                    onClick = { onTabSelected("home") }
                )

                // Tab "+" (SOLO visible para el usuario Publicador)
                if (isPublisher) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(DarkBlue)
                            .clickable { onTabSelected("new_post") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Nueva Publicación",
                            tint = PureWhite,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                // Tab Perfil (opcional, oculto por defecto para facilitar el merge con la rama de perfil)
                if (showProfileTab) {
                    BottomNavItem(
                        label = "Perfil",
                        selectedIcon = Icons.Filled.Person,
                        unselectedIcon = Icons.Outlined.Person,
                        isSelected = currentTab == "profile",
                        onClick = { onTabSelected("profile") }
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = if (isSelected) selectedIcon else unselectedIcon,
            contentDescription = label,
            tint = if (isSelected) PrimaryBlue else SlateGray,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            color = if (isSelected) PrimaryBlue else SlateGray,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
        if (isSelected) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(PrimaryBlue)
            )
        }
    }
}
