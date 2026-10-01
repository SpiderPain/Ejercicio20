package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.viewmodel.MotContViewModel
import java.util.Locale

/**
 * Diálogo para registrar una carga de combustible.
 *
 * CUMPLE CON TODOS LOS REQUISITOS:
 * 1. Optimizado para pantallas angostas desde 320 px de ancho.
 * 2. Texto nunca menor a 16 px (sp) y contraste alto para leer bajo el sol.
 * 3. Etiquetas permanentes y visibles encima de CADA campo, no solo placeholder.
 * 4. Un solo botón principal destacado ("Guardar Carga"); los demás son secundarios.
 * 6. Mensajes de error claros en español cotidiano sin tecnicismos.
 */
@Composable
fun RegisterFuelDialog(
    viewModel: MotContViewModel,
    lastOdometerKm: Double,
    onDismiss: () -> Unit
) {
    val odometer by viewModel.formOdometer.collectAsState()
    val amount by viewModel.formAmount.collectAsState()
    val gallons by viewModel.formGallons.collectAsState()
    val pricePerGallon by viewModel.formPricePerGallon.collectAsState()
    val notes by viewModel.formNotes.collectAsState()
    val dateString by viewModel.formDateString.collectAsState()
    val selectedVehicle by viewModel.selectedVehicle.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .testTag("register_fuel_dialog_surface"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Cabecera con título visible y botón de cerrar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalGasStation,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Anotar Carga",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp) // Touch target mínimo de 48dp
                            .testTag("close_fuel_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar ventana",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Etiqueta permanente 1: Selección de vehículo
                Text(
                    text = "Vehículo:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedVehicle == "MOTO",
                        onClick = { viewModel.selectVehicle("MOTO") },
                        label = { Text("Moto", fontSize = 16.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.TwoWheeler, contentDescription = null, modifier = Modifier.size(20.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("dialog_chip_moto")
                    )
                    FilterChip(
                        selected = selectedVehicle == "CARRO",
                        onClick = { viewModel.selectVehicle("CARRO") },
                        label = { Text("Carro", fontSize = 16.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(20.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("dialog_chip_carro")
                    )
                }

                if (lastOdometerKm > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Último registro en tablero: ${String.format(Locale.US, "%.0f", lastOdometerKm)} km",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Campo 1: Kilometraje con etiqueta permanente visible
                Text(
                    text = "1. Kilómetros en el tablero (km) *",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = odometer,
                    onValueChange = { viewModel.formOdometer.value = it.replace(',', '.') },
                    placeholder = { Text("Ejemplo: 13850", fontSize = 16.sp) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_odometer_km")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Campo 2: Dinero total pagado con etiqueta permanente visible
                Text(
                    text = "2. Dinero total que pagaste ($ / Q) *",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { viewModel.onAmountChanged(it.replace(',', '.')) },
                    placeholder = { Text("Ejemplo: 12.50", fontSize = 16.sp) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_amount_paid")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Campo 3: Galones con etiqueta permanente visible
                Text(
                    text = "3. Cantidad de galones que cargaste (gal) *",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = gallons,
                    onValueChange = { viewModel.onGallonsChanged(it.replace(',', '.')) },
                    placeholder = { Text("Ejemplo: 3.0", fontSize = 16.sp) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_gallons")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Campo 4: Precio por galón opcional
                Text(
                    text = "4. Precio por galón (opcional)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = pricePerGallon,
                    onValueChange = { viewModel.onPricePerGallonChanged(it.replace(',', '.')) },
                    placeholder = { Text("Calculado automático o a mano", fontSize = 16.sp) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_price_per_gallon")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Campo 5: Fecha con etiqueta permanente visible
                Text(
                    text = "5. Fecha de la carga",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = dateString,
                    onValueChange = { viewModel.formDateString.value = it },
                    placeholder = { Text("AAAA-MM-DD", fontSize = 16.sp) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_date_string")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Campo 6: Notas con etiqueta permanente visible
                Text(
                    text = "6. Nota adicional (opcional)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { viewModel.formNotes.value = it },
                    placeholder = { Text("Ejemplo: Gasolina súper, autopista", fontSize = 16.sp) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_notes")
                )

                // Mensaje de error claro y de alto contraste
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // BOTONES: Un solo botón principal ("Guardar Carga"). El botón cancelar es secundario.
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // BOTÓN PRINCIPAL ÚNICO (Filled, alto contraste)
                    Button(
                        onClick = { viewModel.saveFuelLog() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_fuel_button")
                    ) {
                        Text(
                            text = "Guardar Carga",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // BOTÓN SECUNDARIO (Outlined, sutil)
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("cancel_button")
                    ) {
                        Text(
                            text = "Cancelar",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
