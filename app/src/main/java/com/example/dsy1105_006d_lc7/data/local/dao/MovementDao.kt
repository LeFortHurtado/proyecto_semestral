package com.example.dsy1105_006d_lc7.data.local.dao

import androidx.room.*
import com.example.dsy1105_006d_lc7.data.local.entity.MovementEntity
import com.example.dsy1105_006d_lc7.data.local.entity.MovementWithProduct
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones de Movimientos de inventario.
 * Incluye operación transaccional para registrar movimientos y
 * actualizar el stock del producto atómicamente.
 */
@Dao
interface MovementDao {

    @Insert
    suspend fun insert(movement: MovementEntity): Long

    @Query("SELECT * FROM movements ORDER BY fechaHora DESC")
    fun getAllMovements(): Flow<List<MovementEntity>>

    @Query("SELECT * FROM movements WHERE productoId = :productId ORDER BY fechaHora DESC")
    fun getMovementsByProduct(productId: Long): Flow<List<MovementEntity>>

    @Query("SELECT * FROM movements WHERE tipo = :type ORDER BY fechaHora DESC")
    fun getMovementsByType(type: String): Flow<List<MovementEntity>>

    @Query("SELECT * FROM movements WHERE fechaHora BETWEEN :startDate AND :endDate ORDER BY fechaHora DESC")
    fun getMovementsByDateRange(startDate: Long, endDate: Long): Flow<List<MovementEntity>>

    // ── Consultas con relaciones ──

    @Transaction
    @Query("SELECT * FROM movements ORDER BY fechaHora DESC")
    fun getAllMovementsWithProduct(): Flow<List<MovementWithProduct>>

    @Transaction
    @Query("SELECT * FROM movements WHERE productoId = :productId ORDER BY fechaHora DESC")
    fun getMovementsWithProductByProductId(productId: Long): Flow<List<MovementWithProduct>>

    // ── Últimos N movimientos ──

    @Query("SELECT * FROM movements ORDER BY fechaHora DESC LIMIT :limit")
    fun getRecentMovements(limit: Int = 20): Flow<List<MovementEntity>>

    @Transaction
    @Query("SELECT * FROM movements ORDER BY fechaHora DESC LIMIT :limit")
    fun getRecentMovementsWithProduct(limit: Int = 20): Flow<List<MovementWithProduct>>
}
