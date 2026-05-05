package com.example.hito4.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.hito4.data.UserPreferences
import com.example.hito4.data.repo.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _ui = MutableStateFlow(LoginUiState())
    val ui: StateFlow<LoginUiState> = _ui.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _ui.value = LoginUiState(isLoading = true)
            val result = authRepository.login(email, password)
            if (result.isSuccess) {
                userPreferences.saveUsername(result.getOrNull()?.email ?: "")
                _ui.value = LoginUiState(isSuccess = true)
            } else {
                _ui.value = LoginUiState(
                    error = result.exceptionOrNull()?.message ?: "Error al iniciar sesión"
                )
            }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _ui.value = LoginUiState(isLoading = true)
            val result = authRepository.register(email, password)
            if (result.isSuccess) {
                userPreferences.saveUsername(result.getOrNull()?.email ?: "")
                _ui.value = LoginUiState(isSuccess = true)
            } else {
                _ui.value = LoginUiState(
                    error = result.exceptionOrNull()?.message ?: "Error al registrarse"
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            userPreferences.clearUsername()
        }
    }

    fun clearError() {
        _ui.update { it.copy(error = null) }
    }
}

class LoginViewModelFactory(
    private val authRepository: AuthRepository,
    private val userPreferences: UserPreferences
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(authRepository, userPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}