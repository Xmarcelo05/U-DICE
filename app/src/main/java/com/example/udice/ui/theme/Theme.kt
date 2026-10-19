package com.example.udice.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = AzulPrincipal,
    onPrimary = Blanco,
    primaryContainer = AzulInstitucional,
    onPrimaryContainer = Blanco,
    secondary = AzulInstitucional,
    onSecondary = Blanco,
    tertiary = AzulOscuro,
    onTertiary = Blanco,
    background = GrisAzuladoClaro,
    onBackground = AzulOscuro,
    surface = Blanco,
    onSurface = AzulOscuro,
    surfaceVariant = Blanco,
    onSurfaceVariant = GrisPizarra,
    outline = GrisBordes,
    outlineVariant = GrisBordes
)

private val DarkColorScheme = darkColorScheme(
    primary = AzulPrincipal,
    onPrimary = Blanco,
    primaryContainer = AzulInstitucional,
    onPrimaryContainer = Blanco,
    secondary = AzulInstitucional,
    onSecondary = Blanco,
    tertiary = AzulOscuro,
    onTertiary = Blanco,
    background = GrisAzuladoClaro,
    onBackground = AzulOscuro,
    surface = Blanco,
    onSurface = AzulOscuro,
    surfaceVariant = Blanco,
    onSurfaceVariant = GrisPizarra,
    outline = GrisBordes,
    outlineVariant = GrisBordes
)

@Composable
fun UDICETheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}