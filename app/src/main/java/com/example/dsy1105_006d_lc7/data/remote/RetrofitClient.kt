package com.example.dsy1105_006d_lc7.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Singleton para la instancia de Retrofit.
 * La URL base apunta a un servidor de prueba local (Spring Boot).
 * En producción, cambiar a la URL del servidor real.
 *
 * Nota: La app funciona Offline-First, esta conexión es opcional
 * y se usa solo para sincronización cuando hay internet disponible.
 */
object RetrofitClient {

    // URL del servidor de prueba Spring Boot (emulador Android apunta a 10.0.2.2)
    private const val BASE_URL = "http://10.0.2.2:8080/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: InventoryApiService by lazy {
        retrofit.create(InventoryApiService::class.java)
    }
}
