package com.example.hito4.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hito4.ui.ForestCard
import com.example.hito4.ui.rememberAppContainer
import com.example.hito4.viewmodel.ProfileViewModel
import com.example.hito4.viewmodel.ProfileViewModelFactory

@Composable
fun ProfileScreen(modifier: Modifier = Modifier, onLogout: () -> Unit = {}) {
    val container = rememberAppContainer()
    val vm: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(container.userRepository)
    )
    val state by vm.ui.collectAsState()
    var editMode by remember { mutableStateOf(false) }
    var newNickname by remember { mutableStateOf("") }
    var newFullName by remember { mutableStateOf("") }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Diálogo de confirmación de logout
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Seguro que quieres cerrar sesión?") },
            confirmButton = {
                TextButton(onClick = { onLogout() }) {
                    Text("Cerrar sesión", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Mi perfil", style = MaterialTheme.typography.titleLarge)
            IconButton(onClick = {
                if (!editMode) {
                    newNickname = state.profile?.nickname ?: ""
                    newFullName = state.profile?.fullName ?: ""
                }
                editMode = !editMode
            }) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Editar",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (state.isLoading) {
            CircularProgressIndicator()
            return@Column
        }

        val profile = state.profile ?: return@Column

        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = profile.nickname.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                fontSize = 40.sp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        if (editMode) {
            ForestCard(modifier = Modifier.fillMaxWidth()) {
                Text("Editar perfil", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = newFullName,
                    onValueChange = { newFullName = it },
                    label = { Text("Nombre y apellidos") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = newNickname,
                    onValueChange = { newNickname = it },
                    label = { Text("Nickname") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (state.error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        state.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { editMode = false },
                        modifier = Modifier.weight(1f)
                    ) { Text("Cancelar") }
                    Button(
                        onClick = {
                            vm.updateProfile(newFullName, newNickname) {
                                editMode = false
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = newNickname.isNotBlank() && newFullName.isNotBlank()
                    ) { Text("Guardar") }
                }
            }
        } else {
            ForestCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(profile.fullName, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "@${profile.nickname}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            profile.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            profile.phone,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            ForestCard(modifier = Modifier.fillMaxWidth()) {
                Text("Mis estadísticas", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatItem(value = "${profile.totalMinutes}", label = "Minutos\nestudiados", emoji = "📚")
                    StatItem(value = "${state.totalSessions}", label = "Sesiones\ncompletadas", emoji = "✅")
                    StatItem(value = "${state.currentStreak}", label = "Días de\nracha", emoji = "🔥")
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Botón cerrar sesión estilo red social
        OutlinedButton(
            onClick = { showLogoutDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = androidx.compose.ui.graphics.SolidColor(
                    MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                )
            )
        ) {
            Icon(
                Icons.Default.Logout,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("Cerrar sesión")
        }
    }
}

@Composable
private fun StatItem(value: String, label: String, emoji: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 24.sp)
        Text(value, style = MaterialTheme.typography.headlineSmall)
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}