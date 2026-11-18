package com.example.dsy1105_006d_lc7.model.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Base de datos SQLite gestionada con Room.
 * Contiene las tablas del inventario del taller mecánico.
 * Precarga datos de prueba la primera vez que se ejecuta la aplicación.
 */
@Database(
    entities = [
        ProductoEntity::class,
        MovimientoEntity::class,
        UbicacionEntity::class,
        ProveedorEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BaseDeDatos : RoomDatabase() {

    abstract fun productoDao(): ProductoDao
    abstract fun movimientoDao(): MovimientoDao
    abstract fun ubicacionDao(): UbicacionDao
    abstract fun proveedorDao(): ProveedorDao

    // Alias para compatibilidad
    fun productDao(): ProductoDao = productoDao()
    fun movementDao(): MovimientoDao = movimientoDao()
    fun locationDao(): UbicacionDao = ubicacionDao()
    fun supplierDao(): ProveedorDao = proveedorDao()

    companion object {
        @Volatile
        private var INSTANCIA: BaseDeDatos? = null

        fun getDatabase(context: Context): BaseDeDatos {
            return INSTANCIA ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BaseDeDatos::class.java,
                    "taller_inventario_db"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCIA = instance
                instance
            }
        }
    }

    private class DatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCIA?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    precargarDatosPrueba(database)
                }
            }
        }

        private suspend fun precargarDatosPrueba(db: BaseDeDatos) {
            // ── Ubicaciones ficticias ──
            val loc1 = db.ubicacionDao().insert(UbicacionEntity(zona = "Zona A", estante = "Estante 1", repisa = "Repisa Superior", posicion = "Pos 1"))
            val loc2 = db.ubicacionDao().insert(UbicacionEntity(zona = "Zona A", estante = "Estante 1", repisa = "Repisa Inferior", posicion = "Pos 2"))
            val loc3 = db.ubicacionDao().insert(UbicacionEntity(zona = "Zona A", estante = "Estante 2", repisa = "Repisa Superior", posicion = "Pos 1"))
            val loc4 = db.ubicacionDao().insert(UbicacionEntity(zona = "Zona B", estante = "Estante 1", repisa = "Repisa Media", posicion = "Pos 1"))
            val loc5 = db.ubicacionDao().insert(UbicacionEntity(zona = "Zona B", estante = "Estante 2", repisa = "Repisa Inferior", posicion = "Pos 3"))
            val loc6 = db.ubicacionDao().insert(UbicacionEntity(zona = "Zona C", estante = "Estante 1", repisa = "Repisa Superior", posicion = "Pos 1"))

            // ── Proveedores ficticios ──
            val sup1 = db.proveedorDao().insert(ProveedorEntity(nombreFicticio = "AutoPartes Ficticias SpA", correoFicticio = "contacto@autopartes-ficticias.cl", telefonoFicticio = "+56 9 1111 2222"))
            val sup2 = db.proveedorDao().insert(ProveedorEntity(nombreFicticio = "Lubricantes del Sur Ltda", correoFicticio = "ventas@lubricantes-sur.cl", telefonoFicticio = "+56 9 3333 4444"))
            val sup3 = db.proveedorDao().insert(ProveedorEntity(nombreFicticio = "Frenos y Discos Chile SA", correoFicticio = "pedidos@frenosdiscos.cl", telefonoFicticio = "+56 9 5555 6666"))
            val sup4 = db.proveedorDao().insert(ProveedorEntity(nombreFicticio = "Electro Auto Demo EIRL", correoFicticio = "ventas@electroauto-demo.cl", telefonoFicticio = "+56 9 7777 8888"))

            // ── Productos ficticios ──
            db.productoDao().insert(ProductoEntity(codigoInterno = "FIL-001", nombre = "Filtro de aceite genérico", descripcion = "Filtro de aceite compatible con múltiples modelos", categoria = "Filtros", cantidadDisponible = 15, existenciaMinima = 5, precioCompra = 3500.0, precioVenta = 6500.0, proveedorId = sup1, ubicacionId = loc1))
            db.productoDao().insert(ProductoEntity(codigoInterno = "FIL-002", nombre = "Filtro de aire estándar", descripcion = "Filtro de aire para vehículos sedan", categoria = "Filtros", cantidadDisponible = 8, existenciaMinima = 3, precioCompra = 4200.0, precioVenta = 7800.0, proveedorId = sup1, ubicacionId = loc2))
            db.productoDao().insert(ProductoEntity(codigoInterno = "ACE-001", nombre = "Aceite motor 5W-30 sintético", descripcion = "Aceite sintético 1 litro para motores modernos", categoria = "Aceites", cantidadDisponible = 20, existenciaMinima = 10, precioCompra = 5800.0, precioVenta = 9500.0, proveedorId = sup2, ubicacionId = loc3))
            db.productoDao().insert(ProductoEntity(codigoInterno = "ACE-002", nombre = "Aceite motor 10W-40 semisintético", descripcion = "Aceite semisintético 1 litro uso general", categoria = "Aceites", cantidadDisponible = 12, existenciaMinima = 8, precioCompra = 4500.0, precioVenta = 7500.0, proveedorId = sup2, ubicacionId = loc3))
            db.productoDao().insert(ProductoEntity(codigoInterno = "FRE-001", nombre = "Pastillas de freno delanteras", descripcion = "Juego de pastillas de freno delanteras universales", categoria = "Frenos", cantidadDisponible = 6, existenciaMinima = 4, precioCompra = 12000.0, precioVenta = 22000.0, proveedorId = sup3, ubicacionId = loc4))
            db.productoDao().insert(ProductoEntity(codigoInterno = "FRE-002", nombre = "Disco de freno ventilado", descripcion = "Disco de freno ventilado delantero", categoria = "Frenos", cantidadDisponible = 3, existenciaMinima = 2, precioCompra = 18000.0, precioVenta = 32000.0, proveedorId = sup3, ubicacionId = loc4))
            db.productoDao().insert(ProductoEntity(codigoInterno = "ELE-001", nombre = "Batería 12V 60Ah", descripcion = "Batería automotriz de arranque", categoria = "Eléctrico", cantidadDisponible = 4, existenciaMinima = 2, precioCompra = 45000.0, precioVenta = 72000.0, proveedorId = sup4, ubicacionId = loc5))
            db.productoDao().insert(ProductoEntity(codigoInterno = "ELE-002", nombre = "Bujía de encendido estándar", descripcion = "Bujía de encendido para motores a gasolina", categoria = "Eléctrico", cantidadDisponible = 25, existenciaMinima = 10, precioCompra = 1800.0, precioVenta = 3500.0, proveedorId = sup4, ubicacionId = loc6))
            db.productoDao().insert(ProductoEntity(codigoInterno = "SUS-001", nombre = "Amortiguador delantero", descripcion = "Amortiguador hidráulico delantero", categoria = "Suspensión", cantidadDisponible = 2, existenciaMinima = 2, precioCompra = 28000.0, precioVenta = 48000.0, proveedorId = sup1, ubicacionId = loc5))
            db.productoDao().insert(ProductoEntity(codigoInterno = "REF-001", nombre = "Refrigerante motor verde", descripcion = "Refrigerante concentrado 1 litro", categoria = "Refrigerantes", cantidadDisponible = 10, existenciaMinima = 5, precioCompra = 3200.0, precioVenta = 5800.0, proveedorId = sup2, ubicacionId = loc6))
        }
    }
}

typealias AppDatabase = BaseDeDatos
