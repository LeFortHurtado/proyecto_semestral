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

    suspend fun getMovementById(id: Long): MovementEntity? = db.movementDao().getMovementById(id)

    suspend fun getMovementWithProductById(id: Long): MovementWithProduct? =
        db.movementDao().getMovementWithProductById(id)

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

    /**
     * Actualiza un movimiento de inventario existente y ajusta correspondientemente
     * el stock disponible del producto afectado (o de ambos si se cambia de producto).
     */
    suspend fun updateMovement(
        movementId: Long,
        newTipo: String,
        newProductId: Long,
        newCantidad: Int,
        newMotivo: String = "",
        allowNegative: Boolean = false
    ): Result<Unit> {
        val oldMovement = db.movementDao().getMovementById(movementId)
            ?: return Result.failure(Exception("Movimiento no encontrado (ID: $movementId)"))

        val tipoNormalizado = newTipo.lowercase().trim()

        // Si no cambia producto, tipo ni cantidad, solo actualizar el motivo
        if (oldMovement.productoId == newProductId &&
            oldMovement.tipo == tipoNormalizado &&
            oldMovement.cantidad == newCantidad
        ) {
            val updated = oldMovement.copy(motivo = newMotivo.trim())
            db.movementDao().update(updated)
            return Result.success(Unit)
        }

        if (oldMovement.productoId == newProductId) {
            // Mismo producto afectado
            val product = db.productDao().getProductById(newProductId)
                ?: return Result.failure(Exception("Producto no encontrado"))

            val oldDelta = when (oldMovement.tipo) {
                "ingreso", "devolucion" -> oldMovement.cantidad
                "venta", "consumo" -> -oldMovement.cantidad
                "ajuste" -> (oldMovement.existenciaResultante - oldMovement.existenciaAnterior)
                else -> 0
            }

            val stockSinMovimiento = product.cantidadDisponible - oldDelta

            val nuevoStock = when (tipoNormalizado) {
                "ingreso", "devolucion" -> stockSinMovimiento + newCantidad
                "venta", "consumo" -> stockSinMovimiento - newCantidad
                "ajuste" -> newCantidad
                else -> return Result.failure(Exception("Tipo de movimiento no válido: $newTipo"))
            }

            if (nuevoStock < 0 && !allowNegative) {
                return Result.failure(
                    Exception("Stock insuficiente: la modificación dejaría el stock en $nuevoStock uds (disponible sin este mov: $stockSinMovimiento uds)")
                )
            }

            val updatedMovement = oldMovement.copy(
                tipo = tipoNormalizado,
                cantidad = newCantidad,
                motivo = newMotivo.trim(),
                existenciaAnterior = stockSinMovimiento,
                existenciaResultante = nuevoStock
            )

            db.movementDao().update(updatedMovement)
            db.productDao().updateStock(product.id, nuevoStock)
            return Result.success(Unit)
        } else {
            // Producto cambiado: revertir el impacto en el producto original y aplicar al nuevo
            val oldProduct = db.productDao().getProductById(oldMovement.productoId)
            val newProduct = db.productDao().getProductById(newProductId)
                ?: return Result.failure(Exception("Nuevo producto no encontrado"))

            var nuevoStockOldProduct = oldProduct?.cantidadDisponible ?: 0
            if (oldProduct != null) {
                val oldDelta = when (oldMovement.tipo) {
                    "ingreso", "devolucion" -> oldMovement.cantidad
                    "venta", "consumo" -> -oldMovement.cantidad
                    "ajuste" -> (oldMovement.existenciaResultante - oldMovement.existenciaAnterior)
                    else -> 0
                }
                nuevoStockOldProduct = oldProduct.cantidadDisponible - oldDelta
                if (nuevoStockOldProduct < 0 && !allowNegative) {
                    return Result.failure(
                        Exception("No se puede cambiar de producto: dejaría el stock del producto anterior en negativo ($nuevoStockOldProduct uds)")
                    )
                }
            }

            val stockAnteriorNew = newProduct.cantidadDisponible
            val nuevoStockNew = when (tipoNormalizado) {
                "ingreso", "devolucion" -> stockAnteriorNew + newCantidad
                "venta", "consumo" -> stockAnteriorNew - newCantidad
                "ajuste" -> newCantidad
                else -> return Result.failure(Exception("Tipo de movimiento no válido: $newTipo"))
            }

            if (nuevoStockNew < 0 && !allowNegative) {
                return Result.failure(
                    Exception("Stock insuficiente en nuevo producto: disponible $stockAnteriorNew uds, solicitado: $newCantidad uds")
                )
            }

            if (oldProduct != null) {
                db.productDao().updateStock(oldProduct.id, nuevoStockOldProduct)
            }
            db.productDao().updateStock(newProduct.id, nuevoStockNew)

            val updatedMovement = oldMovement.copy(
                productoId = newProductId,
                tipo = tipoNormalizado,
                cantidad = newCantidad,
                motivo = newMotivo.trim(),
                existenciaAnterior = stockAnteriorNew,
                existenciaResultante = nuevoStockNew
            )
            db.movementDao().update(updatedMovement)
            return Result.success(Unit)
        }
    }

    /**
     * Elimina un movimiento de inventario revirtiendo su impacto en el stock del producto asociado.
     */
    suspend fun deleteMovement(
        movement: MovementEntity,
        allowNegative: Boolean = false
    ): Result<Unit> {
        val product = db.productDao().getProductById(movement.productoId)

        if (product != null) {
            val delta = when (movement.tipo) {
                "ingreso", "devolucion" -> movement.cantidad
                "venta", "consumo" -> -movement.cantidad
                "ajuste" -> (movement.existenciaResultante - movement.existenciaAnterior)
                else -> 0
            }

            val nuevoStock = product.cantidadDisponible - delta

            if (nuevoStock < 0 && !allowNegative) {
                return Result.failure(
                    Exception("No es posible eliminar este movimiento: dejaría el stock en negativo ($nuevoStock uds).")
                )
            }

            db.productDao().updateStock(product.id, nuevoStock)
        }

        db.movementDao().delete(movement)
        return Result.success(Unit)
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
