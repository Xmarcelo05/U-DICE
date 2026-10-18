package com.example.udice.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.udice.ui.theme.DarkBlue
import com.example.udice.ui.theme.HeaderGradient
import com.example.udice.ui.theme.PureWhite
import com.example.udice.ui.theme.SlateGray

@Composable
fun UdiceLogo(
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    showText: Boolean = true
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(brush = HeaderGradient),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = "Logo UDICE",
                tint = PureWhite,
                modifier = Modifier.size(size * 0.58f)
            )
        }

        if (showText) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "UDICE",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp,
                    color = DarkBlue
                )
            )
            Text(
                text = "Comunidad Universitaria",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = SlateGray,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}
