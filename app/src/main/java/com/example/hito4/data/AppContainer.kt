package com.example.hito4.data


import android.content.Context
import com.example.hito4.data.bd.DatabaseProvider
import com.example.hito4.data.repo.AuthRepository
import com.example.hito4.data.repo.StudySessionRepository
import com.example.hito4.data.repo.SubjectRepository
import com.example.hito4.data.repo.UserRepository

class AppContainer(context: Context) {
    private val db = DatabaseProvider.get(context)

    val subjectRepository: SubjectRepository = SubjectRepository(db.subjectDao())
    val studySessionRepository: StudySessionRepository = StudySessionRepository(db.studySessionDao())
    val userPreferences: UserPreferences = UserPreferences(context)
    val authRepository: AuthRepository = AuthRepository()
    val userRepository: UserRepository = UserRepository()



}
