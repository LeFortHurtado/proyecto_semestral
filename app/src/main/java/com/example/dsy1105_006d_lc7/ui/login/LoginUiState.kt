package com.example.dsy1105_006d_lc7.ui.login

import com.example.dsy1105_006d_lc7.data.model.UserRole

/**
 * Estado inmutable de autenticación expuesto como StateFlow.
 * Almacena credenciales en edición, estado de carga, errores y el [UserRole] activo.
 */
data class AuthUiState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val role: UserRole? = null,
    val isAuthenticated: Boolean = false
)

// Retrocompatibilidad con la nomenclatura anterior
typealias LoginUiState = AuthUiState