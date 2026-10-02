package com.example.dsy1105_006d_lc7

import android.app.Application
import com.example.dsy1105_006d_lc7.data.local.AppDatabase
import com.example.dsy1105_006d_lc7.data.repository.InventoryRepository

/**
 * Clase Application personalizada que inicializa la base de datos
 * y el repositorio como singletons accesibles en toda la app.
 */
class TallerApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val repository: InventoryRepository by lazy { InventoryRepository(database) }

    companion object {
        lateinit var instance: TallerApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
