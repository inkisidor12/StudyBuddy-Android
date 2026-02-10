package com.example.hito4.ui


import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.hito4.StudyBuddyApp
import com.example.hito4.data.AppContainer

@Composable
fun rememberAppContainer(): AppContainer {
    val context = LocalContext.current.applicationContext
    return (context as StudyBuddyApp).container
}
