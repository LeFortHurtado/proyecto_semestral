package com.example.dsy1105_006d_lc7.model

/**
 * Roles de usuario admitidos en el sistema:
 * - ADMIN: Administrador / Propietario (acceso total a funciones y datos sensibles).
 * - SELLER: Vendedor / Operador (acceso restringido a consultas, ventas e ingresos).
 */
enum class UserRole {
    ADMIN,
    SELLER
}

// Alias en español para mayor claridad en presentaciones
typealias UsuarioRol = UserRole

/**
 * Credenciales de acceso de prueba para la evaluación académica.
 */
data class Credential(
    val username: String,
    val password: String,
    val role: UserRole
) {
    companion object {
        val Admin = Credential(username = "admin", password = "123", role = UserRole.ADMIN)
        val Seller = Credential(username = "vendedor", password = "123", role = UserRole.SELLER)

        fun validar(usuario: String, clave: String): UserRole? {
            val user = usuario.trim().lowercase()
            return when {
                user == Admin.username && clave == Admin.password -> UserRole.ADMIN
                user == Seller.username && clave == Seller.password -> UserRole.SELLER
                else -> null
            }
        }
    }
}

typealias Credencial = Credential
