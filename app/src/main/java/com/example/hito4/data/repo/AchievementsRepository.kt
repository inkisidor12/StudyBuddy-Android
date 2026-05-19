package com.example.hito4.data.repo


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val unlocked: Boolean = false,
    val unlockedAt: Long? = null
)

class AchievementsRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val allAchievements = listOf(
        Achievement("first_session", "Primera semilla", "Completa tu primera sesión", "🌱"),
        Achievement("five_sessions", "Brote", "Completa 5 sesiones", "🌿"),
        Achievement("ten_sessions", "Árbol", "Completa 10 sesiones", "🌳"),
        Achievement("streak_3", "En racha", "3 días seguidos estudiando", "🔥"),
        Achievement("streak_7", "Imparable", "7 días seguidos estudiando", "🔥🔥"),
        Achievement("sixty_minutes", "Dedicado", "Estudia 60 minutos en un día", "⏱️"),
        Achievement("three_hundred_minutes", "Estudioso", "Acumula 300 minutos totales", "📚"),
        Achievement("thousand_minutes", "Maestro", "Acumula 1000 minutos totales", "🏆")
    )

    suspend fun getAchievements(): List<Achievement> {
        val uid = auth.currentUser?.uid ?: return allAchievements
        val unlockedDocs = db.collection("users").document(uid)
            .collection("achievements").get().await()
        val unlockedIds = unlockedDocs.documents.map { it.id }.toSet()

        return allAchievements.map { achievement ->
            val doc = unlockedDocs.documents.find { it.id == achievement.id }
            achievement.copy(
                unlocked = achievement.id in unlockedIds,
                unlockedAt = doc?.getLong("unlockedAt")
            )
        }
    }

    suspend fun checkAndUnlockAchievements(
        totalSessions: Int,
        totalMinutes: Int,
        currentStreak: Int,
        minutesToday: Int
    ): List<Achievement> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        val newlyUnlocked = mutableListOf<Achievement>()

        val unlockedDocs = db.collection("users").document(uid)
            .collection("achievements").get().await()
        val unlockedIds = unlockedDocs.documents.map { it.id }.toSet()

        val toCheck = mapOf(
            "first_session" to (totalSessions >= 1),
            "five_sessions" to (totalSessions >= 5),
            "ten_sessions" to (totalSessions >= 10),
            "streak_3" to (currentStreak >= 3),
            "streak_7" to (currentStreak >= 7),
            "sixty_minutes" to (minutesToday >= 60),
            "three_hundred_minutes" to (totalMinutes >= 300),
            "thousand_minutes" to (totalMinutes >= 1000)
        )

        toCheck.forEach { (id, condition) ->
            if (condition && id !in unlockedIds) {
                db.collection("users").document(uid)
                    .collection("achievements").document(id)
                    .set(mapOf("unlockedAt" to System.currentTimeMillis())).await()
                val achievement = allAchievements.find { it.id == id }
                if (achievement != null) {
                    newlyUnlocked.add(achievement.copy(unlocked = true))
                }
            }
        }

        return newlyUnlocked
    }

    suspend fun getMinutesToday(): Int {
        val uid = auth.currentUser?.uid ?: return 0
        val today = java.util.Calendar.getInstance()
        val startOfDay = today.apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
        }.timeInMillis

        val result = db.collection("sessions")
            .whereEqualTo("uid", uid)
            .whereGreaterThan("timestamp", startOfDay)
            .get()
            .await()

        return result.documents.sumOf { (it.getLong("actualMinutes") ?: 0L).toInt() }
    }
}