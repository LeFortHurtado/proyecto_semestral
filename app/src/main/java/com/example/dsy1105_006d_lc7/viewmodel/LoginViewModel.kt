package com.example.dsy1105_006d_lc7.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.dsy1105_006d_lc7.model.AuthUiState
import com.example.dsy1105_006d_lc7.model.Credential
import com.example.dsy1105_006d_lc7.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel para autenticación y control de roles (ADMIN / SELLER).
 * Basado en la estructura directa de Guía 11 (AndroidViewModel).
 */
class LoginViewModel(app: Application) : AndroidViewModel(app) {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val currentState: AuthUiState
        get() = _uiState.value

    val currentRole: UserRole?
        get() = _uiState.value.role

    fun onUsernameChange(value: String) {
        _uiState.update { it.copy(username = value, error = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, error = null) }
    }

    fun submit(onSuccess: (username: String, role: UserRole) -> Unit) {
        val actual = _uiState.value
        val userTrimmed = actual.username.trim()

        _uiState.update { it.copy(isLoading = true, error = null) }

        val assignedRole = Credential.validar(userTrimmed, actual.password)

        if (assignedRole != null) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    role = assignedRole,
                    isAuthenticated = true,
                    error = null
                )
            }
            onSuccess(userTrimmed, assignedRole)
        } else {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "Credenciales Inválidas. Use admin/123 o vendedor/123"
                )
            }
        }
    }

    fun submit(onSuccess: (String) -> Unit) {
        submit { user, _ -> onSuccess(user) }
    }

    fun logout() {
        _uiState.value = AuthUiState()
    }
}

// Alias para compatibilidad
typealias AuthViewModel = LoginViewModel
