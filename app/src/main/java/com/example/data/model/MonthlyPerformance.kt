package com.example.data.model

/**
 * Agrupación mensual del rendimiento para responder directamente:
 * "¿La moto rinde igual que el mes pasado?".
 *
 * PUNTO CRÍTICO DE ERROR:
 * Al calcular el promedio mensual de km/galón, NUNCA se debe promediar los promedios de cada carga
 * (la media de medias falsea el resultado si las cargas tuvieron diferentes cantidades de galones).
 * La fórmula correcta de ingeniería es:
 *     kmPerGallon = totalKmRecorridosEnElMes / totalGalonesConsumidosEnElMes
 *     costPerKm = totalMontoGastadoEnElMes / totalKmRecorridosEnElMes
 */
data class MonthlyPerformance(
    val yearMonth: String,          // Ej. "2026-09" (clave de ordenamiento ISO)
    val displayMonth: String,       // Ej. "Sep 2026"
    val totalKmDriven: Double,      // Suma de deltaKm en este mes
    val totalGallons: Double,       // Suma de galones cargados en este mes
    val totalAmountPaid: Double,    // Suma de dinero gastado en este mes
    val kmPerGallon: Double,        // Rendimiento consolidado del mes
    val costPerKm: Double,          // Costo consolidado por km en el mes
    val logsCount: Int              // Número de cargas en el mes
)
