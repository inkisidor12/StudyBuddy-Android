package com.example.hito4.data.repo


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val nickname: String = "",
    val fullName: String = "",
    val phone: String = "",
    val birthDate: String = "",
    val totalMinutes: Int = 0
)

class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun isNicknameAvailable(nickname: String): Boolean {
        val result = db.collection("users")
            .whereEqualTo("nickname", nickname.trim().lowercase())
            .get()
            .await()
        return result.isEmpty
    }

    suspend fun createUserProfile(
        fullName: String,
        nickname: String,
        phone: String,
        birthDate: String
    ) {
        val user = auth.currentUser ?: return
        val profile = UserProfile(
            uid = user.uid,
            email = user.email ?: "",
            nickname = nickname.trim().lowercase(),
            fullName = fullName.trim(),
            phone = phone.trim(),
            birthDate = birthDate
        )
        db.collection("users").document(user.uid).set(profile).await()
    }

    suspend fun searchUserByNickname(nickname: String): UserProfile? {
        val result = db.collection("users")
            .whereEqualTo("nickname", nickname.trim().lowercase())
            .get()
            .await()
        return result.documents.firstOrNull()?.toObject(UserProfile::class.java)
    }

    suspend fun addFriend(friendUid: String) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid)
            .collection("friends").document(friendUid)
            .set(mapOf("uid" to friendUid)).await()
    }

    suspend fun getFriends(): List<UserProfile> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        val friendDocs = db.collection("users").document(uid)
            .collection("friends").get().await()
        return friendDocs.documents.mapNotNull { doc ->
            val friendUid = doc.getString("uid") ?: return@mapNotNull null
            db.collection("users").document(friendUid).get().await()
                .toObject(UserProfile::class.java)
        }
    }

    suspend fun updateTotalMinutes(totalMinutes: Int) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid)
            .update("totalMinutes", totalMinutes).await()
    }

    suspend fun getCurrentUserProfile(): UserProfile? {
        val uid = auth.currentUser?.uid ?: return null
        return db.collection("users").document(uid).get().await()
            .toObject(UserProfile::class.java)
    }
}