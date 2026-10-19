package com.example.udice.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.udice.ui.theme.ColorAvisoBg
import com.example.udice.ui.theme.ColorAvisoTexto
import com.example.udice.ui.theme.ColorConferenciaBg
import com.example.udice.ui.theme.ColorConferenciaTexto
import com.example.udice.ui.theme.ColorConvocatoria
import com.example.udice.ui.theme.ColorConvocatoriaTexto
import com.example.udice.ui.theme.ColorEventoBg
import com.example.udice.ui.theme.ColorEventoTexto
import com.example.udice.ui.theme.ColorOtroBg
import com.example.udice.ui.theme.ColorOtroTexto
import com.example.udice.ui.theme.ColorSeminario
import com.example.udice.ui.theme.ColorSeminarioTexto

@Composable
fun CategoryBadge(
    category: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (category.lowercase().trim()) {
        "evento", "eventos" -> Pair(ColorEventoBg, ColorEventoTexto)
        "convocatoria", "convocatorias" -> Pair(ColorConvocatoria, ColorConvocatoriaTexto)
        "conferencia", "conferencias" -> Pair(ColorConferenciaBg, ColorConferenciaTexto)
        "seminario", "seminarios" -> Pair(ColorSeminario, ColorSeminarioTexto)
        "aviso", "avisos" -> Pair(ColorAvisoBg, ColorAvisoTexto)
        else -> Pair(ColorOtroBg, ColorOtroTexto)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = category,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
