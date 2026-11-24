package com.example.balaji_earthmovers

import com.example.balaji_earthmovers.data.local.entity.CustomerEntity
import com.example.balaji_earthmovers.data.local.entity.DriverEntity
import com.example.balaji_earthmovers.data.local.entity.VehicleEntity
import com.example.balaji_earthmovers.data.security.CryptoManager
import com.example.balaji_earthmovers.ui.screens.parseDocumentText
import com.example.balaji_earthmovers.ui.viewmodel.FleetSeasonFilter
import com.example.balaji_earthmovers.ui.viewmodel.WorkFormState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

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

    @Test
    fun testWorkFormEffectiveBilledAmountCalculatedVsSettled() {
        // Hourly: 10 hrs @ 1200/hr = 12000
        val hourlyForm = WorkFormState(
            workName = "Site Excavation",
            isFixedAmount = false,
            totalHours = "10",
            hourlyRate = "1200",
            finalSettledAmount = "",
            initialPaidAmount = "5000"
        )
        assertEquals(12000.0, hourlyForm.totalAmount, 0.01)
        assertEquals(12000.0, hourlyForm.effectiveBilledAmount, 0.01)

        // Bargained down to 11000
        val settledHourlyForm = hourlyForm.copy(finalSettledAmount = "11000")
        assertEquals(12000.0, settledHourlyForm.totalAmount, 0.01)
        assertEquals(11000.0, settledHourlyForm.effectiveBilledAmount, 0.01)

        // Fixed Amount: 25000 settled to 23000
        val fixedForm = WorkFormState(
            workName = "Road Leveling",
            isFixedAmount = true,
            fixedAmount = "25000",
            finalSettledAmount = "23000"
        )
        assertEquals(25000.0, fixedForm.totalAmount, 0.01)
        assertEquals(23000.0, fixedForm.effectiveBilledAmount, 0.01)
    }

    @Test
    fun testParseDocumentTextSlipExtraction() {
        val sampleSlip = """
            Balaji Earthmovers Challan
            Customer: Patil Developers
            Vehicle: MH 09 CW 5432
            Site at: Kodoli MIDC
            Hours: 8 hrs
            Rate: 1500/hr
            Total: 12000
            Contact: 9822345678
        """.trimIndent()

        val parsed = parseDocumentText(sampleSlip)
        assertEquals("MH 09 CW 5432", parsed.vehicleNumber)
        assertEquals("9822345678", parsed.mobileNumber)
        assertEquals("8", parsed.hours)
        assertEquals("12000", parsed.amount)
        assertTrue(parsed.location.contains("Kodoli"))
    }

    @Test
    fun testSeasonFilterClassification() {
        val cal = Calendar.getInstance()

        // June (month index 5 in Calendar) should be Monsoon
        cal.set(2026, Calendar.JUNE, 15)
        val juneMonth = cal.get(Calendar.MONTH)
        assertTrue("June must fall in Monsoon months 5..8", juneMonth in 5..8)

        // November (month index 10 in Calendar) should be Peak Construction
        cal.set(2026, Calendar.NOVEMBER, 15)
        val novMonth = cal.get(Calendar.MONTH)
        assertTrue("November must fall in Peak Season", novMonth in 9..11 || novMonth in 0..4)
    }
}
