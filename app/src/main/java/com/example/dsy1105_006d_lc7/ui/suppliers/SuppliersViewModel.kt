package com.example.dsy1105_006d_lc7.ui.suppliers

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.dsy1105_006d_lc7.data.local.entity.SupplierEntity
import com.example.dsy1105_006d_lc7.data.repository.InventoryRepository
import kotlinx.coroutines.launch

data class SuppliersUiState(
    val suppliers: List<SupplierEntity> = emptyList(),
    val isLoading: Boolean = true,
    val message: String? = null
)

data class SupplierFormState(
    val id: Long = 0,
    val nombreFicticio: String = "",
    val correoFicticio: String = "",
    val telefonoFicticio: String = "",
    val direccionFicticia: String = "",
    val isEditing: Boolean = false,
    val error: String? = null
)

class SuppliersViewModel(private val repository: InventoryRepository) : ViewModel() {

    var uiState by mutableStateOf(SuppliersUiState())
        private set

    var formState by mutableStateOf(SupplierFormState())
        private set

    init {
        viewModelScope.launch {
            repository.getAllSuppliers().collect { suppliers ->
                uiState = uiState.copy(suppliers = suppliers, isLoading = false)
            }
        }
    }

    fun clearMessage() { uiState = uiState.copy(message = null) }

    fun initFormForCreate() { formState = SupplierFormState() }

    fun initFormForEdit(supplier: SupplierEntity) {
        formState = SupplierFormState(
            id = supplier.id,
            nombreFicticio = supplier.nombreFicticio,
            correoFicticio = supplier.correoFicticio,
            telefonoFicticio = supplier.telefonoFicticio,
            direccionFicticia = supplier.direccionFicticia,
            isEditing = true
        )
    }

    fun onFieldChange(field: String, value: String) {
        formState = when (field) {
            "nombre" -> formState.copy(nombreFicticio = value, error = null)
            "correo" -> formState.copy(correoFicticio = value, error = null)
            "telefono" -> formState.copy(telefonoFicticio = value, error = null)
            "direccion" -> formState.copy(direccionFicticia = value, error = null)
            else -> formState
        }
    }

    fun saveSupplier(onSuccess: () -> Unit) {
        if (formState.nombreFicticio.isBlank()) {
            formState = formState.copy(error = "El nombre es obligatorio")
            return
        }
        if (formState.correoFicticio.isBlank()) {
            formState = formState.copy(error = "El correo es obligatorio")
            return
        }

        viewModelScope.launch {
            try {
                val supplier = SupplierEntity(
                    id = if (formState.isEditing) formState.id else 0,
                    nombreFicticio = formState.nombreFicticio.trim(),
                    correoFicticio = formState.correoFicticio.trim(),
                    telefonoFicticio = formState.telefonoFicticio.trim(),
                    direccionFicticia = formState.direccionFicticia.trim()
                )
                if (formState.isEditing) {
                    repository.updateSupplier(supplier)
                    uiState = uiState.copy(message = "Proveedor actualizado")
                } else {
                    repository.insertSupplier(supplier)
                    uiState = uiState.copy(message = "Proveedor creado")
                }
                onSuccess()
            } catch (e: Exception) {
                formState = formState.copy(error = "Error: ${e.message}")
            }
        }
    }

    fun deleteSupplier(supplier: SupplierEntity) {
        viewModelScope.launch {
            try {
                repository.deleteSupplier(supplier)
                uiState = uiState.copy(message = "Proveedor eliminado")
            } catch (e: Exception) {
                uiState = uiState.copy(message = "Error: ${e.message}")
            }
        }
    }

    class Factory(private val repository: InventoryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SuppliersViewModel(repository) as T
        }
    }
}
