package com.example.data.model

/**
 * Recomendación individual sugerida por la IA, ordenada de menor a mayor costo de revisión.
 */
data class AiRecommendation(
    val priority: Int,
    val component: String,
    val estimatedCost: String,
    val costLevel: String, // "GRATIS", "BAJO", "MEDIO", "ALTO"
    val reason: String,
    val howToCheck: String
)

/**
 * Diagnóstico estructurado devuelto por la IA según el responseSchema fijo.
 */
data class AiDiagnosis(
    val dropDetected: Boolean,
    val dropMonth: String,
    val previousMonth: String,
    val dropPercentage: Double,
    val severity: String, // "LEVE", "MODERADA", "ALTA", "NINGUNA"
    val summary: String,
    val recommendations: List<AiRecommendation>,
    val isMockData: Boolean = false
)

/**
 * Estados de la llamada a la IA para la UI.
 */
sealed class AiDiagnosisState {
    object Idle : AiDiagnosisState()
    object Loading : AiDiagnosisState()
    data class Success(val diagnosis: AiDiagnosis) : AiDiagnosisState()
    data class Error(val message: String, val canUseMock: Boolean = true) : AiDiagnosisState()
}
