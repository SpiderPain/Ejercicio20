package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.backup.BackupManager
import com.example.data.local.FuelLogEntity
import com.example.data.model.AiDiagnosisState
import com.example.data.model.FuelLogWithStats
import com.example.data.model.MonthlyPerformance
import com.example.data.remote.GeminiService
import com.example.data.repository.FuelLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Estado general de la interfaz de MotCont.
 */
data class MotContUiState(
    val selectedVehicle: String = "MOTO", // "MOTO" o "CARRO"
    val logsWithStats: List<FuelLogWithStats> = emptyList(), // Ordenados del más reciente al más antiguo para el historial
    val monthlyPerformances: List<MonthlyPerformance> = emptyList(), // Cronológicos para el gráfico
    val overallAvgKmPerGallon: Double = 0.0,
    val overallAvgCostPerKm: Double = 0.0,
    val lastOdometerKm: Double = 0.0,
    val monthComparisonText: String = "",
    val monthComparisonPercent: Double? = null, // Positivo: rinde más, Negativo: rinde menos, null: datos insuficientes
    val isAddDialogOpen: Boolean = false,
    val isBackupDialogOpen: Boolean = false,
    val backupJsonString: String = "",
    val errorMessage: String? = null,
    val successMessage: String? = null
)

/**
 * ViewModel principal de MotCont.
 * Administra el registro de cargas, el cálculo de km/galón, costo por km y la comparativa mes a mes.
 */
class MotContViewModel(private val repository: FuelLogRepository) : ViewModel() {

    private val geminiService = GeminiService()

    private val _aiDiagnosisState = MutableStateFlow<AiDiagnosisState>(AiDiagnosisState.Idle)
    val aiDiagnosisState: StateFlow<AiDiagnosisState> = _aiDiagnosisState.asStateFlow()

    private val _selectedVehicle = MutableStateFlow("MOTO")
    val selectedVehicle: StateFlow<String> = _selectedVehicle.asStateFlow()

    private val _isAddDialogOpen = MutableStateFlow(false)
    val isAddDialogOpen: StateFlow<Boolean> = _isAddDialogOpen.asStateFlow()

    private val _isBackupDialogOpen = MutableStateFlow(false)
    val isBackupDialogOpen: StateFlow<Boolean> = _isBackupDialogOpen.asStateFlow()

    private val _backupJsonString = MutableStateFlow("")
    val backupJsonString: StateFlow<String> = _backupJsonString.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    // Formulario de registro de carga
    var formOdometer = MutableStateFlow("")
    var formAmount = MutableStateFlow("")
    var formGallons = MutableStateFlow("")
    var formPricePerGallon = MutableStateFlow("")
    var formNotes = MutableStateFlow("")
    var formDateString = MutableStateFlow(currentDateString())

    private data class DialogState(
        val isAddOpen: Boolean,
        val isBackupOpen: Boolean,
        val backupJson: String,
        val error: String?,
        val success: String?
    )

    private val _dialogState = combine(
        _isAddDialogOpen,
        _isBackupDialogOpen,
        _backupJsonString,
        _errorMessage,
        _successMessage
    ) { isAdd, isBackup, json, err, ok ->
        DialogState(isAdd, isBackup, json, err, ok)
    }

