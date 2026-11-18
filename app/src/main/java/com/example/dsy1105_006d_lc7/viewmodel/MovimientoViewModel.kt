package com.example.dsy1105_006d_lc7.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dsy1105_006d_lc7.model.MovimientoFormState
import com.example.dsy1105_006d_lc7.model.MovimientosUiState
import com.example.dsy1105_006d_lc7.model.data.local.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * ViewModel para el registro, edición y visualización de movimientos de inventario.
 * Gestiona de forma directa las operaciones atómicas sobre Room y el cálculo de existencias.
 */
class MovimientoViewModel(app: Application) : AndroidViewModel(app) {

    private val db = BaseDeDatos.getDatabase(app)
    private val movimientoDao = db.movimientoDao()
    private val productoDao = db.productoDao()

    var uiState by mutableStateOf(MovimientosUiState())
        private set

    var formState by mutableStateOf(MovimientoFormState())
        private set

    init {
        loadMovements()
        loadProducts()
    }

    private fun loadMovements() {
        viewModelScope.launch {
            movimientoDao.getAllMovementsWithProduct().collect { movements ->
                uiState = uiState.copy(
                    movements = applyFilter(movements, uiState.filterType),
                    isLoading = false
                )
            }
        }
    }

    private fun loadProducts() {
        viewModelScope.launch {
            productoDao.getAllProducts().collect { products ->
                uiState = uiState.copy(products = products)
            }
        }
    }

    private fun applyFilter(
        movements: List<MovimientoConProducto>,
        filter: String?
    ): List<MovimientoConProducto> {
        return if (filter.isNullOrBlank()) {
            movements
        } else {
            movements.filter { it.movement.tipo.equals(filter, ignoreCase = true) }
        }
    }

    fun filterByType(type: String?) {
        viewModelScope.launch {
            movimientoDao.getAllMovementsWithProduct().collect { movements ->
                uiState = uiState.copy(
                    filterType = type,
                    movements = applyFilter(movements, type)
                )
            }
        }
    }

    fun onFilterType(type: String?) = filterByType(type)

    fun clearMessage() {
        uiState = uiState.copy(message = null)
    }

    fun clearScanSuccessMessage() {
        formState = formState.copy(scanSuccessMessage = null)
    }

    // ── Formulario ──

    fun initFormForCreate(preselectedProductId: Long? = null) {
        formState = MovimientoFormState()
        if (preselectedProductId != null) {
            viewModelScope.launch {
                val product = productoDao.getProductById(preselectedProductId)
                if (product != null) {
                    onProductSelected(product)
                }
            }
        }
    }

    fun initFormForEdit(movementWithProduct: MovimientoConProducto) {
        val m = movementWithProduct.movement
        val p = movementWithProduct.product
        formState = MovimientoFormState(
            id = m.id,
            isEditing = true,
            selectedProduct = p,
            selectedProductId = p.id,
            selectedProductName = "${p.codigoInterno} - ${p.nombre}",
            tipo = m.tipo,
            cantidad = m.cantidad.toString(),
            motivo = m.motivo,
            stockActual = p.cantidadDisponible,
            error = null
        )
    }

    fun ensureFormLoaded(movementId: Long) {
        if (movementId > 0L) {
            if (formState.isEditing && formState.id == movementId) return
            viewModelScope.launch {
                val movWithProd = movimientoDao.getMovementWithProductById(movementId)
                if (movWithProd != null) {
                    initFormForEdit(movWithProd)
                } else {
                    formState = MovimientoFormState(
                        id = movementId,
                        isEditing = true,
                        error = "No se encontró el movimiento seleccionado"
                    )
                }
            }
        } else if (formState.isEditing) {
            initFormForCreate()
        }
    }

    fun onProductSelected(product: ProductoEntity) {
        formState = formState.copy(
            selectedProduct = product,
            selectedProductId = product.id,
            selectedProductName = "${product.codigoInterno} - ${product.nombre}",
            stockActual = product.cantidadDisponible,
            error = null
        )
    }

