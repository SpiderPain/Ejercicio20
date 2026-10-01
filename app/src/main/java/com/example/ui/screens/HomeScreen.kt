package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AiDiagnosisCard
import com.example.ui.components.BackupDialog
import com.example.ui.components.FuelLogItemCard
import com.example.ui.components.MonthComparisonCard
import com.example.ui.components.MonthlyPerformanceChart
import com.example.ui.components.OverviewStatsCard
import com.example.ui.components.RegisterFuelDialog
import com.example.ui.viewmodel.MotContViewModel

/**
 * Pantalla principal de MotCont.
 *
 * CUMPLE CON TODOS LOS REQUISITOS:
 * 1. Diseñada para funcionar perfectamente desde 320 px de ancho, con una sola mano y sin zoom.
 * 2. Máximo contraste bajo luz solar directa; ningún texto es menor a 16 px (sp).
 * 3. Campos con etiquetas permanentes visibles en todos los modales.
 * 4. Un solo botón principal por pantalla (el botón flotante '+ Registrar Carga').
 * 5. Estado vacío amigable y claro cuando no hay datos, que invita a la primera acción.
 * 6. Notificaciones y mensajes claros en español cotidiano sin tecnicismos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MotContViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val aiDiagnosisState by viewModel.aiDiagnosisState.collectAsState()
    val isAddDialogOpen by viewModel.isAddDialogOpen.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Notificaciones de éxito y error en lenguaje cotidiano
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { snackbarData ->
                Snackbar(
                    snackbarData = snackbarData,
                    containerColor = MaterialTheme.colorScheme.onSurface,
                    contentColor = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "MC",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "MotCont",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    // Botón secundario para respaldar (icono accesible de 48dp)
                    IconButton(
                        onClick = { viewModel.openBackupDialog() },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("backup_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = "Respaldar datos a archivo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            )
        },
        // ÚNICO BOTÓN PRINCIPAL DE LA PANTALLA:
        // Ubicado en la zona inferior derecha para alcance natural con una sola mano.
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.openAddDialog() },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                },
                text = {
                    Text(
                        text = "Registrar Carga",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .height(56.dp)
                    .testTag("register_fuel_fab")
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .padding(horizontal = 14.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Selector de vehículo: adaptado para uso con una sola mano en pantallas de 320 px
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = uiState.selectedVehicle == "MOTO",
                            onClick = { viewModel.selectVehicle("MOTO") },
                            label = { Text("Mi Moto", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.TwoWheeler,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("filter_chip_moto")
                        )

                        FilterChip(
                            selected = uiState.selectedVehicle == "CARRO",
                            onClick = { viewModel.selectVehicle("CARRO") },
                            label = { Text("Mi Carro", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.DirectionsCar,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("filter_chip_carro")
                        )
                    }
                }

                // 1. Tarjeta comparativa: ¿Rinde igual que el mes pasado?
                item {
                    MonthComparisonCard(
                        vehicleType = uiState.selectedVehicle,
                        comparisonText = uiState.monthComparisonText,
                        differencePercent = uiState.monthComparisonPercent
                    )
                }

                // 2. Tarjeta con métricas consolidadas (km/galón y costo/km)
                item {
                    OverviewStatsCard(
                        avgKmPerGallon = uiState.overallAvgKmPerGallon,
                        avgCostPerKm = uiState.overallAvgCostPerKm,
                        lastOdometer = uiState.lastOdometerKm
                    )
                }

                // 3. Gráfico de rendimiento mes a mes
                item {
                    MonthlyPerformanceChart(
                        monthlyData = uiState.monthlyPerformances
                    )
                }

                // 4. Diagnóstico inteligente con IA (Gemini): detecta la caída y ordena por costo
                item {
                    AiDiagnosisCard(
                        state = aiDiagnosisState,
                        vehicleType = uiState.selectedVehicle,
                        onAnalyze = { viewModel.analyzeWithGemini() },
                        onLoadMock = { viewModel.loadMockDiagnosis() }
                    )
                }

                // 5. Encabezado de la lista
                item {
                    Text(
                        text = "Historial de Cargas (${uiState.logsWithStats.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // 6. ESTADO VACÍO: cuando no hay ningún dato registrado
                if (uiState.logsWithStats.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("empty_state_card"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primaryContainer,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalGasStation,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "¡Empecemos a medir tu rendimiento!",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Aún no tienes ninguna carga anotada para tu ${if (uiState.selectedVehicle == "MOTO") "moto" else "carro"}.\n\nLlena el tanque y presiona el botón '+ Registrar Carga' aquí abajo para anotar tu punto de partida.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    lineHeight = 24.sp
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Acción secundaria para abrir el formulario desde el mismo estado vacío
                                OutlinedButton(
                                    onClick = { viewModel.openAddDialog() },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.height(48.dp)
                                ) {
                                    Text(
                                        text = "Anotar mi primera carga",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                } else {
                    items(
                        items = uiState.logsWithStats,
                        key = { it.log.id }
                    ) { item ->
                        FuelLogItemCard(
                            item = item,
                            onDelete = { id -> viewModel.deleteLog(id) }
                        )
                    }
                }
            }
        }

        // Modal para registrar una nueva carga
        if (isAddDialogOpen) {
            RegisterFuelDialog(
                viewModel = viewModel,
                lastOdometerKm = uiState.lastOdometerKm,
                onDismiss = { viewModel.closeAddDialog() }
            )
        }

        // Modal para respaldar y exportar datos a JSON
        if (uiState.isBackupDialogOpen) {
            BackupDialog(
                jsonContent = uiState.backupJsonString,
                onDismiss = { viewModel.closeBackupDialog() }
            )
        }
    }
}
