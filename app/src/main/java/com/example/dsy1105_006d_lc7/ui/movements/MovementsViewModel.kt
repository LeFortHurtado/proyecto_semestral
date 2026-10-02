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
    val selectedProductId: Long? = null,
    val selectedProductName: String = "",
    val tipo: String = "ingreso",
    val cantidad: String = "",
    val motivo: String = "",
    val stockActual: Int = 0,
    val error: String? = null
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

    // ── Formulario ──

    fun initForm() {
        formState = MovementFormState()
    }

    fun onProductSelected(product: ProductEntity) {
        formState = formState.copy(
            selectedProductId = product.id,
            selectedProductName = "${product.codigoInterno} - ${product.nombre}",
            stockActual = product.cantidadDisponible,
            error = null
        )
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
        // Validaciones
        if (formState.selectedProductId == null) {
            formState = formState.copy(error = "Debe seleccionar un producto")
            return
        }
        val qty = formState.cantidad.toIntOrNull()
        if (qty == null || qty <= 0) {
            formState = formState.copy(error = "Cantidad debe ser un número positivo")
            return
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
                    formState = formState.copy(error = error.message)
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
