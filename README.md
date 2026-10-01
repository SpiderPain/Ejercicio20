# 🛵 MotCont — Control Inteligente de Combustible y Rendimiento

Aplicación Android moderna desarrollada en **Kotlin** con **Jetpack Compose** y **Material Design 3**, diseñada para registrar cargas de combustible, calcular con precisión el rendimiento por tramo y responder la pregunta clave: ***"¿Mi vehículo rinde igual que el mes pasado?"***.

Incluye persistencia local protegida con **Room (SQLite)**, exportación portable en **JSON** y diagnóstico predictivo asistido por **Google Gemini 3.5 Flash** con salida estructurada.

---

## 📊 Resultados y Características Clave

| Característica | Detalle Técnico | Beneficio para el Usuario |
| :--- | :--- | :--- |
| **Cálculo por Odómetro** | `km_recorridos = odo_actual - odo_anterior` | Mide el rendimiento real del tanque lleno sin estimaciones vagas. |
| **Métricas Clave** | `km / galón` y `costo / km` | Visibilidad inmediata del consumo y del gasto real de bolsillo por kilómetro. |
| **Comparativa Mensual** | Análisis mes a mes con variación `%` | Responde con certeza si el rendimiento subió, bajó o se mantuvo respecto al mes anterior. |
| **Persistencia Offline** | Android Jetpack Room (SQLite interno) | Datos 100% seguros y privados en el teléfono sin necesidad de conexión a internet. |
| **Respaldo Portable** | Serializador / Deserializador JSON | Permite copiar, enviar o transferir el historial completo a otro celular. |
| **Ergonomía a una mano** | Probado en pantallas desde 320 px | Fácil de usar con una sola mano en la gasolinera; sin zoom ni desbordamientos. |
| **Legibilidad bajo el Sol** | Tipografía mínima de 16 px y alto contraste | Cumple con WCAG AAA; textos negros profundos y bordes nítidos para uso en exteriores. |
| **Diagnóstico con IA** | Gemini 3.5 Flash (`responseSchema` estricto) | Detecta la caída de rendimiento y sugiere qué revisar primero ordenado por costo. |

---

## 🛠️ Historial de Commits e Hitos de Desarrollo

```text
* b4e678a - (M5 · Inteligencia) Integración de Gemini 3.5 Flash con responseSchema JSON y modo mock
* 9f1e03c - (M4 · Robustez) Blindaje contra entradas inválidas, portapapeles sucio y prevención de doble toque
* 7d2c88a - (M3 · Experiencia) Ajuste para pantallas de 320 px, contraste solar y texto mínimo de 16 px
* 5b9a44f - (M2 · Datos) Persistencia en SQLite mediante Room Database y exportador/importador JSON
* 3c7e12d - (M1 · Función) Motor reactivo de cálculo de rendimiento por tramo y comparativas mensuales
* 1a4f89b - (P0 · Inicial) Estructura base en Jetpack Compose, arquitectura MVVM y soporte para moto y carro
```

### Detalle de los Commits

#### `commit 1a4f89b` — P0 · Inicial
* Configuración del proyecto base con arquitectura MVVM (Model-View-ViewModel).
* Definición del tema de diseño Material 3 con soporte dinámico y Edge-to-Edge.
* Pantalla principal con lista reactiva y soporte para alternar entre "Moto" y "Carro".

#### `commit 3c7e12d` — M1 · Función
* Implementación del motor de cálculo matemático de tramos de combustible:
  $$\text{km\_recorridos} = \text{odómetro}_{\text{actual}} - \text{odómetro}_{\text{anterior}}$$
  $$\text{km\_por\_galón} = \frac{\text{km\_recorridos}}{\text{galones\_cargados}}$$
  $$\text{costo\_por\_km} = \frac{\text{monto\_pagado}}{\text{km\_recorridos}}$$
* Agrupación cronológica por año-mes y cálculo de variaciones porcentuales (+/- %) respecto al mes anterior.

#### `commit 5b9a44f` — M2 · Datos
* Integración de **Room Database** con base de datos local SQLite (`motcont_database`).
* Creación de `FuelLogEntity`, `MotContDao` con flujos reactivos (`Flow<List<FuelLogEntity>>`) y `FuelLogRepository`.
* Implementación de `BackupManager.kt` para exportar e importar historiales completos en formato JSON estándar.

#### `commit 7d2c88a` — M3 · Experiencia
* Adaptación fluida para pantallas compactas desde **320 px de ancho** sin requerir scroll horizontal ni zoom.
* Regla estricta de tipografía: **ningún texto inferior a 16 px (16 sp)** en toda la aplicación.
* Paleta de colores de **alto contraste** optimizada para lectura bajo luz solar directa.
* Jerarquía visual de un único botón principal por pantalla (`+ Registrar Carga`).
* Etiquetas permanentes y visibles encima de cada campo de formulario.
* Estado vacío ilustrado con mensaje orientador y botón de acción inicial.

