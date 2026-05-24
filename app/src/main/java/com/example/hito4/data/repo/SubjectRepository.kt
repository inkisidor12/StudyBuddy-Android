package com.example.hito4.data.repo

import com.example.hito4.data.dao.SubjectDao
import com.example.hito4.data.entity.SubjectEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class SubjectRepository(private val dao: SubjectDao) {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun observeSubjects(): Flow<List<SubjectEntity>> {
        val uid = auth.currentUser?.uid ?: ""
        return dao.observeAll(uid)
    }

    suspend fun addSubject(name: String) {
        val uid = auth.currentUser?.uid ?: return
        val clean = name.trim()
        if (clean.isEmpty()) return

        // Guardar en Room
        val entity = SubjectEntity(name = clean, uid = uid)
        val localId = dao.insert(entity)

        // Guardar en Firestore
        val firestoreData = hashMapOf(
            "name" to clean,
            "uid" to uid,
            "localId" to localId,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("users").document(uid)
            .collection("subjects")
            .document(localId.toString())
            .set(firestoreData)
            .await()
    }

    suspend fun deleteSubject(subject: SubjectEntity) {
        val uid = auth.currentUser?.uid ?: return

        // Borrar de Room
        dao.delete(subject)

        // Borrar de Firestore
        db.collection("users").document(uid)
            .collection("subjects")
            .document(subject.id.toString())
            .delete()
            .await()
    }

    // Sincronizar desde Firestore a Room (al hacer login)
    suspend fun syncFromFirestore() {
        val uid = auth.currentUser?.uid ?: return

        // Solo sincronizamos si Room está vacío para este usuario
        val localCount = dao.countByUid(uid)
        if (localCount > 0) return

        try {
            val result = db.collection("users").document(uid)
                .collection("subjects")
                .get()
                .await()

            result.documents.forEach { doc ->
                val name = doc.getString("name") ?: return@forEach
                val localId = doc.getLong("localId") ?: 0L
                dao.insertWithId(SubjectEntity(id = localId, name = name, uid = uid))
            }
        } catch (e: Exception) {
            android.util.Log.e("SubjectRepo", "Error sincronizando: ${e.message}")
        }
    }
}