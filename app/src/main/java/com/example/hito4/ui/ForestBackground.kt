package com.example.hito4.ui


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme

@Composable
fun ForestBackground(content: @Composable () -> Unit) {
    val cs = MaterialTheme.colorScheme

    // Gradiente suave tipo "cielo -> bosque"
    val brush = Brush.verticalGradient(
        colors = listOf(
            cs.background,
            cs.surface.copy(alpha = 0.95f),
            cs.primary.copy(alpha = 0.10f)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush)
    ) {
        content()
    }
}