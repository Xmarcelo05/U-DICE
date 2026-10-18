package com.example.udice.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.udice.ui.theme.DarkBlue
import com.example.udice.ui.theme.EventRed
import com.example.udice.ui.theme.HeaderGradient
import com.example.udice.ui.theme.PureWhite

@Composable
fun UniversityHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String = "UNIVERSIDAD",
    showBack: Boolean = false,
    onBackClick: (() -> Unit)? = null,
    onProfileClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(brush = HeaderGradient)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (showBack && onBackClick != null) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = PureWhite
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Column {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = PureWhite.copy(alpha = 0.8f),
                            letterSpacing = 1.8.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = PureWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    )
                }
            }

            // Avatar superior derecho con indicador rojo como en el prototipo
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(PureWhite.copy(alpha = 0.2f))
                    .clickable(enabled = onProfileClick != null) { onProfileClick?.invoke() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Perfil",
                    tint = PureWhite,
                    modifier = Modifier.size(22.dp)
                )

                // Punto rojo de notificación
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = (-3).dp, y = (3).dp)
                        .clip(CircleShape)
                        .background(EventRed)
                )
            }
        }
    }
}
