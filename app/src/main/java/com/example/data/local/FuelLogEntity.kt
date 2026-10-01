package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa un registro individual de carga de combustible en Room.
 *
 * PUNTO CRÍTICO DE ERROR:
 * 1. No guardar el odómetro como un número acumulado erróneo: 'odometerKm' debe ser el valor
 *    absoluto que marca el tablero de la moto o carro en ese momento exacto, NO los kilómetros del viaje parcial (trip),
 *    para poder calcular con precisión la diferencia (delta) entre cargas consecutivas.
 * 2. Guardar 'gallons' y 'amountPaid' de forma independiente permite calcular tanto el precio por galón
 *    como el costo por kilómetro y el rendimiento real en km/galón.
 */
@Entity(tableName = "fuel_logs")
data class FuelLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // "MOTO" o "CARRO" para filtrar por tipo de vehículo
    val vehicleType: String,

    // Lectura del odómetro total en el momento de la carga (en kilómetros)
    val odometerKm: Double,

    // Monto total en dinero pagado en la gasolinera
    val amountPaid: Double,

    // Cantidad exacta de galones suministrados
    val gallons: Double,

    // Timestamp en milisegundos para ordenamiento cronológico preciso
    val dateMillis: Long,

    // Fecha en formato legible "yyyy-MM-dd" para agrupar fácilmente por mes sin desfases de zona horaria
    val dateString: String,

    // Nota opcional (ej. "Gasolina Súper", "Carretera", "Ciudad")
    val notes: String = ""
)
