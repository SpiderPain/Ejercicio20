package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Paleta de Alto Contraste para lectura bajo luz solar directa
// Cumple con ratios de contraste WCAG AAA para texto sobre fondos claros
val SunlightBlack = Color(0xFF090D16)          // Texto principal de máximo contraste
val SunlightDarkGrey = Color(0xFF1E293B)       // Texto secundario legible bajo el sol (no gris claro)
val SunlightWhite = Color(0xFFFFFFFF)          // Superficies limpias y brillantes
val SunlightBackground = Color(0xFFF1F5F9)     // Fondo que reduce reflejos

val HighContrastBlue = Color(0xFF0A58CA)       // Azul de alta saturación y contraste
val HighContrastBlueContainer = Color(0xFFDCEBFE) // Contenedor azul claro
val HighContrastBlueOnContainer = Color(0xFF083B87)

val HighContrastGreen = Color(0xFF0A7B42)      // Verde de alta saturación (rendimiento positivo)
val HighContrastGreenContainer = Color(0xFFD1F4E0)
val HighContrastGreenOnContainer = Color(0xFF064E29)

val HighContrastRed = Color(0xFFC5221F)        // Rojo de alerta (rendimiento negativo)
val HighContrastRedContainer = Color(0xFFFCE8E6)
val HighContrastRedOnContainer = Color(0xFF781210)

val BorderHighContrast = Color(0xFF94A3B8)     // Bordes bien definidos para pantallas de 320px
