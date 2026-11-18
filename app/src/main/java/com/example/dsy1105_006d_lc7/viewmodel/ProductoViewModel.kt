package com.example.dsy1105_006d_lc7.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dsy1105_006d_lc7.model.ProductoFormState
import com.example.dsy1105_006d_lc7.model.ProductosUiState
import com.example.dsy1105_006d_lc7.model.data.local.*
import kotlinx.coroutines.launch
import java.math.BigDecimal

/**
 * ViewModel para la gestión de productos e inventario.
 * Hereda de AndroidViewModel con acceso directo a Room, siguiendo el modelo de Guía 11.
 */
class ProductoViewModel(app: Application) : AndroidViewModel(app) {

    private val db = BaseDeDatos.getDatabase(app)
    private val productoDao = db.productoDao()
    private val ubicacionDao = db.ubicacionDao()
    private val proveedorDao = db.proveedorDao()

    var uiState by mutableStateOf(ProductosUiState())
        private set

    var formState by mutableStateOf(ProductoFormState())
        private set

    private var allProducts: List<ProductoConDetalles> = emptyList()

    init {
        loadProducts()
        loadCategories()
        loadLocations()
        loadSuppliers()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            productoDao.getAllProductsWithDetails().collect { products ->
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
            productoDao.getAllCategories().collect { categories ->
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
            ubicacionDao.getAllLocations().collect { locations ->
                uiState = uiState.copy(locations = locations)
            }
        }
    }

    private fun loadSuppliers() {
        viewModelScope.launch {
            proveedorDao.getAllSuppliers().collect { suppliers ->
                uiState = uiState.copy(suppliers = suppliers)
            }
        }
    }

    private fun applyFilters(
        products: List<ProductoConDetalles>,
        selectedCategory: String? = uiState.selectedCategory,
        searchQuery: String = uiState.searchQuery
    ): List<ProductoConDetalles> {
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
        formState = ProductoFormState()
    }

    fun initFormForEdit(product: ProductoEntity) {
        formState = ProductoFormState(
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

    fun ensureFormLoaded(productId: Long) {
        if (productId > 0L) {
            if (formState.isEditing && formState.id == productId) return
            viewModelScope.launch {
                val product = productoDao.getProductById(productId)
                if (product != null) {
                    initFormForEdit(product)
                } else {
                    formState = ProductoFormState(
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
                val existingWithCode = productoDao.getProductByCode(codigo)
                if (existingWithCode != null && (!formState.isEditing || existingWithCode.id != formState.id)) {
                    formState = formState.copy(
                        isSaving = false,
                        error = "Ya existe un producto con el código interno '$codigo'"
                    )
                    return@launch
                }

                val product = ProductoEntity(
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
                    productoDao.update(product)
                    uiState = uiState.copy(message = "Producto actualizado correctamente")
                } else {
                    productoDao.insert(product)
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

    fun deleteProduct(product: ProductoEntity) {
        viewModelScope.launch {
            try {
                productoDao.delete(product)
                uiState = uiState.copy(message = "Producto eliminado")
            } catch (e: Exception) {
                uiState = uiState.copy(message = "Error al eliminar: ${e.message}")
            }
        }
    }

    fun updateMinStock(product: ProductoEntity, newMinStock: Int) {
        if (newMinStock < 0) {
            uiState = uiState.copy(message = "La existencia mínima no puede ser negativa")
            return
        }
        viewModelScope.launch {
            try {
                val updated = product.copy(existenciaMinima = newMinStock)
                productoDao.update(updated)
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
}

typealias ProductsViewModel = ProductoViewModel
