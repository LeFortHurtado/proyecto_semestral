package com.example.dsy1105_006d_lc7.ui.products

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.dsy1105_006d_lc7.data.local.entity.LocationEntity
import com.example.dsy1105_006d_lc7.data.local.entity.ProductEntity
import com.example.dsy1105_006d_lc7.data.local.entity.ProductWithDetails
import com.example.dsy1105_006d_lc7.data.local.entity.SupplierEntity
import com.example.dsy1105_006d_lc7.data.repository.InventoryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Estado de la UI para la pantalla de productos.
 */
data class ProductsUiState(
    val products: List<ProductWithDetails> = emptyList(),
    val categories: List<String> = emptyList(),
    val locations: List<LocationEntity> = emptyList(),
    val suppliers: List<SupplierEntity> = emptyList(),
    val selectedCategory: String? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val message: String? = null
)

/**
 * Estado del formulario para crear/editar productos.
 */
data class ProductFormState(
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
    val error: String? = null
)

/**
 * ViewModel para la gestión de productos.
 * Implementa MVVM y observa los cambios en la base de datos mediante Flow.
 */
class ProductsViewModel(private val repository: InventoryRepository) : ViewModel() {

    var uiState by mutableStateOf(ProductsUiState())
        private set

    var formState by mutableStateOf(ProductFormState())
        private set

    init {
        loadProducts()
        loadCategories()
        loadLocations()
        loadSuppliers()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            repository.getAllProductsWithDetails().collect { products ->
                uiState = uiState.copy(
                    products = applyFilters(products),
                    isLoading = false
                )
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            repository.getAllCategories().collect { categories ->
                uiState = uiState.copy(categories = categories)
            }
        }
    }

    private fun loadLocations() {
        viewModelScope.launch {
            repository.getAllLocations().collect { locations ->
                uiState = uiState.copy(locations = locations)
            }
        }
    }

    private fun loadSuppliers() {
        viewModelScope.launch {
            repository.getAllSuppliers().collect { suppliers ->
                uiState = uiState.copy(suppliers = suppliers)
            }
        }
    }

    private fun applyFilters(products: List<ProductWithDetails>): List<ProductWithDetails> {
        var filtered = products
        if (!uiState.selectedCategory.isNullOrBlank()) {
            filtered = filtered.filter { it.product.categoria == uiState.selectedCategory }
        }
        if (uiState.searchQuery.isNotBlank()) {
            val query = uiState.searchQuery.lowercase()
            filtered = filtered.filter {
                it.product.nombre.lowercase().contains(query) ||
                        it.product.codigoInterno.lowercase().contains(query)
            }
        }
        return filtered
    }

    fun onSearchQueryChange(query: String) {
        uiState = uiState.copy(searchQuery = query)
        refreshFilters()
    }

    fun onCategoryFilter(category: String?) {
        uiState = uiState.copy(selectedCategory = category)
        refreshFilters()
    }

    private fun refreshFilters() {
        viewModelScope.launch {
            repository.getAllProductsWithDetails().first().let { products ->
                uiState = uiState.copy(products = applyFilters(products))
            }
        }
    }

    fun clearMessage() {
        uiState = uiState.copy(message = null)
    }

    // ── Formulario ──

    fun initFormForCreate() {
        formState = ProductFormState()
    }

    fun initFormForEdit(product: ProductEntity) {
        formState = ProductFormState(
            id = product.id,
            codigoInterno = product.codigoInterno,
            nombre = product.nombre,
            descripcion = product.descripcion,
            categoria = product.categoria,
            cantidadDisponible = product.cantidadDisponible.toString(),
            existenciaMinima = product.existenciaMinima.toString(),
            precioCompra = product.precioCompra.toString(),
            precioVenta = product.precioVenta.toString(),
            estado = product.estado,
            proveedorId = product.proveedorId,
            ubicacionId = product.ubicacionId,
            isEditing = true
        )
    }

    fun onFormFieldChange(field: String, value: String) {
        formState = when (field) {
            "codigoInterno" -> formState.copy(codigoInterno = value, error = null)
            "nombre" -> formState.copy(nombre = value, error = null)
            "descripcion" -> formState.copy(descripcion = value, error = null)
            "categoria" -> formState.copy(categoria = value, error = null)
            "cantidadDisponible" -> formState.copy(cantidadDisponible = value, error = null)
            "existenciaMinima" -> formState.copy(existenciaMinima = value, error = null)
            "precioCompra" -> formState.copy(precioCompra = value, error = null)
            "precioVenta" -> formState.copy(precioVenta = value, error = null)
            "estado" -> formState.copy(estado = value, error = null)
            else -> formState
        }
    }

    fun onSupplierSelected(supplierId: Long?) {
        formState = formState.copy(proveedorId = supplierId)
    }

    fun onLocationSelected(locationId: Long?) {
        formState = formState.copy(ubicacionId = locationId)
    }

    fun saveProduct(onSuccess: () -> Unit) {
        // Validaciones
        if (formState.codigoInterno.isBlank()) {
            formState = formState.copy(error = "El código interno es obligatorio")
            return
        }
        if (formState.nombre.isBlank()) {
            formState = formState.copy(error = "El nombre es obligatorio")
            return
        }
        if (formState.categoria.isBlank()) {
            formState = formState.copy(error = "La categoría es obligatoria")
            return
        }

        viewModelScope.launch {
            try {
                val product = ProductEntity(
                    id = if (formState.isEditing) formState.id else 0,
                    codigoInterno = formState.codigoInterno.trim(),
                    nombre = formState.nombre.trim(),
                    descripcion = formState.descripcion.trim(),
                    categoria = formState.categoria.trim(),
                    cantidadDisponible = formState.cantidadDisponible.toIntOrNull() ?: 0,
                    existenciaMinima = formState.existenciaMinima.toIntOrNull() ?: 1,
                    precioCompra = formState.precioCompra.toDoubleOrNull() ?: 0.0,
                    precioVenta = formState.precioVenta.toDoubleOrNull() ?: 0.0,
                    estado = formState.estado,
                    proveedorId = formState.proveedorId,
                    ubicacionId = formState.ubicacionId
                )

                if (formState.isEditing) {
                    repository.updateProduct(product)
                    uiState = uiState.copy(message = "Producto actualizado correctamente")
                } else {
                    repository.insertProduct(product)
                    uiState = uiState.copy(message = "Producto creado correctamente")
                }
                onSuccess()
            } catch (e: Exception) {
                formState = formState.copy(error = "Error al guardar: ${e.message}")
            }
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            try {
                repository.deleteProduct(product)
                uiState = uiState.copy(message = "Producto eliminado")
            } catch (e: Exception) {
                uiState = uiState.copy(message = "Error al eliminar: ${e.message}")
            }
        }
    }

    // ── Factory ──

    class Factory(private val repository: InventoryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProductsViewModel(repository) as T
        }
    }
}
