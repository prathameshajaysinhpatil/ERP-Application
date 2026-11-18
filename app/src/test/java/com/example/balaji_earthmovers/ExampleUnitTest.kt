package com.example.balaji_earthmovers

import com.example.balaji_earthmovers.data.local.entity.CustomerEntity
import com.example.balaji_earthmovers.data.local.entity.DriverEntity
import com.example.balaji_earthmovers.data.local.entity.VehicleEntity
import com.example.balaji_earthmovers.data.security.CryptoManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testVehicleServiceIntervalWarning() {
        val healthyVehicle = VehicleEntity(
            vehicleName = "JCB 3DX",
            vehicleNumber = "MH-12-AB-1234",
            model = "2023",
            workedHours = 200.0,
            lastServiceHours = 0.0,
            purchaseDate = System.currentTimeMillis()
        )
        assertFalse("Vehicle with 200 hours should not be due for 250h service", healthyVehicle.isServiceDue)
        assertEquals(50.0, healthyVehicle.remainingHoursBeforeService, 0.01)

        val overdueVehicle = healthyVehicle.copy(
            workedHours = 460.0,
            lastServiceHours = 200.0 // 260 hours difference
        )
        assertTrue("Vehicle with 260 hours since service must trigger high-priority warning", overdueVehicle.isServiceDue)
        assertEquals(260.0, overdueVehicle.hoursSinceLastService, 0.01)
    }

    @Test
    fun testCustomerPendingBalanceCalculation() {
        val customer = CustomerEntity(
            name = "Shree Balaji Infra",
            mobileNumber = "9823012345",
            address = "Pune",
            totalBilled = 150000.0,
            totalPaid = 100000.0
        )
        assertEquals(50000.0, customer.pendingBalance, 0.01)
    }

    @Test
    fun testDriverPendingSalaryComputation() {
        val now = System.currentTimeMillis()
        val thirtyDaysAgo = now - (30L * 24 * 3600 * 1000)
        val driver = DriverEntity(
            driverName = "Ramesh",
            mobileNumber = "9823012345",
            address = "Pune",
            adharNumber = "123456789012",
            monthlySalary = 30000.0,
            givenSalary = 10000.0,
            joiningDate = thirtyDaysAgo,
            leavedDate = now,
            totalLeaves = 0
        )
        // 30 days worked @ 30000/mo = 30000 total earned, minus 10000 given = 20000 pending
        assertEquals(30000.0, driver.totalEarnedToDate, 500.0)
        assertEquals(20000.0, driver.pendingSalary, 500.0)
    }

    @Test
    fun testAadhaarMasking() {
        val aadhaar = "123456789012"
        val masked = CryptoManager.maskAadhaar(aadhaar)
        assertEquals("XXXX-XXXX-9012", masked)
    }
}
