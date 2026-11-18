package com.example.dsy1105_006d_lc7.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dsy1105_006d_lc7.model.ProveedorFormState
import com.example.dsy1105_006d_lc7.model.ProveedoresUiState
import com.example.dsy1105_006d_lc7.model.data.local.BaseDeDatos
import com.example.dsy1105_006d_lc7.model.data.local.ProveedorEntity
import kotlinx.coroutines.launch

/**
 * ViewModel para la gestión de Proveedores.
 */
class ProveedorViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = BaseDeDatos.getDatabase(app).proveedorDao()

    var uiState by mutableStateOf(ProveedoresUiState())
        private set

    var formState by mutableStateOf(ProveedorFormState())
        private set

    init {
        viewModelScope.launch {
            dao.getAllSuppliers().collect { suppliers ->
                uiState = uiState.copy(suppliers = suppliers, isLoading = false)
            }
        }
    }

    fun clearMessage() {
        uiState = uiState.copy(message = null)
    }

    fun initFormForCreate() {
        formState = ProveedorFormState()
    }

    fun initFormForEdit(supplier: ProveedorEntity) {
        formState = ProveedorFormState(
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
                val supplier = ProveedorEntity(
                    id = if (formState.isEditing) formState.id else 0,
                    nombreFicticio = formState.nombreFicticio.trim(),
                    correoFicticio = formState.correoFicticio.trim(),
                    telefonoFicticio = formState.telefonoFicticio.trim(),
                    direccionFicticia = formState.direccionFicticia.trim()
                )
                if (formState.isEditing) {
                    dao.update(supplier)
                    uiState = uiState.copy(message = "Proveedor actualizado")
                } else {
                    dao.insert(supplier)
                    uiState = uiState.copy(message = "Proveedor creado")
                }
                onSuccess()
            } catch (e: Exception) {
                formState = formState.copy(error = "Error: ${e.message}")
            }
        }
    }

    fun deleteSupplier(supplier: ProveedorEntity) {
        viewModelScope.launch {
            try {
                dao.delete(supplier)
                uiState = uiState.copy(message = "Proveedor eliminado")
            } catch (e: Exception) {
                uiState = uiState.copy(message = "Error: ${e.message}")
            }
        }
    }
}

typealias SuppliersViewModel = ProveedorViewModel
