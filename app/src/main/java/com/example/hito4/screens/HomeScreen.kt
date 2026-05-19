package com.example.hito4.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hito4.ui.ForestBackground
import com.example.hito4.ui.rememberAppContainer
import com.example.hito4.viewmodel.HomeViewModel
import com.example.hito4.viewmodel.HomeViewModelFactory
import com.example.hito4.viewmodel.LoginViewModel
import com.example.hito4.viewmodel.LoginViewModelFactory

private enum class HomeTab { SUBJECTS, FOCUS, FRIENDS, STATS, RANKING, PROFILE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onLogout: () -> Unit) {
    var tab by remember { mutableStateOf(HomeTab.SUBJECTS) }

    val container = rememberAppContainer()

    val loginVm: LoginViewModel = viewModel(
        factory = LoginViewModelFactory(
            container.authRepository,
            container.userPreferences
        )
    )

    val homeVm: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(container.userRepository)
    )
    val homeState by homeVm.ui.collectAsState()

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
                            loginVm.logout()
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
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                // Banner de racha
                AnimatedVisibility(
                    visible = homeState.currentStreak > 0,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                when {
                                    homeState.currentStreak >= 7 -> Color(0xFFFF6B00)
                                    homeState.currentStreak >= 3 -> Color(0xFFFF9500)
                                    else -> Color(0xFFFFC107)
                                }.copy(alpha = 0.15f)
                            )
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when {
                                    homeState.currentStreak >= 7 -> "🔥"
                                    homeState.currentStreak >= 3 -> "⚡"
                                    else -> "✨"
                                },
                                fontSize = 24.sp
                            )
                            Column {
                                Text(
                                    text = "${homeState.currentStreak} días seguidos",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        homeState.currentStreak >= 7 -> Color(0xFFFF6B00)
                                        homeState.currentStreak >= 3 -> Color(0xFFFF9500)
                                        else -> Color(0xFFFFC107)
                                    }
                                )
                                Text(
                                    text = when {
                                        homeState.currentStreak >= 7 -> "¡Estás en racha! 🌳"
                                        homeState.currentStreak >= 3 -> "¡Sigue así! 🌿"
                                        else -> "¡Buen comienzo! 🌱"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                        Text(
                            text = "${homeState.totalMinutes} min",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }

                when (tab) {
                    HomeTab.SUBJECTS -> SubjectsScreen(modifier = Modifier.weight(1f))
                    HomeTab.FOCUS -> FocusScreen(modifier = Modifier.weight(1f))
                    HomeTab.FRIENDS -> FriendsScreen(modifier = Modifier.weight(1f))
                    HomeTab.STATS -> StatsScreen(modifier = Modifier.weight(1f))
                    HomeTab.RANKING -> RankingScreen(modifier = Modifier.weight(1f))
                    HomeTab.PROFILE -> ProfileScreen(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}