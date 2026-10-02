package com.example.dsy1105_006d_lc7.data.local.commit

import android.app.Application
import com.example.dsy1105_006d_lc7.TallerApp
import com.example.dsy1105_006d_lc7.data.local.AppDatabase
import com.example.dsy1105_006d_lc7.data.repository.InventoryRepository

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