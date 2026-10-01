package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.HighContrastBlue
import com.example.ui.theme.HighContrastBlueContainer
import com.example.ui.theme.HighContrastGreen
import com.example.ui.theme.HighContrastGreenContainer
import com.example.ui.theme.HighContrastRed
import com.example.ui.theme.HighContrastRedContainer

/**
 * Tarjeta destacada que responde la pregunta fundamental:
 * "¿No se sabe si la moto rinde igual que el mes pasado?".
 * Diseñada para lectura óptima bajo la luz del sol con tipografía nunca menor a 16 px.
 */
@Composable
fun MonthComparisonCard(
    vehicleType: String,
    comparisonText: String,
    differencePercent: Double?,
    modifier: Modifier = Modifier
) {
    val (statusColor, containerColor, icon) = when {
        differencePercent == null -> Triple(
            HighContrastBlue,
            HighContrastBlueContainer,
            Icons.AutoMirrored.Filled.CompareArrows
        )
        differencePercent > 1.0 -> Triple(
            HighContrastGreen,
            HighContrastGreenContainer,
            Icons.Default.ArrowUpward
        )
        differencePercent < -1.0 -> Triple(
            HighContrastRed,
            HighContrastRedContainer,
            Icons.Default.ArrowDownward
        )
        else -> Triple(
            HighContrastBlue,
            HighContrastBlueContainer,
            Icons.Default.CheckCircle
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("month_comparison_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "¿Rinde igual que el mes pasado?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (differencePercent != null) {
                    val formattedPct = if (differencePercent >= 0) {
                        "+${String.format(java.util.Locale.US, "%.1f", differencePercent)}%"
                    } else {
                        "${String.format(java.util.Locale.US, "%.1f", differencePercent)}%"
                    }
                    Text(
                        text = formattedPct,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = comparisonText,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 24.sp
            )
        }
    }
}
