package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MonthlyPerformance
import java.util.Locale

/**
 * Gráfico mes a mes optimizado para sol y pantallas de 320px.
 * Todos los textos y números tienen tamaño mínimo de 16 px (sp).
 */
@Composable
fun MonthlyPerformanceChart(
    monthlyData: List<MonthlyPerformance>,
    modifier: Modifier = Modifier
) {
    var selectedMetric by remember { mutableStateOf("KM_GAL") }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("monthly_performance_chart_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Rendimiento Mes a Mes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (selectedMetric == "KM_GAL") "Kilómetros por galón (Mayor es mejor)" else "Costo por kilómetro (Menor es mejor)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Selector de métrica con texto claro de 16sp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedMetric == "KM_GAL",
                    onClick = { selectedMetric = "KM_GAL" },
                    label = { Text("Km / Galón", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = selectedMetric == "COST_KM",
                    onClick = { selectedMetric = "COST_KM" },
                    label = { Text("Costo ($ / km)", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (monthlyData.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aún no hay suficientes meses.\nAnota tus cargas para ver la comparativa aquí.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                val values = monthlyData.map {
                    if (selectedMetric == "KM_GAL") it.kmPerGallon else it.costPerKm
                }
                val rawMax = values.maxOrNull() ?: 1.0
                val maxVal = if (rawMax <= 0.0) 1.0 else rawMax * 1.35

                val primaryColor = MaterialTheme.colorScheme.primary
                val secondaryColor = MaterialTheme.colorScheme.secondary
                val surfaceVariantColor = MaterialTheme.colorScheme.outline
                val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
                val subTextColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()

                val animationProgress by animateFloatAsState(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 650),
                    label = "chartAnim"
                )

                // Altura de 240dp para alojar los textos grandes de 16sp con comodidad
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .testTag("canvas_performance_chart")
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height

                    val topPadding = 36f
                    val bottomPadding = 48f
                    val leftPadding = 16f
                    val rightPadding = 16f

                    val chartHeight = canvasHeight - topPadding - bottomPadding
                    val chartWidth = canvasWidth - leftPadding - rightPadding
                    val baselineY = canvasHeight - bottomPadding

                    // Líneas guía horizontales
                    val gridSteps = 3
                    val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

                    for (step in 0..gridSteps) {
                        val fraction = step.toFloat() / gridSteps
                        val y = baselineY - (chartHeight * fraction)

                        drawLine(
                            color = surfaceVariantColor.copy(alpha = 0.5f),
                            start = Offset(leftPadding, y),
                            end = Offset(canvasWidth - rightPadding, y),
                            strokeWidth = 1.5f,
                            pathEffect = if (step > 0) dashPathEffect else null
                        )
                    }

                    // Dibujar barras mensuales
                    val barCount = monthlyData.size
                    val totalSlots = barCount.toFloat()
                    val slotWidth = chartWidth / totalSlots
                    val barWidth = minOf(slotWidth * 0.58f, 56.dp.toPx())

                    for (index in monthlyData.indices) {
                        val month = monthlyData[index]
                        val value = values[index]
                        val isLatestMonth = index == monthlyData.size - 1

                        val centerX = leftPadding + (index + 0.5f) * slotWidth
                        val barLeft = centerX - (barWidth / 2f)

                        val normalizedHeight = (value / maxVal).toFloat() * chartHeight * animationProgress
                        val barTop = baselineY - normalizedHeight

                        val barBrush = if (isLatestMonth) {
                            Brush.verticalGradient(
                                colors = listOf(secondaryColor, primaryColor),
                                startY = barTop,
                                endY = baselineY
                            )
                        } else {
                            Brush.verticalGradient(
                                colors = listOf(primaryColor, primaryColor.copy(alpha = 0.65f)),
                                startY = barTop,
                                endY = baselineY
                            )
                        }

                        drawRoundRect(
                            brush = barBrush,
                            topLeft = Offset(barLeft, barTop),
                            size = Size(barWidth, normalizedHeight),
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                        )

                        // Texto del valor encima de la barra: tamaño mínimo de 16 px (sp)
                        val formattedVal = if (selectedMetric == "KM_GAL") {
                            String.format(Locale.US, "%.0f", value)
                        } else {
                            String.format(Locale.US, "$%.2f", value)
                        }

                        drawContext.canvas.nativeCanvas.apply {
                            val paint = android.graphics.Paint().apply {
                                color = textColor
                                textSize = 16.sp.toPx() // Texto nunca menor a 16 px
                                textAlign = android.graphics.Paint.Align.CENTER
                                isFakeBoldText = true
                            }
                            drawText(
                                formattedVal,
                                centerX,
                                barTop - 10f,
                                paint
                            )

                            // Etiqueta del mes debajo del eje X: tamaño mínimo de 16 px (sp)
                            val monthPaint = android.graphics.Paint().apply {
                                color = subTextColor
                                textSize = 16.sp.toPx() // Texto nunca menor a 16 px
                                textAlign = android.graphics.Paint.Align.CENTER
                                isFakeBoldText = isLatestMonth
                            }
                            drawText(
                                month.displayMonth,
                                centerX,
                                baselineY + 30f,
                                monthPaint
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Leyenda de alto contraste con texto de 16sp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(3.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Mes actual resaltado",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
