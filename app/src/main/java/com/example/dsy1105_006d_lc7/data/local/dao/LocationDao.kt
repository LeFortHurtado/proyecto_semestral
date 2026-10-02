package com.example.dsy1105_006d_lc7.data.local.dao

import androidx.room.*
import com.example.dsy1105_006d_lc7.data.local.entity.LocationEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones CRUD de Ubicaciones de bodega.
 */
@Dao
interface LocationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: LocationEntity): Long

    @Update
    suspend fun update(location: LocationEntity)

    @Delete
    suspend fun delete(location: LocationEntity)

    @Query("SELECT * FROM locations ORDER BY zona ASC, estante ASC, repisa ASC")
    fun getAllLocations(): Flow<List<LocationEntity>>

    @Query("SELECT * FROM locations WHERE id = :id")
    suspend fun getLocationById(id: Long): LocationEntity?

    @Query("SELECT DISTINCT zona FROM locations ORDER BY zona ASC")
    fun getAllZones(): Flow<List<String>>

    @Query("SELECT * FROM locations WHERE zona = :zone ORDER BY estante ASC, repisa ASC")
    fun getLocationsByZone(zone: String): Flow<List<LocationEntity>>
}
