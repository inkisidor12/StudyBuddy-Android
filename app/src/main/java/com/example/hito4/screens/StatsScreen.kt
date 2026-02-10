package com.example.hito4.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hito4.ui.rememberAppContainer
import com.example.hito4.viewmodel.StatsViewModel
import com.example.hito4.viewmodel.StatsViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(modifier: Modifier = Modifier) {
    val container = rememberAppContainer()

    val vm: StatsViewModel = viewModel(
        factory = StatsViewModelFactory(container.studySessionRepository)
    )

    val totalMinutes by vm.totalMinutes.collectAsState()
    val sessions by vm.sessions.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Stats") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Estadísticas", style = MaterialTheme.typography.titleLarge)

            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Minutos totales estudiados: $totalMinutes")
                    Text("Sesiones registradas: ${sessions.size}")
                }
            }

            Text("Historial de sesiones", style = MaterialTheme.typography.titleMedium)

            if (sessions.isEmpty()) {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Aún no hay sesiones.")
                        Text("Completa una sesión en Focus para verla aquí.")
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(sessions) { s ->
                        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    text = s.subjectName,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(Modifier.height(6.dp))
                                Text("Real: ${s.actualMinutes} min | Plan: ${s.plannedMinutes} min")
                                Text("Inicio: ${formatMillis(s.startTimeMillis)}")
                                Text("Fin: ${formatMillis(s.endTimeMillis)}")
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatMillis(ms: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(ms))
}
