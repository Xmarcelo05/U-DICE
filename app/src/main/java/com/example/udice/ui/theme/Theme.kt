package com.example.udice.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val UDICEColorScheme = lightColorScheme(
    primary = AzulPrincipal,
    onPrimary = Blanco,
    primaryContainer = AzulInstitucional,
    onPrimaryContainer = Blanco,
    secondary = AzulOscuro,
    onSecondary = Blanco,
    background = GrisAzuladoClaro,
    onBackground = AzulOscuro,
    surface = Blanco,
    onSurface = AzulOscuro,
    surfaceVariant = GrisAzuladoClaro,
    onSurfaceVariant = GrisPizarra,
    outline = GrisBordes
)

@Composable
fun UDICETheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = UDICEColorScheme,
        typography = Typography,
        content = content
    )
}