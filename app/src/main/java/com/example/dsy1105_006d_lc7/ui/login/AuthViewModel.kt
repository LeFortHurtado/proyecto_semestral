package com.example.dsy1105_006d_lc7.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dsy1105_006d_lc7.data.model.AuthRepository
import com.example.dsy1105_006d_lc7.data.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel principal de Autenticación y Control de Roles (MVVM).
 * Gestiona el ciclo de vida del login y expone un StateFlow inmutable
 * que permite a la navegación y a los Composables reaccionar dinámicamente
 * al perfil activo (ADMIN o SELLER).
 */
open class AuthViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // Getter conveniente para acceso directo al estado actual
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

    /**
     * Valida las credenciales ingresadas contra la regla de negocio:
     * - "admin" / "123" -> UserRole.ADMIN
     * - "vendedor" / "123" -> UserRole.SELLER
     *
     * Asigna el rol en el StateFlow y notifica mediante el callback.
     */
    fun submit(onSuccess: (username: String, role: UserRole) -> Unit) {
        val current = _uiState.value
        val usernameTrimmed = current.username.trim()

        _uiState.update { it.copy(isLoading = true, error = null) }

        val assignedRole = repo.login(usernameTrimmed, current.password)

        if (assignedRole != null) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    role = assignedRole,
                    isAuthenticated = true,
                    error = null
                )
            }
            onSuccess(usernameTrimmed, assignedRole)
        } else {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "Credenciales Inválidas. Use admin/123 o vendedor/123"
                )
            }
        }
    }

    /**
     * Sobrecarga de conveniencia que acepta callback de un solo parámetro (username).
     */
    fun submit(onSuccess: (String) -> Unit) {
        submit { username, _ -> onSuccess(username) }
    }

    /**
     * Cierra la sesión activa y reinicia el estado.
     */
    fun logout() {
        _uiState.value = AuthUiState()
    }
}
