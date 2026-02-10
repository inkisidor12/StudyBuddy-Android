package com.example.hito4


import android.app.Application
import com.example.hito4.data.AppContainer

class StudyBuddyApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
