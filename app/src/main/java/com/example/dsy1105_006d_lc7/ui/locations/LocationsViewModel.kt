package com.example.dsy1105_006d_lc7.ui.locations

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.dsy1105_006d_lc7.data.local.entity.LocationEntity
import com.example.dsy1105_006d_lc7.data.repository.InventoryRepository
import kotlinx.coroutines.launch

data class LocationsUiState(
    val locations: List<LocationEntity> = emptyList(),
    val isLoading: Boolean = true,
    val message: String? = null
)

data class LocationFormState(
    val id: Long = 0,
    val zona: String = "",
    val estante: String = "",
    val repisa: String = "",
    val posicion: String = "",
    val isEditing: Boolean = false,
    val error: String? = null
)

class LocationsViewModel(private val repository: InventoryRepository) : ViewModel() {

    var uiState by mutableStateOf(LocationsUiState())
        private set

    var formState by mutableStateOf(LocationFormState())
        private set

    init {
        viewModelScope.launch {
            repository.getAllLocations().collect { locations ->
                uiState = uiState.copy(locations = locations, isLoading = false)
            }
        }
    }

    fun clearMessage() { uiState = uiState.copy(message = null) }

    fun initFormForCreate() { formState = LocationFormState() }

    fun initFormForEdit(location: LocationEntity) {
        formState = LocationFormState(
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
        if (formState.zona.isBlank()) { formState = formState.copy(error = "La zona es obligatoria"); return }
        if (formState.estante.isBlank()) { formState = formState.copy(error = "El estante es obligatorio"); return }
        if (formState.repisa.isBlank()) { formState = formState.copy(error = "La repisa es obligatoria"); return }

        viewModelScope.launch {
            try {
                val location = LocationEntity(
                    id = if (formState.isEditing) formState.id else 0,
                    zona = formState.zona.trim(),
                    estante = formState.estante.trim(),
                    repisa = formState.repisa.trim(),
                    posicion = formState.posicion.trim()
                )
                if (formState.isEditing) {
                    repository.updateLocation(location)
                    uiState = uiState.copy(message = "Ubicación actualizada")
                } else {
                    repository.insertLocation(location)
                    uiState = uiState.copy(message = "Ubicación creada")
                }
                onSuccess()
            } catch (e: Exception) {
                formState = formState.copy(error = "Error: ${e.message}")
            }
        }
    }

    fun deleteLocation(location: LocationEntity) {
        viewModelScope.launch {
            try {
                repository.deleteLocation(location)
                uiState = uiState.copy(message = "Ubicación eliminada")
            } catch (e: Exception) {
                uiState = uiState.copy(message = "Error: ${e.message}")
            }
        }
    }

    class Factory(private val repository: InventoryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LocationsViewModel(repository) as T
        }
    }
}
