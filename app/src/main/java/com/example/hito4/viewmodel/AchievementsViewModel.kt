package com.example.hito4.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.hito4.data.repo.Achievement
import com.example.hito4.data.repo.AchievementsRepository
import com.example.hito4.data.repo.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AchievementsUiState(
    val achievements: List<Achievement> = emptyList(),
    val newlyUnlocked: List<Achievement> = emptyList(),
    val isLoading: Boolean = false
)

class AchievementsViewModel(
    private val achievementsRepository: AchievementsRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(AchievementsUiState())
    val ui: StateFlow<AchievementsUiState> = _ui.asStateFlow()

    init {
        loadAchievements()
        checkAchievements()
    }

    fun loadAchievements() {
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true) }
            val achievements = achievementsRepository.getAchievements()
            _ui.update { it.copy(achievements = achievements, isLoading = false) }
        }
    }

    fun checkAchievements() {
        viewModelScope.launch {
            val totalSessions = userRepository.getTotalSessions()
            val profile = userRepository.getCurrentUserProfile()
            val totalMinutes = profile?.totalMinutes ?: 0
            val streak = userRepository.getCurrentStreak()
            val minutesToday = achievementsRepository.getMinutesToday()

            val newlyUnlocked = achievementsRepository.checkAndUnlockAchievements(
                totalSessions = totalSessions,
                totalMinutes = totalMinutes,
                currentStreak = streak,
                minutesToday = minutesToday
            )

            if (newlyUnlocked.isNotEmpty()) {
                _ui.update { it.copy(newlyUnlocked = newlyUnlocked) }
                loadAchievements()
            }
        }
    }

    fun clearNewlyUnlocked() {
        _ui.update { it.copy(newlyUnlocked = emptyList()) }
    }
}

class AchievementsViewModelFactory(
    private val achievementsRepository: AchievementsRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AchievementsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AchievementsViewModel(achievementsRepository, userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}