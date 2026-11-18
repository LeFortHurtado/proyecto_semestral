package com.example.dsy1105_006d_lc7.model.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones CRUD de Proveedores ficticios.
 */
@Dao
interface ProveedorDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(supplier: ProveedorEntity): Long

    @Update
    suspend fun update(supplier: ProveedorEntity)

    @Delete
    suspend fun delete(supplier: ProveedorEntity)

    @Query("SELECT * FROM suppliers ORDER BY nombreFicticio ASC")
    fun getAllSuppliers(): Flow<List<ProveedorEntity>>

    @Query("SELECT * FROM suppliers WHERE id = :id")
    suspend fun getSupplierById(id: Long): ProveedorEntity?

    @Query("SELECT * FROM suppliers WHERE nombreFicticio LIKE '%' || :query || '%'")
    fun searchSuppliers(query: String): Flow<List<ProveedorEntity>>
}

typealias SupplierDao = ProveedorDao