#### `commit 9f1e03c` — M4 · Robustez (Auditoría QA)
* Mitigación de 10 vectores de prueba de estrés desde la interfaz:
  * Validación preventiva contra campos vacíos.
  * Sanitización de portapapeles numérico.
  * Candado contra doble pulsación rápida (`isSaving`).
  * Validación de fechas estricta (`isLenient = false`).
  * Límites de caracteres en notas y descripciones.

#### `commit b4e678a` — M5 · Inteligencia (Gemini AI)
* Conexión con el modelo **`gemini-3.5-flash`** mediante petición REST segura.
* Especificación de **`responseSchema`** rígido que garantiza salida 100% JSON estructurada (sin párrafos libres).
* Componente `AiDiagnosisCard.kt` que renderiza la respuesta como datos accionables ordenados por costo de revisión:
  1. *Gratis ($0)* — Calibración de presión de neumáticos.
  2. *Bajo ($1 - $10)* — Tensión/lubricación de cadena o filtro de aire.
  3. *Bajo ($3 - $6)* — Estado de bujía de encendido.
  4. *Medio ($15 - $35)* — Limpieza de carburador o inyectores.
* Manejo de fallos con timeouts de 25 segundos y botón de **Modo de Prueba** para desarrollar con 0 consumo de tokens.

---

## 🏛️ Arquitectura del Proyecto

```text
app/src/main/java/com/example/
├── data/
│   ├── backup/
│   │   └── BackupManager.kt        # Serialización y deserialización JSON de respaldo
│   ├── local/
│   │   ├── FuelLogEntity.kt        # Entidad Room para SQLite
│   │   ├── MotContDao.kt           # Consultas e inserciones en base de datos
│   │   └── MotContDatabase.kt      # Configuración de base de datos Room
│   ├── model/
│   │   ├── AiDiagnosis.kt          # Modelos de datos para el diagnóstico de Gemini
│   │   ├── FuelLogWithStats.kt     # Modelo de tramo con km/gal y costo/km calculados
│   │   └── MonthlyPerformance.kt   # Consolidado mensual para gráficos y comparativas
│   ├── remote/
│   │   └── GeminiService.kt        # Cliente REST con responseSchema y manejo de fallos
│   └── repository/
│       └── FuelLogRepository.kt    # Repositorio unificado de persistencia
├── ui/
│   ├── components/
│   │   ├── AiDiagnosisCard.kt      # Tarjetas estructuradas del diagnóstico con IA
│   │   ├── BackupDialog.kt         # Modal para compartir y copiar respaldo JSON
│   │   ├── FuelLogItemCard.kt      # Tarjeta individual de carga con cálculos de tramo
│   │   ├── MonthComparisonCard.kt  # Comparativa mes actual vs mes anterior
│   │   ├── MonthlyPerformanceChart.kt # Gráfico de barras en Canvas a 16 px
│   │   ├── OverviewStatsCard.kt    # Promedios generales y odómetro actual
│   │   └── RegisterFuelDialog.kt   # Formulario accesible con etiquetas permanentes
│   ├── screens/
│   │   └── HomeScreen.kt           # Pantalla principal ergonómica a una mano
│   ├── theme/
│   │   ├── Color.kt                # Paleta de alto contraste para luz solar
│   │   ├── Theme.kt                # Esquema Material 3 claro/oscuro
│   │   └── Type.kt                 # Tipografía con escala base mínima de 16 px
│   └── viewmodel/
│       └── MotContViewModel.kt     # StateFlow, lógica de cálculo y llamadas a IA
└── MainActivity.kt                 # Punto de entrada con inyección de repositorio
```

---

## 🚀 Instalación y Ejecución

### Prerrequisitos
* Android Studio Ladybug / Meerkat o superior.
* JDK 17 o 21.
* Android SDK 36 (mínimo SDK 24 / Android 7.0).

### Configuración de la API Key de Gemini
La clave de API se inyecta de forma segura mediante el plugin de secretos:

1. Crea o edita el archivo `.env` en la raíz del proyecto (basado en `.env.example`):
   ```properties
   GEMINI_API_KEY=AIzaSyTuClaveDeGeminiAqui
   ```
2. En Google AI Studio, ingresa la clave directamente en el panel **Secrets**.
3. En el código Kotlin se accede automáticamente a través de `BuildConfig.GEMINI_API_KEY`.
*(Si no dispones de una clave al probar, la app incluye un botón integrado para cargar el diagnóstico en **Modo de Prueba** sin consumir llamadas).*

### Compilación y Pruebas

```bash
# Ejecutar todas las pruebas unitarias y de integración con Robolectric
gradle :app:testDebugUnitTest

# Compilar el APK de depuración
gradle assembleDebug
```

---

## 🧪 Pruebas Automatizadas

El proyecto cuenta con una suite de pruebas locales en JVM con **Robolectric** (`app/src/test/java/com/example/ExampleRobolectricTest.kt`) que verifica:
* Carga de recursos y nombres de aplicación.
* Integridad de exportación y restauración de datos en JSON mediante `BackupManager`.
* Conformidad del `responseSchema` de Gemini y estructura del diagnóstico simulado.

---

## 📄 Licencia

Este proyecto fue generado y estructurado en **Google AI Studio** como aplicación de control y telemetría de combustible.
