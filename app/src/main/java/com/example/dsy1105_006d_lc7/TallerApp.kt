package com.example.dsy1105_006d_lc7

import android.app.Application
import com.example.dsy1105_006d_lc7.model.data.local.BaseDeDatos

/**
 * Clase Application que inicializa la base de datos Room singleton.
 */
class TallerApp : Application() {

    val database: BaseDeDatos by lazy { BaseDeDatos.getDatabase(this) }

    companion object {
        lateinit var instance: TallerApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
