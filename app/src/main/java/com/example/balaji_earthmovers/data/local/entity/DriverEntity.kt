package com.example.balaji_earthmovers.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "drivers",
    foreignKeys = [
        ForeignKey(
            entity = VehicleEntity::class,
            parentColumns = ["id"],
            childColumns = ["assignedVehicleId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("assignedVehicleId"),
        Index("mobileNumber")
    ]
)
data class DriverEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val driverName: String,
    val mobileNumber: String,
    val address: String,
    val adharNumber: String, // Encrypted using AES before Room insertion
    val monthlySalary: Double,
    val givenSalary: Double = 0.0,
    val joiningDate: Long,
    val leavedDate: Long? = null,
    val assignedVehicleId: String? = null,
    val totalLeaves: Int = 0,
    val workingDaysMonthly: Map<String, Int> = emptyMap(),
    val deletedAt: Long? = null
) {
    /**
     * Dynamically calculates pending salary:
     * totalEarnedToDate (pro-rated monthly salary minus leave deductions) - givenSalary
     */
    val totalEarnedToDate: Double
        get() {
            val endTimestamp = leavedDate ?: System.currentTimeMillis()
            val totalDays = ((endTimestamp - joiningDate).coerceAtLeast(0L) / (1000L * 60 * 60 * 24)).toDouble()
            val perDaySalary = if (monthlySalary > 0) monthlySalary / 30.0 else 0.0
            val effectiveDays = (totalDays - totalLeaves).coerceAtLeast(0.0)
            return effectiveDays * perDaySalary
        }

    val pendingSalary: Double
        get() = (totalEarnedToDate - givenSalary).coerceAtLeast(0.0)
}
