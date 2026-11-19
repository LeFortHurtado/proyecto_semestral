package com.example.dsy1105_006d_lc7.model.data.remote

import com.example.dsy1105_006d_lc7.model.data.local.ProveedorEntity
import retrofit2.http.GET

interface ApiService {

    @GET("api/proveedores")
    suspend fun getProveedores(): List<ProveedorEntity>
}