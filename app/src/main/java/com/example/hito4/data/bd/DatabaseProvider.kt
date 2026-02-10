package com.example.hito4.data.bd


import android.content.Context
import androidx.room.Room
import com.example.hito4.data.db.AppDatabase

object DatabaseProvider {

    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun get(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "studybuddy.db"
            ).build()
            INSTANCE = instance
            instance
        }
    }
}
