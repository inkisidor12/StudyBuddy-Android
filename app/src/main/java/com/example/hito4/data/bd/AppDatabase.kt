package com.example.hito4.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.hito4.data.dao.StudySessionDao
import com.example.hito4.data.dao.SubjectDao
import com.example.hito4.data.entity.StudySessionEntity
import com.example.hito4.data.entity.SubjectEntity

@Database(
    entities = [SubjectEntity::class, StudySessionEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun studySessionDao(): StudySessionDao
}
