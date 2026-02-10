package com.example.hito4.data.entity


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_sessions")
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val plannedMinutes: Int,
    val actualMinutes: Int
)
