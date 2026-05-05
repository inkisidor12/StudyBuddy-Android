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

data class FriendRequest(
    val id: String = "",
    val fromUid: String = "",
    val fromNickname: String = "",
    val fromFullName: String = "",
    val toUid: String = "",
    val status: String = "pending",
    val timestamp: Long = 0L
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

    suspend fun sendFriendRequest(toUid: String) {
        val currentUser = auth.currentUser ?: return
        val myProfile = getCurrentUserProfile() ?: return

        // Comprobamos que no existe ya una solicitud pendiente
        val existing = db.collection("friendRequests")
            .whereEqualTo("fromUid", currentUser.uid)
            .whereEqualTo("toUid", toUid)
            .whereEqualTo("status", "pending")
            .get()
            .await()
        if (!existing.isEmpty) return

        val request = hashMapOf(
            "fromUid" to currentUser.uid,
            "fromNickname" to myProfile.nickname,
            "fromFullName" to myProfile.fullName,
            "toUid" to toUid,
            "status" to "pending",
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("friendRequests").add(request).await()
    }

    suspend fun getPendingRequests(): List<FriendRequest> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        val result = db.collection("friendRequests")
            .whereEqualTo("toUid", uid)
            .whereEqualTo("status", "pending")
            .get()
            .await()
        return result.documents.mapNotNull { doc ->
            doc.toObject(FriendRequest::class.java)?.copy(id = doc.id)
        }
    }

    suspend fun acceptFriendRequest(request: FriendRequest) {
        val uid = auth.currentUser?.uid ?: return

        // Actualizamos estado de la solicitud
        db.collection("friendRequests").document(request.id)
            .update("status", "accepted").await()

        // Nos añadimos mutuamente como amigos
        db.collection("users").document(uid)
            .collection("friends").document(request.fromUid)
            .set(mapOf("uid" to request.fromUid)).await()

        db.collection("users").document(request.fromUid)
            .collection("friends").document(uid)
            .set(mapOf("uid" to uid)).await()
    }

    suspend fun rejectFriendRequest(request: FriendRequest) {
        db.collection("friendRequests").document(request.id)
            .update("status", "rejected").await()
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

    suspend fun hasPendingRequestTo(toUid: String): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        val result = db.collection("friendRequests")
            .whereEqualTo("fromUid", uid)
            .whereEqualTo("toUid", toUid)
            .whereEqualTo("status", "pending")
            .get()
            .await()
        return !result.isEmpty
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