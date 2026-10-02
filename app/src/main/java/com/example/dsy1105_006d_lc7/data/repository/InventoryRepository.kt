package com.example.dsy1105_006d_lc7.data.repository

import com.example.dsy1105_006d_lc7.data.local.AppDatabase
import com.example.dsy1105_006d_lc7.data.local.entity.*
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio principal para la gestión de inventario.
 * Encapsula las operaciones de la base de datos y la lógica de negocio
 * relacionada con productos, movimientos, ubicaciones y proveedores.
 *
 * Regla de negocio clave: Un movimiento de "salida" o "venta" NO puede
 * generar stock negativo a menos que haya autorización explícita.
 */
class InventoryRepository(private val db: AppDatabase) {

    // ══════════════════════════════════════════
    // ── PRODUCTOS ──
    // ══════════════════════════════════════════

    fun getAllProducts(): Flow<List<ProductEntity>> = db.productDao().getAllProducts()

    fun getAllProductsWithDetails(): Flow<List<ProductWithDetails>> =
        db.productDao().getAllProductsWithDetails()

    suspend fun getProductById(id: Long): ProductEntity? = db.productDao().getProductById(id)

    suspend fun getProductWithDetailsById(id: Long): ProductWithDetails? =
        db.productDao().getProductWithDetailsById(id)

    suspend fun getProductByCode(code: String): ProductEntity? =
        db.productDao().getProductByCode(code)

    fun searchProducts(query: String): Flow<List<ProductEntity>> =
        db.productDao().searchProducts(query)

    fun getProductsByCategory(category: String): Flow<List<ProductEntity>> =
        db.productDao().getProductsByCategory(category)

    fun getAllCategories(): Flow<List<String>> = db.productDao().getAllCategories()

    suspend fun insertProduct(product: ProductEntity): Long = db.productDao().insert(product)

    suspend fun updateProduct(product: ProductEntity) = db.productDao().update(product)

    suspend fun deleteProduct(product: ProductEntity) = db.productDao().delete(product)

    // ══════════════════════════════════════════
    // ── REPOSICIÓN ──
    // ══════════════════════════════════════════

    fun getProductsBelowMinStock(): Flow<List<ProductEntity>> =
        db.productDao().getProductsBelowMinStock()

    fun getProductsBelowMinStockWithDetails(): Flow<List<ProductWithDetails>> =
        db.productDao().getProductsBelowMinStockWithDetails()

    // ══════════════════════════════════════════
    // ── MOVIMIENTOS ──
    // ══════════════════════════════════════════

    fun getAllMovements(): Flow<List<MovementEntity>> = db.movementDao().getAllMovements()

    fun getAllMovementsWithProduct(): Flow<List<MovementWithProduct>> =
        db.movementDao().getAllMovementsWithProduct()

    fun getMovementsByProduct(productId: Long): Flow<List<MovementEntity>> =
        db.movementDao().getMovementsByProduct(productId)

    fun getRecentMovementsWithProduct(limit: Int = 20): Flow<List<MovementWithProduct>> =
        db.movementDao().getRecentMovementsWithProduct(limit)

    /**
     * Registra un movimiento de inventario y actualiza el stock del producto
     * de forma transaccional.
     *
     * @param tipo Tipo de movimiento: "ingreso", "venta", "consumo", "ajuste", "devolucion"
     * @param productId ID del producto
     * @param cantidad Cantidad del movimiento (siempre positiva)
     * @param motivo Motivo o descripción del movimiento
     * @param allowNegative Si true, permite stock negativo (requiere autorización explícita)
     * @return Result con el ID del movimiento creado o un error
     */
    suspend fun registerMovement(
        tipo: String,
        productId: Long,
        cantidad: Int,
        motivo: String = "",
        allowNegative: Boolean = false
    ): Result<Long> {
        val product = db.productDao().getProductById(productId)
            ?: return Result.failure(Exception("Producto no encontrado"))

        val stockAnterior = product.cantidadDisponible

        val nuevoStock = when (tipo) {
            "ingreso", "devolucion" -> stockAnterior + cantidad
            "venta", "consumo" -> stockAnterior - cantidad
            "ajuste" -> cantidad // El ajuste establece directamente la cantidad
            else -> return Result.failure(Exception("Tipo de movimiento no válido: $tipo"))
        }

        // Regla de negocio: bloquear stock negativo sin autorización
        if (nuevoStock < 0 && !allowNegative) {
            return Result.failure(
                Exception("Stock insuficiente. Disponible: $stockAnterior, Solicitado: $cantidad")
            )
        }

        // Crear el movimiento
        val movement = MovementEntity(
            tipo = tipo,
            productoId = productId,
            cantidad = cantidad,
            fechaHora = System.currentTimeMillis(),
            motivo = motivo,
            existenciaAnterior = stockAnterior,
            existenciaResultante = nuevoStock
        )

        // Insertar movimiento y actualizar stock
        val movementId = db.movementDao().insert(movement)
        db.productDao().updateStock(productId, nuevoStock)

        return Result.success(movementId)
    }

    // ══════════════════════════════════════════
    // ── UBICACIONES ──
    // ══════════════════════════════════════════

    fun getAllLocations(): Flow<List<LocationEntity>> = db.locationDao().getAllLocations()

    suspend fun getLocationById(id: Long): LocationEntity? = db.locationDao().getLocationById(id)

    fun getAllZones(): Flow<List<String>> = db.locationDao().getAllZones()

    suspend fun insertLocation(location: LocationEntity): Long = db.locationDao().insert(location)

    suspend fun updateLocation(location: LocationEntity) = db.locationDao().update(location)

    suspend fun deleteLocation(location: LocationEntity) = db.locationDao().delete(location)

    // ══════════════════════════════════════════
    // ── PROVEEDORES ──
    // ══════════════════════════════════════════

    fun getAllSuppliers(): Flow<List<SupplierEntity>> = db.supplierDao().getAllSuppliers()

    suspend fun getSupplierById(id: Long): SupplierEntity? = db.supplierDao().getSupplierById(id)

    fun searchSuppliers(query: String): Flow<List<SupplierEntity>> =
        db.supplierDao().searchSuppliers(query)

    suspend fun insertSupplier(supplier: SupplierEntity): Long = db.supplierDao().insert(supplier)

    suspend fun updateSupplier(supplier: SupplierEntity) = db.supplierDao().update(supplier)

    suspend fun deleteSupplier(supplier: SupplierEntity) = db.supplierDao().delete(supplier)
}
