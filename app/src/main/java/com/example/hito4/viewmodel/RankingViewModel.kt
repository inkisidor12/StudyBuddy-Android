package com.example.hito4.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hito4.data.repo.StudySessionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class RankingViewModel(private val repo: StudySessionRepository) : ViewModel() {

    val ranking = repo.observeSubjectRanking()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}