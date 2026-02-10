package com.example.hito4.screens


import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

private enum class HomeTab { SUBJECTS, FOCUS, STATS }

@Composable
fun HomeScreen() {
    var tab by remember { mutableStateOf(HomeTab.SUBJECTS) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == HomeTab.SUBJECTS,
                    onClick = { tab = HomeTab.SUBJECTS },
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    label = { Text("Asignaturas") }
                )
                NavigationBarItem(
                    selected = tab == HomeTab.FOCUS,
                    onClick = { tab = HomeTab.FOCUS },
                    icon = { Icon(Icons.Default.Timer, contentDescription = null) },
                    label = { Text("Focus") }
                )
                NavigationBarItem(
                    selected = tab == HomeTab.STATS,
                    onClick = { tab = HomeTab.STATS },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                    label = { Text("Stats") }
                )
            }
        }
    ) { padding ->
        when (tab) {
            HomeTab.SUBJECTS -> SubjectsScreen(modifier = Modifier.padding(padding))
            HomeTab.FOCUS -> FocusScreen(modifier = Modifier.padding(padding))
            HomeTab.STATS -> StatsScreen(modifier = Modifier.padding(padding))
        }
    }
}
