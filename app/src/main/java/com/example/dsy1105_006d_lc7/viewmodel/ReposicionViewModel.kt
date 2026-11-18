package com.example.dsy1105_006d_lc7.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dsy1105_006d_lc7.model.ReposicionUiState
import com.example.dsy1105_006d_lc7.model.RepositionItem
import com.example.dsy1105_006d_lc7.model.SupplierRepositionGroup
import com.example.dsy1105_006d_lc7.model.data.local.BaseDeDatos
import kotlinx.coroutines.launch

/**
 * ViewModel para la gestión de reposición de inventario crítico.
 * Identifica repuestos bajo stock mínimo y genera borradores de correo mediante Intent implícito.
 */
class ReposicionViewModel(app: Application) : AndroidViewModel(app) {

    private val productoDao = BaseDeDatos.getDatabase(app).productoDao()

    var uiState by mutableStateOf(ReposicionUiState())
        private set

    init {
        loadRepositionData()
    }

    private fun loadRepositionData() {
        viewModelScope.launch {
            productoDao.getProductsBelowMinStockWithDetails().collect { products ->
                val items = products.map { prod ->
                    val sugerida = (prod.product.existenciaMinima * 2) - prod.product.cantidadDisponible
                    RepositionItem(
                        product = prod,
                        cantidadSugerida = if (sugerida > 0) sugerida else prod.product.existenciaMinima
                    )
                }

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
}

typealias RepositionViewModel = ReposicionViewModel
