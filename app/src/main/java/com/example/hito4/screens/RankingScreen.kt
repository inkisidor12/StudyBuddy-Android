package com.example.hito4.screens


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hito4.ui.rememberAppContainer
import com.example.hito4.viewmodel.RankingViewModel
import com.example.hito4.viewmodel.RankingViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingScreen(modifier: Modifier = Modifier) {
    val container = rememberAppContainer()

    val vm: RankingViewModel = viewModel(
        factory = RankingViewModelFactory(container.studySessionRepository)
    )

    val ranking by vm.ranking.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Ranking 🏆") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Top asignaturas por minutos", style = MaterialTheme.typography.titleLarge)

            if (ranking.isEmpty()) {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Aún no hay datos.")
                        Text("Completa sesiones en Focus para ver el ranking.")
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    itemsIndexed(ranking) { index, row ->
                        val medal = when (index) {
                            0 -> "🥇"
                            1 -> "🥈"
                            2 -> "🥉"
                            else -> "•"
                        }

                        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("$medal ${row.subjectName}", style = MaterialTheme.typography.titleMedium)
                                    Text("${row.totalMinutes} minutos", style = MaterialTheme.typography.bodyMedium)
                                }
                                Text("#${index + 1}", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}