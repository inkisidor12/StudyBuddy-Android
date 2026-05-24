package com.example.hito4.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.hito4.data.UserPreferences
import com.example.hito4.data.repo.AuthRepository
import com.example.hito4.data.repo.StudySessionRepository
import com.example.hito4.data.repo.SubjectRepository
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
    private val userPreferences: UserPreferences,
    private val subjectRepository: SubjectRepository,
    private val sessionRepository: StudySessionRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(LoginUiState())
    val ui: StateFlow<LoginUiState> = _ui.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _ui.value = LoginUiState(isLoading = true)
            val result = authRepository.login(email, password)
            if (result.isSuccess) {
                userPreferences.saveUsername(result.getOrNull()?.email ?: "")
                // Sincronizar asignaturas desde Firestore si Room está vacío
                subjectRepository.syncFromFirestore()
                sessionRepository.syncFromFirestore()
                _ui.value = LoginUiState(isSuccess = true)
            } else {
                _ui.value = LoginUiState(
                    error = when {
                        result.exceptionOrNull()?.message?.contains("credential") == true -> "Email o contraseña incorrectos"
                        result.exceptionOrNull()?.message?.contains("network") == true -> "Error de conexión, comprueba tu internet"
                        else -> "Error al iniciar sesión, inténtalo de nuevo"
                    }
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
    private val userPreferences: UserPreferences,
    private val subjectRepository: SubjectRepository,
    private val sessionRepository: StudySessionRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(authRepository, userPreferences, subjectRepository, sessionRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}