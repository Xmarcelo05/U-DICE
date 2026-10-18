package com.example.udice.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.udice.R
import com.example.udice.ui.theme.AzulOscuro
import com.example.udice.ui.theme.GrisPizarra

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
        Image(
            painter = painterResource(id = R.drawable.logoudice),
            contentDescription = "Logo U-DICE",
            modifier = Modifier.size(size)
        )

        if (showText) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "U-DICE",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp,
                    color = AzulOscuro
                )
            )
            Text(
                text = "Comunidad Universitaria",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = GrisPizarra,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}
