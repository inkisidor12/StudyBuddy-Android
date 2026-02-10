package com.example.hito4.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.hito4.data.repo.StudySessionRepository
import com.example.hito4.data.repo.SubjectRepository

class FocusViewModelV2Factory(
    private val subjectRepo: SubjectRepository,
    private val sessionRepo: StudySessionRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FocusViewModelV2::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FocusViewModelV2(subjectRepo, sessionRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
