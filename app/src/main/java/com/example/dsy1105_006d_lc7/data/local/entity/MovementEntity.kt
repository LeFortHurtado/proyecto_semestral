package com.example.dsy1105_006d_lc7.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad que representa un movimiento de inventario.
 * Tipos de movimiento: ingreso, venta, consumo, ajuste, devolucion.
 * Cada movimiento registra la existencia anterior y resultante para trazabilidad.
 */
@Entity(
    tableName = "movements",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["productoId"]),
        Index(value = ["tipo"]),
        Index(value = ["fechaHora"])
    ]
)
data class MovementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tipo: String,                  // "ingreso", "venta", "consumo", "ajuste", "devolucion"
    val productoId: Long,              // FK al producto
    val cantidad: Int,                 // Cantidad del movimiento (siempre positiva)
    val fechaHora: Long,               // Timestamp en milisegundos
    val motivo: String = "",           // Motivo o descripción del movimiento
    val existenciaAnterior: Int,       // Stock antes del movimiento
    val existenciaResultante: Int      // Stock después del movimiento
)
