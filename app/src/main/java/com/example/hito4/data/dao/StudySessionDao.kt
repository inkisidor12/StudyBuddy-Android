package com.example.hito4.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.hito4.data.entity.StudySessionEntity
import kotlinx.coroutines.flow.Flow

data class SessionWithSubjectName(
    val id: Long,
    val subjectName: String,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val plannedMinutes: Int,
    val actualMinutes: Int
)

@Dao
interface StudySessionDao {

    @Insert
    suspend fun insert(session: StudySessionEntity)

    @Query("SELECT COALESCE(SUM(actualMinutes), 0) FROM study_sessions")
    fun observeTotalMinutes(): Flow<Int>

    @Query(
        """
        SELECT ss.id as id,
               s.name as subjectName,
               ss.startTimeMillis as startTimeMillis,
               ss.endTimeMillis as endTimeMillis,
               ss.plannedMinutes as plannedMinutes,
               ss.actualMinutes as actualMinutes
        FROM study_sessions ss
        INNER JOIN subjects s ON s.id = ss.subjectId
        ORDER BY ss.endTimeMillis DESC
        """
    )
    fun observeSessionsWithSubjectName(): Flow<List<SessionWithSubjectName>>
}
