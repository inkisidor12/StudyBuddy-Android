package com.example.hito4.screens


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hito4.ui.ForestCard
import com.example.hito4.ui.rememberAppContainer
import com.example.hito4.viewmodel.FriendsViewModel
import com.example.hito4.viewmodel.FriendsViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(modifier: Modifier = Modifier) {
    val container = rememberAppContainer()
    val vm: FriendsViewModel = viewModel(
        factory = FriendsViewModelFactory(container.userRepository)
    )
    val state by vm.ui.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Amigos 👥", color = MaterialTheme.colorScheme.onPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Buscador
            item {
                ForestCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Buscar amigos", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = state.searchQuery,
                            onValueChange = { vm.onSearchQueryChange(it) },
                            label = { Text("Nickname") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { vm.searchUser() },
                            enabled = state.searchQuery.isNotBlank() && !state.searching
                        ) {
                            if (state.searching) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            } else {
                                Icon(Icons.Default.Search, contentDescription = "Buscar")
                            }
                        }
                    }

                    // Resultado de búsqueda
                    if (state.searchError != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            state.searchError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    if (state.searchResult != null) {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "@${state.searchResult!!.nickname}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    state.searchResult!!.fullName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    "${state.searchResult!!.totalMinutes} min estudiados",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            if (state.alreadyFriend) {
                                Text(
                                    "Ya es tu amigo ✅",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            } else {
                                IconButton(onClick = { vm.addFriend(state.searchResult!!.uid) }) {
                                    Icon(
                                        Icons.Default.PersonAdd,
                                        contentDescription = "Añadir amigo",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Feed de actividad
            item {
                Text("Actividad reciente", style = MaterialTheme.typography.titleLarge)
            }

            if (state.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (state.feed.isEmpty()) {
                item {
                    ForestCard(modifier = Modifier.fillMaxWidth()) {
                        Text("Aún no hay actividad.")
                        Text("Añade amigos para ver lo que estudian 🌱")
                    }
                }
            } else {
                items(state.feed) { item ->
                    ForestCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "@${item.nickname}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Estudió ${item.actualMinutes} min de ${item.subjectName} 📚",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Text(
                                timeAgo(item.timestamp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            // Lista de amigos
            item {
                Text("Mis amigos (${state.friends.size})", style = MaterialTheme.typography.titleLarge)
            }

            if (state.friends.isEmpty()) {
                item {
                    ForestCard(modifier = Modifier.fillMaxWidth()) {
                        Text("Aún no tienes amigos.")
                        Text("Búscalos por su nickname 🔍")
                    }
                }
            } else {
                items(state.friends) { friend ->
                    ForestCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "@${friend.nickname}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    friend.fullName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            Text(
                                "${friend.totalMinutes} min 🌱",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun timeAgo(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
    val hours = TimeUnit.MILLISECONDS.toHours(diff)
    val days = TimeUnit.MILLISECONDS.toDays(diff)
    return when {
        minutes < 1 -> "ahora"
        minutes < 60 -> "hace ${minutes}min"
        hours < 24 -> "hace ${hours}h"
        else -> "hace ${days}d"
    }
}