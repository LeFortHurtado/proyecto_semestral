package com.example.dsy1105_006d_lc7.ui.dashboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.dsy1105_006d_lc7.data.local.entity.MovementWithProduct
import com.example.dsy1105_006d_lc7.data.repository.InventoryRepository
import kotlinx.coroutines.launch

data class DashboardUiState(
    val totalProducts: Int = 0,
    val lowStockCount: Int = 0,
    val totalCategories: Int = 0,
    val totalSuppliers: Int = 0,
    val recentMovements: List<MovementWithProduct> = emptyList()
)

class DashboardViewModel(private val repository: InventoryRepository) : ViewModel() {

    var uiState by mutableStateOf(DashboardUiState())
        private set

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            repository.getAllProducts().collect { products ->
                uiState = uiState.copy(totalProducts = products.size)
            }
        }
        viewModelScope.launch {
            repository.getProductsBelowMinStock().collect { products ->
                uiState = uiState.copy(lowStockCount = products.size)
            }
        }
        viewModelScope.launch {
            repository.getAllCategories().collect { categories ->
                uiState = uiState.copy(totalCategories = categories.size)
            }
        }
        viewModelScope.launch {
            repository.getAllSuppliers().collect { suppliers ->
                uiState = uiState.copy(totalSuppliers = suppliers.size)
            }
        }
        viewModelScope.launch {
            repository.getRecentMovementsWithProduct(5).collect { movements ->
                uiState = uiState.copy(recentMovements = movements)
            }
        }
    }

    class Factory(private val repository: InventoryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(repository) as T
        }
    }
}
