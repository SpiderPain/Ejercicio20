package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) para las operaciones de combustible.
 *
 * PUNTO CRÍTICO DE ERROR:
 * Las consultas para calcular rendimientos DEBEN ordenarse por 'odometerKm ASC' (o 'dateMillis ASC').
 * Si se ordenan en sentido descendente (DESC) sin invertir luego la lista, al calcular
 * (km_actual - km_anterior) se obtendría un número negativo, rompiendo toda la lógica de km/galón.
 */
@Dao
interface MotContDao {

    @Query("SELECT * FROM fuel_logs WHERE vehicleType = :vehicleType ORDER BY odometerKm ASC, dateMillis ASC")
    fun getLogsByVehicle(vehicleType: String): Flow<List<FuelLogEntity>>

    @Query("SELECT * FROM fuel_logs ORDER BY odometerKm ASC, dateMillis ASC")
    fun getAllLogs(): Flow<List<FuelLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: FuelLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<FuelLogEntity>)

    @Query("SELECT * FROM fuel_logs ORDER BY odometerKm ASC, dateMillis ASC")
    suspend fun getAllLogsList(): List<FuelLogEntity>

    @Query("DELETE FROM fuel_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long)

    @Query("DELETE FROM fuel_logs")
    suspend fun deleteAllLogs()

    @Query("SELECT COUNT(*) FROM fuel_logs")
    suspend fun getLogsCount(): Int
}
