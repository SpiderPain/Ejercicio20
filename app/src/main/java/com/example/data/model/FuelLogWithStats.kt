package com.example.data.model

import com.example.data.local.FuelLogEntity

/**
 * Modelo enriquecido que combina el registro en base de datos con sus métricas calculadas.
 *
 * PUNTO CRÍTICO DE ERROR:
 * Para la PRIMERA carga de la historia de un vehículo, 'deltaKm', 'kmPerGallon' y 'costPerKm'
 * serán null porque no existe una carga previa para medir cuántos kilómetros se recorrieron con ese tanque.
 * Quien intente forzar un cálculo de km/gal en la primera carga sin una referencia previa terminará
 * dividiendo el total del odómetro (ej: 15,000 km / 3 gal = 5,000 km/gal, lo cual es totalmente falso).
 */
data class FuelLogWithStats(
    val log: FuelLogEntity,
    val deltaKm: Double?,         // Kilómetros recorridos desde la carga inmediatamente anterior
    val kmPerGallon: Double?,     // deltaKm / gallons
    val costPerKm: Double?,       // amountPaid / deltaKm
    val pricePerGallon: Double    // amountPaid / gallons (precio pagado por galón en esa carga)
)
