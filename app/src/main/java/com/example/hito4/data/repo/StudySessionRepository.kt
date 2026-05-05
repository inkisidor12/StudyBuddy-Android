package com.example.hito4.data.repo

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.hito4.data.dao.SessionWithSubjectName
import com.example.hito4.data.dao.StudySessionDao
import com.example.hito4.data.entity.StudySessionEntity
import kotlinx.coroutines.flow.Flow
import com.example.hito4.data.dao.SubjectRankingRow
import kotlinx.coroutines.tasks.await

class StudySessionRepository(private val dao: StudySessionDao) {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun addSession(
        subjectId: Long,
        startTimeMillis: Long,
        endTimeMillis: Long,
        plannedMinutes: Int,
        actualMinutes: Int,
        subjectName: String = ""
    ) {
        // Guardamos en Room (local)
        dao.insert(
            StudySessionEntity(
                subjectId = subjectId,
                startTimeMillis = startTimeMillis,
                endTimeMillis = endTimeMillis,
                plannedMinutes = plannedMinutes,
                actualMinutes = actualMinutes
            )
        )

        // Guardamos en Firestore (para el feed social)
        val uid = auth.currentUser?.uid ?: return
        val session = hashMapOf(
            "subjectId" to subjectId,
            "subjectName" to subjectName,
            "startTimeMillis" to startTimeMillis,
            "endTimeMillis" to endTimeMillis,
            "plannedMinutes" to plannedMinutes,
            "actualMinutes" to actualMinutes,
            "uid" to uid,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("sessions").add(session).await()
    }

    fun observeTotalMinutes(): Flow<Int> = dao.observeTotalMinutes()

    fun observeSessions(): Flow<List<SessionWithSubjectName>> =
        dao.observeSessionsWithSubjectName()

    fun observeSubjectRanking(): Flow<List<SubjectRankingRow>> = dao.observeSubjectRanking()
}