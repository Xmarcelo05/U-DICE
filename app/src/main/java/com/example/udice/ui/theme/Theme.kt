package com.example.udice.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = PureWhite,
    primaryContainer = InstitutionalBlue,
    onPrimaryContainer = PureWhite,
    secondary = InstitutionalBlue,
    onSecondary = PureWhite,
    tertiary = DarkBlue,
    onTertiary = PureWhite,
    background = LightBlueGray,
    onBackground = DarkBlue,
    surface = PureWhite,
    onSurface = DarkBlue,
    surfaceVariant = PureWhite,
    onSurfaceVariant = SlateGray,
    outline = BorderGray,
    outlineVariant = BorderGray
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = PureWhite,
    primaryContainer = InstitutionalBlue,
    onPrimaryContainer = PureWhite,
    secondary = InstitutionalBlue,
    onSecondary = PureWhite,
    tertiary = DarkBlue,
    onTertiary = PureWhite,
    background = DarkBlue,
    onBackground = LightBlueGray,
    surface = InstitutionalBlue,
    onSurface = PureWhite,
    surfaceVariant = DarkBlue,
    onSurfaceVariant = SlateGray,
    outline = SlateGray,
    outlineVariant = SlateGray
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