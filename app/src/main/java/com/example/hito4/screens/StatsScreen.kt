package com.example.hito4.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hito4.data.repo.Achievement
import com.example.hito4.ui.ForestCard
import com.example.hito4.ui.rememberAppContainer
import com.example.hito4.viewmodel.AchievementsViewModel
import com.example.hito4.viewmodel.AchievementsViewModelFactory
import com.example.hito4.viewmodel.RankingViewModel
import com.example.hito4.viewmodel.RankingViewModelFactory
import com.example.hito4.viewmodel.StatsViewModel
import com.example.hito4.viewmodel.StatsViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class StatsTab { STATS, RANKING, LOGROS }

@Composable
fun StatsScreen(modifier: Modifier = Modifier) {
    var tab by remember { mutableStateOf(StatsTab.STATS) }

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = tab.ordinal,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = tab == StatsTab.STATS,
                onClick = { tab = StatsTab.STATS },
                text = { Text("Estadísticas") }
            )
            Tab(
                selected = tab == StatsTab.RANKING,
                onClick = { tab = StatsTab.RANKING },
                text = { Text("Ranking") }
            )
            Tab(
                selected = tab == StatsTab.LOGROS,
                onClick = { tab = StatsTab.LOGROS },
                text = { Text("Logros") }
            )
        }

        when (tab) {
            StatsTab.STATS -> StatsContent()
            StatsTab.RANKING -> RankingContent()
            StatsTab.LOGROS -> LogrosContent()
        }
    }
}

@Composable
private fun StatsContent() {
    val container = rememberAppContainer()
    val vm: StatsViewModel = viewModel(
        factory = StatsViewModelFactory(container.userRepository)
    )
    val state by vm.ui.collectAsState()
    LaunchedEffect(Unit) {
        vm.refresh()
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            ForestCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📚", fontSize = 28.sp)
                        Text(
                            "${state.totalMinutes}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Minutos totales",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("✅", fontSize = 28.sp)
                        Text(
                            "${state.totalSessions}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Sesiones",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Text("Historial de sesiones", style = MaterialTheme.typography.titleMedium)

            if (state.sessions.isEmpty()) {
                ForestCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Aún no hay sesiones.")
                    Text("Completa una sesión en Focus para verla aquí.")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.sessions) { s ->
                        ForestCard(modifier = Modifier.fillMaxWidth()) {
                            Text(s.subjectName, style = MaterialTheme.typography.titleMedium)
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

@Composable
private fun RankingContent() {
    val container = rememberAppContainer()
    val vm: RankingViewModel = viewModel(
        factory = RankingViewModelFactory(container.userRepository)
    )
    val ranking by vm.ranking.collectAsState()
    LaunchedEffect(Unit) {
        vm.refresh()
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (ranking.isEmpty()) {
            ForestCard(modifier = Modifier.fillMaxWidth()) {
                Text("Aún no hay datos.")
                Text("Completa sesiones en Focus para ver el ranking.")
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(ranking) { index, row ->
                    val medal = when (index) {
                        0 -> "🥇"
                        1 -> "🥈"
                        2 -> "🥉"
                        else -> "•"
                    }
                    ForestCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
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

@Composable
private fun LogrosContent() {
    val container = rememberAppContainer()
    val vm: AchievementsViewModel = viewModel(
        factory = AchievementsViewModelFactory(
            container.achievementsRepository,
            container.userRepository
        )
    )
    val state by vm.ui.collectAsState()

    if (state.newlyUnlocked.isNotEmpty()) {
        val achievement = state.newlyUnlocked.first()
        Dialog(onDismissRequest = { vm.clearNewlyUnlocked() }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("¡Logro desbloqueado! 🎉",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(achievement.emoji, fontSize = 60.sp)
                    Text(
                        achievement.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        achievement.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = { vm.clearNewlyUnlocked() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onPrimary,
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) { Text("¡Genial!") }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val unlocked = state.achievements.count { it.unlocked }
        val total = state.achievements.size

        ForestCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "$unlocked / $total desbloqueados",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Sigue estudiando para conseguir más 🌱",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Text(
                    "${if (total == 0) 0 else (unlocked * 100f / total).toInt()}%",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { if (total == 0) 0f else unlocked.toFloat() / total.toFloat() },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.achievements) { achievement ->
                    AchievementCard(achievement = achievement)
                }
            }
        }
    }
}

@Composable
private fun AchievementCard(achievement: Achievement) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (achievement.unlocked) 1f else 0.4f),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (achievement.unlocked)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (achievement.unlocked) achievement.emoji else "🔒",
                fontSize = 36.sp
            )
            Text(
                text = achievement.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = achievement.description,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            if (achievement.unlocked) {
                Text(
                    text = "✅ Conseguido",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun formatMillis(ms: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(ms))
}