package com.example.hito4.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(modifier: Modifier = Modifier) {
    val container = rememberAppContainer()
    val context = androidx.compose.ui.platform.LocalContext.current
    val vm: FriendsViewModel = viewModel(
        factory = FriendsViewModelFactory(container.userRepository, context)
    )
    val state by vm.ui.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Feed", "Amigos", "Solicitudes")

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
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Tabs internas
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            if (index == 2 && state.pendingRequests.isNotEmpty()) {
                                // Badge en solicitudes
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(title)
                                    Badge {
                                        Text("${state.pendingRequests.size}")
                                    }
                                }
                            } else {
                                Text(title)
                            }
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> FeedTab(state = state)
                1 -> FriendsTab(state = state, vm = vm)
                2 -> RequestsTab(state = state, vm = vm)
            }
        }
    }
}

@Composable
private fun FeedTab(state: com.example.hito4.viewmodel.FriendsUiState) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
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
    }
}

@Composable
private fun FriendsTab(
    state: com.example.hito4.viewmodel.FriendsUiState,
    vm: FriendsViewModel
) {
    LazyColumn(
        modifier = Modifier
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
                    HorizontalDivider()
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
                        when {
                            state.alreadyFriend -> Text(
                                "Ya es tu amigo ✅",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            state.requestAlreadySent -> Text(
                                "Solicitud enviada ⏳",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            else -> IconButton(
                                onClick = { vm.sendFriendRequest(state.searchResult!!.uid) }
                            ) {
                                Icon(
                                    Icons.Default.PersonAdd,
                                    contentDescription = "Enviar solicitud",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Lista de amigos
        item {
            Text(
                "Mis amigos (${state.friends.size})",
                style = MaterialTheme.typography.titleLarge
            )
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

@Composable
private fun RequestsTab(
    state: com.example.hito4.viewmodel.FriendsUiState,
    vm: FriendsViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Solicitudes pendientes", style = MaterialTheme.typography.titleLarge)
        }

        if (state.pendingRequests.isEmpty()) {
            item {
                ForestCard(modifier = Modifier.fillMaxWidth()) {
                    Text("No tienes solicitudes pendientes 🌿")
                }
            }
        } else {
            items(state.pendingRequests) { request ->
                ForestCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "@${request.fromNickname}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                request.fromFullName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                "Hace ${timeAgo(request.timestamp)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(onClick = { vm.acceptRequest(request) }) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Aceptar",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(onClick = { vm.rejectRequest(request) }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Rechazar",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
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
        minutes < 60 -> "${minutes}min"
        hours < 24 -> "${hours}h"
        else -> "${days}d"
    }
}