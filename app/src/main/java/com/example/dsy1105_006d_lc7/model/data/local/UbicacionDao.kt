package com.example.dsy1105_006d_lc7.model.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones CRUD de Ubicaciones físicas.
 */
@Dao
interface UbicacionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: UbicacionEntity): Long

    @Update
    suspend fun update(location: UbicacionEntity)

    @Delete
    suspend fun delete(location: UbicacionEntity)

    @Query("SELECT * FROM locations ORDER BY zona ASC, estante ASC, repisa ASC")
    fun getAllLocations(): Flow<List<UbicacionEntity>>

    @Query("SELECT * FROM locations WHERE id = :id")
    suspend fun getLocationById(id: Long): UbicacionEntity?

    @Query("SELECT DISTINCT zona FROM locations ORDER BY zona ASC")
    fun getAllZones(): Flow<List<String>>

    @Query("SELECT * FROM locations WHERE zona = :zone ORDER BY estante ASC, repisa ASC")
    fun getLocationsByZone(zone: String): Flow<List<UbicacionEntity>>
}

typealias LocationDao = UbicacionDao
