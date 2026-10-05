package com.example.dsy1105_006d_lc7.data.model

/**
 * Credenciales para autenticación con asignación de rol.
 */
data class Credential(
    val username: String,
    val password: String,
    val role: UserRole
) {
    companion object {
        val Admin = Credential(username = "admin", password = "123", role = UserRole.ADMIN)
        val Seller = Credential(username = "vendedor", password = "123", role = UserRole.SELLER)
    }
}