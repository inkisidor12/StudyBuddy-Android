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
    primary = Leaf,
    secondary = Moss,
    tertiary = ForestGreenLight,

    background = ForestGreenDark,
    surface = SurfaceDark,

    onPrimary = ForestGreenDark,
    onSecondary = ForestGreenDark,
    onTertiary = ForestGreenDark,

    onBackground = Sand,
    onSurface = Sand,

    error = ErrorRed
)

private val LightColorScheme = lightColorScheme(
    primary = ForestGreen,
    secondary = Moss,
    tertiary = Leaf,

    background = SurfaceLight,
    surface = Color(0xFFFFFFFF),

    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onTertiary = ForestGreenDark,

    onBackground = ForestGreenDark,
    onSurface = ForestGreenDark,

    error = ErrorRed
)

@Composable
fun Hito4Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Si quieres forzar a nuestro tema Forest siempre, pon esto en false:
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