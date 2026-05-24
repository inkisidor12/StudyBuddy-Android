package com.example.hito4.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.hito4.data.dao.SubjectRankingRow
import com.example.hito4.data.repo.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RankingViewModel(private val userRepo: UserRepository) : ViewModel() {

    private val _ranking = MutableStateFlow<List<SubjectRankingRow>>(emptyList())
    val ranking: StateFlow<List<SubjectRankingRow>> = _ranking.asStateFlow()

    init {
        loadRanking()
    }

    private fun loadRanking() {
        viewModelScope.launch {
            _ranking.value = userRepo.getRankingFromFirestore()
        }
    }
}

class RankingViewModelFactory(
    private val userRepo: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RankingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RankingViewModel(userRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}