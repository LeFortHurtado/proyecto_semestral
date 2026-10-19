package com.example.dsy1105_006d_lc7.ui.movements

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.dsy1105_006d_lc7.data.local.entity.MovementWithProduct
import com.example.dsy1105_006d_lc7.data.local.entity.ProductEntity
import com.example.dsy1105_006d_lc7.data.repository.InventoryRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Estado de la UI para la pantalla de movimientos.
 */
data class MovementsUiState(
    val movements: List<MovementWithProduct> = emptyList(),
    val products: List<ProductEntity> = emptyList(),
    val isLoading: Boolean = true,
    val message: String? = null,
    val filterType: String? = null // null = todos
)

/**
 * Estado del formulario para registrar un movimiento.
 */
data class MovementFormState(
    val selectedProduct: ProductEntity? = null,
    val selectedProductId: Long? = null,
    val selectedProductName: String = "",
    val tipo: String = "ingreso",
    val cantidad: String = "",
    val motivo: String = "",
    val stockActual: Int = 0,
    val error: String? = null,
    val scanSuccessMessage: String? = null
)

/**
 * ViewModel para la gestión de movimientos de inventario.
 */
class MovementsViewModel(private val repository: InventoryRepository) : ViewModel() {

    var uiState by mutableStateOf(MovementsUiState())
        private set

    var formState by mutableStateOf(MovementFormState())
        private set

    init {
        loadMovements()
        loadProducts()
    }

    private fun loadMovements() {
        viewModelScope.launch {
            repository.getAllMovementsWithProduct().collect { movements ->
                val filtered = if (uiState.filterType != null) {
                    movements.filter { it.movement.tipo == uiState.filterType }
                } else {
                    movements
                }
                uiState = uiState.copy(
                    movements = filtered,
                    isLoading = false
                )
            }
        }
    }

    private fun loadProducts() {
        viewModelScope.launch {
            repository.getAllProducts().collect { products ->
                uiState = uiState.copy(products = products)
            }
        }
    }

    fun onFilterType(type: String?) {
        uiState = uiState.copy(filterType = type)
        viewModelScope.launch {
            repository.getAllMovementsWithProduct().first().let { movements ->
                val filtered = if (type != null) {
                    movements.filter { it.movement.tipo == type }
                } else {
                    movements
                }
                uiState = uiState.copy(movements = filtered)
            }
        }
    }

    fun clearMessage() {
        uiState = uiState.copy(message = null)
    }

    fun clearScanSuccessMessage() {
        formState = formState.copy(scanSuccessMessage = null)
    }

    // ── Formulario ──

    fun initForm() {
        formState = MovementFormState()
    }

    fun onProductSelected(product: ProductEntity) {
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
     * Identifica el producto por su ID numérico (ej: "3", "ID: 3") o código interno ("FILT-001"),
     * y carga inmediatamente sus datos en el formulario del movimiento.
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

        // 1. Buscar en memoria entre los productos ya cargados
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

        // 2. Si no estaba aún en memoria, consultar la base de datos Room
        viewModelScope.launch {
            val dbProduct = if (extractedId != null) {
                repository.getProductById(extractedId)
            } else {
                repository.getProductByCode(trimmed)
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

    fun registerMovement(onSuccess: () -> Unit) {
        // 1. Validar selección de producto
        if (formState.selectedProductId == null) {
            formState = formState.copy(error = "Debe seleccionar un producto del inventario")
            return
        }

        // 2. Validar que la cantidad ingresada sea válida y mayor a 0
        val qty = formState.cantidad.toIntOrNull()
        if (qty == null || qty <= 0) {
            formState = formState.copy(error = "Cantidad debe ser un número positivo mayor a 0")
            return
        }

        // 3. Validación Crítica: Regla de negocio que impide registrar Venta o Consumo
        // si la cantidad solicitada supera las existencias actuales disponibles (evitar stock negativo)
        val tipoNormalizado = formState.tipo.lowercase().trim()
        if (tipoNormalizado == "venta" || tipoNormalizado == "consumo") {
            if (qty > formState.stockActual) {
                val operacion = if (tipoNormalizado == "venta") "Venta" else "Consumo en servicio"
                formState = formState.copy(
                    error = "Operación denegada ($operacion): La cantidad ingresada ($qty uds) supera la cantidad disponible en bodega (${formState.stockActual} uds). Se previenen existencias negativas."
                )
                return
            }
        }

        viewModelScope.launch {
            val result = repository.registerMovement(
                tipo = formState.tipo,
                productId = formState.selectedProductId!!,
                cantidad = qty,
                motivo = formState.motivo.trim()
            )

            result.fold(
                onSuccess = {
                    uiState = uiState.copy(message = "Movimiento registrado correctamente")
                    onSuccess()
                },
                onFailure = { error ->
                    formState = formState.copy(error = error.message ?: "Error al registrar el movimiento")
                }
            )
        }
    }

    // ── Factory ──

    class Factory(private val repository: InventoryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MovementsViewModel(repository) as T
        }
    }
}
