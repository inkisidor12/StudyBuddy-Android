package com.example.hito4.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.hito4.data.dao.SessionWithSubjectName
import com.example.hito4.data.repo.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StatsUiState(
    val totalMinutes: Int = 0,
    val totalSessions: Int = 0,
    val sessions: List<SessionWithSubjectName> = emptyList(),
    val isLoading: Boolean = false
)

class StatsViewModel(private val userRepo: UserRepository) : ViewModel() {

    private val _ui = MutableStateFlow(StatsUiState())
    val ui: StateFlow<StatsUiState> = _ui.asStateFlow()

    init {
        loadStats()
    }

    fun refresh() {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            _ui.value = StatsUiState(isLoading = true)
            val sessions = userRepo.getSessionsFromFirestore()
            val totalMinutes = sessions.sumOf { it.actualMinutes }
            _ui.value = StatsUiState(
                totalMinutes = totalMinutes,
                totalSessions = sessions.size,
                sessions = sessions,
                isLoading = false
            )
        }
    }
}

class StatsViewModelFactory(
    private val userRepo: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StatsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StatsViewModel(userRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}