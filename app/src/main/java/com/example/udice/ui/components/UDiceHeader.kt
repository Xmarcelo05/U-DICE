package com.example.udice.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.udice.R
import com.example.udice.ui.navigation.UserRole
import com.example.udice.ui.theme.Blanco
import com.example.udice.ui.theme.ColorEvento
import com.example.udice.ui.theme.HeaderGradient

@OptIn(ExperimentalMaterial3Api::class)
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
    ) {
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logoblancosinfondo),
                        contentDescription = "Logo U-DICE",
                        modifier = Modifier.size(36.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = stringResource(R.string.universidad_header),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Blanco.copy(alpha = 0.85f),
                                letterSpacing = 1.2.sp
                            )
                        )
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = Blanco
                            ),
                            maxLines = 1
                        )
                    }
                }
            },
            navigationIcon = {
                if (onBackClick != null) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Blanco
                        )
                    }
                }
            },
            actions = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 8.dp)
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
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Blanco
                                ),
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
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = Blanco,
                navigationIconContentColor = Blanco,
                actionIconContentColor = Blanco
            )
        )
    }
}
