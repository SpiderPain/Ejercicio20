package com.example.data.backup

import com.example.data.local.FuelLogEntity
import org.json.JSONArray
import org.json.JSONObject

/**
 * Gestor de respaldo y exportación a JSON.
 *
 * Utiliza org.json integrado nativamente en el SDK de Android (sin librerías externas ni de pago).
 */
object BackupManager {

    /**
     * Convierte la lista de cargas de combustible a un String con formato JSON legible.
     */
    fun exportToJson(logs: List<FuelLogEntity>): String {
        val root = JSONObject()
        root.put("app", "MotCont")
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())
        root.put("totalRecords", logs.size)

        val jsonArray = JSONArray()
        for (log in logs) {
            val item = JSONObject().apply {
                put("id", log.id)
                put("vehicleType", log.vehicleType)
                put("odometerKm", log.odometerKm)
                put("amountPaid", log.amountPaid)
                put("gallons", log.gallons)
                put("dateMillis", log.dateMillis)
                put("dateString", log.dateString)
                put("notes", log.notes)
            }
            jsonArray.put(item)
        }
        root.put("fuelLogs", jsonArray)

        // Indentación de 2 espacios para que sea fácil de leer en cualquier visor de texto
        return root.toString(2)
    }

    /**
     * Parsea un String JSON de respaldo a entidades FuelLogEntity para restaurar o importar.
     */
    fun importFromJson(jsonString: String): List<FuelLogEntity> {
        val list = mutableListOf<FuelLogEntity>()
        val root = JSONObject(jsonString)
        val array = root.getJSONArray("fuelLogs")

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                FuelLogEntity(
                    id = 0, // Se asigna 0 para autogenerar nuevo ID local en Room
                    vehicleType = obj.optString("vehicleType", "MOTO"),
                    odometerKm = obj.getDouble("odometerKm"),
                    amountPaid = obj.getDouble("amountPaid"),
                    gallons = obj.getDouble("gallons"),
                    dateMillis = obj.optLong("dateMillis", System.currentTimeMillis()),
                    dateString = obj.optString("dateString", "2026-10-01"),
                    notes = obj.optString("notes", "")
                )
            )
        }
        return list
    }
}
