package com.example.data.remote

import com.example.BuildConfig
import com.example.data.model.AiDiagnosis
import com.example.data.model.AiRecommendation
import com.example.data.model.MonthlyPerformance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Servicio para consultar la API de Gemini 3.5 Flash con respuesta estructurada JSON (responseSchema).
 */
class GeminiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .writeTimeout(25, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val MODEL = "gemini-3.5-flash"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

        /**
         * Esquema estricto JSON que le exigimos a Gemini (responseSchema).
         */
        val RESPONSE_SCHEMA_JSON = JSONObject().apply {
            put("type", "OBJECT")
            put("properties", JSONObject().apply {
                put("dropDetected", JSONObject().put("type", "BOOLEAN"))
                put("dropMonth", JSONObject().put("type", "STRING"))
                put("previousMonth", JSONObject().put("type", "STRING"))
                put("dropPercentage", JSONObject().put("type", "NUMBER"))
                put("severity", JSONObject().apply {
                    put("type", "STRING")
                    put("enum", JSONArray().apply {
                        put("LEVE")
                        put("MODERADA")
                        put("ALTA")
                        put("NINGUNA")
                    })
                })
                put("summary", JSONObject().put("type", "STRING"))
                put("recommendations", JSONObject().apply {
                    put("type", "ARRAY")
                    put("items", JSONObject().apply {
                        put("type", "OBJECT")
                        put("properties", JSONObject().apply {
                            put("priority", JSONObject().put("type", "INTEGER"))
                            put("component", JSONObject().put("type", "STRING"))
                            put("estimatedCost", JSONObject().put("type", "STRING"))
                            put("costLevel", JSONObject().apply {
                                put("type", "STRING")
                                put("enum", JSONArray().apply {
                                    put("GRATIS")
                                    put("BAJO")
                                    put("MEDIO")
                                    put("ALTO")
                                })
                            })
                            put("reason", JSONObject().put("type", "STRING"))
                            put("howToCheck", JSONObject().put("type", "STRING"))
                        })
                        put("required", JSONArray().apply {
                            put("priority")
                            put("component")
                            put("estimatedCost")
                            put("costLevel")
                            put("reason")
                            put("howToCheck")
                        })
                    })
                })
            })
            put("required", JSONArray().apply {
                put("dropDetected")
                put("dropMonth")
                put("dropPercentage")
                put("severity")
                put("summary")
                put("recommendations")
            })
        }

        /**
         * Diagnóstico de prueba estático para desarrollar y probar la UI sin gastar llamadas de API.
         */
        fun getMockDiagnosis(vehicleType: String): AiDiagnosis {
            val isMoto = vehicleType == "MOTO"
            return AiDiagnosis(
                dropDetected = true,
                dropMonth = "Octubre 2026",
                previousMonth = "Septiembre 2026",
                dropPercentage = -11.1,
                severity = "MODERADA",
                summary = if (isMoto) {
                    "El rendimiento de tu moto cayó de 135.0 km/gal a 120.0 km/gal (-11.1%)."
                } else {
                    "El rendimiento de tu carro cayó de 43.0 km/gal a 41.0 km/gal (-4.7%)."
                },
                recommendations = listOf(
                    AiRecommendation(
                        priority = 1,
                        component = "Presión de neumáticos",
                        estimatedCost = "Gratis ($0)",
                        costLevel = "GRATIS",
                        reason = "Llantas con 3 o 4 PSI por debajo de lo indicado aumentan la fricción contra el asfalto y disparan el consumo hasta un 8%.",
                        howToCheck = "Verifica en frío con un manómetro de gasolinera que tenga la presión del manual (28-32 PSI)."
                    ),
                    AiRecommendation(
                        priority = 2,
                        component = if (isMoto) "Tensión y lubricación de cadena" else "Filtro de aire del motor",
                        estimatedCost = if (isMoto) "Casi gratis ($1 - $3 lubricante)" else "Bajo ($8 - $15)",
                        costLevel = "BAJO",
                        reason = if (isMoto) {
                            "Una cadena seca o desajustada genera resistencia mecánica directa en cada aceleración."
                        } else {
                            "Un filtro de aire obstruido por polvo ahoga la mezcla de aire y fuerza al motor a inyectar más gasolina."
                        },
                        howToCheck = if (isMoto) {
                            "Revisa que tenga 2 a 3 cm de holgura y esté limpia y engrasada."
                        } else {
                            "Abre la caja del filtro y ponlo a contraluz; si no pasa luz, cámbialo."
                        }
                    ),
                    AiRecommendation(
                        priority = 3,
                        component = "Bujía de encendido",
                        estimatedCost = "Bajo ($3 - $6)",
                        costLevel = "BAJO",
                        reason = "Un electrodo desgastado o carbonizado produce chispas irregulares y combustión incompleta.",
                        howToCheck = "Desmonta la bujía con la llave de copa y revisa que la punta sea color café claro y no negra de carbón."
                    ),
                    AiRecommendation(
                        priority = 4,
                        component = "Carburador / Inyectores sucios",
                        estimatedCost = "Medio ($15 - $35)",
                        costLevel = "MEDIO",
                        reason = "Gomas o sedimentos del combustible pueden alterar la pulverización de la gasolina.",
                        howToCheck = "Si el motor presenta tirones leves al acelerar en frío, requiere limpieza en taller."
                    )
                ),
                isMockData = true
            )
        }
    }

    /**
     * Envía las cargas mensuales a Gemini y devuelve un AiDiagnosis estructurado.
     */
    suspend fun analyzePerformance(
        vehicleType: String,
        monthlyData: List<MonthlyPerformance>
    ): Result<AiDiagnosis> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY

        // Manejo de fallo 1: Llave no configurada o con valor por defecto
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException("No se ha configurado la llave GEMINI_API_KEY en los Secrets de AI Studio.")
            )
        }

        if (monthlyData.isEmpty()) {
            return@withContext Result.failure(
                IllegalArgumentException("No hay datos mensuales suficientes para analizar.")
            )
        }

        // Construir el prompt con los datos de telemetría reales del vehículo
        val dataReport = StringBuilder().apply {
            append("Historial de rendimiento mes a mes para $vehicleType:\n")
            monthlyData.forEach {
                append("- Mes: ${it.displayMonth}, Rendimiento: ${String.format(java.util.Locale.US, "%.1f", it.kmPerGallon)} km/gal, Costo: ${String.format(java.util.Locale.US, "$%.3f", it.costPerKm)}/km, Recorrido: ${String.format(java.util.Locale.US, "%.0f", it.totalKmDriven)} km, Galones: ${String.format(java.util.Locale.US, "%.1f", it.totalGallons)}\n")
            }
        }.toString()

        val promptText = """
            Eres un mecánico experto y analista de telemetría vehicular.
            Analiza estos datos mensuales reales de un usuario que usa su $vehicleType todos los días:
            $dataReport
            
            TAREA OBLIGATORIA:
            1. Detecta cuál fue el mes específico en que cayó el rendimiento comparado con el mes anterior más alto.
            2. Sugiere qué revisar primero para solucionar la pérdida de rendimiento, ORDENADO ESTRICTAMENTE DE MENOR A MAYOR COSTO (lo gratis o más barato primero).
            3. Responde estrictamente respetando el esquema JSON especificado.
        """.trimIndent()

        // Construir la petición REST completa
        val requestJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", promptText))
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("responseSchema", RESPONSE_SCHEMA_JSON)
                put("temperature", 0.2)
            })
        }

        val url = "$BASE_URL/$MODEL:generateContent?key=$apiKey"
        val body = requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (!response.isSuccessful || responseBody == null) {
                val errorCode = response.code
                return@withContext Result.failure(
                    Exception("El servidor de la IA respondió con error (Código $errorCode).")
                )
            }

            // Manejo de fallo 2: Extraer y validar el JSON devuelto
            val responseObj = JSONObject(responseBody)
            val candidates = responseObj.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext Result.failure(
                    Exception("La IA no devolvió candidatos válidos de respuesta.")
                )
            }

            val content = candidates.getJSONObject(0).optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val textContent = parts?.getJSONObject(0)?.optString("text")

            if (textContent.isNullOrBlank()) {
                return@withContext Result.failure(
                    Exception("La respuesta de la IA llegó vacía.")
                )
            }

            // Parsear el JSON según el responseSchema
            val parsedResult = parseDiagnosisJson(textContent)
            Result.success(parsedResult)

        } catch (e: java.net.SocketTimeoutException) {
            // Manejo de fallo 3: Respuesta lenta o timeout
            Result.failure(Exception("La IA tardó demasiado en responder (Tiempo de espera agotado)."))
        } catch (e: java.io.IOException) {
            Result.failure(Exception("Fallo de conexión a internet al contactar a la IA."))
        } catch (e: Exception) {
            // Manejo de fallo 4: Respuesta no cumple el esquema
            Result.failure(Exception("La IA devolvió datos en un formato inesperado: ${e.message}"))
        }
    }

    private fun parseDiagnosisJson(jsonString: String): AiDiagnosis {
        val root = JSONObject(jsonString)
        val dropDetected = root.optBoolean("dropDetected", false)
        val dropMonth = root.optString("dropMonth", "Mes detectado")
        val previousMonth = root.optString("previousMonth", "")
        val dropPercentage = root.optDouble("dropPercentage", 0.0)
        val severity = root.optString("severity", "MODERADA")
        val summary = root.optString("summary", "Análisis de rendimiento completado.")

        val recommendationsArray = root.optJSONArray("recommendations") ?: JSONArray()
        val recommendations = mutableListOf<AiRecommendation>()

        for (i in 0 until recommendationsArray.length()) {
            val item = recommendationsArray.getJSONObject(i)
            recommendations.add(
                AiRecommendation(
                    priority = item.optInt("priority", i + 1),
                    component = item.optString("component", "Revisión general"),
                    estimatedCost = item.optString("estimatedCost", "Costo bajo"),
                    costLevel = item.optString("costLevel", "BAJO"),
                    reason = item.optString("reason", "Afecta el rendimiento."),
                    howToCheck = item.optString("howToCheck", "Revisar componente.")
                )
            )
        }

        return AiDiagnosis(
            dropDetected = dropDetected,
            dropMonth = dropMonth,
            previousMonth = previousMonth,
            dropPercentage = dropPercentage,
            severity = severity,
            summary = summary,
            recommendations = recommendations,
            isMockData = false
        )
    }
}
