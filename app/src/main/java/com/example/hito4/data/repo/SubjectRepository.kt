package com.example.hito4.data.repo


import com.example.hito4.data.dao.SubjectDao
import com.example.hito4.data.entity.SubjectEntity
import kotlinx.coroutines.flow.Flow

class SubjectRepository(private val dao: SubjectDao) {

    fun observeSubjects(): Flow<List<SubjectEntity>> = dao.observeAll()

    suspend fun addSubject(name: String) {
        val clean = name.trim()
        if (clean.isNotEmpty()) {
            dao.insert(SubjectEntity(name = clean))
        }
    }

    suspend fun deleteSubject(subject: SubjectEntity) {
        dao.delete(subject)
    }
}
