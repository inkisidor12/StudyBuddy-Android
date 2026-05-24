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
        val uid = auth.currentUser?.uid ?: return

        // Guardar en Room
        dao.insert(
            StudySessionEntity(
                subjectId = subjectId,
                startTimeMillis = startTimeMillis,
                endTimeMillis = endTimeMillis,
                plannedMinutes = plannedMinutes,
                actualMinutes = actualMinutes,
                uid = uid
            )
        )

        // Guardar en Firestore
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

        // Recalcular totalMinutes sumando TODAS las sesiones reales de Firestore
        val allSessions = db.collection("sessions")
            .whereEqualTo("uid", uid)
            .get()
            .await()
        val totalMinutes = allSessions.documents.sumOf {
            (it.getLong("actualMinutes") ?: 0L).toInt()
        }
        db.collection("users").document(uid)
            .update("totalMinutes", totalMinutes).await()
    }

    fun observeTotalMinutes(): Flow<Int> {
        val uid = auth.currentUser?.uid ?: ""
        return dao.observeTotalMinutes(uid)
    }

    fun observeSessions(): Flow<List<SessionWithSubjectName>> {
        val uid = auth.currentUser?.uid ?: ""
        return dao.observeSessionsWithSubjectName(uid)
    }

    fun observeSubjectRanking(): Flow<List<SubjectRankingRow>> {
        val uid = auth.currentUser?.uid ?: ""
        return dao.observeSubjectRanking(uid)
    }

    suspend fun syncFromFirestore() {
        val uid = auth.currentUser?.uid ?: return

        val localCount = dao.countByUid(uid)
        if (localCount > 0) return

        try {
            val result = db.collection("sessions")
                .whereEqualTo("uid", uid)
                .get()
                .await()

            result.documents.forEach { doc ->
                val subjectId = doc.getLong("subjectId") ?: 0L
                val startTimeMillis = doc.getLong("startTimeMillis") ?: 0L
                val endTimeMillis = doc.getLong("endTimeMillis") ?: 0L
                val plannedMinutes = (doc.getLong("plannedMinutes") ?: 0L).toInt()
                val actualMinutes = (doc.getLong("actualMinutes") ?: 0L).toInt()

                dao.insert(
                    StudySessionEntity(
                        subjectId = subjectId,
                        startTimeMillis = startTimeMillis,
                        endTimeMillis = endTimeMillis,
                        plannedMinutes = plannedMinutes,
                        actualMinutes = actualMinutes,
                        uid = uid
                    )
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("SessionRepo", "Error sincronizando sesiones: ${e.message}")
        }
    }
}