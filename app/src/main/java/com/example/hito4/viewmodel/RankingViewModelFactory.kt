package com.example.hito4.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.hito4.data.repo.StudySessionRepository

class RankingViewModelFactory(
    private val repo: StudySessionRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RankingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RankingViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}