package com.example.taskprioritizer.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskprioritizer.repository.AuthRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val emailPattern = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

data class AuthUiState(
    val checkingSession: Boolean = true,
    val userId: String? = null,
    val email: String = "",
    val password: String = "",
    val confirmation: String = "",
    val isRegistering: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null
) {
    val emailError: String?
        get() = if (emailPattern.matches(email.trim())) null else "Ingresa un correo válido."
    val passwordError: String?
        get() = if (password.length >= 6) null else "La contraseña debe tener al menos 6 caracteres."
    val confirmationError: String?
        get() = if (!isRegistering || password == confirmation) null else "Las contraseñas no coinciden."
    val isValid: Boolean
        get() = emailError == null && passwordError == null && confirmationError == null
}

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.currentUserId.catch {
                _uiState.update { it.copy(checkingSession = false, error = "No se pudo recuperar la sesión.") }
            }.collect { userId ->
                _uiState.update {
                    it.copy(
                        checkingSession = false,
                        userId = userId,
                        isSubmitting = false,
                        password = "",
                        confirmation = "",
                        error = null
                    )
                }
            }
        }
    }

    fun onEmailChange(value: String) { _uiState.update { it.copy(email = value, error = null) } }
    fun onPasswordChange(value: String) { _uiState.update { it.copy(password = value, error = null) } }
    fun onConfirmationChange(value: String) { _uiState.update { it.copy(confirmation = value, error = null) } }
    fun onModeChange() {
        if (_uiState.value.isSubmitting) return
        _uiState.update { it.copy(isRegistering = !it.isRegistering, password = "", confirmation = "", error = null) }
    }

    fun onSubmit() {
        val state = _uiState.value
        if (!state.isValid || state.isSubmitting) return
        _uiState.update { it.copy(isSubmitting = true, error = null) }
        viewModelScope.launch {
            val result = if (state.isRegistering) {
                repository.signUp(state.email, state.password)
            } else {
                repository.signIn(state.email, state.password)
            }
            _uiState.update { it.copy(isSubmitting = false, error = result.exceptionOrNull()?.message) }
        }
    }

    fun onSignOut() {
        viewModelScope.launch {
            try {
                repository.signOut()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update { it.copy(error = "No se pudo cerrar la sesión. Intenta nuevamente.") }
            }
        }
    }
}
