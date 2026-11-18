package com.example.dsy1105_006d_lc7.model.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad que representa un movimiento de inventario.
 * Tipos: ingreso, venta, consumo, ajuste, devolucion.
 * Registra existencias anterior y resultante para trazabilidad completa.
 */
@Entity(
    tableName = "movements",
    foreignKeys = [
        ForeignKey(
            entity = ProductoEntity::class,
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
data class MovimientoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tipo: String,                  // "ingreso", "venta", "consumo", "ajuste", "devolucion"
    val productoId: Long,              // FK al producto
    val cantidad: Int,                 // Cantidad del movimiento (positiva)
    val fechaHora: Long,               // Timestamp en milisegundos
    val motivo: String = "",           // Motivo o detalle del movimiento
    val existenciaAnterior: Int,       // Stock antes del movimiento
    val existenciaResultante: Int      // Stock resultante después del movimiento
)

typealias MovementEntity = MovimientoEntity
