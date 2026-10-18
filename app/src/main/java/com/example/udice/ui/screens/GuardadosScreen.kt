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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewCompact
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
fun GuardadosScreen(
    onPostClick: (String) -> Unit = {}
) {
    val categories = listOf("Todos", "Convocatorias", "Eventos", "Conferencias", "Seminarios", "Avisos")
    var selectedCategory by remember { mutableStateOf("Todos") }

    // 8 publicaciones guardadas organizadas en la cuadrícula de 3 columnas (según boceto)
    val savedPosts = remember {
        listOf(
            // Fila 1 - Boceto: Paisaje/Árbol, Círculo/Medalla, Persona/Retrato
            GridPostItem(
                id = "g1",
                title = "Feria de Ciencias y Mar 2026",
                category = "Evento",
                date = "21 Sep",
                icon = Icons.Default.Landscape,
                gradientColors = listOf(Color(0xFF0284C7), Color(0xFF0369A1)),
                isSaved = true
            ),
            GridPostItem(
                id = "g2",
                title = "Beca de Excelencia Académica",
                category = "Convocatoria",
                date = "20 Sep",
                icon = Icons.Default.Stars,
                gradientColors = listOf(AzulOscuro, AzulInstitucional),
                isSaved = true
            ),
            GridPostItem(
                id = "g3",
                title = "Conferencia Magistral IA Ética",
                category = "Conferencia",
                date = "19 Sep",
                icon = Icons.Default.Person,
                gradientColors = listOf(Color(0xFF4338CA), Color(0xFF3730A3)),
                isSaved = true
            ),

            // Fila 2 - Boceto: Ondas/Líneas, Marco/Cuadrícula, Líneas horizontales
            GridPostItem(
                id = "g4",
                title = "Pasantías Comunitarias 2026",
                category = "Convocatoria",
                date = "18 Sep",
                icon = Icons.Default.Description,
                gradientColors = listOf(Color(0xFF0D9488), Color(0xFF0F766E)),
                isSaved = true
            ),
            GridPostItem(
                id = "g5",
                title = "Simposio de Biotecnología Marina",
                category = "Seminario",
                date = "17 Sep",
                icon = Icons.Default.ViewCompact,
                gradientColors = listOf(AzulInstitucional, AzulPrincipal),
                isSaved = true
            ),
            GridPostItem(
                id = "g6",
                title = "Reglamento de Titulación ULEAM",
                category = "Aviso",
                date = "15 Sep",
                icon = Icons.Default.Description,
                gradientColors = listOf(Color(0xFF475569), Color(0xFF334155)),
                isSaved = true
            ),

            // Fila 3 - Boceto: Círculos/Burbujas, Tres barras/Bloques
            GridPostItem(
                id = "g7",
                title = "Jornadas de Investigación Científica",
                category = "Seminario",
                date = "14 Sep",
                icon = Icons.Default.Science,
                gradientColors = listOf(Color(0xFF2563EB), Color(0xFF1D4ED8)),
                isSaved = true
            ),
            GridPostItem(
                id = "g8",
                title = "Feria de Emprendimiento Universitario",
                category = "Evento",
                date = "12 Sep",
                icon = Icons.Default.ViewAgenda,
                gradientColors = listOf(Color(0xFF7C3AED), Color(0xFF6D28D9)),
                isSaved = true
            )
        )
    }

    val filteredPosts = remember(selectedCategory) {
        if (selectedCategory == "Todos") {
            savedPosts
        } else {
            savedPosts.filter { it.category.equals(selectedCategory.removeSuffix("s"), ignoreCase = true) }
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
            // Barra de filtros de categoría
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
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
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        shape = RoundedCornerShape(18.dp),
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

            // Encabezado informativo de cantidad de publicaciones guardadas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = null,
                        tint = AzulPrincipal,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = " ${filteredPosts.size} publicaciones guardadas",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AzulOscuro
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AzulPrincipal.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "Colección personal",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AzulPrincipal,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Cuadrícula 3 Columnas según el boceto
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 20.dp, top = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredPosts, key = { it.id }) { post ->
                    PostGridCard(
                        item = post,
                        isPublisherView = false,
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
