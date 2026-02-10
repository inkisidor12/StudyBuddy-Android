package com.example.hito4.data


import android.content.Context
import com.example.hito4.data.bd.DatabaseProvider
import com.example.hito4.data.repo.StudySessionRepository
import com.example.hito4.data.repo.SubjectRepository

class AppContainer(context: Context) {
    private val db = DatabaseProvider.get(context)

    val subjectRepository: SubjectRepository = SubjectRepository(db.subjectDao())
    val studySessionRepository: StudySessionRepository = StudySessionRepository(db.studySessionDao())
}
