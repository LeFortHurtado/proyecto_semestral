package com.example.dsy1105_006d_lc7.data.model

/**
 * Repositorio de autenticación que valida usuarios y retorna el UserRole asociado.
 */
class AuthRepository {

    /**
     * Valida credenciales de prueba del sistema:
     * - "admin" / "123" -> UserRole.ADMIN
     * - "vendedor" / "123" -> UserRole.SELLER
     *
     * @return UserRole asignado o null si las credenciales son incorrectas.
     */
    fun login(username: String, password: String): UserRole? {
        val user = username.trim().lowercase()
        return when {
            user == Credential.Admin.username && password == Credential.Admin.password -> UserRole.ADMIN
            user == Credential.Seller.username && password == Credential.Seller.password -> UserRole.SELLER
            else -> null
        }
    }
}