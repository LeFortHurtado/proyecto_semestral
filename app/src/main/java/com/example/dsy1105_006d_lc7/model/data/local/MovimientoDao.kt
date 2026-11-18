package com.example.dsy1105_006d_lc7.model.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones de Movimientos de inventario.
 */
@Dao
interface MovimientoDao {

    @Insert
    suspend fun insert(movement: MovimientoEntity): Long

    @Update
    suspend fun update(movement: MovimientoEntity)

    @Delete
    suspend fun delete(movement: MovimientoEntity)

    @Query("SELECT * FROM movements WHERE id = :id")
    suspend fun getMovementById(id: Long): MovimientoEntity?

    @Query("SELECT * FROM movements ORDER BY fechaHora DESC")
    fun getAllMovements(): Flow<List<MovimientoEntity>>

    @Query("SELECT * FROM movements WHERE productoId = :productId ORDER BY fechaHora DESC")
    fun getMovementsByProduct(productId: Long): Flow<List<MovimientoEntity>>

    @Query("SELECT * FROM movements WHERE tipo = :type ORDER BY fechaHora DESC")
    fun getMovementsByType(type: String): Flow<List<MovimientoEntity>>

    @Query("SELECT * FROM movements WHERE fechaHora BETWEEN :startDate AND :endDate ORDER BY fechaHora DESC")
    fun getMovementsByDateRange(startDate: Long, endDate: Long): Flow<List<MovimientoEntity>>

    // ── Consultas con relaciones (Movimiento + Producto) ──
    @Transaction
    @Query("SELECT * FROM movements WHERE id = :id")
    suspend fun getMovementWithProductById(id: Long): MovimientoConProducto?

    @Transaction
    @Query("SELECT * FROM movements ORDER BY fechaHora DESC")
    fun getAllMovementsWithProduct(): Flow<List<MovimientoConProducto>>

    @Transaction
    @Query("SELECT * FROM movements WHERE productoId = :productId ORDER BY fechaHora DESC")
    fun getMovementsWithProductByProductId(productId: Long): Flow<List<MovimientoConProducto>>

    // ── Consultas para el Dashboard / Menú principal ──
    @Query("SELECT * FROM movements ORDER BY fechaHora DESC LIMIT :limit")
    fun getRecentMovements(limit: Int = 20): Flow<List<MovimientoEntity>>

    @Transaction
    @Query("SELECT * FROM movements ORDER BY fechaHora DESC LIMIT :limit")
    fun getRecentMovementsWithProduct(limit: Int = 20): Flow<List<MovimientoConProducto>>
}

typealias MovementDao = MovimientoDao
