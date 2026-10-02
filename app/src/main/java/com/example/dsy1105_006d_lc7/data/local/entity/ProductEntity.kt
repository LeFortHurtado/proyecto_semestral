package com.example.dsy1105_006d_lc7.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad que representa un producto del inventario del taller mecánico.
 * Incluye relaciones con Ubicación (Location) y Proveedor (Supplier).
 * El campo estado indica si el producto está activo o descontinuado.
 */
@Entity(
    tableName = "products",
    foreignKeys = [
        ForeignKey(
            entity = LocationEntity::class,
            parentColumns = ["id"],
            childColumns = ["ubicacionId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = SupplierEntity::class,
            parentColumns = ["id"],
            childColumns = ["proveedorId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["codigoInterno"], unique = true),
        Index(value = ["ubicacionId"]),
        Index(value = ["proveedorId"])
    ]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val codigoInterno: String,         // Código interno del producto (único)
    val nombre: String,                 // Nombre del producto
    val descripcion: String = "",       // Descripción detallada
    val categoria: String,              // Categoría: "Filtros", "Aceites", "Frenos", etc.
    val cantidadDisponible: Int = 0,    // Stock actual
    val existenciaMinima: Int = 1,      // Stock mínimo antes de alertar reposición
    val precioCompra: Double = 0.0,     // Precio de compra al proveedor
    val precioVenta: Double = 0.0,      // Precio de venta al cliente
    val estado: String = "activo",      // "activo" o "descontinuado"
    val proveedorId: Long? = null,      // FK al proveedor (nullable)
    val ubicacionId: Long? = null       // FK a la ubicación en bodega (nullable)
)
