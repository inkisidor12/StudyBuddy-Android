package com.example.hito4.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.hito4.data.dao.StudySessionDao
import com.example.hito4.data.dao.SubjectDao
import com.example.hito4.data.dao.ChatDao
import com.example.hito4.data.entity.StudySessionEntity
import com.example.hito4.data.entity.SubjectEntity
import com.example.hito4.data.entity.ChatMessage

@Database(
    entities = [SubjectEntity::class, StudySessionEntity::class, ChatMessage::class],
    version = 2  // subimos versión porque añadimos tabla
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun studySessionDao(): StudySessionDao
    abstract fun chatDao(): ChatDao
}