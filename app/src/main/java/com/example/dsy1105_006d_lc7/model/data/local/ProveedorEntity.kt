package com.example.dsy1105_006d_lc7.model.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa un proveedor ficticio del taller mecánico.
 * Los datos son ficticios con fines académicos.
 */
@Entity(tableName = "suppliers")
data class ProveedorEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombreFicticio: String,        // Nombre del proveedor
    val correoFicticio: String,        // Email para generar pedidos de reposición
    val telefonoFicticio: String = "", // Teléfono de contacto
    val direccionFicticia: String = "" // Dirección del proveedor
)

typealias SupplierEntity = ProveedorEntity
