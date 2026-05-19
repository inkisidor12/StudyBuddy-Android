package com.example.hito4.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.hito4.data.repo.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val nickname: String = "",
    val currentStreak: Int = 0,
    val totalMinutes: Int = 0
)

class HomeViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(HomeUiState())
    val ui: StateFlow<HomeUiState> = _ui.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            val profile = userRepository.getCurrentUserProfile()
            val streak = userRepository.getCurrentStreak()
            _ui.update {
                it.copy(
                    nickname = profile?.nickname ?: "",
                    currentStreak = streak,
                    totalMinutes = profile?.totalMinutes ?: 0
                )
            }
        }
    }
}

class HomeViewModelFactory(
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}