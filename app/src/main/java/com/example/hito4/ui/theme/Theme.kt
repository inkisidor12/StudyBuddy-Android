package com.example.hito4.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = MintAccent,
    secondary = ForestLight,
    tertiary = MintSoft,

    background = SurfaceDark,
    surface = ForestDark,

    onPrimary = ForestDark,
    onSecondary = ForestDark,
    onTertiary = ForestDark,

    onBackground = MintPale,
    onSurface = MintPale,

    primaryContainer = ForestMedium,
    onPrimaryContainer = MintPale,

    error = ErrorRed
)

private val LightColorScheme = lightColorScheme(
    primary = ForestMedium,
    secondary = ForestLight,
    tertiary = MintAccent,

    background = SurfaceLight,
    surface = Color(0xFFFFFFFF),

    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onTertiary = ForestDark,

    onBackground = ForestDark,
    onSurface = ForestDark,

    primaryContainer = MintPale,
    onPrimaryContainer = ForestDark,

    error = ErrorRed
)

@Composable
fun Hito4Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme =
        if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        } else {
            if (darkTheme) DarkColorScheme else LightColorScheme
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}