    /**
     * Procesa el código QR escaneado con la cámara.
     */
    fun onQrCodeScanned(qrContent: String, onScannedResult: (Boolean, String) -> Unit) {
        val trimmed = qrContent.trim()
        if (trimmed.isBlank()) {
            formState = formState.copy(error = "El código QR leído está vacío")
            onScannedResult(false, "Código QR vacío")
            return
        }

        val directId = trimmed.toLongOrNull()
        val idRegex = Regex("""(?:id|producto)[:=\s]+(\d+)""", RegexOption.IGNORE_CASE)
        val extractedId = directId ?: idRegex.find(trimmed)?.groupValues?.get(1)?.toLongOrNull()

        // 1. Buscar en memoria
        var matched = if (extractedId != null) {
            uiState.products.find { it.id == extractedId }
        } else null

        if (matched == null) {
            matched = uiState.products.find {
                it.codigoInterno.equals(trimmed, ignoreCase = true)
            }
        }

        if (matched != null) {
            onProductSelected(matched)
            val msg = "Producto escaneado: ${matched.nombre} (Stock: ${matched.cantidadDisponible})"
            formState = formState.copy(scanSuccessMessage = msg, error = null)
            onScannedResult(true, msg)
            return
        }

        // 2. Consultar Room
        viewModelScope.launch {
            val dbProduct = if (extractedId != null) {
                productoDao.getProductById(extractedId)
            } else {
                productoDao.getProductByCode(trimmed)
            }

            if (dbProduct != null) {
                onProductSelected(dbProduct)
                val msg = "Producto escaneado: ${dbProduct.nombre} (Stock: ${dbProduct.cantidadDisponible})"
                formState = formState.copy(scanSuccessMessage = msg, error = null)
                onScannedResult(true, msg)
            } else {
                val err = "No se encontró ningún producto con ID o código: '$trimmed'"
                formState = formState.copy(error = err)
                onScannedResult(false, err)
            }
        }
    }

    fun onTipoChange(tipo: String) {
        formState = formState.copy(tipo = tipo, error = null)
    }

    fun onCantidadChange(cantidad: String) {
        formState = formState.copy(cantidad = cantidad, error = null)
    }

    fun onMotivoChange(motivo: String) {
        formState = formState.copy(motivo = motivo, error = null)
    }

    /**
     * Guarda el movimiento (creación o edición) y actualiza el stock en Room.
     */
    fun saveMovement(onSuccess: () -> Unit) {
        if (formState.isSaving) return

        if (formState.selectedProductId == null) {
            formState = formState.copy(error = "Debe seleccionar un producto del inventario")
            return
        }

        val qty = formState.cantidad.toIntOrNull()
        if (qty == null || qty <= 0) {
            formState = formState.copy(error = "Cantidad debe ser un número positivo mayor a 0")
            return
        }

        val tipoNormalizado = formState.tipo.lowercase().trim()
        if (!formState.isEditing && (tipoNormalizado == "venta" || tipoNormalizado == "consumo")) {
            if (qty > formState.stockActual) {
                val operacion = if (tipoNormalizado == "venta") "Venta" else "Consumo en servicio"
                formState = formState.copy(
                    error = "Operación denegada ($operacion): La cantidad ingresada ($qty uds) supera la cantidad disponible en bodega (${formState.stockActual} uds). Se previenen existencias negativas."
                )
                return
            }
        }

        formState = formState.copy(isSaving = true)

        viewModelScope.launch {
            try {
                if (formState.isEditing) {
                    actualizarMovimientoInterno(
                        movementId = formState.id,
                        newTipo = formState.tipo,
                        newProductId = formState.selectedProductId!!,
                        newCantidad = qty,
                        newMotivo = formState.motivo.trim()
                    )
                    formState = formState.copy(isSaving = false)
                    uiState = uiState.copy(message = "Movimiento actualizado correctamente")
                    onSuccess()
                } else {
                    registrarMovimientoInterno(
                        tipo = formState.tipo,
                        productId = formState.selectedProductId!!,
                        cantidad = qty,
                        motivo = formState.motivo.trim()
                    )
                    formState = formState.copy(isSaving = false)
                    uiState = uiState.copy(message = "Movimiento registrado correctamente")
                    onSuccess()
                }
            } catch (e: Exception) {
                formState = formState.copy(
                    isSaving = false,
                    error = e.message ?: "Error al guardar el movimiento"
                )
            }
        }
    }

    private suspend fun registrarMovimientoInterno(
        tipo: String,
        productId: Long,
        cantidad: Int,
        motivo: String
    ) {
        val product = productoDao.getProductById(productId)
            ?: throw Exception("Producto no encontrado")

        val stockAnterior = product.cantidadDisponible
        val nuevoStock = when (tipo.lowercase().trim()) {
            "ingreso", "devolucion" -> stockAnterior + cantidad
            "venta", "consumo" -> stockAnterior - cantidad
            "ajuste" -> cantidad
            else -> throw Exception("Tipo de movimiento no válido: $tipo")
        }

        if (nuevoStock < 0) {
            throw Exception("Stock insuficiente. Disponible: $stockAnterior, Solicitado: $cantidad")
        }

        val movement = MovimientoEntity(
            tipo = tipo.lowercase().trim(),
            productoId = productId,
            cantidad = cantidad,
            fechaHora = System.currentTimeMillis(),
            motivo = motivo,
            existenciaAnterior = stockAnterior,
            existenciaResultante = nuevoStock
        )

        movimientoDao.insert(movement)
        productoDao.updateStock(productId, nuevoStock)
    }

