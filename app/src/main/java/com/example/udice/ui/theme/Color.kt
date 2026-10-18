package com.example.udice.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ==========================================
// PALETA DE COLORES OFICIAL - U-DICE
// ==========================================

// Colores Principales (U-DICE Palette)
val AzulPrincipal = Color(0xFF2563EB)       // Botones, switches activos e iconos seleccionados
val AzulInstitucional = Color(0xFF1D3E87) // Fondos de cabecera y degradados principales
val AzulOscuro = Color(0xFF1B3163)          // Degradados, botones y elementos de énfasis
val GrisAzuladoClaro = Color(0xFFF1F5F9)     // Fondo general de las pantallas
val Blanco = Color(0xFFFFFFFF)             // Tarjetas, formularios y superficies
val GrisPizarra = Color(0xFF64748B)         // Textos secundarios, descripciones e iconos inactivos
val GrisBordes = Color(0xFFE2E8F0)        // Separadores, campos de entrada y contornos

// Colores Secundarios y de Estado (Categorías)
val ColorEvento = Color(0xFFEF4444)
val ColorEventoBg = Color(0xFFFEE2E2)
val ColorConvocatoria = Color(0xFFD1FAE5)
val ColorConvocatoriaTexto = Color(0xFF065F46)
val ColorConferencia = Color(0xFFA78BFA)
val ColorConferenciaBg = Color(0xFFEDE9FE)
val ColorConferenciaTexto = Color(0xFF5B21B6)
val ColorSeminario = Color(0xFFCFFAFE)
val ColorSeminarioTexto = Color(0xFF0E7490)
val ColorAviso = Color(0xFFF59E0B)
val ColorAvisoBg = Color(0xFFFEF3C7)
val ColorAvisoTexto = Color(0xFFB45309)
val ColorOtro = Color(0xFF94A3B8)
val ColorOtroBg = Color(0xFFF1F5F9)
val ColorOtroTexto = Color(0xFF475569)

// Alias para compatibilidad
val PrimaryBlue = AzulPrincipal
val InstitutionalBlue = AzulInstitucional
val DarkBlue = AzulOscuro
val LightBlueGray = GrisAzuladoClaro
val PureWhite = Blanco
val SlateGray = GrisPizarra
val BorderGray = GrisBordes

val EventRed = ColorEvento
val EventRedBg = ColorEventoBg
val EventRedText = Color(0xFFDC2626)
val CallGreen = Color(0xFF10B981)
val CallGreenBg = ColorConvocatoria
val CallGreenText = ColorConvocatoriaTexto
val ConfPurple = ColorConferencia
val ConfPurpleBg = ColorConferenciaBg
val ConfPurpleText = ColorConferenciaTexto
val SeminarCyan = Color(0xFF06B6D4)
val SeminarCyanBg = ColorSeminario
val SeminarCyanText = ColorSeminarioTexto
val NoticeAmber = ColorAviso
val NoticeAmberBg = ColorAvisoBg
val NoticeAmberText = ColorAvisoTexto
val OtherGray = ColorOtro
val OtherGrayBg = ColorOtroBg
val OtherGrayText = ColorOtroTexto

// Degradado institucional principal (#1B3163 -> #1D3E87 -> #2563EB)
val HeaderGradient = Brush.horizontalGradient(
    colors = listOf(AzulOscuro, AzulInstitucional, AzulPrincipal)
)

val CardGradient = Brush.verticalGradient(
    colors = listOf(AzulOscuro.copy(alpha = 0.85f), AzulInstitucional)
)