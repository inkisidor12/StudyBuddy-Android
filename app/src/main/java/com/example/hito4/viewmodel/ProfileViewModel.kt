package com.example.hito4.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.hito4.data.repo.UserProfile
import com.example.hito4.data.repo.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val profile: UserProfile? = null,
    val totalSessions: Int = 0,
    val currentStreak: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

class ProfileViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(ProfileUiState())
    val ui: StateFlow<ProfileUiState> = _ui.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true) }
            val profile = userRepository.getCurrentUserProfile()
            val totalSessions = userRepository.getTotalSessions()
            val streak = userRepository.getCurrentStreak()
            _ui.update {
                it.copy(
                    profile = profile,
                    totalSessions = totalSessions,
                    currentStreak = streak,
                    isLoading = false
                )
            }
        }
    }

    fun updateProfile(fullName: String, nickname: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true, error = null) }
            val nicknameClean = nickname.trim().lowercase()

            // Si cambió el nickname comprobamos disponibilidad
            if (nicknameClean != _ui.value.profile?.nickname) {
                val available = userRepository.isNicknameAvailable(nicknameClean)
                if (!available) {
                    _ui.update { it.copy(isLoading = false, error = "Ese nickname ya está en uso") }
                    return@launch
                }
            }

            userRepository.updateProfile(fullName, nickname)
            loadProfile()
            _ui.update { it.copy(isLoading = false) }
            onSuccess()
        }
    }
}

class ProfileViewModelFactory(
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}