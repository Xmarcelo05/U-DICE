package com.example.udice.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.udice.ui.components.PostCard
import com.example.udice.ui.components.PostItem
import com.example.udice.ui.theme.AzulOscuro
import com.example.udice.ui.theme.AzulPrincipal
import com.example.udice.ui.theme.Blanco
import com.example.udice.ui.theme.GrisAzuladoClaro
import com.example.udice.ui.theme.GrisBordes

@Composable
fun InicioFeedScreen() {
    val categories = listOf("Todos", "Eventos", "Conferencias", "Convocatorias", "Seminarios", "Avisos")
    var selectedCategory by remember { mutableStateOf("Todos") }

    // Publicaciones de ejemplo basadas en el prototipo
    val allPosts = remember {
        listOf(
            PostItem(
                id = "1",
                authorInitial = "BU",
                authorName = "Bienestar Universitario",
                date = "21 Sep · 9:00 a.m.",
                category = "Convocatoria",
                title = "Convocatoria: Becas de Excelencia Académica",
                description = "Se abren las inscripciones para las Becas de Excelencia 2026–2027. Aplica antes del 15 de octubre. Requisitos y formulario disponibles en el apartado de Becas y Ayudas en la app DBANU-Uleam.",
                initialLikes = 213,
                isInitiallySaved = true,
                gradientColors = listOf(Color(0xFFF8FAFC), Color(0xFFEFF6FF))
            ),
            PostItem(
                id = "2",
                authorInitial = "Cd",
                authorName = "C. de la Vida y Tecnologías",
                date = "19 Sep · 11:30 a.m.",
                category = "Seminario",
                title = "Seminario Internacional de Biotecnología y Sostenibilidad",
                description = "Participa en las jornadas académicas con expositores internacionales sobre avances en biotecnología aplicada a los recursos marinos y terrestres.",
                initialLikes = 142,
                isInitiallySaved = false,
                gradientColors = listOf(Color(0xFFECFEFF), Color(0xFFEFF6FF))
            ),
            PostItem(
                id = "3",
                authorInitial = "FI",
                authorName = "Ingeniería y Tecnologías",
                date = "17 Sep · 4:00 p.m.",
                category = "Conferencia",
                title = "Conferencia: Inteligencia Artificial en la Educación",
                description = "Mesa redonda para debatir los desafíos éticos y las oportunidades de la IA generativa en la investigación y formación profesional universitaria.",
                initialLikes = 98,
                isInitiallySaved = false,
                gradientColors = listOf(Color(0xFFF5F3FF), Color(0xFFEFF6FF))
            ),
            PostItem(
                id = "4",
                authorInitial = "CS",
                authorName = "Ciencias Sociales y Derecho",
                date = "15 Sep · 10:00 a.m.",
                category = "Evento",
                title = "Feria de Emprendimiento Universitario 2026",
                description = "Estudiantes y egresados presentarán sus proyectos de innovación empresarial en la plaza central de la facultad. ¡Entrada libre para todos!",
                initialLikes = 320,
                isInitiallySaved = false,
                gradientColors = listOf(Color(0xFFFEF2F2), Color(0xFFEFF6FF))
            )
        )
    }

    val filteredPosts = remember(selectedCategory) {
        if (selectedCategory == "Todos") {
            allPosts
        } else {
            allPosts.filter { it.category.equals(selectedCategory.removeSuffix("s"), ignoreCase = true) }
        }
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
            // Barra de chips / filtros de categorías horizontales
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                categories.forEach { category ->
                    val isSelected = category == selectedCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = {
                            Text(
                                text = category,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AzulPrincipal,
                            selectedLabelColor = Blanco,
                            containerColor = Blanco,
                            labelColor = AzulOscuro
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) AzulPrincipal else GrisBordes
                        )
                    )
                }
            }

            // Lista de publicaciones en el feed
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredPosts, key = { it.id }) { post ->
                    PostCard(post = post)
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
