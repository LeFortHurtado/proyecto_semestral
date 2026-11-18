package com.example.dsy1105_006d_lc7.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dsy1105_006d_lc7.model.UbicacionFormState
import com.example.dsy1105_006d_lc7.model.UbicacionesUiState
import com.example.dsy1105_006d_lc7.model.data.local.BaseDeDatos
import com.example.dsy1105_006d_lc7.model.data.local.UbicacionEntity
import kotlinx.coroutines.launch

/**
 * ViewModel para la gestión de Ubicaciones en bodega.
 */
class UbicacionViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = BaseDeDatos.getDatabase(app).ubicacionDao()

    var uiState by mutableStateOf(UbicacionesUiState())
        private set

    var formState by mutableStateOf(UbicacionFormState())
        private set

    init {
        viewModelScope.launch {
            dao.getAllLocations().collect { lista ->
                uiState = uiState.copy(locations = lista, isLoading = false)
            }
        }
    }

    fun clearMessage() {
        uiState = uiState.copy(message = null)
    }

    fun initFormForCreate() {
        formState = UbicacionFormState()
    }

    fun initFormForEdit(location: UbicacionEntity) {
        formState = UbicacionFormState(
            id = location.id,
            zona = location.zona,
            estante = location.estante,
            repisa = location.repisa,
            posicion = location.posicion,
            isEditing = true
        )
    }

    fun onFieldChange(field: String, value: String) {
        formState = when (field) {
            "zona" -> formState.copy(zona = value, error = null)
            "estante" -> formState.copy(estante = value, error = null)
            "repisa" -> formState.copy(repisa = value, error = null)
            "posicion" -> formState.copy(posicion = value, error = null)
            else -> formState
        }
    }

    fun saveLocation(onSuccess: () -> Unit) {
        if (formState.zona.isBlank()) {
            formState = formState.copy(error = "La zona es obligatoria")
            return
        }
        if (formState.estante.isBlank()) {
            formState = formState.copy(error = "El estante es obligatorio")
            return
        }
        if (formState.repisa.isBlank()) {
            formState = formState.copy(error = "La repisa es obligatoria")
            return
        }

        viewModelScope.launch {
            try {
                val entity = UbicacionEntity(
                    id = if (formState.isEditing) formState.id else 0,
                    zona = formState.zona.trim(),
                    estante = formState.estante.trim(),
                    repisa = formState.repisa.trim(),
                    posicion = formState.posicion.trim()
                )
                if (formState.isEditing) {
                    dao.update(entity)
                    uiState = uiState.copy(message = "Ubicación actualizada")
                } else {
                    dao.insert(entity)
                    uiState = uiState.copy(message = "Ubicación creada")
                }
                onSuccess()
            } catch (e: Exception) {
                formState = formState.copy(error = "Error: ${e.message}")
            }
        }
    }

    fun deleteLocation(location: UbicacionEntity) {
        viewModelScope.launch {
            try {
                dao.delete(location)
                uiState = uiState.copy(message = "Ubicación eliminada")
            } catch (e: Exception) {
                uiState = uiState.copy(message = "Error: ${e.message}")
            }
        }
    }
}

typealias LocationsViewModel = UbicacionViewModel