    val uiState: StateFlow<MotContUiState> = combine(
        _selectedVehicle,
        _dialogState
    ) { vehicle, dialog ->
        val baseState = calculateUiState(vehicle, dialog.isAddOpen, dialog.error, dialog.success)
        baseState.copy(
            isBackupDialogOpen = dialog.isBackupOpen,
            backupJsonString = dialog.backupJson
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MotContUiState()
    )

    init {
        // Observar cambios en la base de datos para refrescar cálculos automáticamente
        viewModelScope.launch {
            _selectedVehicle.collect { vehicle ->
                repository.getLogsByVehicle(vehicle).collect {
                    refreshCalculations()
                }
            }
        }
    }

    private var currentRawLogs: List<FuelLogEntity> = emptyList()

    private fun refreshCalculations() {
        viewModelScope.launch {
            repository.getLogsByVehicle(_selectedVehicle.value).collect { logs ->
                currentRawLogs = logs
                // Forzar emisión del estado recalculado
                val currentState = _selectedVehicle.value
                _selectedVehicle.value = currentState
            }
        }
    }

    fun selectVehicle(vehicle: String) {
        if (_selectedVehicle.value != vehicle) {
            _selectedVehicle.value = vehicle
            _aiDiagnosisState.value = AiDiagnosisState.Idle
            clearMessages()
        }
    }

    /**
     * Consulta a Gemini para detectar el mes en que cayó el rendimiento y sugerir qué revisar por costo.
     */
    fun analyzeWithGemini() {
        viewModelScope.launch {
            _aiDiagnosisState.value = AiDiagnosisState.Loading
            val monthlyData = uiState.value.monthlyPerformances
            val result = geminiService.analyzePerformance(_selectedVehicle.value, monthlyData)
            result.onSuccess { diagnosis ->
                _aiDiagnosisState.value = AiDiagnosisState.Success(diagnosis)
            }.onFailure { error ->
                _aiDiagnosisState.value = AiDiagnosisState.Error(
                    error.message ?: "Ocurrió un error al contactar al servicio de IA."
                )
            }
        }
    }

    /**
     * Carga el diagnóstico de prueba estático para desarrollar y probar sin consumir llamadas de API.
     */
    fun loadMockDiagnosis() {
        _aiDiagnosisState.value = AiDiagnosisState.Success(
            GeminiService.getMockDiagnosis(_selectedVehicle.value)
        )
    }

    fun openAddDialog() {
        // Pre-llenar con la fecha de hoy y el último odómetro sugerido
        val lastKm = currentRawLogs.maxByOrNull { it.odometerKm }?.odometerKm ?: 0.0
        formOdometer.value = if (lastKm > 0) String.format(Locale.US, "%.0f", lastKm + 350.0) else ""
        formAmount.value = ""
        formGallons.value = ""
        formPricePerGallon.value = ""
        formNotes.value = ""
        formDateString.value = currentDateString()
        _errorMessage.value = null
        _isAddDialogOpen.value = true
    }

    fun closeAddDialog() {
        _isAddDialogOpen.value = false
        _errorMessage.value = null
    }

    /**
     * Abre el diálogo de respaldo y genera el archivo JSON con los datos actuales de Room (SQLite).
     */
    fun openBackupDialog() {
        viewModelScope.launch {
            val allLogs = repository.getAllLogsList()
            val json = BackupManager.exportToJson(allLogs)
            _backupJsonString.value = json
            _isBackupDialogOpen.value = true
        }
    }

    fun closeBackupDialog() {
        _isBackupDialogOpen.value = false
    }

    /**
     * Importa registros desde un JSON y los almacena en SQLite (Room).
     */
    fun importFromJson(jsonString: String) {
        viewModelScope.launch {
            try {
                val logs = BackupManager.importFromJson(jsonString)
                if (logs.isNotEmpty()) {
                    repository.insertAll(logs)
                    _successMessage.value = "¡Listo! Se recuperaron ${logs.size} cargas del respaldo."
                    _isBackupDialogOpen.value = false
                    refreshCalculations()
                } else {
                    _errorMessage.value = "El texto de respaldo no contiene datos reconocibles."
                }
            } catch (e: Exception) {
                _errorMessage.value = "No se pudo leer el respaldo. Revisa que el texto no esté cortado."
            }
        }
    }

    fun onPricePerGallonChanged(priceStr: String) {
        formPricePerGallon.value = priceStr
        val price = priceStr.trim().replace(',', '.').toDoubleOrNull()
        val amount = formAmount.value.trim().replace(',', '.').toDoubleOrNull()
        // Si el usuario ingresa monto y precio por galón, calcular automáticamente los galones
        if (price != null && price > 0.0 && amount != null && amount > 0.0) {
            val calculatedGallons = amount / price
            formGallons.value = String.format(Locale.US, "%.2f", calculatedGallons)
        }
    }

    fun onAmountChanged(amountStr: String) {
        formAmount.value = amountStr
        val amount = amountStr.trim().replace(',', '.').toDoubleOrNull()
        val price = formPricePerGallon.value.trim().replace(',', '.').toDoubleOrNull()
        if (price != null && price > 0.0 && amount != null && amount > 0.0) {
            val calculatedGallons = amount / price
            formGallons.value = String.format(Locale.US, "%.2f", calculatedGallons)
        }
    }

    fun onGallonsChanged(gallonsStr: String) {
        formGallons.value = gallonsStr
        val gallons = gallonsStr.trim().replace(',', '.').toDoubleOrNull()
        val amount = formAmount.value.trim().replace(',', '.').toDoubleOrNull()
        // Si ingresa galones y monto, calcular precio por galón
        if (gallons != null && gallons > 0.0 && amount != null && amount > 0.0) {
            val calculatedPrice = amount / gallons
            formPricePerGallon.value = String.format(Locale.US, "%.2f", calculatedPrice)
        }
    }

    /**
     * Valida y guarda una nueva carga de combustible.
     *
     * PUNTOS CRÍTICOS DONDE ALGUIEN PUEDE EQUIVOCARSE:
     * 1. No validar odómetro decreciente: Si el nuevo odómetro es menor o igual al último registrado,
     *    la resta (km_actual - km_anterior) daría un valor negativo o cero, arruinando el cálculo.
     * 2. No validar división por cero: Los galones y el monto deben ser estrictamente mayores a cero.
     * 3. Fecha vacía o mal formateada: Se asegura formato ISO yyyy-MM-dd.
     */
    fun saveFuelLog() {
        val odo = formOdometer.value.trim().replace(',', '.').toDoubleOrNull()
        val amount = formAmount.value.trim().replace(',', '.').toDoubleOrNull()
        val gals = formGallons.value.trim().replace(',', '.').toDoubleOrNull()
        val dateStr = formDateString.value.trim()

        if (odo == null || odo <= 0.0) {
            _errorMessage.value = "Por favor escribe cuántos kilómetros marca el tablero de tu vehículo."
            return
        }
        if (amount == null || amount <= 0.0) {
            _errorMessage.value = "Por favor escribe cuánto dinero pagaste en la gasolinera."
            return
        }
        if (gals == null || gals <= 0.0) {
            _errorMessage.value = "Por favor escribe cuántos galones le cargaste al tanque."
            return
        }

        // Validar contra el último odómetro de la lista
        val lastOdo = currentRawLogs.maxByOrNull { it.odometerKm }?.odometerKm ?: 0.0
        if (currentRawLogs.isNotEmpty() && odo <= lastOdo) {
            _errorMessage.value = "El kilometraje (${String.format(Locale.US, "%.0f", odo)} km) debe ser mayor al último que anotaste (${String.format(Locale.US, "%.0f", lastOdo)} km)."
            return
        }

        viewModelScope.launch {
            val newLog = FuelLogEntity(
                vehicleType = _selectedVehicle.value,
                odometerKm = odo,
                amountPaid = amount,
                gallons = gals,
                dateMillis = System.currentTimeMillis(),
                dateString = if (dateStr.isNotEmpty()) dateStr else currentDateString(),
                notes = formNotes.value.trim()
            )

            repository.insertLog(newLog)
            _isAddDialogOpen.value = false
            _successMessage.value = "¡Listo! Tu carga quedó guardada y el rendimiento se calculó."
            refreshCalculations()
        }
    }

    fun deleteLog(id: Long) {
        viewModelScope.launch {
            repository.deleteLog(id)
            _successMessage.value = "¡Listo! La carga fue eliminada."
            refreshCalculations()
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    /**
     * Motor de cálculo: procesa los registros de combustible en orden cronológico
     * para obtener las métricas de cada tramo y el rendimiento consolidado mes a mes.
     */
    private fun calculateUiState(
        vehicle: String,
        isDialogOpen: Boolean,
        error: String?,
        success: String?
    ): MotContUiState {
        // Filtrar y ordenar los registros del vehículo actual de forma ASCENDENTE por odómetro
        val logsAsc = currentRawLogs.filter { it.vehicleType == vehicle }
            .sortedWith(compareBy({ it.odometerKm }, { it.dateMillis }))

        if (logsAsc.isEmpty()) {
            return MotContUiState(
                selectedVehicle = vehicle,
                isAddDialogOpen = isDialogOpen,
                errorMessage = error,
                successMessage = success,
                monthComparisonText = "Aún no hay cargas registradas para este vehículo."
            )
        }

        // 1. Calcular estadísticas de cada carga individual
        val withStatsList = mutableListOf<FuelLogWithStats>()
        var totalDeltaKm = 0.0
        var totalGallonsConsumed = 0.0
        var totalCostConsumed = 0.0

        for (i in logsAsc.indices) {
            val current = logsAsc[i]
            val pricePerGal = if (current.gallons > 0.0) current.amountPaid / current.gallons else 0.0

            if (i == 0) {
                // Primera carga: es el punto de referencia del odómetro, no tiene delta anterior
                withStatsList.add(
                    FuelLogWithStats(
                        log = current,
                        deltaKm = null,
                        kmPerGallon = null,
                        costPerKm = null,
                        pricePerGallon = pricePerGal
                    )
                )
            } else {
                val previous = logsAsc[i - 1]
                val delta = current.odometerKm - previous.odometerKm

                if (delta > 0.0 && current.gallons > 0.0) {
                    val kpg = delta / current.gallons
                    val cpk = current.amountPaid / delta

                    totalDeltaKm += delta
                    totalGallonsConsumed += current.gallons
                    totalCostConsumed += current.amountPaid

                    withStatsList.add(
                        FuelLogWithStats(
                            log = current,
                            deltaKm = delta,
                            kmPerGallon = kpg,
                            costPerKm = cpk,
                            pricePerGallon = pricePerGal
                        )
                    )
                } else {
                    withStatsList.add(
                        FuelLogWithStats(
                            log = current,
                            deltaKm = null,
                            kmPerGallon = null,
                            costPerKm = null,
                            pricePerGallon = pricePerGal
                        )
                    )
                }
            }
        }

        // Promedios generales globales
        val overallKpg = if (totalGallonsConsumed > 0.0) totalDeltaKm / totalGallonsConsumed else 0.0
        val overallCpk = if (totalDeltaKm > 0.0) totalCostConsumed / totalDeltaKm else 0.0
        val lastOdo = logsAsc.lastOrNull()?.odometerKm ?: 0.0

        // 2. Agrupación mes a mes para el gráfico y la comparativa
        // Agrupamos por los primeros 7 caracteres de "yyyy-MM-dd" -> "yyyy-MM"
        val monthlyMap = mutableMapOf<String, MutableList<FuelLogWithStats>>()
        for (item in withStatsList) {
            if (item.deltaKm != null && item.deltaKm > 0.0) {
                val ym = if (item.log.dateString.length >= 7) {
                    item.log.dateString.substring(0, 7)
                } else {
                    "2026-10"
                }
                monthlyMap.getOrPut(ym) { mutableListOf() }.add(item)
            }
        }

        val monthlyPerformances = monthlyMap.keys.sorted().map { ym ->
            val itemsInMonth = monthlyMap[ym] ?: emptyList()
            val monthKm = itemsInMonth.sumOf { it.deltaKm ?: 0.0 }
            val monthGallons = itemsInMonth.sumOf { it.log.gallons }
            val monthAmount = itemsInMonth.sumOf { it.log.amountPaid }

            val monthKpg = if (monthGallons > 0.0) monthKm / monthGallons else 0.0
            val monthCpk = if (monthKm > 0.0) monthAmount / monthKm else 0.0

            MonthlyPerformance(
                yearMonth = ym,
                displayMonth = formatYearMonthToDisplay(ym),
                totalKmDriven = monthKm,
                totalGallons = monthGallons,
                totalAmountPaid = monthAmount,
                kmPerGallon = monthKpg,
                costPerKm = monthCpk,
                logsCount = itemsInMonth.size
            )
        }

        // 3. Comparativa mes actual vs mes pasado (El problema central: ¿Rinde igual que el mes pasado?)
        var comparisonText = ""
        var comparisonPercent: Double? = null

        if (monthlyPerformances.size >= 2) {
            val currentMonth = monthlyPerformances.last()
            val previousMonth = monthlyPerformances[monthlyPerformances.size - 2]

            val diff = currentMonth.kmPerGallon - previousMonth.kmPerGallon
            if (previousMonth.kmPerGallon > 0.0) {
                val pct = (diff / previousMonth.kmPerGallon) * 100.0
                comparisonPercent = pct

                val currentKpgStr = String.format(Locale.US, "%.1f", currentMonth.kmPerGallon)
                val prevKpgStr = String.format(Locale.US, "%.1f", previousMonth.kmPerGallon)
                val pctStr = String.format(Locale.US, "%.1f%%", Math.abs(pct))

                comparisonText = when {
                    pct > 1.0 -> "¡Buenas noticias! Este mes (${currentMonth.displayMonth}) rinde un $pctStr MÁS ($currentKpgStr km/gal) que el mes pasado ($prevKpgStr km/gal)."
                    pct < -1.0 -> "Atención: Este mes (${currentMonth.displayMonth}) rinde un $pctStr MENOS ($currentKpgStr km/gal) que el mes pasado ($prevKpgStr km/gal)."
                    else -> "Rinde prácticamente IGUAL que el mes pasado (~$currentKpgStr km/gal)."
                }
            } else {
                comparisonText = "Mes actual: ${String.format(Locale.US, "%.1f", currentMonth.kmPerGallon)} km/gal."
            }
        } else if (monthlyPerformances.size == 1) {
            val onlyMonth = monthlyPerformances.first()
            comparisonText = "Rendimiento registrado en ${onlyMonth.displayMonth}: ${String.format(Locale.US, "%.1f", onlyMonth.kmPerGallon)} km/gal. Se necesita otro mes para comparar."
        } else {
            comparisonText = "Registra al menos 2 cargas para calcular y comparar el rendimiento."
        }

        return MotContUiState(
            selectedVehicle = vehicle,
            logsWithStats = withStatsList.reversed(), // El más reciente arriba para la lista visual
            monthlyPerformances = monthlyPerformances,
            overallAvgKmPerGallon = overallKpg,
            overallAvgCostPerKm = overallCpk,
            lastOdometerKm = lastOdo,
            monthComparisonText = comparisonText,
            monthComparisonPercent = comparisonPercent,
            isAddDialogOpen = isDialogOpen,
            errorMessage = error,
            successMessage = success
        )
    }

    private fun currentDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun formatYearMonthToDisplay(ym: String): String {
        return try {
            val parts = ym.split("-")
            val year = parts[0]
            val month = parts[1].toInt()
            val monthNames = arrayOf(
                "Ene", "Feb", "Mar", "Abr", "May", "Jun",
                "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"
            )
            val name = if (month in 1..12) monthNames[month - 1] else ym
            "$name $year"
        } catch (e: Exception) {
            ym
        }
    }
}

/**
 * Factory simple para instanciar el ViewModel sin requerir Hilt.
 */
class MotContViewModelFactory(private val repository: FuelLogRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MotContViewModel::class.java)) {
            return MotContViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
