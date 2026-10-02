package com.example.dsy1105_006d_lc7.data.local.dao

import androidx.room.*
import com.example.dsy1105_006d_lc7.data.local.entity.ProductEntity
import com.example.dsy1105_006d_lc7.data.local.entity.ProductWithDetails
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones CRUD de Productos.
 * Todas las consultas retornan Flow para observar cambios en tiempo real.
 */
@Dao
interface ProductDao {

    // ── CRUD básico ──

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: ProductEntity): Long

    @Update
    suspend fun update(product: ProductEntity)

    @Delete
    suspend fun delete(product: ProductEntity)

    // ── Consultas ──

    @Query("SELECT * FROM products ORDER BY nombre ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE codigoInterno = :code")
    suspend fun getProductByCode(code: String): ProductEntity?

    @Query("SELECT * FROM products WHERE nombre LIKE '%' || :query || '%' OR codigoInterno LIKE '%' || :query || '%'")
    fun searchProducts(query: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE categoria = :category ORDER BY nombre ASC")
    fun getProductsByCategory(category: String): Flow<List<ProductEntity>>

    @Query("SELECT DISTINCT categoria FROM products ORDER BY categoria ASC")
    fun getAllCategories(): Flow<List<String>>

    // ── Reposición: productos bajo stock mínimo ──

    @Query("SELECT * FROM products WHERE cantidadDisponible <= existenciaMinima AND estado = 'activo' ORDER BY nombre ASC")
    fun getProductsBelowMinStock(): Flow<List<ProductEntity>>

    // ── Consultas con relaciones ──

    @Transaction
    @Query("SELECT * FROM products ORDER BY nombre ASC")
    fun getAllProductsWithDetails(): Flow<List<ProductWithDetails>>

    @Transaction
    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductWithDetailsById(id: Long): ProductWithDetails?

    @Transaction
    @Query("SELECT * FROM products WHERE cantidadDisponible <= existenciaMinima AND estado = 'activo' ORDER BY nombre ASC")
    fun getProductsBelowMinStockWithDetails(): Flow<List<ProductWithDetails>>

    // ── Actualización de stock (usado transaccionalmente por el MovementDao) ──

    @Query("UPDATE products SET cantidadDisponible = :newStock WHERE id = :productId")
    suspend fun updateStock(productId: Long, newStock: Int)
}
