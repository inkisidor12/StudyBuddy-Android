package com.example.hito4.data.repo


import com.example.hito4.data.dao.SubjectDao
import com.example.hito4.data.entity.SubjectEntity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow

class SubjectRepository(private val dao: SubjectDao) {
    private val auth = FirebaseAuth.getInstance()

    fun observeSubjects(): Flow<List<SubjectEntity>> {
        val uid = auth.currentUser?.uid ?: ""
        return dao.observeAll(uid)
    }

    suspend fun addSubject(name: String) {
        val uid = auth.currentUser?.uid ?: return
        val clean = name.trim()
        if (clean.isNotEmpty()) {
            dao.insert(SubjectEntity(name = clean, uid = uid))
        }
    }

    suspend fun deleteSubject(subject: SubjectEntity) {
        dao.delete(subject)
    }
}
