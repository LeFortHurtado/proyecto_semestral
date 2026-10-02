package com.example.dsy1105_006d_lc7.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Relación Product con su Location y Supplier.
 * Se usa para consultas que necesitan los datos completos del producto
 * junto a su ubicación y proveedor.
 */
data class ProductWithDetails(
    @Embedded val product: ProductEntity,
    @Relation(
        parentColumn = "ubicacionId",
        entityColumn = "id"
    )
    val location: LocationEntity?,
    @Relation(
        parentColumn = "proveedorId",
        entityColumn = "id"
    )
    val supplier: SupplierEntity?
)

/**
 * Relación Movimiento con su Producto asociado.
 */
data class MovementWithProduct(
    @Embedded val movement: MovementEntity,
    @Relation(
        parentColumn = "productoId",
        entityColumn = "id"
    )
    val product: ProductEntity
)
