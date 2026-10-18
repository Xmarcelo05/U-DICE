package com.example.udice.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ==========================================
// PALETA DE COLORES - APLICACIÓN UNIVERSITARIA
// ==========================================

// Colores principales
val PrimaryBlue = Color(0xFF2563EB)       // Azul principal: Botones, switches activos e iconos seleccionados
val InstitutionalBlue = Color(0xFF1D3E87) // Azul institucional: Fondos de cabecera y degradados principales
val DarkBlue = Color(0xFF1B3163)          // Azul oscuro: Degradados, botones y elementos de énfasis
val LightBlueGray = Color(0xFFF1F5F9)     // Gris azulado claro: Fondo general de las pantallas
val PureWhite = Color(0xFFFFFFFF)         // Blanco: Tarjetas, formularios y superficies
val SlateGray = Color(0xFF64748B)         // Gris pizarra: Textos secundarios, descripciones e iconos inactivos
val BorderGray = Color(0xFFE2E8F0)        // Gris de bordes: Separadores, campos de entrada y contornos

// Colores secundarios y de estado
val EventRed = Color(0xFFEF4444)          // Evento: Actividades especiales
val EventRedBg = Color(0xFFFEE2E2)
val EventRedText = Color(0xFFDC2626)

val CallGreen = Color(0xFF10B981)         // Convocatoria: Llamados y convocatorias
val CallGreenBg = Color(0xFFD1FAE5)
val CallGreenText = Color(0xFF065F46)

val ConfPurple = Color(0xFFA78BFA)        // Conferencia: Charlas y conferencias
val ConfPurpleBg = Color(0xFFEDE9FE)
val ConfPurpleText = Color(0xFF6D28D9)

val SeminarCyan = Color(0xFF06B6D4)       // Seminario: Talleres académicos y seminarios
val SeminarCyanBg = Color(0xFFCFFAFE)
val SeminarCyanText = Color(0xFF0E7490)

val NoticeAmber = Color(0xFFF59E0B)       // Aviso: Avisos importantes y notificaciones
val NoticeAmberBg = Color(0xFFFEF3C7)
val NoticeAmberText = Color(0xFFB45309)

val OtherGray = Color(0xFF94A3B8)         // Otro: Categorías adicionales
val OtherGrayBg = Color(0xFFF1F5F9)
val OtherGrayText = Color(0xFF475569)

// Degradado institucional principal (#1B3163 -> #1D3E87 -> #2563EB)
val HeaderGradient = Brush.horizontalGradient(
    colors = listOf(DarkBlue, InstitutionalBlue, PrimaryBlue)
)

val CardGradient = Brush.verticalGradient(
    colors = listOf(DarkBlue.copy(alpha = 0.85f), InstitutionalBlue)
)