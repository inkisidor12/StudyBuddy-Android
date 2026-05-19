package com.example.hito4.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hito4.ui.rememberAppContainer
import com.example.hito4.viewmodel.AIAssistantViewModel
import com.example.hito4.viewmodel.AIAssistantViewModelFactory
import kotlinx.coroutines.launch

@Composable
fun AIAssistantScreen(modifier: Modifier = Modifier) {
    val container = rememberAppContainer()
    val vm: AIAssistantViewModel = viewModel(
        factory = AIAssistantViewModelFactory(
            container.chatDao,
            container.userRepository
        )
    )
    val state by vm.ui.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Auto-scroll al último mensaje
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {

        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("🤖 StudyBot", style = MaterialTheme.typography.titleMedium)
                if (state.educationLevel.isNotBlank()) {
                    Text(
                        state.educationLevel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            IconButton(onClick = { vm.clearChat() }) {
                Icon(Icons.Default.Delete, contentDescription = "Borrar chat",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        }

        // Lista de mensajes
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            if (state.messages.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🤖", style = MaterialTheme.typography.displayMedium)
                            Text(
                                "¡Hola! Soy StudyBot.",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "Pregúntame lo que quieras sobre tus estudios.\nTe guiaré para que lo entiendas tú mismo.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            items(state.messages) { message ->
                val isUser = message.role == "user"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    if (!isUser) {
                        Text("🤖", modifier = Modifier.padding(end = 6.dp, top = 4.dp))
                    }
                    Box(
                        modifier = Modifier
                            .widthIn(max = 280.dp)
                            .clip(
                                RoundedCornerShape(
                                    topStart = if (isUser) 16.dp else 4.dp,
                                    topEnd = if (isUser) 4.dp else 16.dp,
                                    bottomStart = 16.dp,
                                    bottomEnd = 16.dp
                                )
                            )
                            .background(
                                if (isUser) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = message.content,
                            color = if (isUser) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    if (isUser) {
                        Text("👤", modifier = Modifier.padding(start = 6.dp, top = 4.dp))
                    }
                }
            }

            if (state.isLoading) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🤖", modifier = Modifier.padding(end = 6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        }
                    }
                }
            }
        }

        // Error
        if (state.error != null) {
            Text(
                state.error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Input
        HorizontalDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = state.inputText,
                onValueChange = { vm.onInputChange(it) },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Escribe tu pregunta...") },
                maxLines = 4,
                shape = RoundedCornerShape(24.dp)
            )
            IconButton(
                onClick = { vm.sendMessage() },
                enabled = state.inputText.isNotBlank() && !state.isLoading,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        if (state.inputText.isNotBlank() && !state.isLoading)
                            MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Enviar",
                    tint = if (state.inputText.isNotBlank() && !state.isLoading)
                        MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }
        }
    }
}