package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.backup.BackupManager
import com.example.data.local.FuelLogEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("MotCont", appName)
  }

  @Test
  fun testBackupJsonExportAndImport() {
    val sampleList = listOf(
      FuelLogEntity(
        id = 1,
        vehicleType = "MOTO",
        odometerKm = 12000.0,
        amountPaid = 12.0,
        gallons = 3.0,
        dateMillis = 1785500000000L,
        dateString = "2026-08-05",
        notes = "Tanque lleno"
      )
    )

    val json = BackupManager.exportToJson(sampleList)
    assertTrue(json.contains("\"app\": \"MotCont\""))
    assertTrue(json.contains("\"totalRecords\": 1"))
    assertTrue(json.contains("\"odometerKm\": 12000"))

    val restored = BackupManager.importFromJson(json)
    assertEquals(1, restored.size)
    assertEquals("MOTO", restored[0].vehicleType)
    assertEquals(12000.0, restored[0].odometerKm, 0.01)
    assertEquals(12.0, restored[0].amountPaid, 0.01)
    assertEquals(3.0, restored[0].gallons, 0.01)
  }

  @Test
  fun testGeminiResponseSchemaAndMock() {
    val schema = com.example.data.remote.GeminiService.RESPONSE_SCHEMA_JSON
    assertTrue(schema.has("properties"))
    val properties = schema.getJSONObject("properties")
    assertTrue(properties.has("dropDetected"))
    assertTrue(properties.has("dropMonth"))
    assertTrue(properties.has("recommendations"))

    val mockMoto = com.example.data.remote.GeminiService.getMockDiagnosis("MOTO")
    assertTrue(mockMoto.dropDetected)
    assertEquals("Octubre 2026", mockMoto.dropMonth)
    assertTrue(mockMoto.recommendations.isNotEmpty())
    assertEquals(1, mockMoto.recommendations.first().priority)
    assertEquals("GRATIS", mockMoto.recommendations.first().costLevel)
  }
}
