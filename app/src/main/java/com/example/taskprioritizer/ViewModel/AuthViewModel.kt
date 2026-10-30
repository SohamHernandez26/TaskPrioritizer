package com.example.taskprioritizer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.util.copy
import com.example.taskprioritizer.repository.AuthRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    init {
        viewModelScope.launch {
            repository.currentUserId.collect { uid ->
                _uiState.value = _uiState.value.copy(
                    isAuthenticated = uid != null
                )
            }
        }
    }

    fun signIn(email: String, password: String) {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                val result = repository.signIn(email, password)
                result.onFailure {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "No se pudo iniciar sesión."
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "No se pudo iniciar sesión."
                )
            } finally {
                _uiState.value = _uiState.value.copy(
                    isLoading = false
                )
            }
        }
    }

    fun signUp(email: String, password: String) {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                val result = repository.signUp(email, password)
                result.onFailure {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "No se pudo registrar la cuenta."
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "No se pudo registrar la cuenta."
                )
            } finally {
                _uiState.value = _uiState.value.copy(
                    isLoading = false
                )
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            repository.signOut()
        }
    }
}
