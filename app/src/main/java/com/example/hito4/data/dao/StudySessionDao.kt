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
data class SubjectRankingRow(
    val subjectId: Long,
    val subjectName: String,
    val totalMinutes: Int
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
    @Query(
        """
    SELECT s.id as subjectId,
           s.name as subjectName,
           COALESCE(SUM(ss.actualMinutes), 0) as totalMinutes
    FROM subjects s
    LEFT JOIN study_sessions ss ON ss.subjectId = s.id
    GROUP BY s.id, s.name
    ORDER BY totalMinutes DESC, s.name ASC
    """
    )
    fun observeSubjectRanking(): Flow<List<SubjectRankingRow>>

}


