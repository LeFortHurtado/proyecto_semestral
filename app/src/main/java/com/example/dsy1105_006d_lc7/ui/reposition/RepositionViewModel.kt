package com.example.dsy1105_006d_lc7.ui.reposition

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.dsy1105_006d_lc7.data.local.entity.ProductWithDetails
import com.example.dsy1105_006d_lc7.data.local.entity.SupplierEntity
import com.example.dsy1105_006d_lc7.data.repository.InventoryRepository
import kotlinx.coroutines.launch

/**
 * Datos de un producto que necesita reposición.
 */
data class RepositionItem(
    val product: ProductWithDetails,
    val cantidadSugerida: Int // existenciaMinima * 2 - cantidadDisponible
)

/**
 * Agrupación de productos a reponer por proveedor.
 */
data class SupplierRepositionGroup(
    val supplier: SupplierEntity?,
    val items: List<RepositionItem>
)

/**
 * Estado de la UI para la pantalla de reposición.
 */
data class RepositionUiState(
    val groups: List<SupplierRepositionGroup> = emptyList(),
    val isLoading: Boolean = true,
    val message: String? = null
)

/**
 * ViewModel para la gestión de reposición de inventario.
 * Calcula las cantidades sugeridas de compra y genera borradores
 * de correo electrónico para enviar a los proveedores mediante Intent implícito.
 */
class RepositionViewModel(private val repository: InventoryRepository) : ViewModel() {

    var uiState by mutableStateOf(RepositionUiState())
        private set

    init {
        loadRepositionData()
    }

    private fun loadRepositionData() {
        viewModelScope.launch {
            repository.getProductsBelowMinStockWithDetails().collect { products ->
                val items = products.map { prod ->
                    val sugerida = (prod.product.existenciaMinima * 2) - prod.product.cantidadDisponible
                    RepositionItem(
                        product = prod,
                        cantidadSugerida = if (sugerida > 0) sugerida else prod.product.existenciaMinima
                    )
                }

                // Agrupar por proveedor
                val groups = items.groupBy { it.product.supplier }.map { (supplier, groupItems) ->
                    SupplierRepositionGroup(supplier = supplier, items = groupItems)
                }.sortedBy { it.supplier?.nombreFicticio ?: "ZZZ" }

                uiState = uiState.copy(groups = groups, isLoading = false)
            }
        }
    }

    fun clearMessage() {
        uiState = uiState.copy(message = null)
    }

    /**
     * Genera un Intent implícito para abrir el cliente de correo del dispositivo
     * con un borrador precargado: destinatario, asunto y listado de productos.
     * El envío requiere intervención manual del usuario (regla de negocio).
     */
    fun generateEmailDraft(context: Context, group: SupplierRepositionGroup) {
        val supplier = group.supplier
        val email = supplier?.correoFicticio ?: ""
        val supplierName = supplier?.nombreFicticio ?: "Proveedor Desconocido"

        val subject = "Solicitud de Cotización - Taller Mecánico Puente Alto"

        val body = buildString {
            appendLine("Estimado/a $supplierName,")
            appendLine()
            appendLine("Solicito una cotización para los siguientes productos:")
            appendLine()
            appendLine("─────────────────────────────────────────")
            group.items.forEachIndexed { index, item ->
                val prod = item.product.product
                appendLine("${index + 1}. ${prod.nombre}")
                appendLine("   Código: ${prod.codigoInterno}")
                appendLine("   Cantidad requerida: ${item.cantidadSugerida} unidades")
                appendLine()
            }
            appendLine("─────────────────────────────────────────")
            appendLine()
            appendLine("Agradezco su pronta respuesta con precios y disponibilidad.")
            appendLine()
            appendLine("Saludos cordiales,")
            appendLine("Taller Mecánico Puente Alto")
        }

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        try {
            context.startActivity(Intent.createChooser(intent, "Enviar cotización"))
            uiState = uiState.copy(message = "Borrador generado para $supplierName")
        } catch (e: Exception) {
            uiState = uiState.copy(message = "No se encontró un cliente de correo instalado")
        }
    }

    // ── Factory ──

    class Factory(private val repository: InventoryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RepositionViewModel(repository) as T
        }
    }
}
