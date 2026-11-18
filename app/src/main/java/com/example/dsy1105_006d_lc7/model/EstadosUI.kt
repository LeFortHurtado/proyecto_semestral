package com.example.dsy1105_006d_lc7.model

import com.example.dsy1105_006d_lc7.model.data.local.*

/**
 * Estado para la pantalla de inicio de sesión.
 */
data class AuthUiState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val role: UserRole? = null,
    val isAuthenticated: Boolean = false
)
typealias LoginUiState = AuthUiState

/**
 * Estado para la pantalla del Menú Principal (Dashboard).
 */
data class MenuPrincipalUiState(
    val totalProducts: Int = 0,
    val lowStockCount: Int = 0,
    val totalCategories: Int = 0,
    val totalSuppliers: Int = 0,
    val recentMovements: List<MovimientoConProducto> = emptyList()
)
typealias DashboardUiState = MenuPrincipalUiState

/**
 * Estado para la lista de productos.
 */
data class ProductosUiState(
    val products: List<ProductoConDetalles> = emptyList(),
    val categories: List<String> = emptyList(),
    val locations: List<UbicacionEntity> = emptyList(),
    val suppliers: List<ProveedorEntity> = emptyList(),
    val selectedCategory: String? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val message: String? = null
)
typealias ProductsUiState = ProductosUiState

/**
 * Estado para el formulario de producto (crear o editar).
 */
data class ProductoFormState(
    val id: Long = 0,
    val codigoInterno: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val categoria: String = "",
    val cantidadDisponible: String = "0",
    val existenciaMinima: String = "1",
    val precioCompra: String = "0",
    val precioVenta: String = "0",
    val estado: String = "activo",
    val proveedorId: Long? = null,
    val ubicacionId: Long? = null,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null
)
typealias ProductFormState = ProductoFormState

/**
 * Estado para la pantalla de movimientos.
 */
data class MovimientosUiState(
    val movements: List<MovimientoConProducto> = emptyList(),
    val products: List<ProductoEntity> = emptyList(),
    val isLoading: Boolean = true,
    val message: String? = null,
    val filterType: String? = null // null = todos
)
typealias MovementsUiState = MovimientosUiState

/**
 * Estado para el formulario de movimiento.
 */
data class MovimientoFormState(
    val id: Long = 0,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val selectedProduct: ProductoEntity? = null,
    val selectedProductId: Long? = null,
    val selectedProductName: String = "",
    val tipo: String = "ingreso",
    val cantidad: String = "",
    val motivo: String = "",
    val stockActual: Int = 0,
    val error: String? = null,
    val scanSuccessMessage: String? = null
)
typealias MovementFormState = MovimientoFormState

/**
 * Estado para la pantalla de ubicaciones.
 */
data class UbicacionesUiState(
    val locations: List<UbicacionEntity> = emptyList(),
    val isLoading: Boolean = true,
    val message: String? = null
)
typealias LocationsUiState = UbicacionesUiState

/**
 * Estado para el formulario de ubicación.
 */
data class UbicacionFormState(
    val id: Long = 0,
    val zona: String = "",
    val estante: String = "",
    val repisa: String = "",
    val posicion: String = "",
    val isEditing: Boolean = false,
    val error: String? = null
)
typealias LocationFormState = UbicacionFormState

/**
 * Estado para la pantalla de proveedores.
 */
data class ProveedoresUiState(
    val suppliers: List<ProveedorEntity> = emptyList(),
    val isLoading: Boolean = true,
    val message: String? = null
)
typealias SuppliersUiState = ProveedoresUiState

/**
 * Estado para el formulario de proveedor.
 */
data class ProveedorFormState(
    val id: Long = 0,
    val nombreFicticio: String = "",
    val correoFicticio: String = "",
    val telefonoFicticio: String = "",
    val direccionFicticia: String = "",
    val isEditing: Boolean = false,
    val error: String? = null
)
typealias SupplierFormState = ProveedorFormState

/**
 * Modelos para la reposición de inventario agrupada por proveedor.
 */
data class RepositionItem(
    val product: ProductoConDetalles,
    val cantidadSugerida: Int
)

data class SupplierRepositionGroup(
    val supplier: ProveedorEntity?,
    val items: List<RepositionItem>
)

data class ReposicionUiState(
    val groups: List<SupplierRepositionGroup> = emptyList(),
    val isLoading: Boolean = true,
    val message: String? = null
)
typealias RepositionUiState = ReposicionUiState
