package com.example.hito4.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hito4.data.entity.SubjectEntity
import com.example.hito4.ui.rememberAppContainer
import com.example.hito4.viewmodel.FocusViewModelV2
import com.example.hito4.viewmodel.FocusViewModelV2Factory
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusScreen(modifier: Modifier = Modifier) {

    // 🔹 App container (BD + repositorios únicos)
    val container = rememberAppContainer()

    // 🔹 ViewModel único para Focus (incluye subjects + guardar sesión)
    val vm: FocusViewModelV2 = viewModel(
        factory = FocusViewModelV2Factory(
            container.subjectRepository,
            container.studySessionRepository
        )
    )
    val state by vm.ui.collectAsState()

    // Asignatura seleccionada
    var subjectMenuOpen by remember { mutableStateOf(false) }

    val progress =
        if (state.totalSeconds == 0) 0f
        else 1f - (state.remainingSeconds.toFloat() / state.totalSeconds.toFloat())

    val timeText = formatSeconds(state.remainingSeconds)

    val growthText = when {
        progress < 0.25f -> "Semilla 🌱"
        progress < 0.60f -> "Brote 🌿"
        progress < 0.95f -> "Árbol 🌳"
        else -> "Bosque 🌲"
    }

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Focus 🌲") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (state.subjects.isEmpty()) {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("No tienes asignaturas todavía.")
                        Text("Ve a Asignaturas y crea al menos una para usar Focus.")
                    }
                }
                return@Column
            }

            // selector de asignatura
            ExposedDropdownMenuBox(
                expanded = subjectMenuOpen,
                onExpandedChange = { subjectMenuOpen = !subjectMenuOpen }
            ) {
                OutlinedTextField(
                    value = state.selectedSubject?.name ?: "Selecciona asignatura",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    label = { Text("Asignatura") }
                )
                ExposedDropdownMenu(
                    expanded = subjectMenuOpen,
                    onDismissRequest = { subjectMenuOpen = false }
                ) {
                    state.subjects.forEach { s: SubjectEntity ->
                        DropdownMenuItem(
                            text = { Text(s.name) },
                            onClick = {
                                vm.selectSubject(s)
                                subjectMenuOpen = false
                            }
                        )
                    }
                }
            }

            // Minutos
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Minutos planificados: ${state.plannedMinutes}",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { vm.changePlannedMinutes(-5) },
                            enabled = !state.isRunning
                        ) { Text("-5") }

                        Button(
                            onClick = { vm.changePlannedMinutes(+5) },
                            enabled = !state.isRunning
                        ) { Text("+5") }

                        OutlinedButton(
                            onClick = { vm.setPlannedMinutes(25) },
                            enabled = !state.isRunning
                        ) { Text("25") }
                    }
                }
            }

            // Progreso
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .padding(18.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(150.dp),
                        strokeWidth = 10.dp
                    )

                    Text(
                        timeText,
                        style = MaterialTheme.typography.headlineLarge,
                        textAlign = TextAlign.Center
                    )
                    Text(growthText, style = MaterialTheme.typography.titleMedium)

                    if (state.isFinished) {
                        Text(
                            "¡Sesión completada y guardada! 🎉",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

            // Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { vm.start() },
                    modifier = Modifier.weight(1f),
                    enabled = !state.isRunning && state.remainingSeconds > 0
                ) {
                    Text("Empezar")
                }

                OutlinedButton(
                    onClick = { vm.pause() },
                    modifier = Modifier.weight(1f),
                    enabled = state.isRunning
                ) {
                    Text("Pausar")
                }
            }

            OutlinedButton(
                onClick = { vm.reset() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reset")
            }
        }
    }
}

private fun formatSeconds(total: Int): String {
    val m = total / 60
    val s = total % 60
    return "%02d:%02d".format(m, s)
}
