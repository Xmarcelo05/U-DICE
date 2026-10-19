package com.example.udice.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewCompact
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.udice.ui.components.GridPostItem
import com.example.udice.ui.components.PostGridCard
import com.example.udice.ui.theme.AzulInstitucional
import com.example.udice.ui.theme.AzulOscuro
import com.example.udice.ui.theme.AzulPrincipal
import com.example.udice.ui.theme.Blanco
import com.example.udice.ui.theme.GrisAzuladoClaro
import com.example.udice.ui.theme.GrisBordes
import com.example.udice.ui.theme.GrisPizarra

@Composable
fun MisPublicacionesScreen(
    onNavigateToNuevaPublicacion: () -> Unit = {},
    onPostClick: (String) -> Unit = {}
) {
    // 4 publicaciones creadas por el usuario según el boceto
    val myPosts = remember {
        listOf(
            // Fila 1 - Boceto: Dos rectángulos/tarjetas, Tres líneas de texto, Multitud/Grupo
            GridPostItem(
                id = "p1",
                title = "Seminario Internacional de IA y Futuro",
                category = "Seminario",
                date = "19 Sep",
                icon = Icons.Default.ViewCompact,
                gradientColors = listOf(Color(0xFFF8FAFC), Color(0xFFEFF6FF)),
                views = "142",
                status = "Activo"
            ),
            GridPostItem(
                id = "p2",
                title = "Convocatoria Auxiliares de Cátedra",
                category = "Convocatoria",
                date = "17 Sep",
                icon = Icons.Default.Description,
                gradientColors = listOf(Color(0xFFECFEFF), Color(0xFFEFF6FF)),
                views = "98",
                status = "Activo"
            ),
            GridPostItem(
                id = "p3",
                title = "Congreso de Ciencias de la Vida",
                category = "Evento",
                date = "15 Sep",
                icon = Icons.Default.Groups,
                gradientColors = listOf(Color(0xFFFEF2F2), Color(0xFFEFF6FF)),
                views = "320",
                status = "Activo"
            ),

            // Fila 2 - Boceto: 1 publicación con líneas y 2 espacios libres
            GridPostItem(
                id = "p4",
                title = "Aviso de Fechas de Exámenes Parciales",
                category = "Aviso",
                date = "10 Sep",
                icon = Icons.Default.Campaign,
                gradientColors = listOf(Color(0xFFFEF3C7), Color(0xFFFFFBEB)),
                views = "451",
                status = "Activo"
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GrisAzuladoClaro),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp)
        ) {
            // Panel de estado de publicador y botón de acción rápida
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "4 Publicaciones Activas",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = AzulOscuro
                            )
                        }

                        Text(
                            text = "Gestiona tus anuncios universitarios",
                            fontSize = 11.sp,
                            color = GrisPizarra,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    // Botón para crear nueva publicación
                    Button(
                        onClick = onNavigateToNuevaPublicacion,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AzulPrincipal),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Blanco,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Nueva",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Blanco
                        )
                    }
                }
            }

            // Encabezado de la cuadrícula
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tus publicaciones publicadas",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AzulOscuro
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "Rol: Publicador",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF047857),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Cuadrícula 3 Columnas según el boceto (Fila 1: 3 items, Fila 2: 1 item)
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 20.dp, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(myPosts, key = { it.id }) { post ->
                    PostGridCard(
                        item = post,
                        isPublisherView = true,
                        onClick = { onPostClick(post.id) }
                    )
                }

                item(span = { GridItemSpan(3) }) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
