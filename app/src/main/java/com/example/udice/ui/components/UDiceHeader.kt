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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.udice.R
import com.example.udice.ui.navigation.UserRole
import com.example.udice.ui.theme.AzulOscuro
import com.example.udice.ui.theme.AzulInstitucional
import com.example.udice.ui.theme.AzulPrincipal
import com.example.udice.ui.theme.Blanco
import com.example.udice.ui.theme.ColorEvento
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.IconButton
import com.example.udice.ui.theme.HeaderGradient

@Composable
fun UDiceHeader(
    title: String,
    currentRole: UserRole,
    onRoleToggle: (() -> Unit)? = null,
    onProfileClick: (() -> Unit)? = null,
    onBackClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(HeaderGradient)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (onBackClick != null) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(36.dp)
                            .padding(end = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Blanco
                        )
                    }
                }

                // Logo oficial U-DICE de la universidad
                Image(
                    painter = painterResource(id = R.drawable.logoblancosinfondo),
                    contentDescription = "Logo U-DICE",
                    modifier = Modifier.size(38.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "UNIVERSIDAD",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Blanco.copy(alpha = 0.85f),
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Blanco
                    )
                }
            }

            // Selector de Rol (Lector / Publicador) y Avatar con punto de notificación
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onRoleToggle != null) {
                    Surface(
                        onClick = onRoleToggle,
                        shape = RoundedCornerShape(20.dp),
                        color = Blanco.copy(alpha = 0.2f),
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
                }

                // Avatar de usuario con punto de notificación
                Box {
                    Surface(
                        onClick = { onProfileClick?.invoke() },
                        shape = CircleShape,
                        color = Blanco.copy(alpha = 0.25f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Perfil",
                                tint = Blanco,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // Punto rojo de notificación
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(ColorEvento)
                            .align(Alignment.TopEnd)
                    )
                }
            }
        }
    }
}
