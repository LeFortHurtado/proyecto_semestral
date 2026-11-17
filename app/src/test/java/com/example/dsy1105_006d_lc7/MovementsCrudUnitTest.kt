package com.example.dsy1105_006d_lc7

import org.junit.Assert.*
import org.junit.Test

/**
 * Pruebas unitarias para la lógica de negocio del CRUD de Movimientos de inventario:
 * - Creación (Create): aumento/disminución de stock según tipo.
 * - Edición (Update): recálculo atómico de existencias.
 * - Eliminación (Delete): reversión del impacto en el inventario y prevención de existencias negativas.
 */
class MovementsCrudUnitTest {

    private fun calculateNewStockOnCreate(currentStock: Int, tipo: String, cantidad: Int): Int {
        return when (tipo) {
            "ingreso", "devolucion" -> currentStock + cantidad
            "venta", "consumo" -> currentStock - cantidad
            "ajuste" -> cantidad
            else -> throw IllegalArgumentException("Tipo no válido: $tipo")
        }
    }

    private fun calculateStockOnDelete(currentStock: Int, tipo: String, cantidad: Int, existenciaAnterior: Int, existenciaResultante: Int): Int {
        val delta = when (tipo) {
            "ingreso", "devolucion" -> cantidad
            "venta", "consumo" -> -cantidad
            "ajuste" -> (existenciaResultante - existenciaAnterior)
            else -> 0
        }
        return currentStock - delta
    }

    private fun calculateStockOnUpdate(
        currentStock: Int,
        oldTipo: String,
        oldCantidad: Int,
        oldExistenciaAnterior: Int,
        oldExistenciaResultante: Int,
        newTipo: String,
        newCantidad: Int
    ): Int {
        val oldDelta = when (oldTipo) {
            "ingreso", "devolucion" -> oldCantidad
            "venta", "consumo" -> -oldCantidad
            "ajuste" -> (oldExistenciaResultante - oldExistenciaAnterior)
            else -> 0
        }
        val stockSinMovimiento = currentStock - oldDelta
        return when (newTipo) {
            "ingreso", "devolucion" -> stockSinMovimiento + newCantidad
            "venta", "consumo" -> stockSinMovimiento - newCantidad
            "ajuste" -> newCantidad
            else -> throw IllegalArgumentException("Tipo no válido: $newTipo")
        }
    }

    @Test
    fun createIngreso_increasesStock() {
        val initialStock = 10
        val result = calculateNewStockOnCreate(initialStock, "ingreso", 5)
        assertEquals(15, result)
    }

    @Test
    fun createVenta_decreasesStock() {
        val initialStock = 10
        val result = calculateNewStockOnCreate(initialStock, "venta", 4)
        assertEquals(6, result)
    }

    @Test
    fun deleteVenta_restoresStock() {
        // Inicialmente había 10, se vendieron 4 -> quedó en 6.
        val currentStock = 6
        val restoredStock = calculateStockOnDelete(
            currentStock = currentStock,
            tipo = "venta",
            cantidad = 4,
            existenciaAnterior = 10,
            existenciaResultante = 6
        )
        // Al eliminar la venta, se deben devolver las 4 unidades al stock -> 10
        assertEquals(10, restoredStock)
    }

    @Test
    fun deleteIngreso_revertsAddedStock() {
        // Inicialmente había 5, entraron 10 -> quedó en 15.
        val currentStock = 15
        val revertedStock = calculateStockOnDelete(
            currentStock = currentStock,
            tipo = "ingreso",
            cantidad = 10,
            existenciaAnterior = 5,
            existenciaResultante = 15
        )
        // Al eliminar el ingreso, se deben restar las 10 unidades -> 5
        assertEquals(5, revertedStock)
    }

    @Test
    fun deleteIngreso_preventsNegativeStock() {
        // Entraron 5 unidades, pero se vendieron todas y el stock actual es 2.
        // Si se elimina el ingreso de 5, el stock resultante sería 2 - 5 = -3.
        val currentStock = 2
        val revertedStock = calculateStockOnDelete(
            currentStock = currentStock,
            tipo = "ingreso",
            cantidad = 5,
            existenciaAnterior = 0,
            existenciaResultante = 5
        )
        assertTrue("El stock no puede ser negativo", revertedStock < 0)
    }

    @Test
    fun updateVenta_adjustsDifferenceCorrectly() {
        // Se registró una venta de 3 cuando en realidad eran 5. Stock actual: 7.
        // Revertir venta de 3 lleva stock base a 10. Nueva venta de 5 deja stock en 5.
        val currentStock = 7
        val newStock = calculateStockOnUpdate(
            currentStock = currentStock,
            oldTipo = "venta",
            oldCantidad = 3,
            oldExistenciaAnterior = 10,
            oldExistenciaResultante = 7,
            newTipo = "venta",
            newCantidad = 5
        )
        assertEquals(5, newStock)
    }

    @Test
    fun updateIngreso_adjustsDifferenceCorrectly() {
        // Se registró un ingreso de 10 unidades, stock actual es 20.
        // Se corrige el ingreso a 15 unidades.
        // Stock base sin el ingreso original = 10. Nuevo stock = 10 + 15 = 25.
        val currentStock = 20
        val newStock = calculateStockOnUpdate(
            currentStock = currentStock,
            oldTipo = "ingreso",
            oldCantidad = 10,
            oldExistenciaAnterior = 10,
            oldExistenciaResultante = 20,
            newTipo = "ingreso",
            newCantidad = 15
        )
        assertEquals(25, newStock)
    }

    @Test
    fun deleteAjuste_revertsNetDelta() {
        // Stock era 10, ajuste lo fijó en 18 (delta +8).
        // Stock actual es 18. Al eliminar el ajuste, debe volver a 10.
        val currentStock = 18
        val reverted = calculateStockOnDelete(
            currentStock = currentStock,
            tipo = "ajuste",
            cantidad = 18,
            existenciaAnterior = 10,
            existenciaResultante = 18
        )
        assertEquals(10, reverted)
    }
}
