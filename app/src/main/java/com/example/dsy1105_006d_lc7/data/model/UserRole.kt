package com.example.dsy1105_006d_lc7.data.model

/**
 * Roles de usuario admitidos en el sistema:
 * - ADMIN: Administrador / Propietario (acceso total a funciones y datos sensibles).
 * - SELLER: Vendedor / Operador (acceso restringido a consultas, ventas e ingresos).
 */
enum class UserRole {
    ADMIN,
    SELLER
}
