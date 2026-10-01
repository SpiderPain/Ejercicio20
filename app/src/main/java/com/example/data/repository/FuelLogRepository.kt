package com.example.data.repository

import com.example.data.local.FuelLogEntity
import com.example.data.local.MotContDao
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio que desacopla la fuente de datos Room de los ViewModels y la interfaz.
 */
class FuelLogRepository(private val dao: MotContDao) {

    fun getLogsByVehicle(vehicleType: String): Flow<List<FuelLogEntity>> {
        return dao.getLogsByVehicle(vehicleType)
    }

    fun getAllLogs(): Flow<List<FuelLogEntity>> {
        return dao.getAllLogs()
    }

    suspend fun getAllLogsList(): List<FuelLogEntity> {
        return dao.getAllLogsList()
    }

    suspend fun insertLog(log: FuelLogEntity): Long {
        return dao.insertLog(log)
    }

    suspend fun insertAll(logs: List<FuelLogEntity>) {
        dao.insertAll(logs)
    }

    suspend fun deleteLog(id: Long) {
        dao.deleteLogById(id)
    }

    suspend fun deleteAllLogs() {
        dao.deleteAllLogs()
    }
}
