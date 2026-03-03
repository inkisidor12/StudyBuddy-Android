package com.example.hito4.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hito4.ui.ForestCard
import com.example.hito4.ui.rememberAppContainer
import com.example.hito4.viewmodel.SubjectsViewModel
import com.example.hito4.viewmodel.SubjectsViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectsScreen(modifier: Modifier = Modifier) {
    val container = rememberAppContainer()

    val vm: SubjectsViewModel = viewModel(
        factory = SubjectsViewModelFactory(container.subjectRepository)
    )
    val subjects by vm.subjects.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var newSubject by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Asignaturas", color = MaterialTheme.colorScheme.onPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ) {
                Text("+", style = MaterialTheme.typography.headlineSmall)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (subjects.isEmpty()) {
                ForestCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Aún no tienes asignaturas.")
                    Text("Pulsa + para crear la primera.")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(subjects) { s ->
                        ForestCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(s.name, style = MaterialTheme.typography.titleMedium)
                                TextButton(
                                    onClick = { vm.delete(s) },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    )
                                ) { Text("Borrar") }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },

            // ✅ Fondo del modal VERDE
            containerColor = MaterialTheme.colorScheme.primary,

            // ✅ Colores del texto dentro del modal
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            textContentColor = MaterialTheme.colorScheme.onPrimary,

            title = { Text("Nueva asignatura") },

            text = {
                OutlinedTextField(
                    value = newSubject,
                    onValueChange = { newSubject = it },
                    label = { Text("Nombre") },
                    singleLine = true,

                    // ✅ TextField en modo “sobre verde”
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                        unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,

                        focusedContainerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.10f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.08f),

                        focusedBorderColor = MaterialTheme.colorScheme.onPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),

                        focusedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),

                        cursorColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            },

            confirmButton = {
                Button(
                    onClick = {
                        vm.add(newSubject)
                        newSubject = ""
                        showDialog = false
                    },
                    enabled = newSubject.isNotBlank(),

                    // ✅ Botón “Crear” claro sobre verde (contraste)
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary,
                        contentColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.35f),
                        disabledContentColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                ) {
                    Text("Crear")
                }
            },

            dismissButton = {
                OutlinedButton(
                    onClick = { showDialog = false },

                    // ✅ Botón “Cancelar” borde blanco
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                        )
                    )
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}