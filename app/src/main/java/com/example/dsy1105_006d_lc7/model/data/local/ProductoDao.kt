package com.example.dsy1105_006d_lc7.model.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones de Productos / Repuestos del taller.
 */
@Dao
interface ProductoDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(product: ProductoEntity): Long

    @Update
    suspend fun update(product: ProductoEntity)

    @Delete
    suspend fun delete(product: ProductoEntity)

    @Query("SELECT * FROM products ORDER BY nombre ASC")
    fun getAllProducts(): Flow<List<ProductoEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): ProductoEntity?

    @Query("SELECT * FROM products WHERE codigoInterno = :code")
    suspend fun getProductByCode(code: String): ProductoEntity?

    @Query("SELECT * FROM products WHERE nombre LIKE '%' || :query || '%' OR codigoInterno LIKE '%' || :query || '%'")
    fun searchProducts(query: String): Flow<List<ProductoEntity>>

    @Query("SELECT * FROM products WHERE categoria = :category ORDER BY nombre ASC")
    fun getProductsByCategory(category: String): Flow<List<ProductoEntity>>

    @Query("SELECT DISTINCT categoria FROM products ORDER BY categoria ASC")
    fun getAllCategories(): Flow<List<String>>

    // ── Reposición: productos bajo stock mínimo ──
    @Query("SELECT * FROM products WHERE cantidadDisponible <= existenciaMinima AND estado = 'activo' ORDER BY nombre ASC")
    fun getProductsBelowMinStock(): Flow<List<ProductoEntity>>

    // ── Consultas con relaciones (Ubicación y Proveedor) ──
    @Transaction
    @Query("SELECT * FROM products ORDER BY nombre ASC")
    fun getAllProductsWithDetails(): Flow<List<ProductoConDetalles>>

    @Transaction
    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductWithDetailsById(id: Long): ProductoConDetalles?

    @Transaction
    @Query("SELECT * FROM products WHERE cantidadDisponible <= existenciaMinima AND estado = 'activo' ORDER BY nombre ASC")
    fun getProductsBelowMinStockWithDetails(): Flow<List<ProductoConDetalles>>

    // ── Actualización de stock de forma atómica ──
    @Query("UPDATE products SET cantidadDisponible = :newStock WHERE id = :productId")
    suspend fun updateStock(productId: Long, newStock: Int)
}

typealias ProductDao = ProductoDao
