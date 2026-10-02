package com.example.dsy1105_006d_lc7.data.remote

import com.example.dsy1105_006d_lc7.data.local.entity.MovementEntity
import com.example.dsy1105_006d_lc7.data.local.entity.ProductEntity
import com.example.dsy1105_006d_lc7.data.local.entity.SupplierEntity
import com.example.dsy1105_006d_lc7.data.local.entity.LocationEntity
import retrofit2.Response
import retrofit2.http.*

/**
 * Interfaz de API REST para comunicación con el backend Spring Boot.
 * Preparada para la Fase 5 de integración con microservicios.
 * Actualmente la app funciona Offline-First con Room.
 */
interface InventoryApiService {

    // ── Productos ──

    @GET("api/productos")
    suspend fun getAllProducts(): Response<List<ProductEntity>>

    @GET("api/productos/{id}")
    suspend fun getProductById(@Path("id") id: Long): Response<ProductEntity>

    @POST("api/productos")
    suspend fun createProduct(@Body product: ProductEntity): Response<ProductEntity>

    @PUT("api/productos/{id}")
    suspend fun updateProduct(@Path("id") id: Long, @Body product: ProductEntity): Response<ProductEntity>

    @DELETE("api/productos/{id}")
    suspend fun deleteProduct(@Path("id") id: Long): Response<Unit>

    // ── Movimientos ──

    @GET("api/movimientos")
    suspend fun getAllMovements(): Response<List<MovementEntity>>

    @POST("api/movimientos")
    suspend fun createMovement(@Body movement: MovementEntity): Response<MovementEntity>

    // ── Proveedores ──

    @GET("api/proveedores")
    suspend fun getAllSuppliers(): Response<List<SupplierEntity>>

    @POST("api/proveedores")
    suspend fun createSupplier(@Body supplier: SupplierEntity): Response<SupplierEntity>

    // ── Ubicaciones ──

    @GET("api/ubicaciones")
    suspend fun getAllLocations(): Response<List<LocationEntity>>

    @POST("api/ubicaciones")
    suspend fun createLocation(@Body location: LocationEntity): Response<LocationEntity>
}
