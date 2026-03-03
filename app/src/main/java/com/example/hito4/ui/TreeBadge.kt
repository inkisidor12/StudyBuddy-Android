package com.example.hito4.ui


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TreeBadge(
    emoji: String,
    label: String,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = emoji,
            fontSize = 54.sp,
            textAlign = TextAlign.Center
        )

        // "Suelo" / base
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(10.dp)
                .background(cs.tertiary.copy(alpha = 0.35f), RoundedCornerShape(999.dp))
        )

        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium
        )
    }
}