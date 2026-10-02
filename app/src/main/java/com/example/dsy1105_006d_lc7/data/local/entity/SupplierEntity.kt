package com.example.dsy1105_006d_lc7.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa un proveedor ficticio del taller.
 * Los proveedores no tienen cuentas de acceso reales.
 * Se usan datos 100% ficticios por restricciones de privacidad.
 */
@Entity(tableName = "suppliers")
data class SupplierEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombreFicticio: String,    // Nombre ficticio del proveedor
    val correoFicticio: String,    // Email ficticio para generar borradores de correo
    val telefonoFicticio: String = "", // Teléfono ficticio (opcional)
    val direccionFicticia: String = "" // Dirección ficticia (opcional)
)
