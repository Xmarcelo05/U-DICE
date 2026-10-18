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
import com.example.udice.ui.theme.CallGreenBg
import com.example.udice.ui.theme.CallGreenText
import com.example.udice.ui.theme.ConfPurpleBg
import com.example.udice.ui.theme.ConfPurpleText
import com.example.udice.ui.theme.EventRedBg
import com.example.udice.ui.theme.EventRedText
import com.example.udice.ui.theme.NoticeAmberBg
import com.example.udice.ui.theme.NoticeAmberText
import com.example.udice.ui.theme.OtherGrayBg
import com.example.udice.ui.theme.OtherGrayText
import com.example.udice.ui.theme.SeminarCyanBg
import com.example.udice.ui.theme.SeminarCyanText

@Composable
fun CategoryBadge(
    category: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (category.lowercase().trim()) {
        "evento", "eventos" -> Pair(EventRedBg, EventRedText)
        "convocatoria", "convocatorias" -> Pair(CallGreenBg, CallGreenText)
        "conferencia", "conferencias" -> Pair(ConfPurpleBg, ConfPurpleText)
        "seminario", "seminarios" -> Pair(SeminarCyanBg, SeminarCyanText)
        "aviso", "avisos" -> Pair(NoticeAmberBg, NoticeAmberText)
        else -> Pair(OtherGrayBg, OtherGrayText)
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
