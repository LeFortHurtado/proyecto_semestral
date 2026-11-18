package com.example.dsy1105_006d_lc7.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dsy1105_006d_lc7.model.MenuPrincipalUiState
import com.example.dsy1105_006d_lc7.model.data.local.BaseDeDatos
import kotlinx.coroutines.launch

/**
 * ViewModel para el Menú Principal (Dashboard).
 * Obtiene métricas y movimientos recientes directamente desde Room.
 */
class MenuPrincipalViewModel(app: Application) : AndroidViewModel(app) {

    private val db = BaseDeDatos.getDatabase(app)
    private val productoDao = db.productoDao()
    private val proveedorDao = db.proveedorDao()
    private val movimientoDao = db.movimientoDao()

    var uiState by mutableStateOf(MenuPrincipalUiState())
        private set

    init {
        cargarDatosResumen()
    }

    private fun cargarDatosResumen() {
        viewModelScope.launch {
            productoDao.getAllProducts().collect { productos ->
                uiState = uiState.copy(totalProducts = productos.size)
            }
        }
        viewModelScope.launch {
            productoDao.getProductsBelowMinStock().collect { criticos ->
                uiState = uiState.copy(lowStockCount = criticos.size)
            }
        }
        viewModelScope.launch {
            productoDao.getAllCategories().collect { categorias ->
                uiState = uiState.copy(totalCategories = categorias.size)
            }
        }
        viewModelScope.launch {
            proveedorDao.getAllSuppliers().collect { proveedores ->
                uiState = uiState.copy(totalSuppliers = proveedores.size)
            }
        }
        viewModelScope.launch {
            movimientoDao.getRecentMovementsWithProduct(5).collect { recientes ->
                uiState = uiState.copy(recentMovements = recientes)
            }
        }
    }
}

typealias DashboardViewModel = MenuPrincipalViewModel
