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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.udice.ui.theme.AzulPrincipal
import com.example.udice.ui.theme.Blanco
import com.example.udice.ui.theme.ColorConvocatoria
import com.example.udice.ui.theme.ColorConvocatoriaTexto
import com.example.udice.ui.theme.ColorEvento
import com.example.udice.ui.theme.ColorSeminario
import com.example.udice.ui.theme.ColorSeminarioTexto
import com.example.udice.ui.theme.GrisAzuladoClaro
import com.example.udice.ui.theme.GrisBordes
import com.example.udice.ui.theme.GrisPizarra
import com.example.udice.ui.theme.UDICETheme

@Composable
fun InicioFeedScreen() {
    var selectedFiltro by remember { mutableStateOf("Todos") }
    val filtros = listOf("Todos", "Eventos", "Conferencias", "Convocatorias")

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GrisAzuladoClaro)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // FILTROS SUPERIORES
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(filtros) { filtro ->
                val isSelected = selectedFiltro == filtro
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) AzulPrincipal else Blanco,
                    border = if (isSelected) null else BorderStroke(1.dp, GrisBordes),
                    modifier = Modifier.clickable { selectedFiltro = filtro }
                ) {
                    Text(
                        text = filtro,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Blanco else GrisPizarra,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // TARJETA DE PUBLICACIÓN 1 (Muestra del prototipo)
        PublicacionCard(
            facultadAbreviado = "BU",
            facultadNombre = "Bienestar Universitario",
            fechaHora = "21 Sep · 9:00 a.m.",
            categoriaTag = "Convocatoria",
            categoriaColorBg = ColorConvocatoria,
            categoriaColorTexto = ColorConvocatoriaTexto,
            titulo = "Convocatoria: Becas de Excelencia Académica",
            descripcion = "Se abren las inscripciones para las Becas de Excelencia Académica 2026-2027. Aplica antes del 15 de octubre. Requisitos y formulario disponibles en el apartado de Becas y Ayudas en la app DBANU-Uleam.",
            likesCount = "213",
            isGuardado = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // TARJETA DE PUBLICACIÓN 2
        PublicacionCard(
            facultadAbreviado = "Cd",
            facultadNombre = "C. de la Vida y Tecno...",
            fechaHora = "19 Sep · 11:30 a.m.",
            categoriaTag = "Seminario",
            categoriaColorBg = ColorSeminario,
            categoriaColorTexto = ColorSeminarioTexto,
            titulo = "Seminario de Inteligencia Artificial en Educación",
            descripcion = "Te invitamos al seminario internacional sobre aplicaciones de IA en el desarrollo móvil nativo. Auditorio Principal Facultad de Ingeniería.",
            likesCount = "85",
            isGuardado = false
        )
    }
}

@Composable
private fun PublicacionCard(
    facultadAbreviado: String,
    facultadNombre: String,
    fechaHora: String,
    categoriaTag: String,
    categoriaColorBg: Color,
    categoriaColorTexto: Color,
    titulo: String,
    descripcion: String,
    likesCount: String,
    isGuardado: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Blanco),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header del Post: Avatar + Nombre + Categoria + Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = facultadAbreviado,
                            color = Blanco,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = facultadNombre,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = fechaHora,
                            fontSize = 11.sp,
                            color = GrisPizarra
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = categoriaColorBg
                    ) {
                        Text(
                            text = categoriaTag,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = categoriaColorTexto,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Opciones",
                        tint = GrisPizarra,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Banner o Imagen de ejemplo (Placeholder atractivo)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF334155)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ColorEvento,
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = "Evento Destacado",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Blanco,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = "U-DICE INFORMA",
                        color = Blanco,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        letterSpacing = 2.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Likes y Guardado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Me gusta",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = likesCount,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFEFF6FF),
                    border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = "Guardado",
                            tint = AzulPrincipal,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isGuardado) "Guardado" else "Guardar",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AzulPrincipal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Título y Descripción
            Text(
                text = titulo,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = descripcion,
                fontSize = 13.sp,
                color = GrisPizarra,
                lineHeight = 18.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InicioFeedScreenPreview() {
    UDICETheme {
        InicioFeedScreen()
    }
}
