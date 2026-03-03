package com.example.hito4.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
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
import kotlin.math.max
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusScreen(modifier: Modifier = Modifier) {

    //  App container (BD + repositorios únicos)
    val container = rememberAppContainer()

    //  ViewModel único para Focus (incluye subjects + guardar sesión)
    val vm: FocusViewModelV2 = viewModel(
        factory = FocusViewModelV2Factory(
            container.subjectRepository,
            container.studySessionRepository
        )
    )
    val state by vm.ui.collectAsState()

    // Asignatura seleccionada
    var subjectMenuOpen by remember { mutableStateOf(false) }

//    val progress =
//        if (state.totalSeconds == 0) 0f
//        else 1f - (state.remainingSeconds.toFloat() / state.totalSeconds.toFloat())

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

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Focus 🌲", color = MaterialTheme.colorScheme.onPrimary) },
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
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (state.subjects.isEmpty()) {
                ForestCard(modifier = Modifier.fillMaxWidth()) {
                    Text("No tienes asignaturas todavía.")
                    Text("Ve a Asignaturas y crea al menos una para usar Focus.")
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

            // Progreso
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
                        TreeBadge(
                            emoji = emoji,
                            label = text
                        )
                    }

                    if (state.isFinished) {
                        AnimatedVisibility(
                            visible = state.isFinished,
                            enter = fadeIn() + scaleIn(animationSpec = spring()),
                        ) {
                            Text(
                                "¡Sesión completada y guardada! 🎉",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
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