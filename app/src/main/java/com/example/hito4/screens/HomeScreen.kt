package com.example.hito4.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
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

private enum class HomeTab { INICIO, ESTUDIAR, STATS, AI, PROFILE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onLogout: () -> Unit) {
    var tab by remember { mutableStateOf(HomeTab.INICIO) }
    var bannerVisible by remember { mutableStateOf(true) }

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
                    )
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
                        selected = tab == HomeTab.INICIO,
                        onClick = { tab = HomeTab.INICIO },
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text("Inicio") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = selectedColor,
                            selectedTextColor = selectedColor,
                            unselectedIconColor = unselectedColor,
                            unselectedTextColor = unselectedColor,
                            indicatorColor = indicator
                        )
                    )
                    NavigationBarItem(
                        selected = tab == HomeTab.ESTUDIAR,
                        onClick = { tab = HomeTab.ESTUDIAR },
                        icon = { Icon(Icons.Default.MenuBook, contentDescription = null) },
                        label = { Text("Estudiar") },
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
                        selected = tab == HomeTab.AI,
                        onClick = { tab = HomeTab.AI },
                        icon = { Icon(Icons.Default.Psychology, contentDescription = null) },
                        label = { Text("IA") },
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
                // Banner solo en INICIO y ESTUDIAR
                AnimatedVisibility(
                    visible = homeState.currentStreak > 0
                            && bannerVisible
                            && (tab == HomeTab.INICIO || tab == HomeTab.ESTUDIAR),
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
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${homeState.totalMinutes} min",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Spacer(Modifier.width(8.dp))
                            IconButton(
                                onClick = { bannerVisible = false },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Cerrar",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }
                }

                when (tab) {
                    HomeTab.INICIO -> InicioScreen(modifier = Modifier.weight(1f))
                    HomeTab.ESTUDIAR -> EstudiarScreen(modifier = Modifier.weight(1f))
                    HomeTab.STATS -> StatsScreen(modifier = Modifier.weight(1f))
                    HomeTab.AI -> AIAssistantScreen(modifier = Modifier.weight(1f))
                    HomeTab.PROFILE -> ProfileScreen(
                        modifier = Modifier.weight(1f),
                        onLogout = { loginVm.logout(); onLogout() }
                    )
                }
            }
        }
    }
}