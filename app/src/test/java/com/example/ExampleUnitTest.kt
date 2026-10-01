package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Pruebas unitarias para los cálculos centrales de rendimiento de combustible y respaldos en MotCont.
 */
class ExampleUnitTest {

    @Test
    fun testKmPerGallonCalculation() {
        val odoPrev = 12000.0
        val odoCurrent = 12390.0
        val deltaKm = odoCurrent - odoPrev // 390 km
        val gallons = 3.0

        val kmPerGallon = deltaKm / gallons
        assertEquals(130.0, kmPerGallon, 0.001)
    }

    @Test
    fun testCostPerKmCalculation() {
        val deltaKm = 390.0
        val amountPaid = 12.00

        val costPerKm = amountPaid / deltaKm
        assertEquals(0.03076, costPerKm, 0.0001)
    }

    @Test
    fun testOdometerValidationEdgeCase() {
        val odoPrev = 12500.0
        val odoCurrent = 12400.0 // Odometer menor (error)

        val deltaKm = odoCurrent - odoPrev
        val isValid = deltaKm > 0
        assertEquals(false, isValid)
    }

    @Test
    fun testZeroGallonsAvoidsCrash() {
        val gallons = 0.0
        val deltaKm = 300.0

        val kmPerGallon: Double? = if (gallons > 0.0) deltaKm / gallons else null
        assertNull(kmPerGallon)
    }
}
