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
import java.math.BigDecimal

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
    val isSaving: Boolean = false,
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

    private var allProducts: List<ProductWithDetails> = emptyList()

    init {
        loadProducts()
        loadCategories()
        loadLocations()
        loadSuppliers()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            repository.getAllProductsWithDetails().collect { products ->
                allProducts = products
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
                val activeCategory = uiState.selectedCategory?.takeIf { it in categories }
                val categoryWasCleared = uiState.selectedCategory != null && activeCategory == null
                uiState = uiState.copy(
                    categories = categories,
                    selectedCategory = activeCategory,
                    products = if (categoryWasCleared) applyFilters(allProducts, selectedCategory = null) else uiState.products
                )
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

    private fun applyFilters(
        products: List<ProductWithDetails>,
        selectedCategory: String? = uiState.selectedCategory,
        searchQuery: String = uiState.searchQuery
    ): List<ProductWithDetails> {
        var filtered = products
        if (!selectedCategory.isNullOrBlank()) {
            filtered = filtered.filter { it.product.categoria == selectedCategory }
        }
        if (searchQuery.isNotBlank()) {
            val query = searchQuery.lowercase()
            filtered = filtered.filter {
                it.product.nombre.lowercase().contains(query) ||
                        it.product.codigoInterno.lowercase().contains(query)
            }
        }
        return filtered
    }

    fun onSearchQueryChange(query: String) {
        uiState = uiState.copy(
            searchQuery = query,
            products = applyFilters(allProducts, searchQuery = query)
        )
    }

    fun onCategoryFilter(category: String?) {
        uiState = uiState.copy(
            selectedCategory = category,
            products = applyFilters(allProducts, selectedCategory = category)
        )
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
            precioCompra = formatPriceForInput(product.precioCompra),
            precioVenta = formatPriceForInput(product.precioVenta),
            estado = product.estado.ifBlank { "activo" },
            proveedorId = product.proveedorId,
            ubicacionId = product.ubicacionId,
            isEditing = true,
            isSaving = false,
            error = null
        )
    }

    /**
     * Garantiza que el formulario esté cargado con el producto correspondiente al [productId]
     * cuando se navega directamente o tras recreación del proceso.
     */
    fun ensureFormLoaded(productId: Long) {
        if (productId > 0L) {
            if (formState.isEditing && formState.id == productId) return
            viewModelScope.launch {
                val product = repository.getProductById(productId)
                if (product != null) {
                    initFormForEdit(product)
                } else {
                    formState = ProductFormState(
                        id = productId,
                        isEditing = true,
                        error = "No se encontró el producto seleccionado"
                    )
                }
            }
        } else if (formState.isEditing) {
            initFormForCreate()
        }
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
        formState = formState.copy(proveedorId = supplierId, error = null)
    }

    fun onLocationSelected(locationId: Long?) {
        formState = formState.copy(ubicacionId = locationId, error = null)
    }

    fun onEstadoSelected(estado: String) {
        formState = formState.copy(estado = estado, error = null)
    }

    fun saveProduct(onSuccess: () -> Unit) {
        if (formState.isSaving) return

        val codigo = formState.codigoInterno.trim()
        val nombre = formState.nombre.trim()
        val categoria = formState.categoria.trim()

        if (codigo.isBlank()) {
            formState = formState.copy(error = "El código interno es obligatorio")
            return
        }
        if (nombre.isBlank()) {
            formState = formState.copy(error = "El nombre es obligatorio")
            return
        }
        if (categoria.isBlank()) {
            formState = formState.copy(error = "La categoría es obligatoria")
            return
        }

        val cantidad = formState.cantidadDisponible.trim().toIntOrNull()
        if (cantidad == null || cantidad < 0) {
            formState = formState.copy(error = "El stock actual debe ser un número entero mayor o igual a 0")
            return
        }

        val existenciaMinima = formState.existenciaMinima.trim().toIntOrNull()
        if (existenciaMinima == null || existenciaMinima < 0) {
            formState = formState.copy(error = "El stock mínimo debe ser un número entero mayor o igual a 0")
            return
        }

        val precioCompra = parseDecimalInput(formState.precioCompra)
        if (precioCompra == null || precioCompra < 0.0) {
            formState = formState.copy(error = "El precio de compra debe ser un número válido mayor o igual a 0")
            return
        }

        val precioVenta = parseDecimalInput(formState.precioVenta)
        if (precioVenta == null || precioVenta < 0.0) {
            formState = formState.copy(error = "El precio de venta debe ser un número válido mayor o igual a 0")
            return
        }

        if (formState.isEditing && formState.id <= 0L) {
            formState = formState.copy(error = "No se pudo identificar el producto a actualizar")
            return
        }

        val estadoNormalizado = formState.estado.trim().lowercase().takeIf {
            it == "activo" || it == "descontinuado"
        } ?: "activo"

        formState = formState.copy(isSaving = true, error = null)

        viewModelScope.launch {
            try {
                val existingWithCode = repository.getProductByCode(codigo)
                if (existingWithCode != null && (!formState.isEditing || existingWithCode.id != formState.id)) {
                    formState = formState.copy(
                        isSaving = false,
                        error = "Ya existe un producto con el código interno '$codigo'"
                    )
                    return@launch
                }

                val product = ProductEntity(
                    id = if (formState.isEditing) formState.id else 0,
                    codigoInterno = codigo,
                    nombre = nombre,
                    descripcion = formState.descripcion.trim(),
                    categoria = categoria,
                    cantidadDisponible = cantidad,
                    existenciaMinima = existenciaMinima,
                    precioCompra = precioCompra,
                    precioVenta = precioVenta,
                    estado = estadoNormalizado,
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
                formState = formState.copy(isSaving = false)
                onSuccess()
            } catch (e: Exception) {
                formState = formState.copy(
                    isSaving = false,
                    error = "Error al guardar: ${e.message}"
                )
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

    /**
     * Permite al Administrador modificar la existencia mínima directamente.
     */
    fun updateMinStock(product: ProductEntity, newMinStock: Int) {
        if (newMinStock < 0) {
            uiState = uiState.copy(message = "La existencia mínima no puede ser negativa")
            return
        }
        viewModelScope.launch {
            try {
                val updated = product.copy(existenciaMinima = newMinStock)
                repository.updateProduct(updated)
                uiState = uiState.copy(message = "Existencia mínima de '${product.nombre}' actualizada a $newMinStock uds")
            } catch (e: Exception) {
                uiState = uiState.copy(message = "Error al actualizar stock mínimo: ${e.message}")
            }
        }
    }

    private fun parseDecimalInput(raw: String): Double? {
        val normalized = raw.trim().replace(',', '.')
        val value = normalized.toDoubleOrNull() ?: return null
        return if (value.isFinite()) value else null
    }

    private fun formatPriceForInput(price: Double): String {
        return when {
            price == 0.0 -> "0"
            price % 1.0 == 0.0 && price >= Long.MIN_VALUE && price <= Long.MAX_VALUE ->
                price.toLong().toString()
            else -> BigDecimal.valueOf(price).stripTrailingZeros().toPlainString()
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
