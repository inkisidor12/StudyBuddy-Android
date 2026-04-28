package com.example.hito4.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.hito4.screens.HomeScreen
import com.example.hito4.screens.LoginScreen
import com.example.hito4.ui.rememberAppContainer
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking

@Composable
fun AppNavGraph(navController: NavHostController) {
    val container = rememberAppContainer()

    // Comprobamos sincrónicamente si ya hay usuario guardado
    val savedUsername = remember {
        runBlocking { container.userPreferences.usernameFlow.firstOrNull() }
    }

    val startDestination = if (savedUsername.isNullOrBlank()) Routes.LOGIN else Routes.HOME

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }
    }
}