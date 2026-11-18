package com.example.dsy1105_006d_lc7.model.data.local

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Producto con su Ubicación y Proveedor relacionados.
 * Usado en consultas de Room con anotación @Relation.
 */
data class ProductoConDetalles(
    @Embedded val product: ProductoEntity,
    @Relation(
        parentColumn = "ubicacionId",
        entityColumn = "id"
    )
    val location: UbicacionEntity?,
    @Relation(
        parentColumn = "proveedorId",
        entityColumn = "id"
    )
    val supplier: ProveedorEntity?
)

typealias ProductWithDetails = ProductoConDetalles

/**
 * Movimiento con su Producto asociado.
 */
data class MovimientoConProducto(
    @Embedded val movement: MovimientoEntity,
    @Relation(
        parentColumn = "productoId",
        entityColumn = "id"
    )
    val product: ProductoEntity
)

typealias MovementWithProduct = MovimientoConProducto
