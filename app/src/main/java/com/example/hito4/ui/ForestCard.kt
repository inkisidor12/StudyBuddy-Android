package com.example.hito4.ui


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ForestCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val cs = MaterialTheme.colorScheme

    Card(
        modifier = modifier,
        border = BorderStroke(1.dp, cs.primary.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = cs.surface),
        content = {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.padding(16.dp),
                content = content
            )
        }
    )
}