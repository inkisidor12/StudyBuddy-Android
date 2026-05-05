package com.example.hito4.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hito4.ui.ForestBackground
import com.example.hito4.ui.rememberAppContainer
import com.example.hito4.viewmodel.LoginViewModel
import com.example.hito4.viewmodel.LoginViewModelFactory

private enum class HomeTab { SUBJECTS, FOCUS, FRIENDS, STATS, RANKING, PROFILE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onLogout: () -> Unit) {
    var tab by remember { mutableStateOf(HomeTab.SUBJECTS) }

    val container = rememberAppContainer()
    val vm: LoginViewModel = viewModel(
        factory = LoginViewModelFactory(
            container.authRepository,
            container.userPreferences
        )
    )

    ForestBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("StudyBuddy 🌲", color = MaterialTheme.colorScheme.onPrimary) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    actions = {
                        IconButton(onClick = {
                            vm.logout()
                            onLogout()
                        }) {
                            Icon(
                                Icons.Default.Logout,
                                contentDescription = "Cerrar sesión",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.primary,
                    tonalElevation = 0.dp
                ) {
                    val selectedColor = MaterialTheme.colorScheme.onPrimary
                    val unselectedColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.65f)
                    val indicator = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f)

                    NavigationBarItem(
                        selected = tab == HomeTab.SUBJECTS,
                        onClick = { tab = HomeTab.SUBJECTS },
                        icon = { Icon(Icons.Default.List, contentDescription = null) },
                        label = { Text("Asignaturas") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = selectedColor,
                            selectedTextColor = selectedColor,
                            unselectedIconColor = unselectedColor,
                            unselectedTextColor = unselectedColor,
                            indicatorColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.20f)
                        )
                    )

                    NavigationBarItem(
                        selected = tab == HomeTab.FOCUS,
                        onClick = { tab = HomeTab.FOCUS },
                        icon = { Icon(Icons.Default.Timer, contentDescription = null) },
                        label = { Text("Focus") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = selectedColor,
                            selectedTextColor = selectedColor,
                            unselectedIconColor = unselectedColor,
                            unselectedTextColor = unselectedColor,
                            indicatorColor = indicator
                        )
                    )

                    NavigationBarItem(
                        selected = tab == HomeTab.FRIENDS,
                        onClick = { tab = HomeTab.FRIENDS },
                        icon = { Icon(Icons.Default.People, contentDescription = null) },
                        label = { Text("Amigos") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = selectedColor,
                            selectedTextColor = selectedColor,
                            unselectedIconColor = unselectedColor,
                            unselectedTextColor = unselectedColor,
                            indicatorColor = indicator
                        )
                    )

                    NavigationBarItem(
                        selected = tab == HomeTab.STATS,
                        onClick = { tab = HomeTab.STATS },
                        icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                        label = { Text("Stats") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = selectedColor,
                            selectedTextColor = selectedColor,
                            unselectedIconColor = unselectedColor,
                            unselectedTextColor = unselectedColor,
                            indicatorColor = indicator
                        )
                    )

                    NavigationBarItem(
                        selected = tab == HomeTab.RANKING,
                        onClick = { tab = HomeTab.RANKING },
                        icon = { Icon(Icons.Default.EmojiEvents, contentDescription = null) },
                        label = { Text("Ranking") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = selectedColor,
                            selectedTextColor = selectedColor,
                            unselectedIconColor = unselectedColor,
                            unselectedTextColor = unselectedColor,
                            indicatorColor = indicator
                        )
                    )

                    NavigationBarItem(
                        selected = tab == HomeTab.PROFILE,
                        onClick = { tab = HomeTab.PROFILE },
                        icon = { Icon(Icons.Default.Person, contentDescription = null) },
                        label = { Text("Perfil") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = selectedColor,
                            selectedTextColor = selectedColor,
                            unselectedIconColor = unselectedColor,
                            unselectedTextColor = unselectedColor,
                            indicatorColor = indicator
                        )
                    )
                }
            }
        ) { padding ->
            when (tab) {
                HomeTab.SUBJECTS -> SubjectsScreen(modifier = Modifier.padding(padding))
                HomeTab.FOCUS -> FocusScreen(modifier = Modifier.padding(padding))
                HomeTab.FRIENDS -> FriendsScreen(modifier = Modifier.padding(padding))
                HomeTab.STATS -> StatsScreen(modifier = Modifier.padding(padding))
                HomeTab.RANKING -> RankingScreen(modifier = Modifier.padding(padding))
                HomeTab.PROFILE -> ProfileScreen(modifier = Modifier.padding(padding))
            }
        }
    }
}