    private suspend fun actualizarMovimientoInterno(
        movementId: Long,
        newTipo: String,
        newProductId: Long,
        newCantidad: Int,
        newMotivo: String
    ) {
        val oldMovement = movimientoDao.getMovementById(movementId)
            ?: throw Exception("Movimiento no encontrado (ID: $movementId)")

        val tipoNormalizado = newTipo.lowercase().trim()

        if (oldMovement.productoId == newProductId &&
            oldMovement.tipo == tipoNormalizado &&
            oldMovement.cantidad == newCantidad
        ) {
            val updated = oldMovement.copy(motivo = newMotivo.trim())
            movimientoDao.update(updated)
            return
        }

        if (oldMovement.productoId == newProductId) {
            val product = productoDao.getProductById(newProductId)
                ?: throw Exception("Producto no encontrado")

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
                else -> throw Exception("Tipo de movimiento no válido: $newTipo")
            }

            if (nuevoStock < 0) {
                throw Exception("Stock insuficiente: dejaría el stock en $nuevoStock uds")
            }

            val updatedMovement = oldMovement.copy(
                tipo = tipoNormalizado,
                cantidad = newCantidad,
                motivo = newMotivo.trim(),
                existenciaAnterior = stockSinMovimiento,
                existenciaResultante = nuevoStock
            )

            movimientoDao.update(updatedMovement)
            productoDao.updateStock(product.id, nuevoStock)
        } else {
            val oldProduct = productoDao.getProductById(oldMovement.productoId)
            val newProduct = productoDao.getProductById(newProductId)
                ?: throw Exception("Nuevo producto no encontrado")

            var nuevoStockOldProduct = oldProduct?.cantidadDisponible ?: 0
            if (oldProduct != null) {
                val oldDelta = when (oldMovement.tipo) {
                    "ingreso", "devolucion" -> oldMovement.cantidad
                    "venta", "consumo" -> -oldMovement.cantidad
                    "ajuste" -> (oldMovement.existenciaResultante - oldMovement.existenciaAnterior)
                    else -> 0
                }
                nuevoStockOldProduct = oldProduct.cantidadDisponible - oldDelta
                if (nuevoStockOldProduct < 0) {
                    throw Exception("No se puede cambiar de producto: stock previo quedaría negativo")
                }
            }

            val stockAnteriorNew = newProduct.cantidadDisponible
            val nuevoStockNew = when (tipoNormalizado) {
                "ingreso", "devolucion" -> stockAnteriorNew + newCantidad
                "venta", "consumo" -> stockAnteriorNew - newCantidad
                "ajuste" -> newCantidad
                else -> throw Exception("Tipo no válido: $newTipo")
            }

            if (nuevoStockNew < 0) {
                throw Exception("Stock insuficiente en nuevo producto")
            }

            if (oldProduct != null) {
                productoDao.updateStock(oldProduct.id, nuevoStockOldProduct)
            }
            productoDao.updateStock(newProduct.id, nuevoStockNew)

            val updatedMovement = oldMovement.copy(
                productoId = newProductId,
                tipo = tipoNormalizado,
                cantidad = newCantidad,
                motivo = newMotivo.trim(),
                existenciaAnterior = stockAnteriorNew,
                existenciaResultante = nuevoStockNew
            )
            movimientoDao.update(updatedMovement)
        }
    }

    fun deleteMovement(movement: MovimientoEntity) {
        viewModelScope.launch {
            try {
                val product = productoDao.getProductById(movement.productoId)
                if (product != null) {
                    val delta = when (movement.tipo) {
                        "ingreso", "devolucion" -> movement.cantidad
                        "venta", "consumo" -> -movement.cantidad
                        "ajuste" -> (movement.existenciaResultante - movement.existenciaAnterior)
                        else -> 0
                    }
                    val nuevoStock = product.cantidadDisponible - delta
                    if (nuevoStock < 0) {
                        uiState = uiState.copy(message = "No es posible eliminar este movimiento: stock quedaría negativo ($nuevoStock uds)")
                        return@launch
                    }
                    productoDao.updateStock(product.id, nuevoStock)
                }
                movimientoDao.delete(movement)
                uiState = uiState.copy(message = "Movimiento eliminado y existencias revertidas")
            } catch (e: Exception) {
                uiState = uiState.copy(message = e.message ?: "Error al eliminar el movimiento")
            }
        }
    }

    fun registerMovement(onSuccess: () -> Unit) = saveMovement(onSuccess)
}

typealias MovementsViewModel = MovimientoViewModel
