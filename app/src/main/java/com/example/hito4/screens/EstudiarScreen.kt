package com.example.hito4.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hito4.data.entity.SubjectEntity
import com.example.hito4.ui.ForestCard
import com.example.hito4.ui.TreeBadge
import com.example.hito4.ui.rememberAppContainer
import com.example.hito4.viewmodel.FocusViewModelV2
import com.example.hito4.viewmodel.FocusViewModelV2Factory
import com.example.hito4.viewmodel.SubjectsViewModel
import com.example.hito4.viewmodel.SubjectsViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstudiarScreen(modifier: Modifier = Modifier) {
    val container = rememberAppContainer()

    val subjectsVm: SubjectsViewModel = viewModel(
        factory = SubjectsViewModelFactory(container.subjectRepository)
    )
    val focusVm: FocusViewModelV2 = viewModel(
        factory = FocusViewModelV2Factory(
            container.subjectRepository,
            container.studySessionRepository
        )
    )

    val subjects by subjectsVm.subjects.collectAsState()
    val focusState by focusVm.ui.collectAsState()

    // null = lista de asignaturas, !null = timer de esa asignatura
    var selectedSubject by remember { mutableStateOf<SubjectEntity?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var newSubject by remember { mutableStateOf("") }

    if (selectedSubject != null) {
        // Pantalla del timer
        FocusTimerContent(
            vm = focusVm,
            subject = selectedSubject!!,
            onBack = {
                focusVm.reset()
                selectedSubject = null
            },
            modifier = modifier
        )
    } else {
        // Pantalla de lista de asignaturas
        Scaffold(
            modifier = modifier,
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
                Text("Mis asignaturas", style = MaterialTheme.typography.titleLarge)

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
                            Card(
                                onClick = {
                                    focusVm.selectSubject(s)
                                    selectedSubject = s
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("📚", style = MaterialTheme.typography.titleLarge)
                                        Column {
                                            Text(
                                                s.name,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Text(
                                                "Pulsa para estudiar",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                            )
                                        }
                                    }
                                    TextButton(
                                        onClick = { subjectsVm.delete(s) },
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
    }

    // Dialog nueva asignatura
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            textContentColor = MaterialTheme.colorScheme.onPrimary,
            title = { Text("Nueva asignatura") },
            text = {
                OutlinedTextField(
                    value = newSubject,
                    onValueChange = { newSubject = it },
                    label = { Text("Nombre") },
                    singleLine = true,
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
                        subjectsVm.add(newSubject)
                        newSubject = ""
                        showDialog = false
                    },
                    enabled = newSubject.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary,
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) { Text("Crear") }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDialog = false },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                        )
                    )
                ) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun FocusTimerContent(
    vm: FocusViewModelV2,
    subject: SubjectEntity,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by vm.ui.collectAsState()

    val rawProgress =
        if (state.totalSeconds == 0) 0f
        else 1f - (state.remainingSeconds.toFloat() / state.totalSeconds.toFloat())

    val animatedProgress by animateFloatAsState(
        targetValue = rawProgress,
        animationSpec = tween(durationMillis = 500),
        label = "progressAnimation"
    )

    val timeText = formatSeconds(state.remainingSeconds)

    val growthText = when {
        animatedProgress < 0.25f -> "Semilla 🌱"
        animatedProgress < 0.60f -> "Brote 🌿"
        animatedProgress < 0.95f -> "Árbol 🌳"
        else -> "Bosque 🌲"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header con flecha atrás y nombre asignatura
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
            }
            Text(
                subject.name,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        ForestCard(modifier = Modifier.fillMaxWidth()) {
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

        ForestCard(modifier = Modifier.fillMaxWidth()) {
            val progressColor = when {
                animatedProgress < 0.25f -> MaterialTheme.colorScheme.onPrimary
                animatedProgress < 0.60f -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.secondary
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.size(150.dp),
                    strokeWidth = 10.dp,
                    color = progressColor
                )
                Text(
                    timeText,
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center
                )
                AnimatedContent(
                    targetState = growthText,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) +
                                scaleIn(initialScale = 0.8f, animationSpec = tween(300)) togetherWith
                                fadeOut(animationSpec = tween(300))
                    },
                    label = "treeStage"
                ) { text ->
                    val emoji = when (text) {
                        "Semilla 🌱" -> "🌱"
                        "Brote 🌿" -> "🌿"
                        "Árbol 🌳" -> "🌳"
                        else -> "🌲"
                    }
                    TreeBadge(emoji = emoji, label = text)
                }
                if (state.isFinished) {
                    AnimatedVisibility(
                        visible = state.isFinished,
                        enter = fadeIn() + scaleIn(animationSpec = spring())
                    ) {
                        Text(
                            "¡Sesión completada y guardada! 🎉",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { vm.start() },
                modifier = Modifier.weight(1f),
                enabled = !state.isRunning && state.remainingSeconds > 0
            ) { Text("Empezar") }
            OutlinedButton(
                onClick = { vm.pause() },
                modifier = Modifier.weight(1f),
                enabled = state.isRunning
            ) { Text("Pausar") }
        }

        OutlinedButton(
            onClick = { vm.reset() },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Reset") }
    }
}

private fun formatSeconds(total: Int): String {
    val m = total / 60
    val s = total % 60
    return "%02d:%02d".format(m, s)
}