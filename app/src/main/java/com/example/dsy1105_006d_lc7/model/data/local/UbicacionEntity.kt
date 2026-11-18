package com.example.dsy1105_006d_lc7.model.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa una ubicación física en la bodega del taller.
 * Cada ubicación se identifica por zona, estante, repisa y posición.
 */
@Entity(tableName = "locations")
data class UbicacionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val zona: String,        // Ejemplo: "Zona A", "Zona B"
    val estante: String,     // Ejemplo: "Estante 1", "Estante 2"
    val repisa: String,      // Ejemplo: "Repisa Superior", "Repisa Inferior"
    val posicion: String = "" // Posición específica dentro de la repisa (opcional)
)

typealias LocationEntity = UbicacionEntity
