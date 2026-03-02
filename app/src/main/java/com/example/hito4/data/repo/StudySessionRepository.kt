package com.example.hito4.data.repo

import com.example.hito4.data.dao.SessionWithSubjectName
import com.example.hito4.data.dao.StudySessionDao
import com.example.hito4.data.entity.StudySessionEntity
import kotlinx.coroutines.flow.Flow
import com.example.hito4.data.dao.SubjectRankingRow


class StudySessionRepository(private val dao: StudySessionDao) {

    suspend fun addSession(
        subjectId: Long,
        startTimeMillis: Long,
        endTimeMillis: Long,
        plannedMinutes: Int,
        actualMinutes: Int
    ) {
        dao.insert(
            StudySessionEntity(
                subjectId = subjectId,
                startTimeMillis = startTimeMillis,
                endTimeMillis = endTimeMillis,
                plannedMinutes = plannedMinutes,
                actualMinutes = actualMinutes
            )
        )
    }

    fun observeTotalMinutes(): Flow<Int> = dao.observeTotalMinutes()

    fun observeSessions(): Flow<List<SessionWithSubjectName>> =
        dao.observeSessionsWithSubjectName()

    fun observeSubjectRanking(): Flow<List<SubjectRankingRow>> = dao.observeSubjectRanking()
}

