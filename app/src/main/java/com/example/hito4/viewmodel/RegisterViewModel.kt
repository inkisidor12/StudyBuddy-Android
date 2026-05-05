package com.example.hito4.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.hito4.data.repo.AuthRepository
import com.example.hito4.data.repo.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class RegisterStep { PERSONAL, ACCOUNT, PASSWORD }

data class RegisterUiState(
    val step: RegisterStep = RegisterStep.PERSONAL,
    // Paso 1
    val fullName: String = "",
    val birthDate: String = "",
    val phone: String = "",
    // Paso 2
    val email: String = "",
    val nickname: String = "",
    val nicknameAvailable: Boolean? = null,
    val checkingNickname: Boolean = false,
    // Paso 3
    val password: String = "",
    val confirmPassword: String = "",
    val acceptedTerms: Boolean = false,
    // General
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class RegisterViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(RegisterUiState())
    val ui: StateFlow<RegisterUiState> = _ui.asStateFlow()

    // Paso 1
    fun onFullNameChange(value: String) = _ui.update { it.copy(fullName = value) }
    fun onBirthDateChange(value: String) = _ui.update { it.copy(birthDate = value) }
    fun onPhoneChange(value: String) = _ui.update { it.copy(phone = value) }

    // Paso 2
    fun onEmailChange(value: String) = _ui.update { it.copy(email = value) }
    fun onNicknameChange(value: String) {
        _ui.update { it.copy(nickname = value, nicknameAvailable = null) }
    }

    fun checkNickname() {
        val nickname = _ui.value.nickname.trim()
        if (nickname.length < 3) {
            _ui.update { it.copy(nicknameAvailable = false) }
            return
        }
        viewModelScope.launch {
            _ui.update { it.copy(checkingNickname = true) }
            val available = userRepository.isNicknameAvailable(nickname)
            _ui.update { it.copy(nicknameAvailable = available, checkingNickname = false) }
        }
    }

    // Paso 3
    fun onPasswordChange(value: String) = _ui.update { it.copy(password = value) }
    fun onConfirmPasswordChange(value: String) = _ui.update { it.copy(confirmPassword = value) }
    fun onAcceptedTermsChange(value: Boolean) = _ui.update { it.copy(acceptedTerms = value) }

    // Navegación entre pasos
    fun nextStep() {
        val current = _ui.value
        when (current.step) {
            RegisterStep.PERSONAL -> {
                if (!validateStep1()) return
                _ui.update { it.copy(step = RegisterStep.ACCOUNT, error = null) }
            }
            RegisterStep.ACCOUNT -> {
                if (!validateStep2()) return
                _ui.update { it.copy(step = RegisterStep.PASSWORD, error = null) }
            }
            RegisterStep.PASSWORD -> register()
        }
    }

    fun prevStep() {
        val current = _ui.value
        when (current.step) {
            RegisterStep.ACCOUNT -> _ui.update { it.copy(step = RegisterStep.PERSONAL, error = null) }
            RegisterStep.PASSWORD -> _ui.update { it.copy(step = RegisterStep.ACCOUNT, error = null) }
            else -> {}
        }
    }

    private fun validateStep1(): Boolean {
        val s = _ui.value
        return when {
            s.fullName.isBlank() -> {
                _ui.update { it.copy(error = "El nombre es obligatorio") }
                false
            }
            s.birthDate.isBlank() -> {
                _ui.update { it.copy(error = "La fecha de nacimiento es obligatoria") }
                false
            }
            !isOldEnough(s.birthDate) -> {
                _ui.update { it.copy(error = "Debes tener al menos 13 años") }
                false
            }
            s.phone.isBlank() -> {
                _ui.update { it.copy(error = "El teléfono es obligatorio") }
                false
            }
            else -> true
        }
    }

    private fun validateStep2(): Boolean {
        val s = _ui.value
        return when {
            s.email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(s.email).matches() -> {
                _ui.update { it.copy(error = "Email no válido") }
                false
            }
            s.nickname.isBlank() || s.nickname.length < 3 -> {
                _ui.update { it.copy(error = "El nickname debe tener al menos 3 caracteres") }
                false
            }
            s.nicknameAvailable != true -> {
                _ui.update { it.copy(error = "Comprueba que el nickname está disponible") }
                false
            }
            else -> true
        }
    }

    private fun validateStep3(): Boolean {
        val s = _ui.value
        return when {
            s.password.length < 6 -> {
                _ui.update { it.copy(error = "La contraseña debe tener al menos 6 caracteres") }
                false
            }
            s.password != s.confirmPassword -> {
                _ui.update { it.copy(error = "Las contraseñas no coinciden") }
                false
            }
            !s.acceptedTerms -> {
                _ui.update { it.copy(error = "Debes aceptar los términos y condiciones") }
                false
            }
            else -> true
        }
    }

    private fun isOldEnough(birthDate: String): Boolean {
        return try {
            val parts = birthDate.split("/")
            if (parts.size != 3) return false
            val day = parts[0].toInt()
            val month = parts[1].toInt()
            val year = parts[2].toInt()
            val today = java.util.Calendar.getInstance()
            val age = today.get(java.util.Calendar.YEAR) - year -
                    if (today.get(java.util.Calendar.MONTH) + 1 < month ||
                        (today.get(java.util.Calendar.MONTH) + 1 == month &&
                                today.get(java.util.Calendar.DAY_OF_MONTH) < day)) 1 else 0
            age >= 13
        } catch (e: Exception) {
            false
        }
    }

    fun passwordStrength(password: String): Int {
        var score = 0
        if (password.length >= 8) score++
        if (password.any { it.isUpperCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++
        return score // 0-4
    }

    private fun register() {
        if (!validateStep3()) return
        val s = _ui.value
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true, error = null) }
            val result = authRepository.register(s.email, s.password)
            if (result.isSuccess) {
                userRepository.createUserProfile(
                    fullName = s.fullName,
                    nickname = s.nickname,
                    phone = s.phone,
                    birthDate = s.birthDate
                )
                _ui.update { it.copy(isLoading = false, isSuccess = true) }
            } else {
                _ui.update {
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Error al registrarse"
                    )
                }
            }
        }
    }

    fun clearError() = _ui.update { it.copy(error = null) }
}

class RegisterViewModelFactory(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RegisterViewModel(authRepository, userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}